package main.java.distributed.configuration;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import main.java.distributed.ConnectionManager;
import main.java.distributed.IConnectionManager;

/**
 * SQL-implementation of the IConfigurationDAO
 * (Table) 
 * - Configuration: depth|section|key|value|
 * - <secton,key> is UNIQUE
 * - ORDER BY depth DESC
 */
public class ConfigurationDAO implements IConfigurationDAO {

	final Logger logger = Logger.getLogger(ConfigurationDAO.class.getName());
	
	private static String TABLE = "configuration"; 
	private static String COLUMN_SECTION = "section"; 
	private static String COLUMN_KEY = "key"; 
	private static String COLUMN_VALUE = "value"; 
	private static String COLUMN_DEPTH = "depth";
	
	private IConnectionManager connMgr;

	public ConfigurationDAO() throws IOException {
		connMgr = new ConnectionManager();
	}

	public Map<String, String> getConfiguration(List<String> sections) {
		Map<String,String> config = new HashMap<String,String>();
		try {
			Connection conn = connMgr.getConnection();
			String where = " WHERE ";
			for( String section : sections) {
				where += COLUMN_SECTION + "=\"" + section + "\" OR ";
			}
			ResultSet res = conn.createStatement().executeQuery("SELECT * FROM  " + TABLE + where.substring(0, where.length() - 4));
			while (res.next()) {
				config.put(res.getString(COLUMN_KEY), res.getString(COLUMN_VALUE));
				logger.info("Configurations retrieved: [" + res.getString(COLUMN_KEY) + "=" + res.getString(COLUMN_VALUE) + "]");
			}
			connMgr.closeConnection();
		} catch (SQLException e) {
			logger.warning("Error while retrieving configurations: " + e.getMessage());
		}
		return config;
	}

	public void updateConfiguration(List<String> sections, Map<String, String> configuration, boolean replaceOld) {
		assert !sections.isEmpty();
		try {
			Connection conn = connMgr.getConnection();
			String values = "";
			for (Map.Entry<String, String> entry : configuration.entrySet()) {
			    values += entry.getKey() + "=\"" + entry.getValue() + "\",";
			}
			
			String where = ""; 
			if (!sections.isEmpty()) 
				where += " WHERE ";
			for (String section : sections) {
				where += COLUMN_SECTION + "=\"" + section + "\" OR ";
			}
			
			conn.createStatement().executeUpdate("UPDATE "+ TABLE +" SET " + values.substring(0, values.length() - 1) + 
					where.substring(0, where.length() - 4));
			logger.info("From section updated keys: " + Arrays.toString(configuration.entrySet().toArray()));
			connMgr.closeConnection();
		} catch (SQLException e) {
			logger.warning("Error while updating configurations: " + e.getMessage());
		}
	}

	public void deleteConfiguration(List<String> sections, List<String> keys) {
		assert !sections.isEmpty();
		try {
			Connection conn = connMgr.getConnection();
			String where = " WHERE (";
			for(String section : sections) {
				where += COLUMN_SECTION + "=\"" + section + "\" AND ";
			}
			where = where.substring(0,where.length() - 4);
			if (!keys.isEmpty()) {
				where += " AND ";
				for( String key : keys) {
					where += COLUMN_KEY + "=\"" + key + "\" OR ";
				}
			}
			conn.createStatement().executeUpdate("DELETE FROM  " + TABLE + where.substring(0, where.length() - 4));
			logger.info("From section deleted keys: " + Arrays.toString(keys.toArray()));
			connMgr.closeConnection();
		} catch (SQLException e) {
			logger.warning("Error while deleting configurations: " + e.getMessage());
		}
	}
	
	// aliases	
	public Map<String, String> getConfiguration(String section) {
		List<String> sections = new ArrayList<String>();
		sections.add(section);
		return getConfiguration(sections);
	}
	
	public void updateConfiguration(String section, String key, String value, boolean replaceOld) {
		Map<String,String> map = new HashMap<String,String>();
		List<String> sections = new ArrayList<String>();
		sections.add(section);
		map.put(key, value);
		updateConfiguration(sections, map,replaceOld);
	}

	public void deleteConfiguration(String section, String key) {
		List<String> keys = new ArrayList<String>();
		List<String> sections = new ArrayList<String>();
		sections.add(section);
		keys.add(key);
		deleteConfiguration(sections, keys);	
	}

	public void deleteConfiguration(String section) {
		try {
			Connection conn = connMgr.getConnection();
			conn.createStatement().executeUpdate("DELETE FROM  " + TABLE + " WHERE " + COLUMN_SECTION + "=\"" + section + "\"");
			logger.info("Deleted section: " + section);
			connMgr.closeConnection();
		} catch (SQLException e) {
			logger.warning("Error while deleting configurations: " + e.getMessage());
		}
	}
	
	public Map<String, String> getConfiguration() {
		Map<String,String> config = new HashMap<String,String>();
		try {
			Connection conn = connMgr.getConnection();
			ResultSet res = conn.createStatement().executeQuery("SELECT * FROM  " + TABLE);
			while (res.next()) {
				config.put(res.getString(COLUMN_KEY), res.getString(COLUMN_VALUE));
				logger.info("Configurations retrieved: [" + res.getString(COLUMN_KEY) + "=" + res.getString(COLUMN_VALUE) + "]");
			}
			connMgr.closeConnection();
		} catch (SQLException e) {
			logger.warning("Error while retrieving configurations: " + e.getMessage());
		}
		return config;
	}
}
