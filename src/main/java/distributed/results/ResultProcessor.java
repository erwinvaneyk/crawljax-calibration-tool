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

import com.crawljax.core.state.duplicatedetection.DuplicateDetectionModule;
import com.crawljax.core.state.duplicatedetection.FeatureException;
import com.crawljax.core.state.duplicatedetection.NearDuplicateDetection;
import com.google.inject.Guice;

/**
 * ResultProcessor should deal with the results of crawls, sending them to the SQL server. 
 */
public class ResultProcessor implements IResultProcessor {
	final Logger log = LoggerFactory.getLogger(this.getClass());
	
	private static final String PATH_RESULTS_JSON = "result.json"; 
	private static final String PATH_RESULTS_DOM = "doms";  
	private static final String PATH_RESULTS_STRIPPEDDOM = "strippedDOM"; 
	private static final String PATH_RESULTS_SCREENSHOTS = "screenshots"; 
	
	private UploadResult upload;

	private NearDuplicateDetection hasher;
	
	public ResultProcessor(UploadResult upload) {
		this.upload = upload;
		this.hasher =  Guice.createInjector(new DuplicateDetectionModule()).getInstance(
				NearDuplicateDetection.class);
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
		
		log.info(files.length +" domstates found");
		for (int i = 0; i < files.length; i++) {
			String fileContent = this.readFile(files[i]);
			String stateId = this.getStateId(files[i]);

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
		
		log.info(files.length +" stripped dom-states found");
		for (int i = 0; i < files.length; i++) {
			String fileContent = this.readFile(files[i]);
			String stateId = this.getStateId(files[i]);
			int hashStrippedDom = this.makeHash(fileContent);
			upload.uploadStrippedDom(id, fileContent, stateId, hashStrippedDom);
		}
	}
	
	private int makeHash(String fileContent) {
		int hash;
		try {
			hash = hasher.generateHash(fileContent)[0];
		} catch (FeatureException e) {
			hash = fileContent.hashCode();
			log.error("Failed to generate hash, because: " + e.getMessage());
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
		
		log.info(files.length +" screenshots found");
		for (int i = 0; i < files.length; i++) {
			String stateId = this.getStateId(files[i]);
			try {
				FileInputStream fr = new FileInputStream(files[i]);
				
				upload.uploadScreenshotAction(id, fr, stateId);
				fr.close();
			} catch (FileNotFoundException e) {
				log.error("Screenshot of state{} cannot be uploaded to the database.", stateId);
				e.printStackTrace();
			} catch (IOException e) {
				log.warn("Can not close FileInputStream by uploading state{}.", stateId);
			}
		}
	}
	
	private void removeDir(String dir) {
		try {
			FileUtils.deleteDirectory(new File(dir));
			log.debug("Output directory removed.");
		} catch (IOException e) {
			log.error("IOException while removing the output directory: " + e.getMessage());
		}
	}
	
	public File findFile(final String dir, String file) throws ResultProcessorException  {
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
