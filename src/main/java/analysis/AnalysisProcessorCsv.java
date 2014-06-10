package main.java.analysis;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.Collection;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * This processor is responsible for outputting the results of the analysis to a csv-file.
 */
@Slf4j
public class AnalysisProcessorCsv extends AnalysisProcessorFile implements AnalysisProcessor {
	private static final String SEPERATOR = ";";
	private static final String ENTRY_SEPERATOR = "\r\n";
	private static final String HEADER_ANALYSEID = "Test ID";

	@Getter @Setter
	private String filename;

	public AnalysisProcessorCsv(String filename) {
		this.filename = filename;
	}

	public void apply(Analysis analysisReport) {
		assert analysisReport.hasMetric(StateAnalysisMetric.class)
		        && analysisReport.hasMetric(SpeedMetric.class);
		try {
			Writer writer =
			        openOrCreateFile(new File(this.getOutputDir() + "/" + filename + ".csv"),
			                true);
			writeContents(writer, analysisReport);
			closeFile(writer);
		} catch (IOException e) {
			e.printStackTrace();
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
