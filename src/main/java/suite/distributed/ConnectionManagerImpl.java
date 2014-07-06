package suite.distributed;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.Properties;

import com.mysql.jdbc.Driver;
import com.google.inject.Singleton;

import lombok.extern.slf4j.Slf4j;

/**
 * ConnectionManagerImpl manages a single connection resource to the database.
 */
@Slf4j
@Singleton
public class ConnectionManagerImpl implements ConnectionManager {

	private Connection connection;
	private Properties settings;
	private String url;
	private String database;
	private String username;
	private String password;

	/**
	 * Load setting files for ConnectionManagerImpl
	 */
	public ConnectionManagerImpl() {
		try (FileInputStream file =
		        new FileInputStream(System.getProperty("user.dir") + DEFAULT_SETTINGS_FILE)) {
			setup(file);
		} catch (IOException e) {
			log.error("Failed to retrieve database-settings, because {}", e.getMessage());
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
	private void setup(InputStream input) throws IOException {
		// load the properties file
		settings = new Properties();
		settings.load(input);
		url = settings.getProperty("url");
		database = settings.getProperty("database");
		username = settings.getProperty("username");
		password = settings.getProperty("password");
		// Setup Driver
		try {
			new Driver();
		} catch (SQLException e) {
			log.error("Failed to setup Driver: {} ", e.getMessage());
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
			log.debug("Exception caught because of closed connection: {}", e.getMessage());
		} catch (SQLException e) {
			log.error(e.getMessage());
		}
		connection = null;
	}

	@Override
	public String toString() {
		return "ConnectionManagerImpl [connection=" + connection + ", url=" + url + ":"
		        + database + "]";
	}
}
