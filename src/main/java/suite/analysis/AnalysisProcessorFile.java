package suite.analysis;

import java.io.*;

import suite.distributed.results.WebsiteResult;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
/**
 * This processor is responsible for outputting the results of an analysis to a file.
 */
public class AnalysisProcessorFile implements AnalysisProcessor {

	private final static String FILE_EXTENSION = ".txt";
	private final static String LINEBREAK = System.lineSeparator();
	private final static String HORIZONTALBREAK = "----------------------------" + LINEBREAK;

	@Getter
	@Setter
	private File outputDir = new File(System.getProperty("user.dir") + "/output/");

	@Getter
	protected File output;

	public void apply(Analysis analysisReport) {
		try (Writer writer =
		        openOrCreateFile(new File(outputDir + "/" + analysisReport.getTitle()
		                + FILE_EXTENSION), false)) {
			writeContentsToFile(analysisReport, writer);
		} catch (IOException e) {
			log.error("Error while creating file: {}", e.getMessage());
		}
	}

	protected Writer openOrCreateFile(File file, boolean append) throws IOException {
		Writer writer = new FileWriter(file, append);
		output = file;
		log.info("Report written to file: " + file);
		return writer;
	}

	protected void closeFile(Writer writer) throws IOException {
		writer.close();
	}

	private void writeContentsToFile(Analysis analysisReport, Writer writer) throws IOException {

		writer.write(analysisReport.getTitle() + LINEBREAK);
		writer.write(HORIZONTALBREAK);
		writer.write("Benchmarked Websites:" + LINEBREAK);
		for (WebsiteResult website : analysisReport.getBenchmarkWebsites()) {
			writer.write("(" + website.getId() + ") " + website.getWorkTask().getURL()
			        + LINEBREAK);
		}
		writer.write(HORIZONTALBREAK);
		writer.write("metrics:" + LINEBREAK);
		printStatistics(analysisReport, writer);
	}

	private void printStatistics(Analysis analysisReport, Writer writer) throws IOException {
		for (Statistic stat : analysisReport.getStatistics()) {
			writer.write(stat.getName() + ":\t\t" + stat.getValue());
			if (stat.hasDetails()) {
				for (Object value : stat.getDetails()) {
					writer.write("- " + value.toString() + LINEBREAK);
				}
			}
		}
	}
}
