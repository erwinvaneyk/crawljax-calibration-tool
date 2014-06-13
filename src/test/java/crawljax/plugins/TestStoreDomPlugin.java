package test.java.crawljax.plugins;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Test;

import lombok.extern.slf4j.Slf4j;
import main.java.crawljax.plugins.StoreDomPlugin;

import com.crawljax.core.CrawlerContext;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.core.plugin.OnNewStatePlugin;
import com.crawljax.core.state.StateVertex;

@Slf4j
public class TestStoreDomPlugin {

	@After
	public void removeTestDir() {
		try {
			FileUtils.deleteDirectory(new File("TestDir"));
			log.debug("Test directory removed.");
		} catch (IOException e) {
			log.error("IOException while removing the TestDir directory: " + e.getMessage());
		}
	}

	@Test
	public void testStoreDomPluginToString() {
		OnNewStatePlugin domPlugin = new StoreDomPlugin();
		assertEquals("StrippedDom plugin", domPlugin.toString());
	}

	@Test
	public void testStoreDomPluginOnNewState() throws IOException {
		OnNewStatePlugin domPlugin = new StoreDomPlugin();

		// Mock the context and its methods that are called in the onNewState method
		CrawlerContext context = mock(CrawlerContext.class);
		CrawljaxConfiguration config = mock(CrawljaxConfiguration.class);
		when(context.getConfig()).thenReturn(config);
		File testDir = new File("TestDir");
		when(config.getOutputDir()).thenReturn(testDir);

		// Mock the state and its methods that are called in the onNewState method
		StateVertex newState = mock(StateVertex.class);
		when(newState.getName()).thenReturn("TestState");
		when(newState.getStrippedDom()).thenReturn("This represents the StrippedDom");

		// Call the method we want to test
		domPlugin.onNewState(context, newState);

		// Make sure the strippedDom is written correctly to the file
		String fileContent = "";
		File strippedDom = testDir.listFiles()[0];
		for (File file : strippedDom.listFiles()) {
			if (file.getName().equals("TestState.html")) {
				BufferedReader br = new BufferedReader(new FileReader(file));
				String line;
				while ((line = br.readLine()) != null) {
					fileContent += line;
				}
				br.close();
			}
		}
		assertEquals("This represents the StrippedDom", fileContent);
	}
}
