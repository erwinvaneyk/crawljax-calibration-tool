package main.java;

import java.util.List;

import com.google.inject.Guice;
import com.google.inject.Inject;

import main.java.analysis.Analysis;
import main.java.analysis.AnalysisProcessorCsv;
import main.java.distributed.configuration.ConfigurationDao;

public class ShingleSizeAndThesholdRunner {
	private static int[] WEBSITE_IDS = new int[] { 1, 2, 15, 48, 51, 53 };
	private static int TYPE = 1;

	private static String FILENAME = "results";

	private ConfigurationDao config;
	private ThresholdRunner runner;

	public static void main(String[] args) {
		Guice.createInjector(new TestingSuiteModule())
		        .getInstance(ShingleSizeAndThesholdRunner.class).run();
	}

	@Inject
	public ShingleSizeAndThesholdRunner(ThresholdRunner runner, ConfigurationDao config) {
		this.runner = runner;
		this.config = config;
	}

	public void run() {
		for (int i = 1; i <= 9; i++) {
			config.updateConfiguration("common", "feature",
			        "FeatureShingles;" + String.valueOf(i) + ";" + String.valueOf(TYPE), 6);
			List<Analysis> results = runner.analyseThresholds(1, 1, 1, WEBSITE_IDS);
			for (Analysis analysis : results) {
				new AnalysisProcessorCsv(FILENAME + TYPE + "-" + i).apply(analysis);
			}
		}

	}
}