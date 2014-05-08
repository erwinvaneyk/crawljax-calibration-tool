package main.java;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

import com.crawljax.core.CandidateElement;
import com.crawljax.core.CrawlerContext;
import com.crawljax.core.plugin.PreStateCrawlingPlugin;
import com.crawljax.core.state.StateVertex;
import com.google.common.collect.ImmutableList;

public class StoreDOMPlugin implements PreStateCrawlingPlugin {

	@Override
	public String toString() {
		return "Chuck Norris";
	}

	public void preStateCrawling(CrawlerContext context,
			ImmutableList<CandidateElement> candidateElements,
			StateVertex state) {
		File outputDir = context.getConfig().getOutputDir();
		File domFile = new File(outputDir + "/strippedDOM/");
		domFile.mkdirs();
		try {
			PrintWriter out = new PrintWriter(domFile + "/" + state.getName() + ".html");
			out.print(state.getStrippedDom());
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
}
