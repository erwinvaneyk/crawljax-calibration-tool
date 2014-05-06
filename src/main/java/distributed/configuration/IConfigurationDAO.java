package main.java.distributed.configuration;

import java.util.List;
import java.util.Map;

/**
 * ConfigurationDAO implementations take care of managing distributed ini-like configurations.
 *
 */
public interface IConfigurationDAO {
	
	/**
	 * Get all configurations.
	 * @return map with configurations
	 */
	public Map<String,String> getConfiguration();
	
	/**
	 * Get the configurations associated with the sections.
	 * @param sections
	 * @return map with configurations of the sections.
	 */
	public Map<String,String> getConfiguration(List<String> sections);
	
	/**
	 * Get the configurations associated with the section 
	 * @param section
	 * @return map with configurations of the section.
	 */
	public Map<String,String> getConfiguration(String section);
	
	/**
	 * Update a single key/value of the section
	 * @param section
	 * @param key
	 * @param value 
	 * @param importance
	 */
	public void updateConfiguration(String section, String key, String value, int importance);

	
	/**
	 * Delete a key from one section
	 * @param section sections which are scanned to delete keys 
	 * @param keys keys to be deleted
	 */
	public void deleteConfiguration(String section, String key);

	/**
	 * Delete all keys from a given section
	 * @param section section all keys need to be deleted.
	 */
	public void deleteConfiguration(String section);
}
