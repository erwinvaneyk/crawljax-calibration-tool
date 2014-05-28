package main.java.distributed.configuration;

import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ini4j.Ini;
import org.ini4j.Profile.Section;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigurationIni implements IConfigurationDAO {

	final Logger logger = LoggerFactory.getLogger(this.getClass());

	public static final String DEFAULT_SETTINGS_DIR = System.getProperty("user.dir") + "/config";
	public static final String INI_SECTION_COMMON = "common";
	private String settingsIniFile;

	private static Ini ini;
	
	
	public ConfigurationIni(String fileName) {
		this.settingsIniFile = fileName;
		try {
			ini = new Ini(new FileReader(DEFAULT_SETTINGS_DIR + this.settingsIniFile));
			if (ini.containsKey(INI_SECTION_COMMON))
				LoggerFactory.getLogger(ConfigurationIni.class).warn("Common section could not be found in INI-file");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public ConfigurationIni() {
		this.settingsIniFile = "/settings.ini";
		try {
			ini = new Ini(new FileReader(DEFAULT_SETTINGS_DIR + this.settingsIniFile));
			if (ini.containsKey(INI_SECTION_COMMON))
				LoggerFactory.getLogger(ConfigurationIni.class).warn("Common section could not be found in INI-file");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public String getSettingsFile() {
		return this.settingsIniFile;
	}
	/**
	 * Add/replace additional setting to a existing settings-build.
	 * @param args the argument-set to which the settings are added.
	 * @param section the section (ini) that needs to be added.
	 */
	private void addSettings(Map<String,String> args, String section) {
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
	public void setIni(Ini newIni) {
		if (newIni.containsKey(INI_SECTION_COMMON))
			logger.warn("Common section could not be found in INI-file");
		ini = newIni;
	}
	

	public Ini getIni() {
		return ini;
	}


	public Map<String, String> getConfiguration() {
		// Load common settings{
		Map<String,String> args = new HashMap<String,String>();
	    addSettings(args, INI_SECTION_COMMON);
		logger.debug("Common Settings retrieved.");
	    return args;
	}

	public Map<String, String> getConfiguration(List<String> websites) {
		assert websites != null;
		Map<String,String> args = new HashMap<String,String>();
		for(String section : websites) {
			addSettings(args, section);
		}
		return args;
	}
	
	/**
	 * Generates a map with settings extracted from the ini and website and outputdir-keys.
	 * If defined custom settings of the website will be added. 
	 * @param website the website for which the arguments are build.
	 * @return map with arguments for the crawling of the website.
	 * @throws URISyntaxException invalid website-url
	 * @throws MalformedURLException 
	 */
	public Map<String, String> getConfiguration(String website) {
		assert website != null;
		// Load common settings{
		Map<String, String> args = getConfiguration();
		addSettings(args, website);
	    
	    // Setup vital arguments
		logger.info("Settings build for website: " + website);
		return args;
	}

	// importance not used
	public void updateConfiguration(String section, String key, String value, int importance) {
		assert section != null;
		assert key != null;
		Section settings = ini.containsKey(section) ? ini.get(section) : ini.add(section);
		settings.put(key, value);
		try {
			ini.store();
			logger.info("Configuration updated for section: " + section + " -> " + key + "=" + value);
		} catch (IOException e) {
			logger.error("Failed to save configuration: " + e.getMessage());
		}
	}

	public void deleteConfiguration(String section, String key) {
		assert section != null;
		assert key != null;
		if (ini.containsKey(section)) {
			Section settings = ini.get(section);
			settings.remove(key);
		}
		try {
			ini.store();
			logger.info("Configuration deleted for section: " + section + " -> " + key);
		} catch (IOException e) {
			logger.error("Failed to delete configuration: " + e.getMessage());
		}
	}

	public void deleteConfiguration(String section) {
		assert section != null;
		ini.remove(section);
		try {
			ini.store();
			logger.info("Configuration deleted section: " + section);
		} catch (IOException e) {
			logger.error("Failed to delete section: " + e.getMessage());
		}
	}
	
	
	
}
