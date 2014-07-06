package suite.distributed;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Properties;

import com.google.inject.Singleton;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.mysql.jdbc.Driver;

import lombok.extern.slf4j.Slf4j;

/**
 * ConnectionManagerImpl, which besides regular connections also supports connections compatible
 * with ORMLite.
 */
@Slf4j
@Singleton
public class ConnectionManagerOrmImpl extends ConnectionManagerImpl
        implements ConnectionManagerOrm {

	private JdbcConnectionSource connection;
	private Properties settings;
	private String url;
	private String database;
	private String username;
	private String password;

	/**
	 * Load setting files for ConnectionManagerImpl
	 */
	public ConnectionManagerOrmImpl() {
		super();
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

	@Override
	public String toString() {
		return "ConnectionManagerOrmImpl [connection=" + connection + ", url=" + url + ":"
		        + database + "]";
	}
}
