package main.java.distributed.configuration;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import main.java.distributed.IConnectionManager;

/**
 * SQL-implementation of the IConfigurationDAO
 * (Table) 
 * - Configuration: depth|section|key|value|
 * - pair <secton,key> is UNIQUE
 * - ORDER BY depth DESC
 */
@Slf4j
public class ConfigurationDAO implements IConfigurationDAO {
	
	private static final String TABLE = "configuration"; 
	private static final String COLUMN_SECTION = "section"; 
	private static final String COLUMN_KEY = "key"; 
	private static final String COLUMN_VALUE = "value"; 
	private static final String COLUMN_DEPTH = "depth";
	
	private IConnectionManager connMgr;

	public ConfigurationDAO(IConnectionManager conn) {
		connMgr = conn;
	}

	public Map<String, String> getConfiguration(List<String> sections) {
		log.debug("Retrieving configurations of sections: " + Arrays.toString(sections.toArray()));
		assert sections != null;
		Map<String,String> config = new HashMap<String,String>();
		try {
			Connection conn = connMgr.getConnection();
			String where = "WHERE ";
			for( String section : sections) {
				where += "`" + COLUMN_SECTION + "`=\"" + section + "\" OR ";
			}
			ResultSet res = conn.createStatement().executeQuery("SELECT * FROM  `" + TABLE + "` " + where.substring(0, where.length() - 4) 
					+ " ORDER BY `" + COLUMN_DEPTH +"` DESC");
			while (res.next()) {
				if(!config.containsKey(res.getString(COLUMN_KEY))) {
					config.put(res.getString(COLUMN_KEY), res.getString(COLUMN_VALUE));
					log.info("Configurations retrieved: [" + res.getString(COLUMN_KEY) + "=" + res.getString(COLUMN_VALUE) + "]");
				}
			}
			connMgr.closeConnection();
		} catch (SQLException e) {
			log.error("Error while retrieving configurations: " + e.getMessage());
		}
		return config;
	}

	public Map<String, String> getConfiguration(String section) {
		List<String> sections = new ArrayList<String>();
		sections.add(section);
		return getConfiguration(sections);
	}
	
	public void updateConfiguration(String section, String key, String value, int importance) {
		assert section != null;
		assert key.length() > 0;
		try {
			Connection conn = connMgr.getConnection();
			// Attempt update for new value
			if(conn.createStatement().executeUpdate("UPDATE `" + TABLE + "` SET `"
					+ COLUMN_VALUE + "`=\""+ value + "\",`" + COLUMN_DEPTH + "`=" + importance 
					+ " WHERE `" + COLUMN_SECTION + "`=\"" + section + "\" AND `" + COLUMN_KEY + "`=\"" + key + "\"") > 0) {
				log.info("Updated in section " + section + " key " + key + " to value " + value);
			} else {
				// If update failed, try insert.
				conn.createStatement().executeUpdate("INSERT INTO  " + TABLE + " VALUES (\"" + section + "\",\"" 
						+ key + "\",\"" + value + "\"," + importance +")");
				log.info("Inserted into section " + section + " key " + key + " to value " + value);
			}
			connMgr.closeConnection();
		} catch (SQLException e) {
			log.error("Error while updating configurations: " + e.getMessage());
		}
	}

	public void deleteConfiguration(String section, String key) {
		assert section != null;
		assert key.length() > 0;
		try {
			Connection conn = connMgr.getConnection();
			conn.createStatement().executeUpdate("DELETE FROM  `" + TABLE + "` WHERE `" + 
					COLUMN_SECTION + "`=\"" + section + "\" AND `" + COLUMN_KEY + "`=\"" + key + "\"");
			log.info("Deleted section: " + section);
			connMgr.closeConnection();
		} catch (SQLException e) {
			log.error("Error while deleting configurations: " + e.getMessage());
		}
	}

	public void deleteConfiguration(String section) {
		assert section != null;
		try {
			Connection conn = connMgr.getConnection();
			conn.createStatement().executeUpdate("DELETE FROM `" + TABLE + "` WHERE `" + COLUMN_SECTION + "`=\"" + section + "\"");
			log.info("Deleted section: " + section);
			connMgr.closeConnection();
		} catch (SQLException e) {
			log.error("Error while deleting configurations: " + e.getMessage());
		}
	}
	
	public Map<String, String> getConfiguration() {
		Map<String,String> config = new HashMap<String,String>();
		try {
			Connection conn = connMgr.getConnection();
			ResultSet res = conn.createStatement().executeQuery("SELECT * FROM `" + TABLE + "`");
			while (res.next()) {
				config.put(res.getString(COLUMN_KEY), res.getString(COLUMN_VALUE));
				log.info("Configurations retrieved: [" + res.getString(COLUMN_KEY) + "=" + res.getString(COLUMN_VALUE) + "]");
			}
			connMgr.closeConnection();
		} catch (SQLException e) {
			log.error("Error while retrieving configurations: " + e.getMessage());
		}
		return config;
	}
}
