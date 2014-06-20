package suite.distributed.results;

import static org.mockito.Mockito.*;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import suite.distributed.ConnectionManagerImpl;
import suite.distributed.results.ResultDao;
import suite.distributed.results.ResultProcessorException;
import suite.distributed.results.ResultProcessorImpl;

@Slf4j
public class TestResultProcessor {

	/**
	 * Make the directory 'TestDir'
	 */
	@Before
	public void makeTestDir() {
		new File("TestDir").mkdir();
		log.debug("Test directory setup.");
	}

	/**
	 * Delete the whole directory 'TestDir'
	 * 
	 * @throws IOException
	 */
	@After
	public void removeFileStructure() {
		try {
			FileUtils.deleteDirectory(new File("TestDir"));
			log.debug("Test directory removed.");
		} catch (IOException e) {
			log.error("IOException while removing the TestDir directory: " + e.getMessage());
		}
	}

	private void mockAndRun(boolean dbContainsTuple, int updateSucces) throws SQLException,
	        ResultProcessorException {
		// Mock objects
		ConnectionManagerImpl connMgr = mock(ConnectionManagerImpl.class);
		Connection conn = mock(Connection.class);
		PreparedStatement statement = mock(PreparedStatement.class);
		ResultSet resultset = mock(ResultSet.class);
		// Mock methods
		when(connMgr.getConnection()).thenReturn(conn);
		when(conn.prepareStatement(anyString(), anyInt())).thenReturn(statement);
		assertEquals(statement, conn.prepareStatement("SELECT * FROM workload", 5));

		when(statement.executeUpdate()).thenReturn(updateSucces);
		assertEquals(updateSucces, statement.executeUpdate());

		when(statement.getGeneratedKeys()).thenReturn(resultset);

		when(statement.executeQuery()).thenReturn(resultset);
		when(resultset.next()).thenReturn(dbContainsTuple);
		// Method under inspection
		ResultDao upload = new ResultDao(connMgr);
		ResultProcessorImpl resProc = new ResultProcessorImpl(upload);
		resProc.uploadResults(1, new File("TestDir"), 10);
	}

	/**
	 * Make a stub file named result.json
	 * 
	 * @throws FileNotFoundException
	 * @throws UnsupportedEncodingException
	 */
	private void makeJsonStub() {
		PrintWriter json;
		try {
			json = new PrintWriter("TestDir/result.json", "UTF-8");
			json.println("This is a test");
			json.println("For the class ResultProcessorImpl.java");
			json.close();
		} catch (FileNotFoundException e) {
			log.error("FileNotFoundException while adding the stub Json-file to the test directory");
		} catch (UnsupportedEncodingException e) {
			log.error("UnsupportedEncodingException while making the the stub Json-file");
		}
	}

	/**
	 * Make a the directory 'screenshot' and a stub file for a single screenshot
	 * 
	 * @throws IOException
	 */
	private void makeScreenshotStub() {
		new File("TestDir/screenshots").mkdir();
		byte imageBin[] = { 0, 1, 0, 0 };

		FileOutputStream screenshot;
		try {
			screenshot = new FileOutputStream(new File("TestDir/screenshots/shot1.jpg"));
			screenshot.write(imageBin);
			screenshot.close();
		} catch (IOException e) {
			log.error("IOException while making the screenshot stub file");
		}
	}

	private void makeDomStub(String sd) {
		new File("TestDir/" + sd).mkdir();
		PrintWriter dom;
		try {
			dom = new PrintWriter("TestDir/" + sd + "/state1.html", "UTF-8");
			dom.println("Just a test");
			dom.println("For the " + sd);
			dom.close();
		} catch (FileNotFoundException e) {
			log.error("FileNotFoundException while adding the stub " + sd
			        + "-file to the test directory");
		} catch (UnsupportedEncodingException e) {
			log.error("UnsupportedEncodingException while making the the stub " + sd + "-file");
		}
	}

	private void makeFileStructure() {
		makeJsonStub();
		makeScreenshotStub();
		makeDomStub("doms");
		makeDomStub("strippedDOM");
	}

	/**
	 * Test a good run
	 * 
	 * @throws ResultProcessorException
	 * @throws SQLException
	 */
	@Test
	public void testUploadAction() throws ResultProcessorException, SQLException {
		makeFileStructure();

		mockAndRun(false, 1);
	}

	/**
	 * Test a run with missing Json-file
	 * 
	 * @throws ResultProcessorException
	 * @throws SQLException
	 */
	@Test(expected = ResultProcessorException.class)
	public void testMissingFiles() throws ResultProcessorException, SQLException {
		mockAndRun(false, 1);
	}

	/**
	 * Test a good run where the database already contains the tuple that need te be inserted
	 * 
	 * @throws ResultProcessorException
	 * @throws SQLException
	 */
	@Test
	public void testContainsTuple() throws ResultProcessorException, SQLException {
		makeFileStructure();

		mockAndRun(true, 1);
	}

	/**
	 * Test if a ResultProcessorException is thrown if the update of a json-file
	 * 
	 * @throws SQLException
	 * @throws ResultProcessorException
	 */
	@Test(expected = ResultProcessorException.class)
	public void testFailedInsertJson() throws SQLException, ResultProcessorException {
		makeFileStructure();

		mockAndRun(false, 0);
	}

	/**
	 * Test a SQLException in the sql-statement
	 * 
	 * @throws SQLException
	 * @throws ResultProcessorException
	 */
	@Test(expected = ResultProcessorException.class)
	public void testSQLException() throws SQLException, ResultProcessorException {
		makeFileStructure();

		// Mock objects
		ConnectionManagerImpl connMgr = mock(ConnectionManagerImpl.class);
		Connection conn = mock(Connection.class);
		PreparedStatement statement = mock(PreparedStatement.class);
		ResultSet resultset = mock(ResultSet.class);
		// Mock methods
		when(connMgr.getConnection()).thenReturn(conn);
		when(conn.prepareStatement(anyString(), anyInt())).thenThrow(new SQLException());

		when(statement.executeUpdate()).thenReturn(1);

		when(statement.getGeneratedKeys()).thenReturn(resultset);

		when(statement.executeQuery()).thenReturn(resultset);
		when(resultset.next()).thenReturn(false);
		// Method under inspection
		ResultDao upload = new ResultDao(connMgr);
		ResultProcessorImpl resProc = new ResultProcessorImpl(upload);
		resProc.uploadResults(1, new File("TestDir"), 10);
	}
}
