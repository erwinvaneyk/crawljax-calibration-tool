package main.java;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.PriorityQueue;
import java.util.Properties;
import java.util.Queue;

import org.apache.commons.validator.routines.UrlValidator;
import java.util.logging.*;

import com.crawljax.cli.JarRunner;

public class SuiteManager {
	
	final Logger logger = Logger.getLogger(SuiteManager.class.getName());
	
	public static String DEFAULT_SETTINGS_DIR = System.getProperty("user.dir") + "\\src\\main\\config";
	public static String DEFAULT_OUTPUT_DIR = System.getProperty("user.dir") + "\\target\\output";
	private Properties settings;
	private Queue<String> websiteQueue;
	private ArrayList<String> args;
	
	public SuiteManager(String properties, String websites) throws IOException {
		setupSettings(properties);
		setupSettings(websites);
	}
	
	public SuiteManager() throws IOException {
		logger.warning("Using the default paths for the config-files.");
		setupSettings(DEFAULT_SETTINGS_DIR + "/settings.ini");
		setupWebsites(DEFAULT_SETTINGS_DIR + "/websites.txt");
	}
	
	private void setupSettings(String propertiesPath) throws IOException {
		settings = new Properties();
		FileInputStream input = new FileInputStream(propertiesPath);
		 
		// load a properties file
		settings.load(input);
		
		// Setup args
		args = new ArrayList<String>();
		Enumeration<?> e = settings.propertyNames();
	    while (e.hasMoreElements()) {
	      String key = (String) e.nextElement();
	      args.add("--" + key);
	      if (!settings.getProperty(key).equals(""))
	      	args.add(settings.getProperty(key)); 
	    }
		args.add(0, null);
		args.add(1, null);
		logger.info("Settings loaded.");
	}
	
	private void setupWebsites(String websitesPath) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(websitesPath));
		UrlValidator urlValidator = new UrlValidator();
		websiteQueue = new PriorityQueue<String>();
		String line;
		while ((line = br.readLine()) != null) {
			urlValidator.isValid(line);
			websiteQueue.add(line);
		}
		br.close();
		logger.info("Website-queue loaded.");
	}
	
	public void crawlWebsites() {
		String website = websiteQueue.poll();
		while(website != null) {
			String dir = generateOutputDir(website);
			
			runCrawler(website, dir);
			
			website = websiteQueue.poll();
		}
	}
	
	public String generateOutputDir(String website) {
		Date date= new Date();
		URI uri = null;
		try {
			uri = new URI(website);
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Timestamp timestamp = new Timestamp(date.getTime());
		return DEFAULT_OUTPUT_DIR + "/" + uri.getHost() + "-" + timestamp.getTime();
	}

	public void runCrawler(String website, String outputDir) {
		logger.info("Starting crawler on: " + website + " outputting to: " + outputDir + ".");
		args.set(0, website);
		args.set(1, outputDir);
		String[] finargs = new String[args.size()];
		finargs = (String[]) args.toArray(finargs);
		logger.info("Arguments provided: " + Arrays.toString(finargs));
		JarRunner.main(finargs);
		logger.info("Finished crawling " + website + ".");
	}
	
}
