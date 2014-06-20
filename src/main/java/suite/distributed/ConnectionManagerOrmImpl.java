package suite.distributed;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Properties;

import com.google.inject.Singleton;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;

import lombok.extern.slf4j.Slf4j;

/**
 * ConnectionManagerImpl, which besides regular connections also supports connections compatible
 * with ORMLite.
 */
@Slf4j
@Singleton
public class ConnectionManagerOrmImpl extends ConnectionManagerImpl
        implements ConnectionManagerOrm {

	public static final String DRIVER = "com.mysql.jdbc.Driver";

	private JdbcConnectionSource connection;
	private static Properties settings;
	private static String url;
	private static String database;
	private static String username;
	private static String password;

	/**
	 * Load setting files for ConnectionManagerImpl
	 */
	public ConnectionManagerOrmImpl() {
		super();
		try {
			setup(new FileInputStream(System.getProperty("user.dir") + DEFAULT_SETTINGS_FILE));
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
	private void setup(InputStream input) throws IOException {
		// load the properties file
		settings = new Properties();
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
	 * @return
	 * @throws SQLException
	 */
	public ConnectionSource getConnectionORM() throws SQLException {
		if (connection == null || !connection.isOpen()) {
			connection = new JdbcConnectionSource(url + database, username, password);
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
