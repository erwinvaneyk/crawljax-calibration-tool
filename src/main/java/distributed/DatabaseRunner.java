package main.java.distributed;

import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import lombok.extern.slf4j.Slf4j;
import main.java.SuiteRunner;
import main.java.distributed.results.StateResult;
import main.java.distributed.results.WebsiteResult;
import main.java.distributed.workload.WorkTask;
import main.java.distributed.workload.WorkloadDAO;

@Slf4j
public class DatabaseRunner {

	public static void main(String[] args) throws SQLException, MalformedURLException {
		System.out.println(compareCrawls(new int[]{3}));
	}
	
	private static void test() throws SQLException, MalformedURLException {
		//WorkTask wt = new WorkTask(42,new URL("http://demo.crawljax.com"));
		ConnectionSource connectionSource = new JdbcConnectionSource("jdbc:mysql://sql.ewi.tudelft.nl:3306/crawljaxsuite","erwin","maven.pom.xml");
		TableUtils.createTable(connectionSource, WebsiteResult.class);
		Dao<WorkTask, String> accountDao = DaoManager.createDao(connectionSource, WorkTask.class);
		//accountDao.create(wt);
	}
	
	private static void cleanDatabase() throws SQLException {
		IConnectionManager cm = new ConnectionManager();
		Connection conn = cm.getConnection();
		Statement stat = conn.createStatement();
		stat.executeUpdate("DELETE FROM DomResults");
		stat.executeUpdate("DELETE FROM TestResults");
		stat.executeUpdate("DELETE FROM workload");
		conn.close();
	}
	
	private static float compareCrawls(int[] websiteids) throws SQLException {
		// Setup connections
		ConnectionSource connectionSource = new JdbcConnectionSource("jdbc:mysql://sql.ewi.tudelft.nl:3306/crawljaxsuite","erwin","maven.pom.xml");
		Dao<WebsiteResult, String> websiteResultDAO = DaoManager.createDao(connectionSource, WebsiteResult.class);
		log.debug("Database connection setup.");

		// get websites
		Map<String,Object> map = new HashMap<String,Object>();
		for(Object id : websiteids) {
			map.put("workTask_id", id);
		}
		List<WebsiteResult> baseWebsites = websiteResultDAO.queryForFieldValues(map);
		log.debug("Reference websites retrieved: " + baseWebsites);
		
		// submit work
		ConnectionManager connMgr = new ConnectionManager();
		WorkloadDAO workload = new WorkloadDAO(connMgr);
		Map<String, Object> crawledIds = new HashMap<String,Object>();
		for(WebsiteResult baseWebsite : baseWebsites) {
			log.debug("Work to submit: " + baseWebsite.getWorkTask());
			int newId = workload.submitWork(baseWebsite.getWorkTask().getURL());
			if(newId > -1) {
				crawledIds.put("workTask_id",newId);
				log.debug("Work submitted: " + baseWebsite.getWorkTask().getURL());
			} else {
				log.warn("Work rejected?!! dafuq");
			}
		}
		// wait until websites have been crawled
		SuiteRunner.main(new String[]{"-w","-finish"});
		log.debug("Finished crawling.");
		
		// retrieve crawled websites
		List<WebsiteResult> crawledWebsites = websiteResultDAO.queryForFieldValues(crawledIds);
		log.debug("Newly crawled websites retrieved.");
		float result = 0;
		// compare results
		for(WebsiteResult baseWebsite : baseWebsites) {
			WebsiteResult crawledWebsite = null;
			// find equivalent crawled website
			for(WebsiteResult cw : crawledWebsites) {
				if(cw.getWorkTask().getURL().equals(baseWebsite.getWorkTask().getURL())) {
					crawledWebsite = cw;
					break;
				}
			}
			// check which states are present
			log.debug("Compare states...");
			Collection<StateResult> baseStates = baseWebsite.getStateResults();
			Collection<StateResult> crawledStates = crawledWebsite.getStateResults();
			result += (float) crawledStates.size() / (float) baseStates.size();
			log.debug(baseWebsite.getWorkTask().getURL().toString() + ": " + crawledStates.size() / baseStates.size()
					+ "(" + crawledStates.size() + " / " + baseStates.size() + ")");
		}
		return result / (float) crawledWebsites.size();
	}
}
