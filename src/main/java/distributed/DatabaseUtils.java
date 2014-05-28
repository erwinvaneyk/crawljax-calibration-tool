package main.java.distributed;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map.Entry;

import org.ini4j.Ini;
import org.ini4j.Profile.Section;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import main.java.CrawlManager;
import main.java.distributed.configuration.ConfigurationDAO;
import main.java.distributed.configuration.ConfigurationIni;
import main.java.distributed.configuration.IConfigurationDAO;
import main.java.distributed.workload.IWorkloadDAO;
import main.java.distributed.workload.WorkloadDAO;

public class DatabaseUtils {
	
	IConnectionManager con;
	final Logger log = LoggerFactory.getLogger(this.getClass());
	
	public DatabaseUtils(IConnectionManager con) {
		this.con = con;
	}
	
	/**
	 * Delete all the results that are inserted in the database with the crawl of id.
	 * @param id The id of the website, which results should be deleted
	 * @return true if the results are correctly deleted
	 */
	public boolean deleteAllResultsById(int id) {
		boolean result = false;
		Connection connection = con.getConnection();
		try {
			String getWorkTaskId = "SELECT workTask_id FROM WebsiteResults WHERE id=?";
			PreparedStatement statementWId = connection.prepareStatement(getWorkTaskId);
			statementWId.setInt(1, id);
			ResultSet resultWorkTask = statementWId.executeQuery();
			int workTaskId = 0;
			while (resultWorkTask.next()) {
				workTaskId = resultWorkTask.getInt(1);
				log.info("workTaskId={}", workTaskId);
			}
			log.info("Deleting the results of websiteId: " + id + ", workTaksId: " + workTaskId + "...");
			
			boolean deleteDomResult = this.deleteById("DomResults", "websiteResult_id", id, connection);
			boolean deleteWebsiteResults = this.deleteById("WebsiteResults", "id", id, connection);
			boolean deleteWorkTask = this.deleteById("workload", "id", workTaskId, connection);
			
			con.closeConnection();
			if (deleteDomResult && deleteWebsiteResults && deleteWorkTask) {
				log.info("Succesfull deleted the results of id={}", id);
				result = true;
			}
		} catch (SQLException e) {
			log.info("SQLException while deleting the results of id={}", id);
			e.printStackTrace();
		}
		
		return result;
	}
	
	private boolean deleteById(String table, String column, int id, Connection connection) throws SQLException {
		boolean succes = false;
		
		String sql = "DELETE FROM " + table + " WHERE " + column + " = ?";
		PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
		statement.setInt(1, id);
		int deleteDom = statement.executeUpdate();
		if (deleteDom > 0) {
			succes = true;
		} else {
			log.info(""+deleteDom);;
			log.warn("The {} of id={} can not be deleted.", table, id);
		}
		return succes;
	}
	
	/**
	 * Sent a file, which contains websites, to the database.
	 * @param fileName The location of the file which content will be sent to the database
	 */
	public void actionFlushWebsitesFile(String fileName) {
		try {
			IWorkloadDAO workload = new WorkloadDAO(con);
			CrawlManager suite = new CrawlManager();
			suite.websitesFromFile(new File(ConfigurationIni.DEFAULT_SETTINGS_DIR + fileName));
			URL url;
			String rawUrl;
			while((rawUrl = suite.getWebsiteQueue().poll()) != null) {
				url = new URL(rawUrl);
				workload.submitWork(url, false);
			}
		} catch (IOException e1) {
			System.out.println(e1.getMessage());
		}
		System.out.println("File flushed to server.");
	}
	
	public void actionFlushSettingsFile(String fileName) {
		IConfigurationDAO conf = new ConfigurationDAO(con);
		Ini ini = new ConfigurationIni(fileName).getIni();

		for (Section section : ini.values()) {
			for (Entry<String, String> el : section.entrySet()) {
				conf.updateConfiguration(section.getName(), el.getKey(), el.getValue(), section.getName().length());
			}
		}
	}
}
