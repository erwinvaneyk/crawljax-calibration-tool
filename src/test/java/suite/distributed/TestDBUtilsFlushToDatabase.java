package suite.distributed;

import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.hamcrest.CoreMatchers.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import suite.TestingSuiteModule;

import com.google.inject.Guice;

import lombok.extern.slf4j.Slf4j;

/**
 * Integration test for the database utils.
 */
@Slf4j
public class TestDBUtilsFlushToDatabase {
	
	private static final String WEBSITES_FILE = "output/testFlushWebsites.txt";
	private static final String STUD_WEBSITE_1 = "http://www.teststubwebsiteA.hu";
	private static final String STUD_WEBSITE_2 = "http://www.teststubwebsiteB.hu";
	private static final String STUD_WEBSITE_3 = "http://www.testsstubwebsiteC.hu";
	private static final String SETTINGS_FILE = "output/testFlushSettings.txt";
	
	private ConnectionManager con = new ConnectionManagerImpl();
	private DatabaseUtils dbUtils = Guice.createInjector(new TestingSuiteModule("TEST")).getInstance(
	        DatabaseUtils.class);
	
	@BeforeClass
	public static void buildFile() {
		// remove old
		new File(WEBSITES_FILE).delete();
		new File(SETTINGS_FILE).delete();
		
		makeWebsiteFile(WEBSITES_FILE);
	}
	
	@AfterClass
	public static void deleteFile() {
		new File(WEBSITES_FILE).delete();
		new File(SETTINGS_FILE).delete();
	}

	@Test
	public void testFlushWebsites() throws SQLException, IOException {

		dbUtils.actionFlushWebsitesFile(new File(WEBSITES_FILE));

		int idFirst = this.getIdFromUrl(STUD_WEBSITE_1);
		assertThat(idFirst, not(is(-1)));
		assertThat(deleteWorktaskById(idFirst), is(1));
		int idSecond = this.getIdFromUrl(STUD_WEBSITE_2);
		assertThat(idSecond, not(is(-1)));
		assertThat(deleteWorktaskById(idSecond), is(1));
		int idThird = this.getIdFromUrl(STUD_WEBSITE_3);
		assertThat(idThird, not(is(-1)));
		assertThat(deleteWorktaskById(idThird), is(1));
	}

	private static void makeWebsiteFile(String filename) {
		try(PrintWriter websiteFile = new PrintWriter(new FileWriter(filename, true))) {
			websiteFile.println(STUD_WEBSITE_1);
			websiteFile.println(STUD_WEBSITE_2);
			websiteFile.println(STUD_WEBSITE_3);
			websiteFile.close();
		} catch (FileNotFoundException e) {
			log.error("FileNotFoundException while making the stub website-file: {}", e.getMessage());
		} catch (UnsupportedEncodingException e) {
			log.error("UnsupportedEncodingException while making the the stub website-file: {}", e.getMessage());
		} catch (IOException e) {
			log.error("IOException while making the the stub website-file: {}", e.getMessage());
        }
	}

	/**
	 * Get the workload id from the database for the given url.
	 * 
	 * @return The workload id, if the database contains the url, else -1.
	 * @throws SQLException
	 */
	private int getIdFromUrl(String url) throws SQLException {
		String sql = "SELECT id FROM workload WHERE `url`=?";
		PreparedStatement st = con.getConnection().prepareStatement(sql);
		st.setString(1, url);

		ResultSet resset = st.executeQuery();
		int id = -1;
		while (resset.next()) {
			id = resset.getInt(1);
		}
		return id;
	}

	private int deleteWorktaskById(int id) throws SQLException {
		String sql = "DELETE FROM workload WHERE id=?";
		PreparedStatement st = con.getConnection().prepareStatement(sql);
		st.setInt(1, id);
		return st.executeUpdate();
	}

	@Test
	public void testFlushSettings() throws SQLException, IOException {
		this.makeSettingsFile();

		dbUtils.actionFlushSettingsFile(new File(SETTINGS_FILE));
		// Test if succesful
		this.checkIfCorrectlyInserted("notExists", "common", "99");
		this.checkIfCorrectlyInserted("something", "demo.crawljax.com", "-9");

		assertThat(deleteConfigByKey("notExists"), is(1));
		assertThat(deleteConfigByKey("something"), is(1));

		new File("config/testFlushSettings.txt").delete();
	}

	private void makeSettingsFile() {
		try(PrintWriter websiteFile = new PrintWriter(new FileWriter(SETTINGS_FILE, true))) {
			websiteFile.println("[common]");
			websiteFile.println("notExists = 99");
			websiteFile.println("[demo.crawljax.com]");
			websiteFile.println("something = -9");
			websiteFile.close();
		} catch (FileNotFoundException e) {
			log.error("FileNotFoundException while making the stub settings-file: {}", e.getMessage());
		} catch (UnsupportedEncodingException e) {
			log.error("UnsupportedEncodingException while making the the stub settings-file: {}", e.getMessage());
		} catch (IOException e) {
			log.error("IOException while making the the stub website-file: {}", e.getMessage());
        }
	}

	private void checkIfCorrectlyInserted(String key, String sectionExp, String valueExp)
	        throws SQLException {
		String sql = "SELECT * FROM configuration WHERE `key`=?";
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

	private int deleteConfigByKey(String key) throws SQLException {
		String sql = "DELETE FROM configuration WHERE `key`=?";
		PreparedStatement st = con.getConnection().prepareStatement(sql);
		st.setString(1, key);
		return st.executeUpdate();
	}
}
