package suite.analysis;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.Collection;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * This processor is responsible for outputting the results of the analysis to a csv-file.
 */
@Slf4j
public class AnalysisProcessorCsv extends AnalysisProcessorFile implements AnalysisProcessor {
	private static final String SEPERATOR = ";";
	private static final String ENTRY_SEPERATOR = "\r\n";
	private static final String HEADER_ANALYSEID = "Test ID";
	private static final String FILE_EXTENSION = ".csv";

	@Getter
	private String filename;

	public AnalysisProcessorCsv(String filename) {
		if (filename == null || filename == "")
			throw new AnalysisException("Invalid filename provided: " + filename
			        + ". Filename cannot be null or empty.");
		this.filename = filename;
		if (!filename.endsWith(FILE_EXTENSION))
			this.filename += FILE_EXTENSION;
	}

	public void apply(Analysis analysisReport) {
		if (!analysisReport.hasMetric(StateAnalysisMetric.class)
		        || !analysisReport.hasMetric(SpeedMetric.class)) {
			log.error("AnalysisProcessorCsv.apply requires the metrics StateAnalysisMetric and SpeedMetric to be present. Metrics present: "
			        + analysisReport.getMetrics());
			return;
		}
		try {
			Writer writer = openOrCreateFile(
			        new File(this.getOutputDir() + "/" + filename), true);
			writeContents(writer, analysisReport);
			writer.close();
		} catch (IOException e) {
			log.error("IOException while writing to csv-file: " + e.getMessage());
		}
	}

	@Override
	protected Writer openOrCreateFile(File file, boolean append) throws IOException {
		Writer writer = null;
		if (!(file.exists() && !file.isDirectory())) {
			log.info("No file found. Creating file.");
			writer = super.openOrCreateFile(file, append);
			writer.write(HEADER_ANALYSEID
			        + SEPERATOR + StateAnalysisMetric.TOTAL_BENCHMARK_STATES
			        + SEPERATOR + StateAnalysisMetric.TOTAL_BENCHMARK_UNIQUE_STATES
			        + SEPERATOR + StateAnalysisMetric.TOTAL_TESTED_STATES
			        + SEPERATOR + StateAnalysisMetric.MISSED_STATES
			        + SEPERATOR + StateAnalysisMetric.MISSED_UNIQUE_STATES
			        + SEPERATOR + StateAnalysisMetric.DUPLICATE_STATES + ENTRY_SEPERATOR
			        );
		} else {
			log.info("File found. Appending to file.");
			writer = super.openOrCreateFile(file, append);
		}
		return writer;
	}

	private void writeContents(Writer writer, Analysis analysisReport) throws IOException {
		Collection<Statistic> stats = analysisReport.getStatistics();
		writer.write(analysisReport.getTitle()
		        + SEPERATOR
		        + findStatisticByName(stats, StateAnalysisMetric.TOTAL_BENCHMARK_STATES)
		                .getValue()
		        + SEPERATOR
		        + findStatisticByName(stats, StateAnalysisMetric.TOTAL_BENCHMARK_UNIQUE_STATES)
		                .getValue()
		        + SEPERATOR
		        + findStatisticByName(stats, StateAnalysisMetric.TOTAL_TESTED_STATES).getValue()
		        + SEPERATOR
		        + findStatisticByName(stats, StateAnalysisMetric.MISSED_STATES).getValue()
		        + SEPERATOR
		        + findStatisticByName(stats, StateAnalysisMetric.MISSED_UNIQUE_STATES).getValue()
		        + SEPERATOR
		        + findStatisticByName(stats, StateAnalysisMetric.DUPLICATE_STATES).getValue()
		        + ENTRY_SEPERATOR
		        );
	}

	private Statistic findStatisticByName(Collection<Statistic> stats, String name) {
		for (Statistic stat : stats) {
			if (stat.getName().equals(name))
				return stat;
		}
		return null;
	}

}
