package main.java.distributed;

import java.sql.Connection;

/**
 * ConnectionManager manages a single connection resource to the database. 
 *
 */
public interface IConnectionManager {
	
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
