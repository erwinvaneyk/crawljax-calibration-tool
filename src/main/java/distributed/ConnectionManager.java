package main.java.distributed;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.util.Properties;
import java.util.logging.Logger;

public class ConnectionManager {
	public static String DEFAULT_SETTINGS_DIR = System.getProperty("user.dir") + "\\src\\main\\config";
	
	final Logger logger = Logger.getLogger(ConnectionManager.class.getName());
	
	private Connection connection;

	private Properties settings;
	
	public ConnectionManager(String connectionDetails) throws IOException {
		setupConnection(connectionDetails);
	}
	
	public ConnectionManager() throws IOException {
		logger.warning("Using the default paths for the config-files.");
		setupConnection(DEFAULT_SETTINGS_DIR + "/dist.ini");
	}
	
	
	public void setupConnection(String connectionDetailsPath) throws IOException {
		settings = new Properties();
		try {
			FileInputStream input = new FileInputStream(connectionDetailsPath);
			 
			// load a properties file
			settings.load(input);
			
			// Setup args
			String driver = "com.mysql.jdbc.Driver";
			Class.forName(driver).newInstance();
			String url = settings.getProperty("url");
			String database = settings.getProperty("database");
			String username = settings.getProperty("username");
			String password = settings.getProperty("password");
			
			connection = DriverManager.getConnection(url + database,username,password);
			logger.info("Connection established with: " + url + database);
			
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void closeConnection() {
		try {
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public Connection getConnection() {
		return connection;
	}
	
	
}
