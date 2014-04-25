package main.java;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.Queue;

import org.apache.commons.validator.routines.UrlValidator;
import org.ini4j.Ini;
import org.ini4j.Profile.Section;

import java.util.logging.*;

import com.crawljax.cli.JarRunner;

public class SuiteManager {

	final Logger logger = Logger.getLogger(SuiteManager.class.getName());

	static final String DEFAULT_SETTINGS_DIR = System.getProperty("user.dir") + "\\config";
	static final String DEFAULT_SETTINGS_INI = "/settings.ini";
	static final String DEFAULT_OUTPUT_DIR = System.getProperty("user.dir") + "\\output";
	static final String INI_SECTION_COMMON = "common";
	static final String ARG_WEBSITE = "website";
	static final String ARG_OUTPUTDIR = "outputdir";
	private Queue<String> websiteQueue = new PriorityQueue<String>();

	private Ini ini;


	public SuiteManager(String iniPath) throws IOException {
		ini = new Ini(new FileReader(iniPath));
	}

	public SuiteManager() throws IOException {
		logger.warning("Using the default paths for the config-file.");
		ini = new Ini(new FileReader(DEFAULT_SETTINGS_DIR + DEFAULT_SETTINGS_INI));
	}
	
	public HashMap<String,String> buildSettings(String website) throws URISyntaxException {
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
		args.put(ARG_WEBSITE, website);
		args.put(ARG_OUTPUTDIR, generateOutputDir(website));
		return args;
	}

	public void websitesFromFile(String websitesPath) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(websitesPath));
		UrlValidator urlValidator = new UrlValidator();
		String line;
		while ((line = br.readLine()) != null) {
			if(urlValidator.isValid(line))
				websiteQueue.add(line);
		}
		br.close();
		logger.info("Website-queue loaded.");
	}

	public ArrayList<String> crawlWebsites() {
		String website = websiteQueue.poll();
		ArrayList<String> outputdirs = new ArrayList<String>();
		while(website != null) {
			try {
					HashMap<String,String> args = buildSettings(website);
					runCrawler(args);
					outputdirs.add(args.get(DEFAULT_OUTPUT_DIR));
			} catch (URISyntaxException e) {
				logger.info("Invalid uri provided: " + website);
			};
			website = websiteQueue.poll();
		}
		return outputdirs;
	}

	public String generateOutputDir(String website) {
		Date date= new Date();
		URI uri = null;
		try {
			uri = new URI(website);
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		Timestamp timestamp = new Timestamp(date.getTime());
		return DEFAULT_OUTPUT_DIR + "/" + uri.getHost() + "-" + timestamp.getTime();
	}

	public void runCrawler(HashMap<String, String> args) {
		logger.info("Starting crawler on: " + args.get(ARG_WEBSITE) + " outputting to: " + args.get(ARG_OUTPUTDIR) + ".");
		
		// Convert args to valid args-line
		String[] finargs = new String[(args.size() * 2 - 2)];
		finargs[0] = args.remove(ARG_WEBSITE);
		finargs[1] = args.remove(ARG_OUTPUTDIR);
		int index = 2;
		
		for(String key : args.keySet()) {
			finargs[index] = "-" + key;
			finargs[index+1] = args.get(key);
 			index += 2;
		}
		logger.info("Arguments provided: " + Arrays.toString(finargs));
		JarRunner.main(finargs);
		logger.info("Finished crawling " + args.get(ARG_WEBSITE) + ".");
	}
	
	public void addSettings(HashMap<String,String> args, String section) {
		try {
			Section settings = ini.get(section);
			for(String key : settings.keySet()) {
				args.put(key, settings.get(key));
			}
			logger.info("Custom settings loaded for section: " + section);
		} catch (Exception e) {
			logger.warning("Could not find custom settings-section: " + section);
		}
	}

	public Queue<String> getWebsiteQueue() {
		return websiteQueue;
	}

	public void setWebsiteQueue(Queue<String> websiteQueue) {
		this.websiteQueue = websiteQueue;
	}
}
