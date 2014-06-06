package main.java.analysis;

import java.io.PrintStream;

import lombok.extern.slf4j.Slf4j;
import main.java.distributed.results.WebsiteResult;

/**
 * This processor is responsible for outputting the results of the analysis to the console.
 */
@Slf4j
public class AnalysisProcessorCmd implements IAnalysisProcessor {

	private final static String LINEBREAK = "\r\n";
	private final static String HORIZONTALBREAK_MAIN = "============================" + LINEBREAK;
	private final static String HORIZONTALBREAK_SUB = "----------------------------" + LINEBREAK;

	PrintStream writer = System.out;

	public void apply(Analysis analysisReport) {
		assert writer != null && !writer.checkError();
		try {
			writer.print(HORIZONTALBREAK_MAIN);
			writer.print(analysisReport.getTitle() + LINEBREAK);
			writer.print(HORIZONTALBREAK_SUB);
			writer.print("Benchmarked Websites: " + LINEBREAK);
			for (WebsiteResult website : analysisReport.getBenchmarkWebsites()) {
				writer.print("(" + website.getId() + ") " + website.getWorkTask().getURL()
				        + LINEBREAK);
			}
			writer.print(HORIZONTALBREAK_SUB);
			writer.print("metrics: " + LINEBREAK);
			for (IMetric metric : analysisReport.getMetrics()) {
				writer.print("- " + metric.getMetricName() + LINEBREAK);
			}
			writer.print(HORIZONTALBREAK_SUB);
			writer.print("results: " + LINEBREAK);
			printStatistics(analysisReport);
			writer.print(HORIZONTALBREAK_MAIN);
		} catch (Exception e) {
			writer.print("Failed to print report: " + analysisReport + ", because "
			        + e.getMessage());
			log.error("Failed to print report: " + analysisReport + ", because " + e.getMessage());
		}
	}

	private void printStatistics(Analysis analysisReport) {
		for (Statistic stat : analysisReport.getStatistics()) {
			writer.print(stat.getName() + ":\t\t" + stat.getValue() + LINEBREAK);
			if (stat.hasDetails()) {
				for (Object value : stat.getDetails()) {
					writer.print("- " + value.toString() + LINEBREAK);
				}
			}
		}
	}

}
