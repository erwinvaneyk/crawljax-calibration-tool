package suite.distributed;

import java.sql.Connection;

/**
 * ConnectionManagerImpl manages a single connection resource to the database.
 */
public interface ConnectionManager {

	public static final String DEFAULT_SETTINGS_FILE = "dist.ini";

	/**
	 * Returns the connection. If not present, create a new connection.
	 * 
	 * @return the active connection
	 */
	public Connection getConnection();

	/**
	 * Closes the connection.
	 */
	public void closeConnection();
}
