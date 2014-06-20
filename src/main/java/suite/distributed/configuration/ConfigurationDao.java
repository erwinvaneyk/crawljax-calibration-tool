package suite.distributed.configuration;

import java.util.List;
import java.util.Map;

/**
 * ConfigurationDaoImpl implementations take care of managing distributed ini-like configurations.
 */
public interface ConfigurationDao {
	
	public static final String SECTION_COMMON = "common";

	/**
	 * Get all configurations.
	 * 
	 * @return map with configurations
	 */
	public Map<String, String> getConfiguration();

	/**
	 * Get the configurations associated with the sections.
	 * 
	 * @param sections
	 * @return map with configurations of the sections.
	 */
	public Map<String, String> getConfiguration(List<String> sections);

	/**
	 * Get the configurations associated with the section
	 * 
	 * @param section
	 * @return map with configurations of the section.
	 */
	public Map<String, String> getConfiguration(String section);

	/**
	 * Update a single key/value of the section
	 * 
	 * @param section
	 *            the section/namespace in which the key-value is stored
	 * @param key
	 *            the key of the setting
	 * @param value
	 *            the value of the setting
	 * 
	 */
	public void updateConfiguration(String section, String key, String value);

	/**
	 * Delete a key from one section
	 * 
	 * @param section
	 *            sections which are scanned to delete keys
	 * @param keys
	 *            keys to be deleted
	 */
	public void deleteConfiguration(String section, String key);

	/**
	 * Delete all keys from a given section
	 * 
	 * @param section
	 *            section all keys need to be deleted.
	 */
	public void deleteConfiguration(String section);
	
	/**
	 * Sets the relative importance of a section to a new value.
	 * 
	 * @param section
	 *            The relevant section
	 * @param importance
	 *            specifies the relative importance between two conflicting settings. When in a
	 *            getConfiguration two or more conflicting (= same key) settings are retrieved, the
	 *            value is returned of the setting with the highest importance.
	 */
	public void setImportance(String section, int importance);
}
