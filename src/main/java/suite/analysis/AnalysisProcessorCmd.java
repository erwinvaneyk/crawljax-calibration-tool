package suite.analysis;

import java.io.PrintStream;

import suite.distributed.results.WebsiteResult;

/**
 * This processor is responsible for outputting the results of the analysis to the console.
 */
public class AnalysisProcessorCmd implements AnalysisProcessor {

	private final static String LINEBREAK = System.lineSeparator();
	private final static String HORIZONTALBREAK_MAIN = "============================" + LINEBREAK;
	private final static String HORIZONTALBREAK_SUB = "----------------------------" + LINEBREAK;

	PrintStream writer = System.out;

	public void apply(Analysis analysisReport) {
		assert writer != null && !writer.checkError();
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
		for (Metric metric : analysisReport.getMetrics()) {
			writer.print("- " + metric.getName() + LINEBREAK);
		}
		writer.print(HORIZONTALBREAK_SUB);
		writer.print("results: " + LINEBREAK);
		printStatistics(analysisReport);
		writer.print(HORIZONTALBREAK_MAIN);
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
