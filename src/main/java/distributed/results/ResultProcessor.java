package main.java.distributed.results;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mysql.jdbc.PreparedStatement;

import main.java.distributed.ConnectionManager;
import main.java.distributed.IConnectionManager;

/**
 * ResultProcessor should deal with the results of crawls, sending them to the SQL server. 
 */
public class ResultProcessor implements IResultProcessor {
	final Logger logger = LoggerFactory.getLogger(this.getClass());

	private IConnectionManager con;

	/**
	 * Upload the resulting JSON file of a crawled wesite to the sql database.
	 * @param website The crawled website that genarates the output folder
	 * @param dir The directory that contains the output of the crawl
	 * @throws ResultProcessorException 
	 */
	public void uploadAction(int id, String dir) throws ResultProcessorException {

		con = new ConnectionManager();
		
		File jsonFile = this.findFile(dir, "result.json");
		System.out.println(jsonFile.getName());
		this.uploadJson(id, jsonFile);
		
		File screenshots = this.findFile(dir, "screenshots");
		File[] screenshot = screenshots.listFiles();
		for (int i = 0; i < screenshot.length; i++) {
			this.uploadScreenshot(id, screenshot[i]);
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
		try {
			String fileContent = "";
			String line;
			BufferedReader bufr = new BufferedReader(new FileReader(f));

			while ((line = bufr.readLine()) != null) {
				fileContent += line.replaceAll("\"", "'");
			}

			String sql = "INSERT INTO TestResults(id,JsonResults) VALUES(?,?)";
			PreparedStatement statement = (PreparedStatement) con.getConnection().prepareStatement(sql);
			
			statement.setInt(1, id);
			statement.setString(2, fileContent);
			statement.executeUpdate();	
			
			System.out.println("Result of the crawl is sent to the database.");
			bufr.close();
		} catch (SQLException e) {
			logger.error("SQLException: " + e.getMessage());
			throw new ResultProcessorException("SQLException during the upload of the json-file");
		} catch (FileNotFoundException e) {
			logger.error("FileNotFoundException: " + e.getMessage());
			throw new ResultProcessorException(e.getMessage());
		} catch (IOException e) {
			logger.error("IOException: " + e.getMessage());
			throw new ResultProcessorException("IOException during the upload of the json-file");
		}
	}
	
	private void uploadScreenshot(int id, final File f) throws ResultProcessorException {
		try {
			FileInputStream fr = new FileInputStream(f);
			
			String sql = "INSERT INTO screenshots(id, screenshot) VALUES(?,?)";
			PreparedStatement prepStat = (PreparedStatement) con.getConnection().prepareStatement(sql);
			
			prepStat.setInt(1, id);
			prepStat.setBinaryStream(2, fr);
			
			int result = prepStat.executeUpdate();
			if(result == 1) {
				logger.info("A screenshot is inserted into the database.");
			} else {
				logger.warn("A problem while inserting a screenshot into the database.");
			}
		} catch (FileNotFoundException e) {
			logger.error("IOException during upload screenshot " + id + ". Message: " + e.getMessage());
			throw new ResultProcessorException(e.getMessage());
		} catch (SQLException e) {
			logger.error("SQLException during upload screenshot " + id + ". Message: " + e.getMessage());
			throw new ResultProcessorException("IOException during the upload of a screenshot");
		}
		
		
	}
}
