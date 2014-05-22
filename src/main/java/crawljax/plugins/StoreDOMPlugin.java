package main.java.crawljax.plugins;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

import com.crawljax.core.CrawlerContext;
import com.crawljax.core.plugin.OnNewStatePlugin;
import com.crawljax.core.state.StateVertex;

public class StoreDOMPlugin implements OnNewStatePlugin {
	
	static final String specificOutputDir = "/strippedDOM/";
	static final String fileExtension = ".html";

	@Override
	public String toString() {
		return "Chuck Norris";
	}

	public void onNewState(CrawlerContext context,
			StateVertex state) {
		File outputDir = context.getConfig().getOutputDir();
		File domFile = new File(outputDir + specificOutputDir);
		domFile.mkdirs();
		try {
			PrintWriter out = new PrintWriter(domFile + "/" + state.getName() + fileExtension);
			out.print(state.getStrippedDom());
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
}
