package main.java.analysis;

import java.io.PrintStream;

import main.java.distributed.results.WebsiteResult;

public class AnalysisProcessorCmd implements IAnalysisProcessor {

	PrintStream writer = System.out;	

	public void apply(AnalysisReport analysisReport) {
	    writer.print("============================\r\n");
		writer.print(analysisReport.getTitle() + "\r\n");
	    writer.print("----------------------------\r\n");
	    writer.print("Benchmarked Websites: \r\n");
	    for(WebsiteResult website : analysisReport.getBenchmarkWebsites()) {
	    	writer.print(website.getWorkTask().getURL() + "\r\n");
	    }
	    writer.print("----------------------------\r\n");
	    writer.print("metrics: \r\n");
	    writer.print("Accuracy:\t" + String.valueOf(analysisReport.getAccuracy()) + "\r\n");
	    writer.print("Speed diff:\t" + analysisReport.getSpeedDifference() + "\r\n");
	    writer.print("============================\r\n");
	}

}
