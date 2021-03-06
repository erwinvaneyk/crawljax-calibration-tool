package suite.distributed.workload;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import suite.distributed.ConnectionManager;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import lombok.extern.slf4j.Slf4j;

/**
 * SQL-server-based implementation of the IWorkloadDistributor-interface. The WorkloadDistributor is
 * responsible for managing the workload of the clients.
 */
@Slf4j
@Singleton
public class WorkloadDaoImpl implements WorkloadDao {

	private static final String TABLE = "workload";
	private static final String COLUMN_ID = "id";
	private static final String COLUMN_URL = "url";
	private static final String COLUMN_WORKERID = "worker";
	private static final String COLUMN_CRAWLED = "crawled";
	private static final String COLUMN_NAMESPACE = "namespace";

	private ConnectionManager connMgr;
	private String namespace;
	private static String WORKER_ID;

	static {
		try {
			WORKER_ID = InetAddress.getLocalHost().toString();
		} catch (UnknownHostException e) {
			// If host-name is not available, use an alternative name.
			WORKER_ID = System.getProperty("user.name");
			log.error("Hostname could not be retrieved, using system-name: {}." + WORKER_ID);
			log.debug("Host not found while retrieving system-name: {}", e.getMessage());
		}
	}

	/**
	 * Sets up the ConnectionManagerImpl and creates an ID based on the hostname and local ip.
	 * @param conn
	 * 			The manager for the connection
	 * @param namespace
	 * 			The domain of the workload
	 */
	@Inject
	public WorkloadDaoImpl(ConnectionManager conn, @Named("namespace") String namespace) {
		this.connMgr = conn;
		this.namespace = namespace;
		log.info("WorkerID: " + WORKER_ID);
	}

	/**
	 * Sets up the ConnectionManagerImpl and creates an ID based on the hostname and local ip.
	 * @param conn
	 * 			The manager for the connection
	 */
	public WorkloadDaoImpl(ConnectionManager conn) {
		this.connMgr = conn;
		this.namespace = "";
		log.info("WorkerID: " + WORKER_ID);
	}

	/**
	 * Retrieve and claim a number of urls from the server (if nothing is available, an empty
	 * ArrayList is returned).
	 * 
	 * @param maxcount
	 *            the maximum number of urls to retrieve.
	 * @return a list with claimed urls
	 */
	public List<WorkTask> retrieveWork(int maxcount) {
		assert maxcount >= 0;
		List<WorkTask> workTasks = new ArrayList<WorkTask>(maxcount);
		Connection conn = connMgr.getConnection();
		try {
			int claimed =
			        conn.createStatement().executeUpdate(
			                "UPDATE " + TABLE + " SET " + COLUMN_WORKERID + "=\"" + WORKER_ID
			                        + "\"  WHERE " + COLUMN_CRAWLED + " = 0 AND "
			                        + COLUMN_WORKERID + "=\"\" AND " + COLUMN_NAMESPACE + "=\""
			                        + namespace + "\" LIMIT " + maxcount);
			log.debug("Workunits claimed by worker: " + claimed);
			// Retrieve urls from the server.
			// Note: this will also return the claimed/unfinished websites not signed off.
			ResultSet res =
			        conn.createStatement().executeQuery(
			                "SELECT * FROM  " + TABLE + " WHERE " + COLUMN_WORKERID + " = \""
			                        + WORKER_ID + "\" AND " + COLUMN_NAMESPACE + "=\""
			                        + namespace + "\" AND " + COLUMN_CRAWLED + " = 0");
			while (res.next()) {
				try {
					int id = res.getInt("id");
					URL url = new URL(res.getString("url"));
					WorkTask workTask = new WorkTask(id, url);
					workTasks.add(workTask);
					log.info("Worktask retrieved: " + workTask.getURL());
				} catch (MalformedURLException e) {
					log.error(e.getMessage());
				}
			}
		} catch (SQLException e) {
			log.error(e.getMessage());
		}
		connMgr.closeConnection();
		return workTasks;
	}

	/**
	 * Registering a succesful crawl on the server.
	 * 
	 * @param wt
	 *            The worktask to be checked out.
	 * @return true if checkout was succesful, else false.
	 */
	public boolean checkoutWork(WorkTask wt) {
		int ret = 0;
		Connection conn = connMgr.getConnection();
		try {
			// Update crawled-field to 1 to show crawl has finished.
			ret =
			        conn.createStatement().executeUpdate(
			                "UPDATE " + TABLE + " SET " + COLUMN_CRAWLED + "=1 WHERE "
			                        + COLUMN_ID + "=\"" + wt.getId() + "\"");
			log.info("Checked out crawl of id: " + wt.getId());
		} catch (SQLException e) {
			log.error(e.getMessage());
		} catch (NullPointerException e) {
			log.error("Unexpected nullPointerException {}, probably the connection " + conn,
			        e.getMessage());
		}
		connMgr.closeConnection();
		return ret != 0;
	}

	/**
	 * Submit a new url/workunit to the server to crawl.
	 * 
	 * @param url
	 *            the url to be crawled
	 * @return generated id of the url, if failed: -1
	 */
	public int submitWork(URL url, boolean claim) {
		int ret = -1;
		Connection conn = connMgr.getConnection();
		try {
			String worker = claim ? WORKER_ID : "";
			// Insert a new row containing the url in the workload-table.
			Statement statement = conn.createStatement();
			ret =
			        statement.executeUpdate("INSERT INTO " + TABLE + " (" + COLUMN_URL + ","
			                + COLUMN_CRAWLED + "," + COLUMN_WORKERID + "," + COLUMN_NAMESPACE
			                + ") VALUES (\"" + url + "\",0, \"" + worker + "\",\"" + namespace
			                + "\")", Statement.RETURN_GENERATED_KEYS);
			log.info("Succesfully submitted {} to the server.", url);

			// Get generated key
			ResultSet generatedkeys = statement.getGeneratedKeys();
			if (generatedkeys.next()) {
				ret = generatedkeys.getInt(1);
			}
		} catch (SQLException e) {
			log.error(e.getMessage());
		} catch (NullPointerException e) {
			log.error("Unexpected nullPointerException {}, probably the input " + url
			        + " or the connection " + conn, e.getMessage());
		}
		connMgr.closeConnection();
		return ret;
	}

	/**
	 * Reverts previously checked out or claimed work to the available state.
	 * 
	 * @param id
	 *            the id of the website to be reverted
	 * @return true if successful, else false.
	 */
	public boolean revertWork(int id) {
		int ret = 0;
		Connection conn = connMgr.getConnection();
		try {
			Statement st = conn.createStatement();
			// Update the worker and crawled field to the default values for the url.
			ret =
			        st.executeUpdate("UPDATE " + TABLE + " SET " + COLUMN_CRAWLED + "=0, "
			                + COLUMN_WORKERID
			                + "=\"\" WHERE " + COLUMN_ID + "=\"" + id + "\"");
			log.info("Reverted claim/checkout of crawl for id: " + id);
		} catch (SQLException e) {
			log.error(e.getMessage());
		}
		connMgr.closeConnection();
		return ret != 0;
	}

	@Override
	public String toString() {
		return "WorkloadDaoImpl [worker_id=" + WORKER_ID + "namespace=" + namespace + "]";
	}

}
