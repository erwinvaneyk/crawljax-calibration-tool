package main.java.distributed.results;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.crawljax.core.state.duplicatedetection.NearDuplicateDetectionSingleton;

import main.java.distributed.IConnectionManager;

/**
 * ResultProcessor should deal with the results of crawls, sending them to the SQL server. 
 */
public class ResultProcessor implements IResultProcessor {
	final Logger logger = LoggerFactory.getLogger(this.getClass());

	private static final String TABLE_STATE_RESULTS = "DomResults";
	private static final String COLUMN_ID_WEBSITE = "websiteResult_id";
	private static final String COLUMN_ID_STATE = "stateId";
	private static final String COLUMN_DOM = "dom";
	private static final String COLUMN_STRIPPEDDOM = "strippedDom";
	private static final String COLUMN_STRIPPEDDOMHASH = "strippedDomHash";
	private static final String COLUMN_SCREENSHOT = "screenshot";
	
	private static final String TABLE_WEBSITE_RESULTS = "WebsiteResults";
	private static final String COLUMN_ID_WORKTASK = "workTask_id";
	private static final String COLUMN_RESULTS_JSON = "jsonResults";
	private static final String COLUMN_DURATION = "duration";
	
	private static final String PATH_RESULTS_JSON = "result.json"; 
	private static final String PATH_RESULTS_DOM = "doms";  
	private static final String PATH_RESULTS_STRIPPEDDOM = "strippedDOM"; 
	private static final String PATH_RESULTS_SCREENSHOTS = "screenshots"; 
	
	private IConnectionManager con;

	public ResultProcessor(IConnectionManager conn) {
		this.con = conn;
	}
	
	/**
	 * Upload the resulting all the results to the database.
	 * @param website The crawled website that genarates the output folder
	 * @param dir The directory that contains the output of the crawl
	 * @throws ResultProcessorException 
	 */
	public void uploadResults(int id, String dir, long duration) throws ResultProcessorException {
		int websiteID = this.uploadJson(id, dir, duration);
		this.uploadDom(websiteID, dir);
		this.uploadStrippedDom(websiteID, dir);
		this.uploadScreenshot(websiteID, dir);
				
		//this.removeDir(dir);
		
		con.closeConnection();
	}
	
	/**
	 * Upload only the result.json file to the database.
	 * @param id The id of the website
	 * @param dir The output directory
	 * @param duration The duration of the crawl
	 * @throws ResultProcessorException
	 */
	public int uploadJson(int id, String dir, long duration) throws ResultProcessorException {
		File jsonFile = this.findFile(dir, PATH_RESULTS_JSON);
		return this.uploadJson(id, jsonFile, duration);
	}
	
	/**
	 * Upload only the dom of every state to the database.
	 * @param id The id of the website
	 * @param dir The output directory
	 * @throws ResultProcessorException
	 */
	public void uploadDom(int id, String dir) throws ResultProcessorException {
		File dirOfMap = this.findFile(dir,PATH_RESULTS_DOM);
		File[] files = dirOfMap.listFiles();
		
		logger.info(files.length +" domstates found");
		for (int i = 0; i < files.length; i++) {
			this.uploadDomAction(id, files[i]);
		}
	}
	
	/**
	 * Upload only the stripped dom of every state to the database.
	 * @param id The id of the website
	 * @param dir The output directory
	 * @throws ResultProcessorException
	 */
	public void uploadStrippedDom(int id, String dir) throws ResultProcessorException {
		File dirOfMap = this.findFile(dir, PATH_RESULTS_STRIPPEDDOM);
		File[] files = dirOfMap.listFiles();
		
		logger.info(files.length +" stripped dom-states found");
		for (int i = 0; i < files.length; i++) {
			this.uploadStrippedDom(id, files[i]);
		}
	}
	
	/**
	 * Upload only the screenshot of every state to the database.
	 * @param id The id of the website
	 * @param dir The output directory
	 * @throws ResultProcessorException
	 */
	public void uploadScreenshot(int id, String dir) throws ResultProcessorException {
		File dirOfMap = this.findFile(dir, PATH_RESULTS_SCREENSHOTS);
		File[] files = dirOfMap.listFiles();
		
		logger.info(files.length +" screenshots found");
		for (int i = 0; i < files.length; i++) {
			this.uploadScreenshotAction(id, files[i]);
		}
	}
	
	private void removeDir(String dir) {
		try {
			FileUtils.deleteDirectory(new File(dir));
			logger.debug("Output directory removed.");
		} catch (IOException e) {
			logger.error("IOException while removing the output directory: " + e.getMessage());
		}
	}
	
