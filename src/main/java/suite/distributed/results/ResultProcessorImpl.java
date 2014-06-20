package suite.distributed.results;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.io.FileUtils;

import com.google.inject.Inject;

/**
 * ResultProcessorImpl should deal with the results of crawls, sending them to the SQL server.
 */
@Slf4j
public class ResultProcessorImpl implements ResultProcessor {

	private static final String PATH_RESULTS_JSON = "result.json";
	private static final String PATH_RESULTS_DOM = "doms";
	private static final String PATH_RESULTS_STRIPPEDDOM = "strippedDOM";
	private static final String PATH_RESULTS_SCREENSHOTS = "screenshots";

	private ResultDao upload;

	@Inject
	public ResultProcessorImpl(ResultDao upload) {
		this.upload = upload;
	}

	/**
	 * Upload the resulting all the results to the database.
	 * 
	 * @param website
	 *            The crawled website that genarates the output folder
	 * @param dir
	 *            The directory that contains the output of the crawl
	 * @throws ResultProcessorException
	 */
	public void uploadResults(int id, File dir, long duration) {
		int websiteID = this.uploadJson(id, dir, duration);
		this.uploadDom(websiteID, dir);
		this.uploadStrippedDom(websiteID, dir);
		this.uploadScreenshot(websiteID, dir);

		// this.removeDir(dir);

		upload.closeConnection();
	}

	/**
	 * Upload only the result.json file to the database.
	 * 
	 * @param id
	 *            The id of the website
	 * @param dir
	 *            The output directory
	 * @param duration
	 *            The duration of the crawl
	 * @throws ResultProcessorException
	 */
	public int uploadJson(int id, File dir, long duration) {
		File jsonFile = this.findFile(dir, PATH_RESULTS_JSON);
		String fileContent = this.readFile(jsonFile);
		return upload.uploadJson(id, fileContent, duration);
	}

	/**
	 * Upload only the dom of every state to the database.
	 * 
	 * @param id
	 *            The id of the website
	 * @param dir
	 *            The output directory
	 * @throws ResultProcessorException
	 */
	public void uploadDom(int websiteId, File dir) {
		File dirOfMap = this.findFile(dir, PATH_RESULTS_DOM);
		File[] files = dirOfMap.listFiles();

		log.info(files.length + " domstates found");
		for (File file : files) {
			String fileContent = this.readFile(file);
			String stateId = this.getStateId(file);

			upload.uploadDomAction(websiteId, fileContent, stateId);
		}
	}

	/**
	 * Upload only the stripped dom of every state to the database.
	 * 
	 * @param id
	 *            The id of the website
	 * @param dir
	 *            The output directory
	 * @throws ResultProcessorException
	 */
	public void uploadStrippedDom(int id, File dir) throws ResultProcessorException {
		File dirOfMap = this.findFile(dir, PATH_RESULTS_STRIPPEDDOM);
		File[] files = dirOfMap.listFiles();

		log.info(files.length + " stripped dom-states found");
		for (File file : files) {
			String fileContent = this.readFile(file);
			String stateId = this.getStateId(file);
			upload.uploadStrippedDom(id, fileContent, stateId);
		}
	}

	/**
	 * Upload only the screenshot of every state to the database.
	 * 
	 * @param id
	 *            The id of the website
	 * @param dir
	 *            The output directory
	 * @throws ResultProcessorException
	 */
	public void uploadScreenshot(int id, File dir) throws ResultProcessorException {
		File dirOfMap = this.findFile(dir, PATH_RESULTS_SCREENSHOTS);
		for (File file : dirOfMap.listFiles()) {
			String stateId = this.getStateId(file);
			try {
				FileInputStream fr = new FileInputStream(file);

				upload.uploadScreenshotAction(id, fr, stateId);
				fr.close();
			} catch (IOException e) {
				log.warn("Can not close FileInputStream by uploading state{}.", stateId);
			}
		}
	}

	@SuppressWarnings("unused")
	private void removeDir(String dir) {
		try {
			FileUtils.deleteDirectory(new File(dir));
			log.debug("Output directory removed.");
		} catch (IOException e) {
			log.error("IOException while removing the output directory: " + e.getMessage());
		}
	}

	private File findFile(File dir, String file) throws ResultProcessorException {
		File result = null;
		for (File fileOfDir : dir.listFiles()) {
			if (fileOfDir.getName().contains(file)) {
				result = fileOfDir;
			}
		}
		if (result == null) {
			throw new ResultProcessorException("The file, " + file
			        + ", cannot be found in the output directory " + dir.getAbsolutePath());
		}
		return result;
	}

	private String getStateId(File f) {
		String fileName = f.getName();
		int indexOfExtension = fileName.lastIndexOf(".");
		return fileName.substring(0, indexOfExtension);
	}

	/**
	 * Reads and returns all contents from a given file.
	 * 
	 * @param file
	 *            the relevant file
	 * @return contents of file
	 */
	private String readFile(File file) throws ResultProcessorException {
		StringBuffer fileContent = new StringBuffer();
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line;

			while ((line = br.readLine()) != null) {
				fileContent.append(line);
			}
			br.close();
		} catch (IOException e) {
			throw new ResultProcessorException("Could not read file " + file.getName());
		}
		return fileContent.toString();
	}
}
