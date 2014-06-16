package suite.distributed.configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import org.ini4j.Ini;
import org.ini4j.Profile.Section;

@Slf4j
public class ConfigurationIni implements ConfigurationDao {

	public static final String INI_SECTION_COMMON = "common";
	private static final File DEFAULT_SETTINGS_FILE = new File("src/mainconfig/settings.ini");
	private File settingsIniFile;

	private static Ini ini;

	public ConfigurationIni(File absoluteFilepath) {
		this.settingsIniFile = absoluteFilepath;
		try {
			ini = new Ini(new FileInputStream(settingsIniFile));
			if (ini.containsKey(INI_SECTION_COMMON))
				log.warn("Common section could not be found in INI-file");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public ConfigurationIni() {
		this.settingsIniFile = DEFAULT_SETTINGS_FILE;
		try {
			ini = new Ini(new FileInputStream(System.getProperty("user.dir") + DEFAULT_SETTINGS_FILE));
			if (ini.containsKey(INI_SECTION_COMMON))
				log.warn("Common section could not be found in INI-file");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public File getSettingsFile() {
		return this.settingsIniFile;
	}

	/**
	 * Add/replace additional setting to a existing settings-build.
	 * 
	 * @param args
	 *            the argument-set to which the settings are added.
	 * @param section
	 *            the section (ini) that needs to be added.
	 */
	private void addSettings(Map<String, String> args, String section) {
		try {
			Section settings = ini.get(section);
			for (String key : settings.keySet()) {
				args.put(key, settings.get(key));
			}
			log.info("Custom settings loaded for section: " + section);
		} catch (Exception e) {
			log.warn("Could not find custom settings-section: " + section);
		}
	}

	/**
	 * Set a new ini-object for setting-building. It requires a common-section to be present.
	 * 
	 * @param ini
	 *            the new ini.
	 */
	public void setIni(Ini newIni) {
		if (newIni.containsKey(INI_SECTION_COMMON))
			log.warn("Common section could not be found in INI-file");
		ini = newIni;
	}

	public Ini getIni() {
		return ini;
	}

	public Map<String, String> getConfiguration() {
		// Load common settings{
		Map<String, String> args = new HashMap<String, String>();
		addSettings(args, INI_SECTION_COMMON);
		log.debug("Common Settings retrieved.");
		return args;
	}

	public Map<String, String> getConfiguration(List<String> websites) {
		assert websites != null;
		Map<String, String> args = new HashMap<String, String>();
		for (String section : websites) {
			addSettings(args, section);
		}
		return args;
	}

	/**
	 * Generates a map with settings extracted from the ini and website and outputdir-keys. If
	 * defined custom settings of the website will be added.
	 * 
	 * @param website
	 *            the website for which the arguments are build.
	 * @return map with arguments for the crawling of the website.
	 * @throws URISyntaxException
	 *             invalid website-url
	 * @throws MalformedURLException
	 */
	public Map<String, String> getConfiguration(String website) {
		assert website != null;
		// Load common settings{
		Map<String, String> args = getConfiguration();
		addSettings(args, website);

		// Setup vital arguments
		log.info("Settings build for website: " + website);
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
			log.info("Configuration updated for section: " + section + " -> " + key + "=" + value);
		} catch (IOException e) {
			log.error("Failed to save configuration: " + e.getMessage());
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
			log.info("Configuration deleted for section: " + section + " -> " + key);
		} catch (IOException e) {
			log.error("Failed to delete configuration: " + e.getMessage());
		}
	}

	public void deleteConfiguration(String section) {
		assert section != null;
		ini.remove(section);
		try {
			ini.store();
			log.info("Configuration deleted section: " + section);
		} catch (IOException e) {
			log.error("Failed to delete section: " + e.getMessage());
		}
	}

}