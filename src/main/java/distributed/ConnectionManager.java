package main.java.distributed;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ConnectionManager manages a single connection resource to the database. 
 *
 */
public class ConnectionManager implements IConnectionManager {
	
	public static final String DEFAULT_SETTINGS_DIR = System.getProperty("user.dir") + "\\config";
	
	public static String DRIVER = "com.mysql.jdbc.Driver";

	final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private Connection connection;
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
			//logger.warning("ConnectionManager uses the default paths for the config-files.");
			setup(DEFAULT_SETTINGS_DIR + "/dist.ini");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * The common constructor-method. Reads settings from the file and loads driver-class
	 * @param connectionDetailsPath the path to the settings-file.
	 * @throws IOException the connection-settings file could not be found.
	 */
	private static void setup(String connectionDetailsPath) throws IOException {
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
		//logger.info("Connection settings loaded. Database-user: " + username);
	}
	
	/**
	 * Returns the connection. If not present, create a new connection.
	 * @return the active connection
	 */
	public Connection getConnection() {
		try {
			if (connection == null || connection.isClosed()) {
					// Setup connection
					connection = DriverManager.getConnection(url + database,username,password);
					logger.debug("Connection established with: " + url + database);
			}
		} catch (SQLException e) {
			logger.error(e.getMessage());
		}
		return connection;
	}
	
	/**
	 * Closes the connection.
	 */
	public void closeConnection() {
		try {
			connection.close();
			logger.debug("Connection with: " + url + database + " closed.");
		} catch (NullPointerException e) {
			logger.warn("Connection was already closed");
		} catch (SQLException e) {
			logger.error(e.getMessage());
		}
		connection = null;
	}
}
