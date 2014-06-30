package suite.distributed.workload;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import suite.distributed.ConnectionManagerImpl;

public class TestWorkloadDao {

	@Test
	public void testRetrieveWorkInt0() throws SQLException {
		// Result set
		List<WorkTask> expected = new ArrayList<WorkTask>();
		// Mock objects
		ConnectionManagerImpl connMgr = mock(ConnectionManagerImpl.class);
		Connection conn = mock(Connection.class);
		Statement statement = mock(Statement.class);
		ResultSet results = mock(ResultSet.class);
		// Mock methods
		when(connMgr.getConnection()).thenReturn(conn);
		when(conn.createStatement()).thenReturn(statement);
		when(statement.executeUpdate(anyString())).thenReturn(expected.size());
		when(statement.executeQuery(anyString())).thenReturn(results);
		when(results.next()).thenReturn(false);
		// Run method under inspection
		WorkloadDaoImpl wldao = new WorkloadDaoImpl(connMgr);
		List<WorkTask> finalReturn = wldao.retrieveWork(expected.size());
		assertEquals(finalReturn, expected);
	}

	@Test
	public void testRetrieveWorkInt1() throws SQLException, MalformedURLException {
		// Result set
		List<WorkTask> expected = new ArrayList<WorkTask>();
		WorkTask wt1 = new WorkTask(1, new URL("http://1.com"));
		expected.add(wt1);
		// Mock objects
		ConnectionManagerImpl connMgr = mock(ConnectionManagerImpl.class);
		Connection conn = mock(Connection.class);
		Statement statement = mock(Statement.class);
		ResultSet results = mock(ResultSet.class);
		// Mock methods
		when(connMgr.getConnection()).thenReturn(conn);
		when(conn.createStatement()).thenReturn(statement);
		when(statement.executeUpdate(anyString())).thenReturn(expected.size());
		when(statement.executeQuery(anyString())).thenReturn(results);
		when(results.next()).thenReturn(true, false);
		when(results.getInt("id")).thenReturn(wt1.getId());
		when(results.getString("url")).thenReturn(wt1.getURL().toString());
		// Run method under inspection
		WorkloadDaoImpl wldao = new WorkloadDaoImpl(connMgr);
		List<WorkTask> finalReturn = wldao.retrieveWork(expected.size());
		assertEquals(finalReturn, expected);
	}

	@Test
	public void testRetrieveWorkInt2() throws SQLException, MalformedURLException {
		// Result set
		List<WorkTask> expected = new ArrayList<WorkTask>();
		WorkTask wt1 = new WorkTask(1, new URL("http://1.com"));
		WorkTask wt2 = new WorkTask(2, new URL("http://2.com"));
		expected.add(wt1);
		expected.add(wt2);
		// Mock objects
		ConnectionManagerImpl connMgr = mock(ConnectionManagerImpl.class);
		Connection conn = mock(Connection.class);
		Statement statement = mock(Statement.class);
		ResultSet results = mock(ResultSet.class);
		// Mock methods
		when(connMgr.getConnection()).thenReturn(conn);
		when(conn.createStatement()).thenReturn(statement);
		when(statement.executeUpdate(anyString())).thenReturn(expected.size());
		when(statement.executeQuery(anyString())).thenReturn(results);
		when(results.next()).thenReturn(true, true, false);
		when(results.getInt("id")).thenReturn(wt1.getId(), wt2.getId());
		when(results.getString("url")).thenReturn(wt1.getURL().toString(),
		        wt2.getURL().toString());
		// Run method under inspection
		WorkloadDaoImpl wldao = new WorkloadDaoImpl(connMgr);
		List<WorkTask> finalReturn = wldao.retrieveWork(expected.size());
		assertEquals(finalReturn, expected);
	}

	@Test
	public void testRetrieveWorkIntSQLExceptionSELECT() throws SQLException {
		// Result set
		int expectedSize = 4;
		List<WorkTask> expected = new ArrayList<WorkTask>();
		// Mock objects
		ConnectionManagerImpl connMgr = mock(ConnectionManagerImpl.class);
		Connection conn = mock(Connection.class);
		Statement statement = mock(Statement.class);
		// Mock methods
		when(connMgr.getConnection()).thenReturn(conn);
		when(conn.createStatement()).thenReturn(statement);
		when(statement.executeUpdate(anyString())).thenReturn(expectedSize);
		when(statement.executeQuery(anyString())).thenThrow(new SQLException("MOCK SQL ERROR"));
		// Run method under inspection
		WorkloadDaoImpl wldao = new WorkloadDaoImpl(connMgr);
		List<WorkTask> finalReturn = wldao.retrieveWork(expectedSize);
		assertEquals(finalReturn, expected);
	}

