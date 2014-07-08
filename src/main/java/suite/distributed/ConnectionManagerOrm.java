package suite.distributed;

import java.sql.SQLException;

import com.j256.ormlite.support.ConnectionSource;

/**
 * Extension on the original interface, requiring a ORM-specific connection to be implemented.
 */
public interface ConnectionManagerOrm extends ConnectionManager {

	/**
	 * @return returns the Connection compatible with the ORMLite.
	 * @throws SQLException
	 *             connection could not be established.
	 */
	ConnectionSource getConnectionORM() throws SQLException;

}