	private File findFile(final String dir, String file) throws ResultProcessorException  {
		File directory = new File(dir);
		File[] files = directory.listFiles();

		File result = null;

		for (int i = 0; i < files.length; i++) {
			if (files[i].getName().contains(file)) {
				result = files[i];
			}
		}
		
		if (result == null) {
			throw new ResultProcessorException("The file \"" + file + "\" cannot be found in the given output directory \"" + dir + "\"");
		} else {
			return result;
		}
	}

	private int uploadJson(int id, final File f, long duration) throws ResultProcessorException {
		BufferedReader bufr = null;
		int ret = -1;
		try {
			String fileContent = "";
			String line;
			bufr = new BufferedReader(new FileReader(f));
			
			while ((line = bufr.readLine()) != null) {
				fileContent += line;
			}

			if (this.tableContainsJson(id)) {
				logger.warn("There already excist a result.json file of this website_id in the database, so this result.json will be discarded");
			} else {
				String sql = "INSERT INTO " + TABLE_WEBSITE_RESULTS + "(" + COLUMN_ID_WORKTASK 
						+ "," + COLUMN_RESULTS_JSON + "," + COLUMN_DURATION + ") VALUES(?,?,?)";
				PreparedStatement statement = con.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
				
				statement.setInt(1, id);
				statement.setString(2, fileContent);
				statement.setFloat(3, duration);
				int insert = statement.executeUpdate();	
				
				if (insert == 1) {
					logger.info("The result.json file is sent to the database");
				} else {
					logger.warn("The result.json file is NOT sent to the database");
					throw new ResultProcessorException("Can not insert the json-file");
				}
				
				// Get generated key
				ResultSet generatedkeys = statement.getGeneratedKeys();
		        if (generatedkeys.next()) {
		            ret = generatedkeys.getInt(1);
		        }
			}
		} catch (SQLException e) {
			logger.error("SQLException: " + e.getMessage());
			throw new ResultProcessorException("SQLException during the upload of the json-file");
		} catch (FileNotFoundException e) {
			logger.error("FileNotFoundException: " + e.getMessage());
			throw new ResultProcessorException("FileNotFoundException: the json-file cannot be found");
		} catch (IOException e) {
			logger.error("IOException: " + e.getMessage());
			throw new ResultProcessorException("IOException during the upload of the json-file");
		} finally {
			try {
				bufr.close();
			} catch (IOException e) {
				logger.error("IOException while closing file " + f.getName() + ". Message: " + e.getMessage());
				
			}
		}
		return ret;
	}
	
	private void uploadDomAction(int id, final File f) throws ResultProcessorException {
		try {
			String fileContent = this.readFile(f);
			String stateId = getStateId(f);

			if (!this.tableContainsTuple(id, stateId)) {
				this.makeTuple(id, stateId);
			}
			this.insertInTuple(COLUMN_DOM, fileContent, id, stateId);
			
		} catch (SQLException e) {
			logger.error("SQLException: " + e.getMessage());
			throw new ResultProcessorException("SQLException during the upload of the json-file");
		}
	}
	
	private int insertInTuple(String column, String content, int websiteId, String stateId) throws SQLException {
		String update  = "UPDATE " + TABLE_STATE_RESULTS + " SET " + column + " = ? WHERE " + COLUMN_ID_WEBSITE + " = ? AND " + COLUMN_ID_STATE +" = ?";
		PreparedStatement statement = con.getConnection().prepareStatement(update);
		
		statement.setString(1, content);
		statement.setInt(2, websiteId);
		statement.setString(3, stateId);
		
		return statement.executeUpdate();
	}
	
	private void uploadStrippedDom(int id, final File f) throws ResultProcessorException {
		try {
			String fileContent = this.readFile(f);
			String stateId = getStateId(f);
			long hash = NearDuplicateDetectionSingleton.getInstance().generateHash(fileContent);
			
			String hashBits = Integer.toBinaryString((int)hash);
			while (hashBits.length() != 32) {
				hashBits = "0" + hashBits;
			}
	
			if (!this.tableContainsTuple(id, stateId)) {
				this.makeTuple(id, stateId);
			}
			
			String update  = "UPDATE " + TABLE_STATE_RESULTS + " SET " + COLUMN_STRIPPEDDOM + "=?, " + COLUMN_STRIPPEDDOMHASH + "=? WHERE " + COLUMN_ID_WEBSITE + "=? AND " + COLUMN_ID_STATE + "=?";
			PreparedStatement statement = con.getConnection().prepareStatement(update);
			
			statement.setString(1, fileContent);
			statement.setString(2, hashBits);
			statement.setInt(3, id);
			statement.setString(4, stateId);
			
			int updateSt = statement.executeUpdate();
			
			
			
			if(updateSt != 1) {
				logger.warn("A problem while inserting a screenshot into the database.");
			}
		} catch (SQLException e) {
			logger.error("SQLException during upload of stripped-dom " + id + ". Message: " + e.getMessage());
			throw new ResultProcessorException("IOException during the upload of a stripped-dom");
		}
	}
	
