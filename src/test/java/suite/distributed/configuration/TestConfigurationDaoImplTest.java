package suite.distributed.configuration;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import suite.distributed.ConnectionManager;
import suite.distributed.configuration.ConfigurationDaoImpl;

import com.mysql.jdbc.Connection;
import com.mysql.jdbc.Statement;

public class TestConfigurationDaoImplTest {

	@Test
	public void testGetConfigurationListOfString() throws SQLException {
		// Setup mocked Database
		ConnectionManager connMgr = mock(ConnectionManager.class);
		Connection conn = mock(Connection.class);
		Statement statement = mock(Statement.class);
		ResultSet resultSet = mock(ResultSet.class);
		when(connMgr.getConnection()).thenReturn(conn);
		when(conn.createStatement()).thenReturn(statement);
		when(statement.executeQuery(anyString())).thenReturn(resultSet);
		// result mock
		when(resultSet.next()).thenReturn(true, true, false);
		when(resultSet.getString("key")).thenReturn("depth", "author");
		when(resultSet.getString("value")).thenReturn("42", "me");

		// Run test
		ConfigurationDaoImpl config = new ConfigurationDaoImpl(connMgr);
		List<String> sections = new ArrayList<String>();
		sections.add("something");
		Map<String, String> result = config.getConfiguration(sections);
		assertEquals(result.get("depth"), "42");
		assertEquals(result.get("author"), "me");
	}

	@Test
	public void testGetConfigurationListOfStringOverlappingSettings() throws SQLException {
		// Setup mocked Database
		ConnectionManager connMgr = mock(ConnectionManager.class);
		Connection conn = mock(Connection.class);
		Statement statement = mock(Statement.class);
		ResultSet resultSet = mock(ResultSet.class);
		when(connMgr.getConnection()).thenReturn(conn);
		when(conn.createStatement()).thenReturn(statement);
		when(statement.executeQuery(anyString())).thenReturn(resultSet);
		// result mock
		when(resultSet.next()).thenReturn(true, true, true, false);
		when(resultSet.getString("key")).thenReturn("depth", "author", "depth");
		when(resultSet.getString("value")).thenReturn("42", "me", "10");

		// Run test
		ConfigurationDaoImpl config = new ConfigurationDaoImpl(connMgr);
		List<String> sections = new ArrayList<String>();
		sections.add("something");
		Map<String, String> result = config.getConfiguration(sections);
		assertEquals(result.get("depth"), "42");
		assertEquals(result.get("author"), "me");
	}

	@Test
	public void testGetConfigurationListOfStringNoSettings() throws SQLException {
		// Setup mocked Database
		ConnectionManager connMgr = mock(ConnectionManager.class);
		Connection conn = mock(Connection.class);
		Statement statement = mock(Statement.class);
		ResultSet resultSet = mock(ResultSet.class);
		when(connMgr.getConnection()).thenReturn(conn);
		when(conn.createStatement()).thenReturn(statement);
		when(statement.executeQuery(anyString())).thenReturn(resultSet);
		// result mock
		when(resultSet.next()).thenReturn(false);

		// Run test
		ConfigurationDaoImpl config = new ConfigurationDaoImpl(connMgr);
		List<String> sections = new ArrayList<String>();
		sections.add("something");
		Map<String, String> result = config.getConfiguration(sections);
		assertEquals(result.size(), 0);
	}

	@Test
	public void testGetConfigurationStringNoSettings() throws SQLException {
		// Setup mocked Database
		ConnectionManager connMgr = mock(ConnectionManager.class);
		Connection conn = mock(Connection.class);
		Statement statement = mock(Statement.class);
		ResultSet resultSet = mock(ResultSet.class);
		when(connMgr.getConnection()).thenReturn(conn);
		when(conn.createStatement()).thenReturn(statement);
		when(statement.executeQuery(anyString())).thenReturn(resultSet);
		// result mock
		when(resultSet.next()).thenReturn(false);

		// Run test
		ConfigurationDaoImpl config = new ConfigurationDaoImpl(connMgr);
		Map<String, String> result = config.getConfiguration("something");
		assertEquals(result.size(), 0);
	}

	@Test
	public void testGetConfigurationString() throws SQLException {
		// Setup mocked Database
		ConnectionManager connMgr = mock(ConnectionManager.class);
		Connection conn = mock(Connection.class);
		Statement statement = mock(Statement.class);
		ResultSet resultSet = mock(ResultSet.class);
		when(connMgr.getConnection()).thenReturn(conn);
		when(conn.createStatement()).thenReturn(statement);
		when(statement.executeQuery(anyString())).thenReturn(resultSet);
		// result mock
		when(resultSet.next()).thenReturn(true, true, false);
		when(resultSet.getString("key")).thenReturn("depth", "author");
		when(resultSet.getString("value")).thenReturn("42", "me");

		// Run test
		ConfigurationDaoImpl config = new ConfigurationDaoImpl(connMgr);
		Map<String, String> result = config.getConfiguration("something");
		assertEquals(result.get("depth"), "42");
		assertEquals(result.get("author"), "me");
	}

