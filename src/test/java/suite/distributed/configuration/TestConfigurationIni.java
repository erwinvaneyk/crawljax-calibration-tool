package suite.distributed.configuration;

import static org.junit.Assert.*;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestConfigurationIni {
	private static File file = new File("/testConfiguration.ini");
 
	@BeforeClass 
	public static void setUpBeforeClass() throws Exception {
		PrintWriter writer = new PrintWriter(new File(System.getProperty("user.dir")  + file), "UTF-8");
		writer.println("[section1]");
		writer.println("key1=value1");
		writer.println("[section2]");
		writer.println("key2=value2");
		writer.println("[section3]");
		writer.println("key3=value3");
		writer.close();
		new File(System.getProperty("user.dir")  + file).deleteOnExit();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		new File(System.getProperty("user.dir")  + file).delete();
	}

	@Test
	public void testConfigurationIniFile() { 
		ConfigurationDao config = new ConfigurationIni(file);
		config.setImportance("doesnotmatter", 42);
		assertNotNull(config.toString());
	}
	
	@Test
	public void testConfigurationIniFileNull() {
		ConfigurationDao defaultConfig = new ConfigurationIni();
		ConfigurationDao config = new ConfigurationIni(null);
		assertNotNull(config.toString(), defaultConfig.toString());
	}

	@Test
	public void testGetConfiguration() {
		ConfigurationDao config = new ConfigurationIni(file);
		Map<String, String> settings = config.getConfiguration();
		assertEquals(settings.get("key1"),"value1");
		assertEquals(settings.get("key2"),"value2");
		assertEquals(settings.get("key3"),"value3");
	}

	@Test
	public void testGetConfigurationListOfString() {
		ConfigurationDao config = new ConfigurationIni(file);
		List<String> sections = new ArrayList<>(2);
		sections.add("section2");
		sections.add("section3");
		Map<String, String> settings = config.getConfiguration(sections);
		assertEquals(settings.size(), 2);
		assertEquals(settings.get("key3"),"value3");
		assertEquals(settings.get("key2"),"value2");
	}

	@Test
	public void testGetConfigurationString() {
		ConfigurationDao config = new ConfigurationIni(file);
		Map<String, String> settings = config.getConfiguration("section3");
		assertEquals(settings.size(), 1);
		assertEquals(settings.get("key3"),"value3");
	}
   
	@Test
	public void testUpdateConfigurationNewSection() {
		ConfigurationDao config = new ConfigurationIni(file);
		config.updateConfiguration("section4", "key4", "value4");
		Map<String, String> settings = config.getConfiguration("section4");
		assertEquals(settings.size(), 1);
		assertEquals(settings.get("key4"), "value4");
		config.deleteConfiguration("section4");
	}


	@Test
	public void testUpdateConfigurationUpdateKeyValue() {
		ConfigurationDao config = new ConfigurationIni(file);
		config.updateConfiguration("section3", "key3", "42");
		Map<String, String> settings = config.getConfiguration("section3");
		assertEquals(settings.size(), 1);
		assertEquals(settings.get("key3"), "42");
	}

	
	@Test
	public void testDeleteConfigurationString() {
		ConfigurationDao config = new ConfigurationIni(file);
		config.deleteConfiguration("section3");
		Map<String, String> settings = config.getConfiguration();
		assertEquals(settings.size(), 2);
		assertFalse(settings.containsKey("key3"));
		config.updateConfiguration("section3", "key3", "value3"); //restore
	}
 
	@Test
	public void testDeleteConfigurationStringString() {
		ConfigurationDao config = new ConfigurationIni(file);
		config.updateConfiguration("section3", "key3b","value3b");
		config.deleteConfiguration("section3","key3b");
		Map<String, String> settings = config.getConfiguration();
		assertEquals(settings.size(), 3);
		assertFalse(settings.containsKey("key3b"));
		assertTrue(settings.containsKey("key3"));
	}
}
