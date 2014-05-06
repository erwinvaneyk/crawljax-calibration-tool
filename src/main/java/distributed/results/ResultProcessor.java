package main.java.distributed.results;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Statement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import main.java.distributed.ConnectionManager;
import main.java.distributed.IConnectionManager;

/**
 * ResultProcessor should deal with the results of crawls, sending them to the SQL server. 
 */
public class ResultProcessor implements IResultProcessor {
	final Logger logger = LoggerFactory.getLogger(this.getClass());

	private IConnectionManager con;
	private int id;

	/**
	 * Upload the resulting JSON file of a crawled wesite to the sql database.
	 * @param website The crawled website that genarates the output folder
	 * @param dir The directory that contains the output of the crawl
	 */
	public void uploadOutputJson(int id, String dir) {
		this.id = id;
		try {
			con = new ConnectionManager();
			
			File jsonFile = this.findJsonFile(dir);
			this.uploadFile(jsonFile);

			con.closeConnection();
		} catch (FileNotFoundException e) {
			logger.error("FileNotFoundException: " + e.getMessage());
		}
	}

	/**
	 * Find the JSON file in the generated output of Crawljax.
	 * @param dir The directory of the output of Crawljax
	 * @return the JSON file with the results of the crawl
	 * @throws FileNotFoundException if the file cannot be found in the given output directory dir
	 */
	private File findJsonFile(final String dir) throws FileNotFoundException {
		File directory = new File(dir);
		File[] files = directory.listFiles();

		File resultJson = null;

		for (int i = 0; i < files.length; i++) {
			if (files[i].getName().contains("result.json")) {
				resultJson = files[i];
			}
		}

		if (resultJson == null) {
			throw new FileNotFoundException("The file cannot be found in the given output directory");
		} else {
			return resultJson;
		}
	}

	/**
	 * Upload file to sql database.
	 * @param f The file which should be uploaded
	 */
	private void uploadFile(final File f) {
		try {
			String fileContent = "";
			String line;
			BufferedReader bufr = new BufferedReader(new FileReader(f));

			while ((line = bufr.readLine()) != null) {
				fileContent += line.replaceAll("\"", "'");
			}

			String insertStatement = "INSERT INTO TestResults(id,JsonResults) VALUES(" + this.id + ", \"" + fileContent + "\")";			
			Statement statement = con.getConnection().createStatement();
			statement.execute(insertStatement);	
			
			System.out.println("Result of the crawl is sent to the database.");
			bufr.close();
		} catch (SQLException e) {
			logger.error("SQLException: " + e.getMessage());
		} catch (FileNotFoundException e) {
			logger.error("FileNotFoundException: " + e.getMessage());
		} catch (IOException e) {
			logger.error("IOException: " + e.getMessage());
		}
	}
}
