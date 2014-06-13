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

import org.junit.Test;

import suite.TestingSuiteModule;
import suite.distributed.ConnectionManager;
import suite.distributed.ConnectionManagerImpl;
import suite.distributed.DatabaseUtils;

import com.google.inject.Guice;

import lombok.extern.slf4j.Slf4j;

/**
 * Integration test for the database utils.
 */
@Slf4j
public class TestDBUtilsFlushToDatabase {
	private ConnectionManager con = new ConnectionManagerImpl();
	private DatabaseUtils dbUtils = Guice.createInjector(new TestingSuiteModule()).getInstance(
	        DatabaseUtils.class);

	@Test
	public void testFlushWebsites() throws SQLException, IOException {
		this.makeWebsiteFile();

		dbUtils.actionFlushWebsitesFile(new File("/testFlushWebsites.txt"));

		int idFirst = this.getIdFromUrl("http://thiMayNotExist.hu");
		assertThat(idFirst, not(is(-1)));
		this.deleteWorktaskById(idFirst);

		int idSecond = this.getIdFromUrl("http://maybeThis.uk");
		assertThat(idSecond, not(is(-1)));
		this.deleteWorktaskById(idSecond);

		int idThird = this.getIdFromUrl("http://andTheLastOne.pl");
		assertThat(idThird, not(is(-1)));
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
			log.error("FileNotFoundException while making the stub website-file");
			System.exit(1);
		} catch (UnsupportedEncodingException e) {
			log.error("UnsupportedEncodingException while making the the stub website-file");
			System.exit(1);
		}
	}

	/**
	 * Get the workload id from the database for the given url.
	 * 
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

		dbUtils.actionFlushSettingsFile(new File("/testFlushSettings.txt"));
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
			log.error("FileNotFoundException while making the stub settings-file");
			System.exit(1);
		} catch (UnsupportedEncodingException e) {
			log.error("UnsupportedEncodingException while making the the stub settings-file");
			System.exit(1);
		}
	}

	private void checkIfCorrectlyInserted(String key, String sectionExp, String valueExp)
	        throws SQLException {
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
