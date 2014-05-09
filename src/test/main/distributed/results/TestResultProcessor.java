package test.main.distributed.results;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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

import main.java.distributed.ConnectionManager;
import main.java.distributed.results.ResultProcessor;
import main.java.distributed.results.ResultProcessorException;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class TestResultProcessor {
	
	final Logger logger = LoggerFactory.getLogger(this.getClass());

	/**
	 * Make the directory 'TestDir' and mock the connection-objects
	 * @throws IOException
	 * @throws SQLException 
	 */
	@Before
	public void makeTestDirAndMockObjects() {
		new File("TestDir").mkdir();
		logger.debug("Test directory setup.");
	}
	
	/**
	 * Delete the whole directory 'TestDir'
	 * @throws IOException
	 */
	@After
	public void removeFileStructure() {
		try {
			FileUtils.deleteDirectory(new File("TestDir"));
			logger.debug("Test directory removed.");
		} catch (IOException e) {
			logger.error("IOException while removing the TestDir directory: " + e.getMessage());
		}
	}
	
	private void mockAndRun(boolean dbContainsTuple, int updateSucces) throws SQLException, ResultProcessorException {
		// Mock objects
		ConnectionManager connMgr = mock(ConnectionManager.class);
		Connection conn = mock(Connection.class);
		PreparedStatement statement = mock(PreparedStatement.class);
		ResultSet resultset = mock(ResultSet.class);
		// Mock methods
		when(connMgr.getConnection()).thenReturn(conn);
		when(conn.prepareStatement(anyString())).thenReturn(statement);
		when(statement.executeUpdate()).thenReturn(updateSucces);
		when(statement.executeQuery()).thenReturn(resultset);
		when(resultset.next()).thenReturn(dbContainsTuple);
		// Method under inspection
		ResultProcessor resProc = new ResultProcessor(connMgr);
		resProc.uploadResults(1, "TestDir", 10L);
	}
	
	/**
	 * Make a stub file named result.json
	 * @throws FileNotFoundException
	 * @throws UnsupportedEncodingException
	 */
	private void makeJsonStub() {
		PrintWriter json;
		try {
			json = new PrintWriter("TestDir/result.json", "UTF-8");
			json.println("This is a test");
			json.println("For the class ResultProcessor.java");
			json.close();
		} catch (FileNotFoundException e) {
			logger.error("FileNotFoundException while adding the stub Json-file to the test directory");
			System.exit(1);
		} catch (UnsupportedEncodingException e) {
			logger.error("UnsupportedEncodingException while making the the stub Json-file");
			System.exit(1);
		}
	}
	
	/**
	 * Make a the directory 'screenshot' and a stub file for a single screenshot
	 * @throws IOException
	 */
	private void makeScreenshotStub() {
		new File("TestDir/screenshots").mkdir();		
		byte imageBin[] = { 0,1,0,0 };
		
		FileOutputStream screenshot;
		try {
			screenshot = new FileOutputStream(new File("TestDir/screenshots/shot1.jpg"));
			screenshot.write(imageBin);
			screenshot.close();
		} catch (IOException e) {
			logger.error("IOException while making the screenshot stub file");
			System.exit(1);
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
			logger.error("FileNotFoundException while adding the stub " + sd + "-file to the test directory");
			System.exit(1);
		} catch (UnsupportedEncodingException e) {
			logger.error("UnsupportedEncodingException while making the the stub " + sd + "-file");
			System.exit(1);
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
	 * @throws ResultProcessorException
	 * @throws SQLException 
	 */
	@Test (expected = ResultProcessorException.class)
	public void testMissingFiles() throws ResultProcessorException, SQLException {
		mockAndRun(false, 1);
	}
	
	/**
	 * Test a good run where the database already contains the tuple that need te be inserted
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
	 * @throws SQLException
	 * @throws ResultProcessorException
	 */
	@Test (expected = ResultProcessorException.class)
	public void testFailedInsertJson() throws SQLException, ResultProcessorException {
		makeFileStructure();
		
		mockAndRun(false, 0);
	}
	
	/**
	 * Test a SQLException in the sql-statement
	 * @throws SQLException
	 * @throws ResultProcessorException
	 */
	@Test (expected = ResultProcessorException.class)
	public void testSQLException() throws SQLException, ResultProcessorException {
		makeFileStructure();
		
		// Mock objects
		ConnectionManager connMgr = mock(ConnectionManager.class);
		Connection conn = mock(Connection.class);
		PreparedStatement statement = mock(PreparedStatement.class);
		ResultSet resultset = mock(ResultSet.class);
		// Mock methods
		when(connMgr.getConnection()).thenReturn(conn);
		when(conn.prepareStatement(anyString())).thenThrow(new SQLException());
		when(statement.executeUpdate(anyString())).thenReturn(1);
		when(statement.executeQuery()).thenReturn(resultset);
		when(resultset.next()).thenReturn(false);
		// Method under inspection
		ResultProcessor resProc = new ResultProcessor(connMgr);
		resProc.uploadResults(1, "TestDir", 10L);
	}
	
	/**
	 * Test a SQLException in the sql-statement
	 * @throws SQLException
	 * @throws ResultProcessorException
	 */
	@Test (expected = ResultProcessorException.class)
	public void testiets() throws SQLException, ResultProcessorException {
		makeFileStructure();
		
		// Mock objects
		ConnectionManager connMgr = mock(ConnectionManager.class);
		Connection conn = mock(Connection.class);
		PreparedStatement statement = mock(PreparedStatement.class);
		ResultSet resultset = mock(ResultSet.class);
		// Mock methods
		when(connMgr.getConnection()).thenReturn(conn);
		when(conn.prepareStatement(anyString())).thenThrow(new SQLException());
		when(statement.executeUpdate(anyString())).thenReturn(1);
		when(statement.executeQuery()).thenReturn(resultset);
		when(resultset.next()).thenReturn(false);
		// Method under inspection
		ResultProcessor resProc = new ResultProcessor(connMgr);
		resProc.uploadResults(1, "TestDir", 10L);
	}
	
	
}
