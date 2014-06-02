package main.java.distributed.results;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.crawljax.core.state.duplicatedetection.FeatureException;
import com.crawljax.core.state.duplicatedetection.NearDuplicateDetectionSingleton;

/**
 * ResultProcessor should deal with the results of crawls, sending them to the SQL server. 
 */
public class ResultProcessor implements IResultProcessor {
	private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
	
	private static final String PATH_RESULTS_JSON = "result.json"; 
	private static final String PATH_RESULTS_DOM = "doms";  
	private static final String PATH_RESULTS_STRIPPEDDOM = "strippedDOM"; 
	private static final String PATH_RESULTS_SCREENSHOTS = "screenshots"; 
	
	private UploadResult upload;
	
	public ResultProcessor(UploadResult upload) {
		this.upload = upload;
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
		
		upload.closeConnection();
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
		String fileContent = this.readFile(jsonFile);
		return upload.uploadJson(id, fileContent, duration);
	}
	
	/**
	 * Upload only the dom of every state to the database.
	 * @param id The id of the website
	 * @param dir The output directory
	 * @throws ResultProcessorException
	 */
	public void uploadDom(int websiteId, String dir) throws ResultProcessorException {
		File dirOfMap = this.findFile(dir,PATH_RESULTS_DOM);
		File[] files = dirOfMap.listFiles();
		
		LOGGER.info(files.length +" domstates found");
		for (File file : files) {
			String fileContent = this.readFile(file);
			String stateId = this.getStateId(file);

			upload.uploadDomAction(websiteId, fileContent, stateId);
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
		
		LOGGER.info(files.length +" stripped dom-states found");
		for (File file : files) {
			String fileContent = this.readFile(file);
			String stateId = this.getStateId(file);
			int hashStrippedDom = this.makeHash(fileContent);
			upload.uploadStrippedDom(id, fileContent, stateId, hashStrippedDom);
		}
	}
	
	private int makeHash(String fileContent) {
		int hash;
		try {
			hash = NearDuplicateDetectionSingleton.getInstance().generateHash(fileContent)[0];
		} catch (FeatureException e) {
			hash = fileContent.hashCode();
			LOGGER.error(e.getMessage());
			e.printStackTrace();
		}
		return hash;
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
		
		LOGGER.info(files.length +" screenshots found");
		for (File file : files) {
			String stateId = this.getStateId(file);
			try {
				FileInputStream fr = new FileInputStream(file);
				
				upload.uploadScreenshotAction(id, fr, stateId);
				fr.close();
			} catch (FileNotFoundException e) {
				LOGGER.error("Screenshot of state{} cannot be uploaded to the database.", stateId);
				e.printStackTrace();
			} catch (IOException e) {
				LOGGER.warn("Can not close FileInputStream by uploading state{}.", stateId);
			}
		}
	}
	
	private void removeDir(String dir) {
		try {
			FileUtils.deleteDirectory(new File(dir));
			LOGGER.debug("Output directory removed.");
		} catch (IOException e) {
			LOGGER.error("IOException while removing the output directory: " + e.getMessage());
		}
	}
	
	public File findFile(final String dir, String file) throws ResultProcessorException  {
		File directory = new File(dir);
		File[] files = directory.listFiles();

		File result = null;

		for (File fileOfDir : files) {
			if (fileOfDir.getName().contains(file)) {
				result = fileOfDir;
			}
		}
		
		if (result == null) {
			throw new ResultProcessorException("The file \"" + file + "\" cannot be found in the given output directory \"" + dir + "\"");
		} else {
			return result;
		}
	}

	public String getStateId(File f) {
		String fileName = f.getName();
		int indexOfExtension = fileName.lastIndexOf(".");
		return fileName.substring(0, indexOfExtension);
	}
	
	public String readFile(File f) throws ResultProcessorException {
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
}