	@Test
	public void testRetrieveWorkIntSQLExceptionUPDATE() throws SQLException {
		// Result set
		int expectedSize = 4;
		List<WorkTask> expected = new ArrayList<WorkTask>();
		// Mock objects
		ConnectionManagerImpl connMgr = mock(ConnectionManagerImpl.class);
		Connection conn = mock(Connection.class);
		Statement statement = mock(Statement.class);
		ResultSet results = mock(ResultSet.class);
		// Mock methods
		when(connMgr.getConnection()).thenReturn(conn);
		when(conn.createStatement()).thenReturn(statement);
		when(statement.executeUpdate(anyString())).thenThrow(new SQLException("MOCK SQL ERROR"));
		when(statement.executeQuery(anyString())).thenReturn(results);
		// Run method under inspection
		WorkloadDaoImpl wldao = new WorkloadDaoImpl(connMgr);
		List<WorkTask> finalReturn = wldao.retrieveWork(expectedSize);
		assertEquals(finalReturn, expected);
	}

	@Test
	public void testRetrieveWorkIntMoreThanAvailable() throws SQLException, MalformedURLException {
		// Result set
		List<WorkTask> expected = new ArrayList<WorkTask>();
		WorkTask wt1 = new WorkTask(1, new URL("http://1.com"));
		expected.add(wt1);
		// Mock objects
		ConnectionManagerImpl connMgr = mock(ConnectionManagerImpl.class);
		Connection conn = mock(Connection.class);
		Statement statement = mock(Statement.class);
		ResultSet results = mock(ResultSet.class);
		// Mock methods
		when(connMgr.getConnection()).thenReturn(conn);
		when(conn.createStatement()).thenReturn(statement);
		when(statement.executeUpdate(anyString())).thenReturn(expected.size());
		when(statement.executeQuery(anyString())).thenReturn(results);
		when(results.next()).thenReturn(true, false);
		when(results.getInt("id")).thenReturn(wt1.getId());
		when(results.getString("url")).thenReturn(wt1.getURL().toString());
		// Run method under inspection
		WorkloadDaoImpl wldao = new WorkloadDaoImpl(connMgr);
		List<WorkTask> finalReturn = wldao.retrieveWork(expected.size() + 10); // Ask for 10 more,
																			   // than received
		assertEquals(finalReturn, expected);
	}

	@Test(expected = AssertionError.class)
	public void testRetrieveWorkNegativeInt() {
		ConnectionManagerImpl connMgr = mock(ConnectionManagerImpl.class);
		WorkloadDaoImpl wldao = new WorkloadDaoImpl(connMgr);
		wldao.retrieveWork(-1);
	}

	@Test
	public void testCheckoutWork() throws SQLException, MalformedURLException {
		int expected = 3;
		WorkTask wt1 = new WorkTask(1, new URL("http://1.com"));
		// Mock objects
		ConnectionManagerImpl connMgr = mock(ConnectionManagerImpl.class);
		Connection conn = mock(Connection.class);
		Statement statement = mock(Statement.class);
		// Mock methods
		when(connMgr.getConnection()).thenReturn(conn);
		when(conn.createStatement()).thenReturn(statement);
		when(statement.executeUpdate(anyString())).thenReturn(expected);
		// Run method under inspection
		WorkloadDaoImpl wldao = new WorkloadDaoImpl(connMgr);
		assertEquals(wldao.checkoutWork(wt1), true);
		verify(statement).executeUpdate(anyString());
	}

	@Test
	public void testCheckoutWorkSQLException() throws SQLException, MalformedURLException {
		WorkTask wt1 = new WorkTask(1, new URL("http://1.com"));
		// Mock objects
		ConnectionManagerImpl connMgr = mock(ConnectionManagerImpl.class);
		Connection conn = mock(Connection.class);
		Statement statement = mock(Statement.class);
		// Mock methods
		when(connMgr.getConnection()).thenReturn(conn);
		when(conn.createStatement()).thenReturn(statement);
		when(statement.executeUpdate(anyString())).thenThrow(new SQLException("MOCK SQL ERROR"));
		// Run method under inspection
		WorkloadDaoImpl wldao = new WorkloadDaoImpl(connMgr);
		assertEquals(wldao.checkoutWork(wt1), false);
		verify(statement).executeUpdate(anyString());
	}

