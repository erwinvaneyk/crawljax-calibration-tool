package test.java.distributed;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import main.java.distributed.ConnectionManager;
import main.java.distributed.DatabaseUtils;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestDatabaseUtils {
	
	ConnectionManager con;
	
	@Before
	public void insertNewResult() throws SQLException {
		con = new ConnectionManager();
		Statement st = con.getConnection().createStatement();
		
		String makeWorload = "INSERT INTO workload VALUES (-1, 'http://test.nl', '', 0)";
		String makeWebsiteResults = "INSERT INTO WebsiteResults VALUES (-2, -1, 'json', 10)";
		String makeDomResults1 = "INSERT INTO DomResults VALUES (-2, 'index', 'dom', 'strippeddom', 101101001, null)";
		String makeDomResults2 = "INSERT INTO DomResults VALUES (-2, 'state1', 'dom', 'strippeddom', 110101011, null)";
		String makeDomResults3 = "INSERT INTO DomResults VALUES (-2, 'state2', 'dom', 'strippeddom', 100001001, null)";
		
		
		st.executeUpdate(makeWorload);
		st.executeUpdate(makeWebsiteResults);
		st.executeUpdate(makeDomResults1);
		st.executeUpdate(makeDomResults2);
		st.executeUpdate(makeDomResults3);
		
		this.testIfCorrectlyInserted("DomResults", "websiteResult_id", -2, 3);
		this.testIfCorrectlyInserted("WebsiteResults", "id", -2, 1);
		this.testIfCorrectlyInserted("workload", "id", -1, 1);
	}
	
	@After
	public void closeConnection() {
		con.closeConnection();
	}
	
	public void testIfCorrectlyInserted(String table, String column, int id, int expected) throws SQLException {
		String getDomResultBeforeDeletion = "SELECT COUNT(*) FROM " + table + " WHERE " + column + "=" + id;
		Statement st = con.getConnection().createStatement();
		
		ResultSet resset = st.executeQuery(getDomResultBeforeDeletion);
		int countBefore = -1;
		while (resset.next()) {
			countBefore = resset.getInt(1);
		}
		assertEquals(expected, countBefore);
	}
	
	public void deleteAndCheck(String sql, int expected) throws SQLException {
		DatabaseUtils dbutils = new DatabaseUtils(con);
		
		boolean deleted = dbutils.deleteAllResultsById(-2);
		assertTrue(deleted);
		
		con = new ConnectionManager();
		Statement st = con.getConnection().createStatement();
		
		ResultSet resultset = st.executeQuery(sql);
		int count = -1;
		while (resultset.next()) {
			count = resultset.getInt(1);
		}
		assertEquals(expected, count);
	}
	
	@Test
	public void testDeleteDomCorrectly() throws SQLException {
		String getDomResult = "SELECT COUNT(*) FROM DomResults WHERE websiteResult_id=-2";
		deleteAndCheck(getDomResult, 0);
	}
	
	@Test
	public void testDeleteWebsiteResultsCorrectly() throws SQLException {
		String getDomResult = "SELECT COUNT(*) FROM WebsiteResults WHERE id=-2";
		deleteAndCheck(getDomResult, 0);
	}
	
	@Test
	public void testDeleteworkloadCorrectly() throws SQLException {
		String getDomResult = "SELECT COUNT(*) FROM workload WHERE id=-1";
		deleteAndCheck(getDomResult, 0);
	}
}
