package main.java.distributed;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * ConnectionManager manages a single connection resource to the database. 
 *
 */
public class ConnectionManager {
	
	public static String DEFAULT_SETTINGS_DIR = System.getProperty("user.dir") + "\\config";
	
	public static String DRIVER = "com.mysql.jdbc.Driver";
	
	final Logger logger = Logger.getLogger(ConnectionManager.class.getName());
	
	private Connection connection;
	private Properties settings;
	private String url;
	private String database;
	private String username;
	private String password;
	
	/**
	 * Constructor calls the common constructor-method setup().
	 * @param connectionDetailsPath the path to a custom connection-settings file.
	 * @throws IOException connectionDetailsPath-file could not be found.
	 */
	public ConnectionManager(String connectionDetailsPath) throws IOException {
		setup(connectionDetailsPath);
	}
	
	/**
	 * Constructor calls the common constructor-method setup().
	 * @throws IOException the default connection-settings file could not be found.
	 */
	public ConnectionManager() throws IOException {
		logger.warning("ConnectionManager uses the default paths for the config-files.");
		setup(DEFAULT_SETTINGS_DIR + "/dist.ini");
	}
	
	/**
	 * The common constructor-method. Reads settings from the file and loads driver-class
	 * @param connectionDetailsPath the path to the settings-file.
	 * @throws IOException the connection-settings file could not be found.
	 */
	private void setup(String connectionDetailsPath) throws IOException {
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
		logger.info("Connection settings loaded. User: " + username);
	}
	
	/**
	 * Returns the connection. If not present, create a new connection.
	 * @return the active connection
	 * @throws SQLException 
	 */
	public Connection getConnection() {
		if (connection == null) {
			try {
				// Setup connection
				connection = DriverManager.getConnection(url + database,username,password);
				logger.info("Connection established with: " + url + database);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return connection;
	}
	
	/**
	 * Closes the connection.
	 */
	public void closeConnection() {
		try {
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		logger.info("Connection with: " + url + database + " closed.");
		connection = null;
	}
}
