package main.java.distributed;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.util.Properties;

import com.google.inject.Singleton;

import lombok.extern.slf4j.Slf4j;

/**
 * ConnectionManagerImpl manages a single connection resource to the database.
 */
@Slf4j
@Singleton
public class ConnectionManagerImpl implements ConnectionManager {

	public static String DRIVER = "com.mysql.jdbc.Driver";

	private Connection connection;
	private static Properties settings;
	private static String url;
	private static String database;
	private static String username;
	private static String password;

	/**
	 * Load setting files for ConnectionManagerImpl
	 */
	public ConnectionManagerImpl() {
		try {
			// log.warning("ConnectionManagerImpl uses the default paths for the config-files.");
			setup(DEFAULT_SETTINGS_PATH);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * The common constructor-method. Reads settings from the file and loads driver-class
	 * 
	 * @param connectionDetailsPath
	 *            the path to the settings-file.
	 * @throws IOException
	 *             the connection-settings file could not be found.
	 */
	private void setup(File connectionDetailsPath) throws IOException {
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
		} catch (Exception e) {
			e.printStackTrace();
		}
		log.debug("Connection settings loaded. Database-user: " + username);
	}

	/**
	 * Returns the connection. If not present, create a new connection.
	 * 
	 * @return the active connection
	 */
	public Connection getConnection() {
		try {
			if (connection == null || connection.isClosed()) {
				// Setup connection
				connection = DriverManager.getConnection(url + database, username, password);
				log.debug("Connection established with: " + url + database);
			}
		} catch (SQLException e) {
			log.error(e.getMessage());
		}
		return connection;
	}

	/**
	 * Closes the connection.
	 */
	public void closeConnection() {
		try {
			connection.close();
			log.debug("Connection with: " + url + database + " closed.");
		} catch (NullPointerException e) {
			log.warn("Connection was already closed");
		} catch (SQLException e) {
			log.error(e.getMessage());
		}
		connection = null;
	}
}
