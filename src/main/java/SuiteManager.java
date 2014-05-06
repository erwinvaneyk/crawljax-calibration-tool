package main.java;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;

import org.apache.commons.validator.routines.UrlValidator;
import org.ini4j.Ini;
import org.ini4j.Profile.Section;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.crawljax.cli.JarRunner;

/**
 * SuiteManager is responsible for running the actual crawler. Therefore it 
 * deals with the arguments needed by the CrawlJax CLI and websites to be crawled.
 */
public class SuiteManager {

	final Logger logger = LoggerFactory.getLogger(this.getClass());

	static final String DEFAULT_SETTINGS_DIR = System.getProperty("user.dir") + "\\config";
	static final String DEFAULT_SETTINGS_INI = "/settings.ini";
	static final String DEFAULT_OUTPUT_DIR = System.getProperty("user.dir") + "\\output";
	static final String INI_SECTION_COMMON = "common";
	static final String ARG_WEBSITE = "website";
	static final String ARG_OUTPUTDIR = "outputdir";
	
	private Queue<String> websiteQueue = new PriorityQueue<String>();

	private Ini ini;

	/**
	 * Constructor for providing a non-default path to an ini.
	 * @param iniPath path to a ini-file.
	 * @throws IOException ini-file could not be found/accessed.
	 */
	public SuiteManager(String iniPath) throws IOException {
		ini = new Ini(new FileReader(iniPath));
	}

	/**
	 * Constructor for using the default ini.
	 * @throws IOException the default ini could not be found.
	 */
	public SuiteManager() throws IOException {
		logger.warn("Using the default paths for the config-file.");
		ini = new Ini(new FileReader(DEFAULT_SETTINGS_DIR + DEFAULT_SETTINGS_INI));
	}
	
	/**
	 * Generates a map with settings extracted from the ini and website and outputdir-keys.
	 * If defined custom settings of the website will be added. 
	 * @param website the website for which the arguments are build.
	 * @return map with arguments for the crawling of the website.
	 * @throws URISyntaxException invalid website-url
	 */
	public Map<String,String> buildSettings(String website) throws URISyntaxException {
		// Check URI
		URI uri = new URI(website);

		// Load common settings
		HashMap<String,String> args = new HashMap<String,String>();
		Section settings = ini.get(INI_SECTION_COMMON);
	    for(String key : settings.keySet()) {
	    	args.put(key, settings.get(key));
	    }
	    // Load custom settings
	    addSettings(args, uri.getHost());
	    
	    // Setup vital arguments
		logger.info("Settings build for website: " + website);
		return args;
	}

	/**
	 * Reads a file containing website (1 website per line) and adds them to the queue.
	 * @param websitesPath the path to the file containing websites.
	 * @throws IOException the file could not be found.
	 */
	public void websitesFromFile(String websitesPath) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(websitesPath));
		UrlValidator urlValidator = new UrlValidator();
		String line;
		while ((line = br.readLine()) != null) {
			if(urlValidator.isValid(line))
				websiteQueue.add(line);
			else
				logger.warn("Website: " + line + " is an invalid url. Ignoring website.");
		}
		br.close();
		logger.info("Website-queue loaded.");
	}

	/**
	 * Crawl all websites in the queue.
	 * @return the outputdirs of the crawled websites
	 */
	public List<String> crawlWebsites() {
		String website = websiteQueue.poll();
		List<String> outputdirs = new ArrayList<String>();
		while(website != null) {
			try {
					Map<String,String> args = buildSettings(website);
					String outputDir = generateOutputDir(website);
					runCrawler(website, outputDir, args);
					outputdirs.add(outputDir);
			} catch (MalformedURLException | URISyntaxException e) {
				logger.warn("Invalid uri provided: " + website);
			}
			website = websiteQueue.poll();
		}
		return outputdirs;
	}

	/**
	 * Generate a unique outputdir-name for a website
	 * @param website the website needing a outputdir
	 * @return unique name for the dir
	 * @throws URISyntaxException website contains an invalid syntax
	 * @throws MalformedURLException 
	 */
	public String generateOutputDir(String website) throws MalformedURLException {
		Date date= new Date();
		URL uri = new URL(website);
		Timestamp timestamp = new Timestamp(date.getTime());
		return DEFAULT_OUTPUT_DIR + "/" + uri.getHost() + "-" + timestamp.getTime();
	}
	
	/**
	 * Run CrawlJax for a given set of args. Output can be found in args.get(ARG_OUTPUTDIR).
	 * @param args arguments which need to be send to crawljax.
	 */
	public void runCrawler(String website, String outputdir, Map<String, String> args) {		
		// Convert args to valid args-line
		String[] finargs = new String[(args.size() * 2 + 2)];
		finargs[0] = website;
		finargs[1] = outputdir;
		int index = 2;
		for(String key : args.keySet()) {
			finargs[index] = "-" + key;
			finargs[index+1] = args.get(key);
 			index += 2;
		}
		args.put(ARG_WEBSITE, finargs[0]);
		args.put(ARG_OUTPUTDIR, finargs[1]);
		
		logger.debug("Arguments provided: " + Arrays.toString(finargs));
		JarRunner.main(finargs);
		logger.debug("Finished crawling " + args.get(ARG_WEBSITE) + ".");
	}
	
	/**
	 * Add/replace additional setting to a existing settings-build.
	 * @param args the argument-set to which the settings are added.
	 * @param section the section (ini) that needs to be added.
	 */
	public void addSettings(Map<String,String> args, String section) {
		try {
			Section settings = ini.get(section);
			for(String key : settings.keySet()) {
				args.put(key, settings.get(key));
			}
			logger.info("Custom settings loaded for section: " + section);
		} catch (Exception e) {
			logger.warn("Could not find custom settings-section: " + section);
		}
	}

	/**
	 * Get website-queue.
	 * @return website-queue.
	 */
	public Queue<String> getWebsiteQueue() {
		return websiteQueue;
	}

	/**
	 * Set Website-queue
	 * @param websiteQueue the new website-queue.
	 */
	public void setWebsiteQueue(Queue<String> websiteQueue) {
		this.websiteQueue = websiteQueue;
	}

	/**
	 * Set a new ini-object for setting-building. It requires a common-section to be present.
	 * @param ini the new ini.
	 */
	public void setIni(Ini ini) {
		assert ini.get(INI_SECTION_COMMON) != null;
		this.ini = ini;
	}
	
	
}
