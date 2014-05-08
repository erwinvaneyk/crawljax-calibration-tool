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
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import main.java.distributed.ConnectionManager;
import main.java.distributed.results.IResultProcessor;
import main.java.distributed.results.ResultProcessor;
import main.java.distributed.results.ResultProcessorException;
import main.java.distributed.workload.WorkTask;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
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
		} catch (IOException e) {
			logger.error("IOException while removing the TestDir directory: " + e.getMessage());
		}
		logger.debug("Test directory removed.");
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
	
	/**
	 * Test a good run
	 * @throws ResultProcessorException
	 */
	@Test
	public void testUploadAction() throws ResultProcessorException {
		// Make the correct file structure
		makeJsonStub();
		makeScreenshotStub();
		
		IResultProcessor resProc = mock(ResultProcessor.class);
		resProc.uploadAction(1, System.getProperty("user.dir") + "/TestDir");
	}
	
	/**
	 * Test a run with missing Json-file
	 * @throws ResultProcessorException
	 */
	@Test (expected = ResultProcessorException.class)
	public void testMissingJsonFile() throws ResultProcessorException {
		makeScreenshotStub();
		// Mock objects
		ConnectionManager connMgr = mock(ConnectionManager.class);
		// Method under inspection
		ResultProcessor resProc = new ResultProcessor(connMgr);
		resProc.uploadAction(1, "TestDir");
	}
	
	/**
	 * Test a run with missing Screenshot directory
	 * @throws ResultProcessorException
	 * @throws SQLException 
	 */
	@Test (expected = ResultProcessorException.class)
	public void testMissingScreenshotDirectory() throws ResultProcessorException, SQLException {
		makeJsonStub();
		// Mock objects
		ConnectionManager connMgr = mock(ConnectionManager.class);
		Connection conn = mock(Connection.class);
		PreparedStatement statement = mock(PreparedStatement.class);
		// Mock methods
		when(connMgr.getConnection()).thenReturn(conn);
		when(conn.prepareStatement(anyString())).thenReturn(statement);
		when(statement.executeUpdate(anyString())).thenReturn(1);
		// Method under inspection
		ResultProcessor resProc = new ResultProcessor(connMgr);
		resProc.uploadAction(1, "TestDir");
	}
}
