package main.java.distributed;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;

import com.google.inject.Singleton;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;

import lombok.extern.slf4j.Slf4j;

/**
 * ConnectionManager, which besides regular connections also supports connections compatible with ORMLite.
 */
@Slf4j
@Singleton
public class ConnectionManagerORM extends ConnectionManager implements IConnectionManagerORM {
	
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
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
			e.printStackTrace();
		}
		log.debug("Connection settings loaded. Database-user: " + username);
	}

	/**
	 * 
	 * @return
	 * @throws SQLException
	 */
	public ConnectionSource getConnectionORM() throws SQLException {
		if (connection == null || !connection.isOpen()) {
			connection = new JdbcConnectionSource(url + database,username,password);
		}
		return connection;
	}
	
	public void closeConnection() {
		if (connection != null) {
			connection.closeQuietly();	
			super.closeConnection();	
		}
	}
	
	
}