	private void uploadScreenshotAction(int id, final File f) throws ResultProcessorException {
		FileInputStream fr = null;
		try {
			fr = new FileInputStream(f);
			String stateId = getStateId(f);
			
			if (!stateId.contains("small")) {
				if (!this.tableContainsTuple(id, stateId)) {
					this.makeTuple(id, stateId);
				}
				String sql = "UPDATE " + TABLE_STATE_RESULTS + " SET " + COLUMN_SCREENSHOT + " = ? WHERE " + COLUMN_ID_WEBSITE + " = ? AND " + COLUMN_ID_STATE + " = ?";
				PreparedStatement prepStat = con.getConnection().prepareStatement(sql);
				
				prepStat.setBinaryStream(1, fr);
				prepStat.setInt(2, id);
				prepStat.setString(3, stateId);
				
				int result = prepStat.executeUpdate();
				if(result != 1) {
					logger.warn("A problem while inserting a screenshot into the database.");
				}
			}
		} catch (FileNotFoundException e) {
			logger.error("IOException during upload screenshot " + id + ". Message: " + e.getMessage());
			throw new ResultProcessorException(e.getMessage());
		} catch (SQLException e) {
			logger.error("SQLException during upload screenshot " + id + ". Message: " + e.getMessage());
			throw new ResultProcessorException("IOException during the upload of a screenshot");
		} finally {
			try {
				fr.close();
			} catch (IOException e) {
				logger.error("IOException while closing file " + f.getName() + ". Message: " + e.getMessage());
			}
		}
	}
	
	private String getStateId(File f) {
		String fileName = f.getName();
		int indexOfExtension = fileName.lastIndexOf(".");
		return fileName.substring(0, indexOfExtension);
	}
	
	private boolean tableContainsTuple(int id, String stateId) throws ResultProcessorException {
		boolean res = false;
		
		try {
			String select = "SELECT * FROM " + TABLE_STATE_RESULTS + " WHERE " + COLUMN_ID_WEBSITE + " = ? AND " + COLUMN_ID_STATE +" = ?";
			PreparedStatement selectSt = con.getConnection().prepareStatement(select);
			selectSt.setInt(1, id);
			selectSt.setString(2, stateId);
			
			ResultSet resSet = selectSt.executeQuery();
			if (resSet.next()) {
				res = true;
			} else {
				res = false;
			}
		} catch (SQLException e) {
			logger.error("SQLException: It is not possible to check if the table contains a tuple with id=" + id + " and StateId=" + stateId + ". Message: " + e.getMessage());
			throw new ResultProcessorException("SQLException during the search of excisting tuple");
		}
		return res;
	}
	
	private boolean tableContainsJson(int id) {
		boolean res = false;
		
		try {
			String select = "SELECT * FROM " + TABLE_WEBSITE_RESULTS + " WHERE " + COLUMN_ID_WORKTASK + " = ?";
			PreparedStatement selectSt = con.getConnection().prepareStatement(select);
			selectSt.setInt(1, id);
			
			ResultSet resSet = selectSt.executeQuery();
			
			if (resSet.next()) {
				res = true;
			} else {
				res = false;
			}
		} catch (SQLException e) {
			logger.error("SQLException: It is not possible to check if the table contains a the Json with id=" + id + ". Message: " + e.getMessage());
		}
		
		return res;
	}
	
	private String readFile(File f) throws ResultProcessorException {
		String fileContent = "";
		try {
			BufferedReader br = new BufferedReader(new FileReader(f));;
			String line;
			
			while ((line = br.readLine()) != null) {
				fileContent += line;
			}
			br.close();
		} catch(IOException e) {
			throw new ResultProcessorException("Could not read file " + f.getName());
		}
		return fileContent;
	}
	
	private void makeTuple(int id, String stateId) throws ResultProcessorException {
		try {
			String sql = "INSERT INTO " + TABLE_STATE_RESULTS + "(" + COLUMN_ID_WEBSITE + ","+COLUMN_ID_STATE+") VALUES(?,?)";
			PreparedStatement statement = (PreparedStatement) con.getConnection().prepareStatement(sql);
			
			statement.setInt(1, id);
			statement.setString(2, stateId);
			
			int result = statement.executeUpdate();	
			if(result != 1) {
				logger.info("A problem while insterted a dom in the database");
			}
		
		} catch (SQLException e) {
			logger.error("SQLException: " + e.getMessage());
			throw new ResultProcessorException("SQLException: can not make new tupe with id=" + id + " and StateId=" + stateId);
		}
	}
}