	@Test
	public void testCheckoutWorkInvalid() throws MalformedURLException {
		WorkTask wt1 = new WorkTask(1, new URL("http://1.com"));
		// Mock objects
		ConnectionManagerImpl connMgr = mock(ConnectionManagerImpl.class);
		// Run method under inspection
		WorkloadDaoImpl wldao = new WorkloadDaoImpl(connMgr);
		assertFalse(wldao.checkoutWork(wt1));
	}

	@Test
	public void testSubmitWork() throws MalformedURLException, SQLException {
		int expected = 1;
		URL url1 = new URL("http://1.com");
		// Mock objects
		ConnectionManagerImpl connMgr = mock(ConnectionManagerImpl.class);
		Connection conn = mock(Connection.class);
		Statement statement = mock(Statement.class);
		// Mock methods
		when(connMgr.getConnection()).thenReturn(conn);
		when(conn.createStatement()).thenReturn(statement);
		when(statement.executeUpdate(anyString(), anyInt())).thenReturn(expected);
		// Run method under inspection
		WorkloadDaoImpl wldao = new WorkloadDaoImpl(connMgr);
		assertEquals(wldao.submitWork(url1, true), expected);
	}

	@Test
	public void testSubmitWorkSQLException() throws MalformedURLException, SQLException {
		URL url1 = new URL("http://1.com");
		// Mock objects
		ConnectionManagerImpl connMgr = mock(ConnectionManagerImpl.class);
		Connection conn = mock(Connection.class);
		Statement statement = mock(Statement.class);
		// Mock methods
		when(connMgr.getConnection()).thenReturn(conn);
		when(conn.createStatement()).thenReturn(statement);
		when(statement.executeUpdate(anyString(), anyInt())).thenThrow(
		        new SQLException("MOCK SQL ERROR"));
		// Run method under inspection
		WorkloadDaoImpl wldao = new WorkloadDaoImpl(connMgr);
		assertEquals(wldao.submitWork(url1, true), -1);
	}

	@Test
	public void testSubmitWorkNull() {
		URL url1 = null;
		// Mock objects
		ConnectionManagerImpl connMgr = mock(ConnectionManagerImpl.class);
		// Run method under inspection
		WorkloadDaoImpl wldao = new WorkloadDaoImpl(connMgr);
		wldao.submitWork(url1, true);
	}

	@Test
	public void testRevertWork() throws SQLException {
		// Mock objects
		ConnectionManagerImpl connMgr = mock(ConnectionManagerImpl.class);
		Connection conn = mock(Connection.class);
		Statement statement = mock(Statement.class);
		// Mock methods
		when(connMgr.getConnection()).thenReturn(conn);
		when(conn.createStatement()).thenReturn(statement);
		when(statement.executeUpdate(anyString())).thenReturn(1);
		// Run method under inspection
		WorkloadDaoImpl wldao = new WorkloadDaoImpl(connMgr);
		assertTrue(wldao.revertWork(42));
	}

	@Test
	public void testRevertWorkUnknownId() throws SQLException {
		// Mock objects
		ConnectionManagerImpl connMgr = mock(ConnectionManagerImpl.class);
		Connection conn = mock(Connection.class);
		Statement statement = mock(Statement.class);
		// Mock methods
		when(connMgr.getConnection()).thenReturn(conn);
		when(conn.createStatement()).thenReturn(statement);
		when(statement.executeUpdate(anyString())).thenReturn(0);
		// Run method under inspection
		WorkloadDaoImpl wldao = new WorkloadDaoImpl(connMgr);
		assertFalse(wldao.revertWork(42));
	}

	@Test
	public void testRevertWorkSQLException() throws SQLException {
		int id = 1;
		// Mock objects
		ConnectionManagerImpl connMgr = mock(ConnectionManagerImpl.class);
		Connection conn = mock(Connection.class);
		Statement statement = mock(Statement.class);
		// Mock methods
		when(connMgr.getConnection()).thenReturn(conn);
		when(conn.createStatement()).thenReturn(statement);
		when(statement.executeUpdate(anyString())).thenThrow(new SQLException("MOCK SQL ERROR"));
		// Run method under inspection
		WorkloadDaoImpl wldao = new WorkloadDaoImpl(connMgr);
		assertNotNull(wldao.toString());
		assertFalse(wldao.revertWork(id));
	}
}