	@Test
	public void testGetConfigurationStringOverlappingSettings() throws SQLException {
		// Setup mocked Database
		ConnectionManager connMgr = mock(ConnectionManager.class);
		Connection conn = mock(Connection.class);
		Statement statement = mock(Statement.class);
		ResultSet resultSet = mock(ResultSet.class);
		when(connMgr.getConnection()).thenReturn(conn);
		when(conn.createStatement()).thenReturn(statement);
		when(statement.executeQuery(anyString())).thenReturn(resultSet);
		// result mock
		when(resultSet.next()).thenReturn(true, true, true, false);
		when(resultSet.getString("key")).thenReturn("depth", "author", "depth");
		when(resultSet.getString("value")).thenReturn("42", "me", "10");

		// Run test
		ConfigurationDaoImpl config = new ConfigurationDaoImpl(connMgr);
		Map<String, String> result = config.getConfiguration("Something");
		assertEquals(result.get("depth"), "42");
		assertEquals(result.get("author"), "me");
	}

	@Test
	public void testGetConfiguration() throws SQLException {
		// Setup mocked Database
		ConnectionManager connMgr = mock(ConnectionManager.class);
		Connection conn = mock(Connection.class);
		Statement statement = mock(Statement.class);
		ResultSet resultSet = mock(ResultSet.class);
		when(connMgr.getConnection()).thenReturn(conn);
		when(conn.createStatement()).thenReturn(statement);
		when(statement.executeQuery(anyString())).thenReturn(resultSet);
		// result mock
		when(resultSet.next()).thenReturn(true, true, true, false);
		when(resultSet.getString("key")).thenReturn("depth", "author", "depth");
		when(resultSet.getString("value")).thenReturn("42", "me", "10");

		// Run test
		ConfigurationDaoImpl config = new ConfigurationDaoImpl(connMgr);
		Map<String, String> result = config.getConfiguration();
		assertEquals(result.get("depth"), "10");
		assertEquals(result.get("author"), "42");
	}

	@Test
	public void testUpdateConfiguration() throws SQLException {
		// Setup mocked Database
		ConnectionManager connMgr = mock(ConnectionManager.class);
		Connection conn = mock(Connection.class);
		Statement statement = mock(Statement.class);
		when(connMgr.getConnection()).thenReturn(conn);
		when(conn.createStatement()).thenReturn(statement);
		when(statement.executeUpdate(anyString())).thenReturn(1);

		// Run test
		ConfigurationDaoImpl config = new ConfigurationDaoImpl(connMgr);
		config.updateConfiguration("section", "key", "value", 0);
		verify(statement).executeUpdate(anyString());
	}

	@Test
	public void testUpdateConfigurationFailed() throws SQLException {
		// Setup mocked Database
		ConnectionManager connMgr = mock(ConnectionManager.class);
		Connection conn = mock(Connection.class);
		Statement statement = mock(Statement.class);
		when(connMgr.getConnection()).thenReturn(conn);
		when(conn.createStatement()).thenReturn(statement);
		when(statement.executeUpdate(anyString())).thenReturn(0, 1);

		// Run test
		ConfigurationDaoImpl config = new ConfigurationDaoImpl(connMgr);
		config.updateConfiguration("section", "key", "value", 0);
		verify(statement, times(2)).executeUpdate(anyString());
	}

	@Test
	public void testDeleteConfiguration() throws SQLException {
		// Setup mocked Database
		ConnectionManager connMgr = mock(ConnectionManager.class);
		Connection conn = mock(Connection.class);
		Statement statement = mock(Statement.class);
		when(connMgr.getConnection()).thenReturn(conn);
		when(conn.createStatement()).thenReturn(statement);
		when(statement.executeUpdate(anyString())).thenReturn(1);

		// Run test
		ConfigurationDaoImpl config = new ConfigurationDaoImpl(connMgr);
		config.deleteConfiguration("section");
		verify(statement).executeUpdate(anyString());
	}

	@Test
	public void testDeleteConfiguration2() throws SQLException {
		// Setup mocked Database
		ConnectionManager connMgr = mock(ConnectionManager.class);
		Connection conn = mock(Connection.class);
		Statement statement = mock(Statement.class);
		when(connMgr.getConnection()).thenReturn(conn);
		when(conn.createStatement()).thenReturn(statement);
		when(statement.executeUpdate(anyString())).thenReturn(1);

		// Run test
		ConfigurationDaoImpl config = new ConfigurationDaoImpl(connMgr);
		config.deleteConfiguration("section", "key");
		verify(statement).executeUpdate(anyString());
	}

}
