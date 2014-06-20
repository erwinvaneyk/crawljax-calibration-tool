package suite.distributed.configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import org.ini4j.Ini;
import org.ini4j.Profile.Section;

import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Implementation of ConfigurationDao, which uses a local INI-file.to store the settings. *
 */
@Slf4j
@Singleton
public class ConfigurationIni implements ConfigurationDao {

	private static final File DEFAULT_SETTINGS_FILE = new File("/src/main/config/settings.ini");
	private File settingsIniFile;

	private static Ini ini;

	/**
	 * Provide a custom INI-file to use in the ConfigurationIni
	 * 
	 * @throws IOException
	 *             provided INI-file could not be read
	 */
	public ConfigurationIni(File absoluteFilepath) throws IOException {
		this.settingsIniFile = absoluteFilepath;
		try {
			ini = new Ini(new FileInputStream(settingsIniFile));
			if (ini.containsKey(SECTION_COMMON))
				log.warn("Common section could not be found in INI-file");
		} catch (IOException e) {
			log.error("Failed reading file: {}. Attempting to read default INI instead."
			        + absoluteFilepath);
			getDefaultIni();
		}
	}

	/**
	 * Use the default INI-file to use in the ConfigurationIni
	 * 
	 * @throws IOException
	 *             Default INI-file could not be read
	 */
	@Inject
	public ConfigurationIni() throws IOException {
		getDefaultIni();
	}
	
	/**
	 * Use the default INI-file to use in the ConfigurationIni
	 * 
	 * @throws IOException
	 *             Default INI-file could not be read
	 */
	private void getDefaultIni() throws IOException {
		this.settingsIniFile = DEFAULT_SETTINGS_FILE;
		ini =
		        new Ini(new FileInputStream(System.getProperty("user.dir")
		                + DEFAULT_SETTINGS_FILE));
		if (ini.containsKey(SECTION_COMMON))
			log.warn("Common section could not be found in INI-file");
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

	public Map<String, String> getConfiguration() {
		// Load common settings{
		Map<String, String> args = new HashMap<String, String>();
		addSettings(args, SECTION_COMMON);
		log.debug("Common Settings retrieved.");
		return args;
	}

	public Map<String, String> getConfiguration(@NonNull List<String> sections) {
		Map<String, String> args = new HashMap<String, String>();
		for (String section : sections) {
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
	public Map<String, String> getConfiguration(@NonNull String section) {
		// Load common settings{
		Map<String, String> args = getConfiguration();
		addSettings(args, section);

		// Setup vital arguments
		log.info("Settings build for website: " + section);
		return args;
	}

	/**
	 * Updates a key=value-setting in the section.
	 */
	public void updateConfiguration(@NonNull String section, @NonNull String key, String value,
	        int importance) {
		Section settings = ini.containsKey(section) ? ini.get(section) : ini.add(section);
		settings.put(key, value);
		try {
			ini.store();
			log.info("Configuration updated for section: " + section + " -> " + key + "=" + value);
		} catch (IOException e) {
			log.error("Failed to save configuration: " + e.getMessage());
		}
	}

	public void deleteConfiguration(@NonNull String section, @NonNull String key) {
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

	public void deleteConfiguration(@NonNull String section) {
		ini.remove(section);
		try {
			ini.store();
			log.info("Configuration deleted section: " + section);
		} catch (IOException e) {
			log.error("Failed to delete section: " + e.getMessage());
		}
	}

}
