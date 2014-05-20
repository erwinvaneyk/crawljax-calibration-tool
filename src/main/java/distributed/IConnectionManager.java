package main.java.distributed;

import java.io.File;
import java.sql.Connection;

/**
 * ConnectionManager manages a single connection resource to the database. 
 *
 */
public interface IConnectionManager {
	
	public static final File DEFAULT_SETTINGS_PATH = new File(System.getProperty("user.dir") + "/config/dist.ini");
	
	/**
	 * Returns the connection. If not present, create a new connection.
	 * @return the active connection
	 */
	public Connection getConnection();
	
	/**
	 * Closes the connection.
	 */
	public void closeConnection();
}
