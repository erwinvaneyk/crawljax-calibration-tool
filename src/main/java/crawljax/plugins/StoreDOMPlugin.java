package main.java.crawljax.plugins;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

import lombok.extern.slf4j.Slf4j;

import com.crawljax.core.CrawlerContext;
import com.crawljax.core.plugin.OnNewStatePlugin;
import com.crawljax.core.state.StateVertex;

/**
 * Plugin for storing the stripped DOM, next to the original DOM.
 */
@Slf4j
public class StoreDOMPlugin implements OnNewStatePlugin {

	private static final String OUTPUT_SUBDIR = "/strippedDOM/";
	private static final String FILE_EXTENSION = ".html";
	private static final String PLUGIN_NAME = "Chuck Norris";

	/**
	 * Returns the name of the metric.
	 */
	public String toString() {
		return PLUGIN_NAME;
	}

	/**
	 * On each new state, store the strippedDOM as {state}.html in the predefined subdir of output.
	 */
	public void onNewState(CrawlerContext context, StateVertex state) {
		File domFile = new File(context.getConfig().getOutputDir() + OUTPUT_SUBDIR);
		domFile.mkdirs();
		try {
			PrintWriter out = new PrintWriter(domFile + "/" + state.getName() + FILE_EXTENSION);
			out.print(state.getStrippedDom());
			out.close();
		} catch (FileNotFoundException e) {
			log.error("File could not be found while storing the strippedDOM: {}", e.getMessage());
		}
	}
}
