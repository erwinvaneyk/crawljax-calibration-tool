package main.java.analysis;

import java.io.PrintStream;
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
		    for(IMetric metric : analysisReport.getMetrics()) {
			    writer.print("- " + metric.getMetricName() + " \r\n");
		    }
		    writer.print("----------------------------\r\n");
		    writer.print("results: \r\n");
		    printStatistics(analysisReport);		    
		    writer.print("----------------------------\r\n");
		    writer.print("Score: " + analysisReport.getScore() + "\r\n");
		    writer.print("============================\r\n");
		} catch(Exception e) {
			writer.print("Failed to print report: " + analysisReport + ", because " + e.getMessage());
		}
	}

	private void printStatistics(Analysis analysisReport) {
		for(Statistic stat : analysisReport.getStatistics()) {
			writer.print(stat.getName() + ":\t\t" + stat.getValue() + "\r\n");
			if(stat.hasDetails()) {
				for(Object value : stat.getDetails()) {
		    		writer.print("- " + value.toString()+ "\r\n");
		    	}
			}
		}
	}

}
