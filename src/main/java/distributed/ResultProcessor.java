package main.java.distributed;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.ResultSet;
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
			logger.warning("FileNotFoundException: " + e.getMessage());
		} catch (IOException e) {
			logger.warning("IOException: " + e.getMessage());
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
			logger.warning("SQLException: " + e.getMessage());
		} catch (FileNotFoundException e) {
			logger.warning("FileNotFoundException: " + e.getMessage());
		} catch (IOException e) {
			logger.warning("IOException: " + e.getMessage());
		}
	}
}
