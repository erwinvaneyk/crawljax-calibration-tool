package main.java.distributed;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;

import main.java.SuiteManager;

/**
 * ResultProcessor should deal with the results of crawls, sending them to the server. 
 */
public class ResultProcessor {
	private final Logger logger = Logger.getLogger(SuiteManager.class.getName());

	private ConnectionManager con;
	private String website;

	/**
	 * Upload the resulting JSON file of a crawled wesite to the sql database.
	 * @param website The crawled website that genarates the output folder
	 * @param dir The directory that contains the output of the crawl
	 */
	public void uploadOutputJson(String website, String dir) {
		this.website = website;
		this.enableConnection();
		File jsonFile = this.findJsonFile(dir);
		this.uploadFile(jsonFile);
		this.closeConnection();
	}

	/**
	 * Get a connection with the server.
	 */
	private void enableConnection() {
		try {
			con = new ConnectionManager();
		} catch (IOException e) {
			logger.warning("IOException: " + e.getMessage());
		}
	}

	/**
	 * Find the JSON file in the generated output of Crawljax.
	 * @param dir The directory of the output of Crawljax
	 * @return the JSON file with the results of the crawl
	 */
	private File findJsonFile(final String dir) {
		File directory = new File(dir);
		File[] files = directory.listFiles();

		File resultJson = null;

		for (int i = 0; i < files.length; i++) {
			if (files[i].getName().contains("result.json")) {
				resultJson = files[i];
			}
		}

		// Assert resultJson != null
		return resultJson;
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

			while( (line = bufr.readLine()) != null) {
				fileContent += line.replaceAll("\"", "'");
			}

			String insertStatement = "INSERT INTO TestResults VALUES(\"" + this.website + "\", \"" + fileContent + "\")";			
			Statement statement = con.getConnection().createStatement();
			statement.execute(insertStatement);	

			bufr.close();
		} catch (SQLException e) {
			logger.warning("SQLException: " + e.getMessage());
		} catch (FileNotFoundException e) {
			logger.warning("FileNotFoundException: " + e.getMessage());
		} catch (IOException e) {
			logger.warning("IOException: " + e.getMessage());
		}
	}

	/**
	 * Close the connection with the database.
	 */
	private void closeConnection() {
		con.closeConnection();
	}
}
