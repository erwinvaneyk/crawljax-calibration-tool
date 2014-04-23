package main.java.distributed;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.util.Properties;
import java.util.logging.Logger;

public class ConnectionManager {
	public static String DEFAULT_SETTINGS_DIR = System.getProperty("user.dir") + "\\src\\main\\config";
	public static String DRIVER = "com.mysql.jdbc.Driver";
	
	final Logger logger = Logger.getLogger(ConnectionManager.class.getName());
	
	private Connection connection;

	private Properties settings;
	private String url;
	private String database;
	private String username;
	private String password;
	
	public ConnectionManager(String connectionDetails) throws IOException {
		setup(connectionDetails);
	}
	
	public ConnectionManager() throws IOException {
		logger.warning("ConnectionManager uses the default paths for the config-files.");
		setup(DEFAULT_SETTINGS_DIR + "/dist.ini");
	}
	
	
	private void setup(String connectionDetailsPath) throws IOException {
		settings = new Properties();
		FileInputStream input = new FileInputStream(connectionDetailsPath);
		 
		// load a properties file
		settings.load(input);
		url = settings.getProperty("url");
		database = settings.getProperty("database");
		username = settings.getProperty("username");
		password = settings.getProperty("password");
		
		
		// Setup args
		try {
			Class.forName(DRIVER).newInstance();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		logger.info("Connection settings loaded. User: " + username);
	}
	
	public void startConnection() {
		try {
			connection = DriverManager.getConnection(url + database,username,password);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		logger.info("Connection established with: " + url + database);
	}
	
	public void closeConnection() {
		try {
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		logger.info("Connection with: " + url + database + " closed.");
	}

	public Connection getConnection() {
		return connection;
	}
	
	
}
