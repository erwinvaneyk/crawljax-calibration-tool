package main.java.distributed;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;

import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConnectionManagerORM extends ConnectionManager implements IConnectionManager {
	
	public static String DRIVER = "com.mysql.jdbc.Driver";
	
	private JdbcConnectionSource connection;
	private static Properties settings;
	private static String url;
	private static String database;
	private static String username;
	private static String password;
	
	/**
	 * Load setting files for ConnectionManager
	 */
	static {
		try {
			//log.warning("ConnectionManager uses the default paths for the config-files.");
			setup(DEFAULT_SETTINGS_PATH);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * The common constructor-method. Reads settings from the file and loads driver-class
	 * @param connectionDetailsPath the path to the settings-file.
	 * @throws IOException the connection-settings file could not be found.
	 */
	private static void setup(File connectionDetailsPath) throws IOException {
		settings = new Properties();
		FileInputStream input = new FileInputStream(connectionDetailsPath);
		 
		// load the properties file
		settings.load(input);
		url = settings.getProperty("url");
		database = settings.getProperty("database");
		username = settings.getProperty("username");
		password = settings.getProperty("password");
		
		// Load driver
		try {
			Class.forName(DRIVER).newInstance();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		log.debug("Connection settings loaded. Database-user: " + username);
	}

	public ConnectionSource getConnectionORM() throws SQLException {
		if (connection == null || !connection.isOpen()) {
			connection = new JdbcConnectionSource(url + database,username,password);
		}
		return connection;
	}
	
	public void closeConnection() {
		super.closeConnection();
		connection.closeQuietly();		
	}
	
	
}
