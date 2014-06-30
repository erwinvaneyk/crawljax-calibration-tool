package suite.distributed.configuration;

import java.io.File;
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
	private static final int DEFAULT_MAPSIZE = 20;
	private Ini ini;

	/**
	 * Provide a custom INI-file to use in the ConfigurationIni
	 * 
	 * @throws IOException
	 *             provided INI-file could not be read
	 */
	public ConfigurationIni(File absoluteFilepath) {
		try {
			ini = new Ini(new File(System.getProperty("user.dir") + absoluteFilepath));
        } catch (IOException e) {
        	log.error("Failed to load custom settings, trying to load default settings (reason: {}).", e.getMessage());
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
	public ConfigurationIni() {
		getDefaultIni();
	}
	
	/**
	 * Use the default INI-file to use in the ConfigurationIni
	 * 
	 * @throws IOException 
	 *             Default INI-file could not be read
	 */
	private void getDefaultIni() {
		try {
			ini = new Ini(new File(System.getProperty("user.dir") + DEFAULT_SETTINGS_FILE));
        } catch (IOException e) {
        	log.error("Failed to load default settings, because {}.", e.getMessage());
        } 
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
		Section settings = ini.get(section);
		if(settings != null) {
			for (String key : settings.keySet()) {
				args.put(key, settings.get(key));
			}
			log.info("Custom settings loaded for section: " + section);
		} else {
			log.debug("Section {} seems to be empty. No settings added." + section);
		}
	}

	@Override
	public Map<String, String> getConfiguration() {
		Map<String, String> args = new HashMap<String, String>(DEFAULT_MAPSIZE);
		for(String section : ini.keySet()) {
			addSettings(args, section);
		} 
		return args;
	} 

	@Override
	public Map<String, String> getConfiguration(@NonNull List<String> sections) {
		Map<String, String> args = new HashMap<String, String>(DEFAULT_MAPSIZE);
		for (String section : sections) {
			addSettings(args, section);
		}
		return args;
	}

	@Override
	public Map<String, String> getConfiguration(@NonNull String section) {
		Map<String, String> args = new HashMap<String, String>(DEFAULT_MAPSIZE);
		addSettings(args, section);

		// Setup vital arguments
		log.info("Settings build for website: " + section);
		return args;
	}

	/**
	 * Updates a key=value-setting in the section.
	 */
	public void updateConfiguration(@NonNull String section, @NonNull String key, String value) {
		Section settings = ini.containsKey(section) ? ini.get(section) : ini.add(section);
		settings.put(key, value);
		storeConfiguration();
	}

	public void deleteConfiguration(@NonNull String section, @NonNull String key) {
		if (ini.containsKey(section)) {
			Section settings = ini.get(section);
			settings.remove(key);
		}
		storeConfiguration();
	}
 
	public void deleteConfiguration(@NonNull String section) {
		ini.remove(section);
		storeConfiguration();
	}
	
	private void storeConfiguration() {
		try {
			ini.store();
			log.info("Configuration stored in {}.", ini.getFile());
		} catch (IOException e) {
			log.error("Failed to store ini, because: " + e.getMessage());
		}
	}

	public void setImportance(String section, int importance) {
		log.warn("Method setImportance not relevant for ConfigurationIni.");
    }

	@Override
    public String toString() {
	    return "ConfigurationIni [ini=" + ini.getFile() + "]";
    }
}
