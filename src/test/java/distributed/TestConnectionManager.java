package test.java.distributed;

import static org.junit.Assert.*;

import java.sql.Connection;
import java.sql.SQLException;

import main.java.distributed.*;

import org.junit.Test;

public class TestConnectionManager {

	@Test
	public void testGetConnection() {
		IConnectionManager connMgr = new ConnectionManager();
		Connection conn = connMgr.getConnection();
		assertNotNull(conn);
	}
	
	@Test
	public void testGetActiveConnection() throws SQLException {
		IConnectionManager connMgr = new ConnectionManager();
		Connection conn = connMgr.getConnection();
		Connection conn2 = connMgr.getConnection();
		assertEquals(conn, conn2);
	}
	
	@Test
	public void testCloseActiveConnection() throws SQLException {
		IConnectionManager connMgr = new ConnectionManager();
		Connection conn = connMgr.getConnection();
		connMgr.closeConnection();
		assertTrue(conn.isClosed());
	}
	
	@Test
	public void testRedundantCloseActiveConnection() throws SQLException {
		IConnectionManager connMgr = new ConnectionManager();
		Connection conn = connMgr.getConnection();
		connMgr.closeConnection();
		connMgr.closeConnection();
		assertTrue(conn.isClosed());
	}
	
	@Test
	public void testCloseInactiveConnection() throws SQLException {
		IConnectionManager connMgr = new ConnectionManager();
		connMgr.closeConnection();
	}
	
	@Test
	public void testGetClosedConnection() {
		IConnectionManager connMgr = new ConnectionManager();
		connMgr.getConnection();
		connMgr.closeConnection();
		Connection conn = connMgr.getConnection();
		assertNotNull(conn);		
	}

}
