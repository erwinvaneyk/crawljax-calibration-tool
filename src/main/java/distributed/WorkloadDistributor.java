package main.java.distributed;

import java.io.IOException;
import java.net.InetAddress;
import java.sql.*;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.logging.Logger;

import org.apache.commons.validator.routines.UrlValidator;

public class WorkloadDistributor {
	final Logger logger = Logger.getLogger(WorkloadDistributor.class.getName());
	
	private ConnectionManager connMgr;
	private String workerID;

	public WorkloadDistributor() throws IOException {
		connMgr = new ConnectionManager();
		workerID = InetAddress.getLocalHost().toString();
		logger.info("WorkerID: " + workerID);
	}
	
	public ArrayList<String> retrieveWork(int maxcount) {
		assert maxcount > 0;
		ArrayList<String> urls = new ArrayList<String>();
		Connection conn = connMgr.getConnection();
		try {
			Statement st = conn.createStatement();
			ResultSet res = st.executeQuery("SELECT * FROM  workload WHERE worker = \"\" AND crawled = 0 LIMIT " + maxcount);
			while (res.next()) {
				int id = res.getInt("id");
				urls.add(res.getString("url"));
				conn.createStatement().executeUpdate("UPDATE workload SET worker=\"" + workerID + "\" WHERE id=" + id);
			}
		} catch (SQLException e) {
			logger.warning(e.getMessage());
		}
		connMgr.closeConnection();
		logger.info("Retrieved workload: " + urls);
		return urls;		
	}
	
	public boolean checkoutWork(String url) {
		int ret = 0;
		Connection conn = connMgr.getConnection();
		try {
			Statement st = conn.createStatement();
			ret = st.executeUpdate("UPDATE workload SET crawled=1 WHERE url=\"" + url + "\"");
			logger.info("Checked out crawl of " + url);
		} catch (SQLException e) {
			logger.warning(e.getMessage());
		}
		connMgr.closeConnection();
		return ret != 0;
	}
	
	public boolean submitWork(String url) {
		int ret = 0;
		Connection conn = connMgr.getConnection();
		try {
			Statement st = conn.createStatement();
			ret = st.executeUpdate("INSERT INTO workload (url,crawled,worker) VALUES (\"" + url + "\",0,\"\")");
			logger.info("Succesfully submitted " + url + " to the server.");
		} catch (SQLException e) {
			logger.warning(e.getMessage());
		}
		connMgr.closeConnection();
		return ret != 0;
	}

	public ArrayList<String> retrieveWork(int maxcount, int sleepMilisecs) throws InterruptedException {
		ArrayList<String> ret = new ArrayList<String>();
		while(ret.isEmpty()) {
			ret = retrieveWork(maxcount);
			logger.info("Sleeping for " + sleepMilisecs + " miliseconds");
			Thread.sleep(sleepMilisecs);
		}
		return ret;
	}
}
