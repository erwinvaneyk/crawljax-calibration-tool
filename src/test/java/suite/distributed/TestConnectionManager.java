package suite.distributed;

import static org.junit.Assert.*;

import java.sql.Connection;
import java.sql.SQLException;

import org.junit.Test;

import suite.distributed.ConnectionManager;
import suite.distributed.ConnectionManagerImpl;

public class TestConnectionManager {

	@Test
	public void testGetConnection() {
		ConnectionManager connMgr = new ConnectionManagerImpl();
		Connection conn = connMgr.getConnection();
		assertNotNull(conn);
	}

	@Test
	public void testGetActiveConnection() throws SQLException {
		ConnectionManager connMgr = new ConnectionManagerImpl();
		Connection conn = connMgr.getConnection();
		assertNotNull(conn);
		Connection conn2 = connMgr.getConnection();
		assertNotNull(conn2);
		assertEquals(conn, conn2);
	}

	@Test
	public void testCloseActiveConnection() throws SQLException {
		ConnectionManager connMgr = new ConnectionManagerImpl();
		Connection conn = connMgr.getConnection();
		connMgr.closeConnection();
		assertTrue(conn.isClosed());
	}

	@Test
	public void testRedundantCloseActiveConnection() throws SQLException {
		ConnectionManager connMgr = new ConnectionManagerImpl();
		Connection conn = connMgr.getConnection();
		connMgr.closeConnection();
		connMgr.closeConnection();
		assertTrue(conn.isClosed());
	}

	@Test
	public void testCloseInactiveConnection() throws SQLException {
		ConnectionManager connMgr = new ConnectionManagerImpl();
		connMgr.closeConnection();
	}

	@Test
	public void testGetClosedConnection() {
		ConnectionManager connMgr = new ConnectionManagerImpl();
		connMgr.getConnection();
		connMgr.closeConnection();
		Connection conn = connMgr.getConnection();
		assertNotNull(conn);
	}

}
