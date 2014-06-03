package test.java.distributed;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import main.java.distributed.ConnectionManager;
import main.java.distributed.DatabaseUtils;
import main.java.distributed.IConnectionManager;

/**
 * Integration test for the database utils.
 */
public class TestDBUtilsFlushToDB {

	private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
	private IConnectionManager con = new ConnectionManager();
	private DatabaseUtils dbUtils = new DatabaseUtils(con);
	
	
	@Test
	public void testFlushWebsites() throws SQLException, IOException {
		this.makeWebsiteFile();
		
		dbUtils.actionFlushWebsitesFile("/testFlushWebsites.txt");

		int idFirst = this.getIdFromUrl("http://thiMayNotExist.hu");
		assertFalse(idFirst == -1);
		this.deleteWorktaskById(idFirst);
		
		int idSecond = this.getIdFromUrl("http://maybeThis.uk");
		assertFalse(idSecond == -1);
		this.deleteWorktaskById(idSecond);
		
		int idThird = this.getIdFromUrl("http://andTheLastOne.pl");
		assertFalse(idThird == -1);
		this.deleteWorktaskById(idThird);
		
		new File("config/testFlushWebsites.txt").delete();
	}
	
	private void makeWebsiteFile() throws IOException {
		PrintWriter websiteFile;
		try {
			websiteFile = new PrintWriter(new FileWriter("config/testFlushWebsites.txt", true));
			websiteFile.println("http://thiMayNotExist.hu");
			websiteFile.println("http://maybeThis.uk");
			websiteFile.println("http://andTheLastOne.pl");
			websiteFile.close();
		} catch (FileNotFoundException e) {
			LOGGER.error("FileNotFoundException while making the stub website-file");
			System.exit(1);
		} catch (UnsupportedEncodingException e) {
			LOGGER.error("UnsupportedEncodingException while making the the stub website-file");
			System.exit(1);
		}
	}
	/**
	 * Get the workload id from the database for the given url.
	 * @return The workload id, if the database contains the url, else -1.
	 * @throws SQLException
	 */
	private int getIdFromUrl(String url) throws SQLException {
		String sql = "SELECT id FROM workload WHERE url=?";
		PreparedStatement st = con.getConnection().prepareStatement(sql);
		st.setString(1, url);
		
		ResultSet resset = st.executeQuery();
		int id = -1;
		while (resset.next()) {
			id = resset.getInt(1);
		}
		return id;
	}
	
	private void deleteWorktaskById(int id) throws SQLException {
		String sql = "DELETE FROM workload WHERE id=?";
		PreparedStatement st = con.getConnection().prepareStatement(sql);
		st.setInt(1, id);
		
		int delete = st.executeUpdate();
		assertEquals(1, delete);
	}
	
	@Test
	public void testFlushSettings() throws SQLException, IOException {
		this.makeSettingsFile();
		
		dbUtils.actionFlushSettingsFile("/testFlushSettings.txt");
		// Test if succesful
		this.checkIfCorrectlyInserted("notExists", "common", "99");
		this.checkIfCorrectlyInserted("something", "demo.crawljax.com", "-9");
		
		this.deleteConfigByKey("notExists");
		this.deleteConfigByKey("something");
		
		new File("config/testFlushSettings.txt").delete();
	}
	
	private void makeSettingsFile() throws IOException {
		PrintWriter websiteFile;
		try {
			websiteFile = new PrintWriter(new FileWriter("config/testFlushSettings.txt", true));
			websiteFile.println("[common]");
			websiteFile.println("notExists = 99");
			websiteFile.println("[demo.crawljax.com]");
			websiteFile.println("something = -9");
			websiteFile.close();
		} catch (FileNotFoundException e) {
			LOGGER.error("FileNotFoundException while making the stub settings-file");
			System.exit(1);
		} catch (UnsupportedEncodingException e) {
			LOGGER.error("UnsupportedEncodingException while making the the stub settings-file");
			System.exit(1);
		}
	}
	
	private void checkIfCorrectlyInserted(String key, String sectionExp, String valueExp) throws SQLException {
		String sql = "SELECT * FROM configuration WHERE 'key'=?";
		PreparedStatement st = con.getConnection().prepareStatement(sql);
		st.setString(1, key);
		
		ResultSet resset = st.executeQuery();
		while (resset.next()) {
			String section = resset.getString(1);
			assertEquals(sectionExp, section);
			
			String value = resset.getString(3);
			assertEquals(valueExp, value);
		}
	}
	
	private void deleteConfigByKey(String key) throws SQLException {
		String sql = "DELETE FROM configuration WHERE `key`=?";
		PreparedStatement st = con.getConnection().prepareStatement(sql);
		st.setString(1, key);
		
		int delete = st.executeUpdate();
		assertEquals(1, delete);
	}
}
