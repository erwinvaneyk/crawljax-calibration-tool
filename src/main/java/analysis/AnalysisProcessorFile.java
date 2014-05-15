package main.java.analysis;

import java.io.*;

import lombok.extern.slf4j.Slf4j;
import main.java.distributed.results.StateResult;
import main.java.distributed.results.WebsiteResult;

@Slf4j
public class AnalysisProcessorFile implements IAnalysisProcessor {
	
	private static final File DIR_OUTPUT = new File(System.getProperty("user.dir") + "/output/");
	
	private AnalysisReport analysisReport;
	
	public void apply(AnalysisReport analysisReport) {
		this.analysisReport = analysisReport;
		Writer writer = null;
		try {
			File file = new File(DIR_OUTPUT + "/" + analysisReport.getTitle() + ".txt");
		    writer = new BufferedWriter(new OutputStreamWriter(
		          new FileOutputStream(file), "utf-8"));
		    writeContentsToFile(writer);
		    log.info("Report written to file: " + file);
		} catch (IOException ex) {
			log.error("ERROOORRRRRRR");
		} finally {
		   try {writer.close();} catch (Exception ex) {}
		}
	}
	
	private void writeContentsToFile(Writer writer) throws IOException {

	    writer.write(analysisReport.getTitle() + "\r\n");
	    writer.write("----------------------------\r\n");
	    writer.write("Benchmarked Websites: \r\n");
	    for(WebsiteResult website : analysisReport.getBenchmarkWebsites()) {
	    	writer.write("("+ website.getId() + ") " + website.getWorkTask().getURL() + "\r\n");
	    }
	    writer.write("----------------------------\r\n");
	    writer.write("metrics: \r\n");
	    writer.write("Accuracy:\t" + String.valueOf(analysisReport.getAccuracy()) + "\r\n");
	    writer.write("Speed diff:\t" + analysisReport.getSpeedDifference() + "\r\n");	    
	    writer.write("Dupli. states:\t" + analysisReport.getFailedStatesDuplicates().size() + "\r\n");
	    for(StateResult state : analysisReport.getFailedStatesDuplicates()) {
	    	writer.write("- (" + state.getWebsiteResult().getId() + ") " + state.getStateId() +  "\r\n");
	    }
	    writer.write("Missed states:\t" + analysisReport.getFailedStatesMissed().size() + "\r\n");
	    for(StateResult state : analysisReport.getFailedStatesMissed()) {
	    	writer.write("- (" + state.getWebsiteResult().getId() + ") " +  state.getStateId() + "\r\n");
	    }
	}
}
