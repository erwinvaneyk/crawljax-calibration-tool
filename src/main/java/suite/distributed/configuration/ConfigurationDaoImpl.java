package suite.distributed.configuration;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import suite.distributed.ConnectionManager;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

/**
 * A ConfigurationDao implementation, which makes use of a SQL-database to store the
 * configuration-settings. SQL-implementation of the ConfigurationDao (Table) - Configuration:
 * depth|section|key|value| - pair <secton,key> is UNIQUE - ORDER BY depth DESC
 */
@Slf4j
@Singleton
public class ConfigurationDaoImpl implements ConfigurationDao {

	private static final String TABLE = "configuration";
	private static final String COLUMN_SECTION = "section";
	private static final String COLUMN_KEY = "key";
	private static final String COLUMN_VALUE = "value";
	private static final String COLUMN_DEPTH = "depth";
	private static final int    DEFAULT_IMPORTANCE = 10;

	private ConnectionManager connMgr;
	private Map<String, Integer> importances;

	@Inject
	public ConfigurationDaoImpl(ConnectionManager conn) {
		this.connMgr = conn;
		try {
	        this.importances = this.getImportanceOfSections();
        } catch (SQLException e) {
        	log.error("Failed to retrieve sections: " + e.getMessage());
        	importances = new HashMap<String,Integer>();
        }
	}

	public Map<String, String> getConfiguration(@NonNull List<String> sections) {
		log.debug("Retrieving configurations of sections: " + Arrays.toString(sections.toArray()));
		Map<String, String> config = new HashMap<String, String>();
		try {
			Connection conn = connMgr.getConnection();
			StringBuffer where = new StringBuffer("WHERE ");
			for (String section : sections) {
				where.append("`" + COLUMN_SECTION + "`=\"" + section + "\" OR ");
			}
			ResultSet res =
			        conn.createStatement().executeQuery(
			                "SELECT * FROM  `" + TABLE + "` "
			                        + where.substring(0, where.length() - 4)
			                        + " ORDER BY `" + COLUMN_DEPTH + "` DESC");
			while (res.next()) {
				String key = res.getString(COLUMN_KEY);
				String value = res.getString(COLUMN_VALUE);
				if (!config.containsKey(key)) {
					config.put(key, value);
					log.info("Configurations retrieved: [" + key + "=" + value + "]");
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

	public void updateConfiguration(@NonNull String section,@NonNull String key, String value) {
		try {
			Connection conn = connMgr.getConnection();
			// Attempt update for new value
			if (conn.createStatement().executeUpdate(
			        "UPDATE `" + TABLE + "` SET `"
			                + COLUMN_VALUE + "`=\"" + value + "\",`" + COLUMN_DEPTH + "`="
			                + getImportance(section, DEFAULT_IMPORTANCE)
			                + " WHERE `" + COLUMN_SECTION + "`=\"" + section + "\" AND `"
			                + COLUMN_KEY + "`=\"" + key + "\"") > 0) {
				log.info("Updated in section " + section + " key " + key + " to value " + value);
			} else {
				// If update failed, try insert.
				conn.createStatement().executeUpdate(
				        "INSERT INTO  " + TABLE + " VALUES (\"" + section + "\",\""
				                + key + "\",\"" + value + "\","
				                + getImportance(section, DEFAULT_IMPORTANCE) + ")");
				log.info("Inserted into section " + section + " key " + key + " = value "
				        + value);
			}
			connMgr.closeConnection();
		} catch (SQLException e) {
			log.error("Error while updating configurations: " + e.getMessage());
		}
	}

	public void deleteConfiguration(@NonNull String section,@NonNull String key) {
		try {
			Connection conn = connMgr.getConnection();
			conn.createStatement().executeUpdate(
			        "DELETE FROM  `" + TABLE + "` WHERE `" +
			                COLUMN_SECTION + "`=\"" + section + "\" AND `" + COLUMN_KEY + "`=\""
			                + key + "\"");
			log.info("Deleted section: " + section);
			connMgr.closeConnection();
		} catch (SQLException e) {
			log.error("Error while deleting configurations: " + e.getMessage());
		}
	}

	public void deleteConfiguration(@NonNull String section) {
		try {
			Connection conn = connMgr.getConnection();
			conn.createStatement().executeUpdate(
			        "DELETE FROM `" + TABLE + "` WHERE `" + COLUMN_SECTION + "`=\"" + section
			                + "\"");
			log.info("Deleted section: " + section);
			connMgr.closeConnection();
		} catch (SQLException e) {
			log.error("Error while deleting configurations: " + e.getMessage());
		}
	}

	public Map<String, String> getConfiguration() {
		Map<String, String> config = new HashMap<String, String>();
		try {
			Connection conn = connMgr.getConnection();
			ResultSet res =
			        conn.createStatement().executeQuery(
			                "SELECT * FROM `" + TABLE + "` ORDER BY `" + COLUMN_DEPTH + "` DESC");
			while (res.next()) {
				if (!config.containsKey(res.getString(COLUMN_KEY))) {
					config.put(res.getString(COLUMN_KEY), res.getString(COLUMN_VALUE));
					log.info("Configurations retrieved: [" + res.getString(COLUMN_KEY) + "="
					        + res.getString(COLUMN_VALUE) + "]");
				}
			}
			connMgr.closeConnection();
		} catch (SQLException e) {
			log.error("Error while retrieving configurations: " + e.getMessage());
		}
		return config;
	}

	private Map<String,Integer> getImportanceOfSections() throws SQLException {
		Connection conn = connMgr.getConnection();
		ResultSet res = conn.createStatement().executeQuery("SELECT * FROM  `" + TABLE + "` ");
		Map<String,Integer> sections = new HashMap<String,Integer>();
		while (res.next()) {
			sections.put(res.getString(COLUMN_SECTION),res.getInt(COLUMN_DEPTH));
		}
		connMgr.closeConnection();
		return sections;
	}
	
	private int getImportance(@NonNull String section, int defaultValue) {
		Integer importance = importances.get(section);
		if(importance != null) {
			return importance;
		} else {
			importances.put(section, defaultValue);
			return defaultValue;
		}
	}
	
	public void setImportance(@NonNull String section, int importance) {
		try {
			Connection conn = connMgr.getConnection();
	        conn.createStatement().executeUpdate("UPDATE `" + TABLE + "` SET `"
	                        + COLUMN_DEPTH + "`=\"" + importance
	                        + " WHERE `" + COLUMN_SECTION + "`=\"" + section);
	        importances.put(section, importance);
	        connMgr.closeConnection();
        } catch (SQLException e) {
	        log.error("Failed to set new importance: " + e.getMessage());
        }
		
	}
}
