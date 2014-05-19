package main.java.analysis;

import java.io.PrintStream;
import java.util.Collection;
import java.util.Map.Entry;

import main.java.distributed.results.StateResult;
import main.java.distributed.results.WebsiteResult;

/**
 * This processor is responsible for outputting the results of the analysis to the console.
 */
public class AnalysisProcessorCmd implements IAnalysisProcessor {

	PrintStream writer = System.out;	

	public void apply(Analysis analysisReport) {
		assert writer != null && !writer.checkError();
		try {
		    writer.print("============================\r\n");
		    writer.print(analysisReport.getTitle() + "\r\n");
		    writer.print("----------------------------\r\n");
		    writer.print("Benchmarked Websites: \r\n");
		    for(WebsiteResult website : analysisReport.getBenchmarkWebsites()) {
		    	writer.print("("+ website.getId() + ") " + website.getWorkTask().getURL() + "\r\n");
		    }
		    writer.print("----------------------------\r\n");
		    writer.print("metrics: \r\n");
		    printStatistics(analysisReport);		    
		    writer.print("----------------------------\r\n");
		    writer.print("Score: " + analysisReport.getScore() + "\r\n");
		    writer.print("============================\r\n");
		} catch(Exception e) {
			writer.print("Failed to print report: " + analysisReport + ", because " + e.getMessage());
		}
	}

	@SuppressWarnings("unchecked")
	private void printStatistics(Analysis analysisReport) {
		for(Entry<String, Object> stat : analysisReport.getStatistics().entrySet()) {
		    if(stat.getValue() instanceof Collection) {
		    	writer.print(stat.getKey() + ": \r\n");
		    	for(StateResult value : (Collection<StateResult>) stat.getValue()) {
		    		writer.print("- " + value.getStateId()+ "\r\n");
		    	}
		    } else if(stat.getValue() instanceof StateResult) { 
		    	StateResult value = (StateResult) stat.getValue();
		    	writer.print(stat.getKey() + ":\t\t" + value.getStateId() + "\r\n");
			} else {
		    	writer.print(stat.getKey() + ":\t\t" + stat.getValue() + "\r\n");
		    }
		}
	}

}
