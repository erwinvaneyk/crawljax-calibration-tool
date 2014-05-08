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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import main.java.distributed.IConnectionManager;

/**
 * ResultProcessor should deal with the results of crawls, sending them to the SQL server. 
 */
public class ResultProcessor implements IResultProcessor {
	final Logger logger = LoggerFactory.getLogger(this.getClass());

	private IConnectionManager con;

	public ResultProcessor(IConnectionManager conn) {
		this.con = conn;
	}
	
	/**
	 * Upload the resulting JSON file of a crawled wesite to the sql database.
	 * @param website The crawled website that genarates the output folder
	 * @param dir The directory that contains the output of the crawl
	 * @throws ResultProcessorException 
	 */
	public void uploadAction(int id, String dir) throws ResultProcessorException {

		// Json
		File jsonFile = this.findFile(dir, "result.json");
		this.uploadJson(id, jsonFile);
		
		
		// DOM
		File doms = this.findFile(dir, "doms");
		File[] domState = doms.listFiles();
		
		logger.info(domState.length +" domstates found");
		for (int i = 0; i < domState.length; i++) {
			this.uploadDom(id, domState[i]);
		}
		
		// Screenshots
		File screenshots = this.findFile(dir, "screenshots");
		File[] screenshot = screenshots.listFiles();
		
		for (int i = 0; i < screenshot.length; i++) {
			this.uploadScreenshot(id, screenshot[i]);
		}
		
		// strippedDOM
		File strippesDoms = this.findFile(dir, "strippedDOM");
		File[] strippedDomState = strippesDoms.listFiles();
		
		logger.info(strippedDomState.length +" strippedDom-states found");
		for (int i = 0; i < strippedDomState.length; i++) {
			this.uploadStrippedDom(id, strippedDomState[i]);
		}

		con.closeConnection();
	}

	/**
	 * Find the JSON file in the generated output of Crawljax.
	 * @param dir The directory of the output of Crawljax
	 * @return the JSON file with the results of the crawl
	 * @throws ResultProcessorException 
	 */
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

	/**
	 * Upload file to sql database.
	 * @param f The file which should be uploaded
	 * @throws ResultProcessorException 
	 */
	private void uploadJson(int id, final File f) throws ResultProcessorException {
		BufferedReader bufr = null;
		try {
			String fileContent = "";
			String line;
			bufr = new BufferedReader(new FileReader(f));
			
			while ((line = bufr.readLine()) != null) {
				fileContent += line.replaceAll("\"", "'");
			}

			String select = "SELECT * FROM TestResults WHERE id = ?";
			PreparedStatement selectSt = con.getConnection().prepareStatement(select);
			selectSt.setInt(1, id);
			
			ResultSet res = selectSt.executeQuery();
			
			if (res.next()) {
				logger.warn("There already excist a result.json file of this website_id in the database, so this result.json will be discarded");
			} else {
				String sql = "INSERT INTO TestResults(id,JsonResults) VALUES(?,?)";
				PreparedStatement statement = con.getConnection().prepareStatement(sql);
				
				statement.setInt(1, id);
				statement.setString(2, fileContent);
				statement.executeUpdate();	
				
				System.out.println("Result of the crawl is sent to the database.");
			}
		} catch (SQLException e) {
			logger.error("SQLException: " + e.getMessage());
			throw new ResultProcessorException("SQLException during the upload of the json-file");
		} catch (FileNotFoundException e) {
			logger.error("FileNotFoundException: " + e.getMessage());
			throw new ResultProcessorException(e.getMessage());
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
	}
	
	private void uploadScreenshot(int id, final File f) throws ResultProcessorException {
		FileInputStream fr = null;
		try {
			fr = new FileInputStream(f);
			String stateId = getStateId(f);
			
			if (!stateId.contains("small")) {
				String sql = "UPDATE DomResults SET Screenshot = ? WHERE WebsiteId = ? AND StateId = ?";
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
	
	private void uploadStrippedDom(int id, final File f) throws ResultProcessorException {
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(f));
			String line;
			String fileContent = "";
			while ((line = br.readLine()) != null) {
				fileContent += line; //.replaceAll("\"", "'")
			}
			
			String stateId = getStateId(f);
			
			if (!stateId.contains("small")) {
				String sql = "UPDATE DomResults SET StrippedDom = ? WHERE WebsiteId = ? AND StateId = ?";
				PreparedStatement prepStat = con.getConnection().prepareStatement(sql);
				
				prepStat.setString(1, fileContent);
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
		} catch (IOException e) {
			logger.error("IOException during upload screenshot " + id + ". Message: " + e.getMessage());
		} finally {
			try {
				br.close();
			} catch (IOException e) {
				logger.error("IOException while closing file " + f.getName() + ". Message: " + e.getMessage());
			}
		}
	}
	
	private void uploadDom(int id, final File f) throws ResultProcessorException {
		BufferedReader bufr = null;
		try {
			String fileContent = "";
			String line;
			bufr = new BufferedReader(new FileReader(f));

			while ((line = bufr.readLine()) != null) {
				fileContent += line.replaceAll("\"", "'");
			}
			
			String stateId = getStateId(f);

			String select = "SELECT * FROM DomResults WHERE WebsiteId = ? AND StateId = ?";
			PreparedStatement selectSt = con.getConnection().prepareStatement(select);
			selectSt.setInt(1, id);
			selectSt.setString(2, stateId);
			
			ResultSet res = selectSt.executeQuery();
			
			if (res.next()) {
				logger.warn("There already excist a dom for this state in the database");
			} else {
				String sql = "INSERT INTO DomResults(WebsiteId,StateId,Dom,StrippedDom) VALUES(?,?,?,?)";
				PreparedStatement statement = (PreparedStatement) con.getConnection().prepareStatement(sql);
				
				statement.setInt(1, id);
				statement.setString(2, stateId);
				statement.setString(3, fileContent);
				statement.setString(4, "");
				
				int result = statement.executeUpdate();	
				if(result != 1) {
					logger.info("A problem while insterted a dom in the database");
				}
			}
		} catch (SQLException e) {
			logger.error("SQLException: " + e.getMessage());
			throw new ResultProcessorException("SQLException during the upload of the json-file");
		} catch (FileNotFoundException e) {
			logger.error("FileNotFoundException: " + e.getMessage());
			throw new ResultProcessorException(e.getMessage());
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
	}
	
	private String getStateId(File f) {
		String fileName = f.getName();
		int indexOfExtension = fileName.lastIndexOf(".");
		return fileName.substring(0, indexOfExtension);
	}
}
