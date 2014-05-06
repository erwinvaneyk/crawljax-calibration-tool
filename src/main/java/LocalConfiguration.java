package main.java;

import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.ini4j.Ini;
import org.ini4j.Profile.Section;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LocalConfiguration {

	final Logger logger = LoggerFactory.getLogger(this.getClass());

	static final String DEFAULT_SETTINGS_DIR = System.getProperty("user.dir") + "\\config";
	static final String DEFAULT_SETTINGS_INI = "/settings.ini";
	static final String DEFAULT_OUTPUT_DIR = System.getProperty("user.dir") + "\\output";
	static final String INI_SECTION_COMMON = "common";
	static final String ARG_WEBSITE = "website";
	static final String ARG_OUTPUTDIR = "outputdir";
	

	private Ini ini;

	/**
	 * Constructor for providing a non-default path to an ini.
	 * @param iniPath path to a ini-file.
	 * @throws IOException ini-file could not be found/accessed.
	 */
	public LocalConfiguration(String iniPath) throws IOException {
		ini = new Ini(new FileReader(iniPath));
	}

	/**
	 * Constructor for using the default ini.
	 * @throws IOException the default ini could not be found.
	 */
	public LocalConfiguration() throws IOException {
		logger.warn("Using the default paths for the config-file.");
		ini = new Ini(new FileReader(DEFAULT_SETTINGS_DIR + DEFAULT_SETTINGS_INI));
	}
	
	
	/**
	 * Generates a map with settings extracted from the ini and website and outputdir-keys.
	 * If defined custom settings of the website will be added. 
	 * @param website the website for which the arguments are build.
	 * @return map with arguments for the crawling of the website.
	 * @throws URISyntaxException invalid website-url
	 * @throws MalformedURLException 
	 */
	public Map<String,String> buildSettings(String website) throws URISyntaxException, MalformedURLException {
		// Check URI
		URL uri = new URL(website);

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
	 * Set a new ini-object for setting-building. It requires a common-section to be present.
	 * @param ini the new ini.
	 */
	public void setIni(Ini ini) {
		assert ini.get(INI_SECTION_COMMON) != null;
		this.ini = ini;
	}
	
}
