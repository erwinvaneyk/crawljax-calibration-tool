package main.java.analysis;

import java.io.*;
import java.util.Collection;
import java.util.Map.Entry;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import main.java.distributed.results.StateResult;
import main.java.distributed.results.WebsiteResult;

@Slf4j
/**
 * This processor is responsible for outputting the results of an analysis to a file.
 */
public class AnalysisProcessorFile implements IAnalysisProcessor {
	
	@Getter @Setter private File outputDir = new File(System.getProperty("user.dir") + "/output/");
	
	@Getter private File output;
	
	private Writer writer;
	
	public void apply(Analysis analysisReport) {
		try {
			File file = new File(outputDir + "/" + analysisReport.getTitle() + ".txt");
			writer = new BufferedWriter(new OutputStreamWriter(
		          new FileOutputStream(file), "utf-8"));
		    writeContentsToFile(analysisReport);
		    output = file;
		    log.info("Report written to file: " + file);
		} catch (Exception ex) {
			log.error("Error while writing to file: " + ex.getMessage());
		} finally {
		   try {writer.close();} catch (Exception ex) {}
		}
	}
	
	private void writeContentsToFile(Analysis analysisReport) throws IOException {

	    writer.write(analysisReport.getTitle() + "\r\n");
	    writer.write("----------------------------\r\n");
	    writer.write("Benchmarked Websites: \r\n");
	    for(WebsiteResult website : analysisReport.getBenchmarkWebsites()) {
	    	writer.write("("+ website.getId() + ") " + website.getWorkTask().getURL() + "\r\n");
	    }
	    writer.write("----------------------------\r\n");
	    writer.write("metrics: \r\n");
	    printStatistics(analysisReport);
	   
	}
	
	@SuppressWarnings("unchecked")
	private void printStatistics(Analysis analysisReport) throws IOException {
		for(Entry<String, Object> stat : analysisReport.getStatistics().entrySet()) {
		    if(stat.getValue() instanceof Collection) {
		    	Collection<StateResult> collection = (Collection<StateResult>) stat.getValue();
		    	writer.write(stat.getKey() + ":\t\t" + collection.size() + "\r\n");
		    	for(StateResult value : collection) {
		    		writer.write("- (" + value.getWebsiteResult().getId() + ")" + value.getStateId()+ "\r\n");
		    	}
		    } else if(stat.getValue() instanceof StateResult) { 
		    	StateResult value = (StateResult) stat.getValue();
		    	writer.write(stat.getKey() + ":\t\t" + value.getStateId() + "\r\n");
			} else {
		    	writer.write(stat.getKey() + ":\t\t" + stat.getValue() + "\r\n");
		    }
		}
	}
}
