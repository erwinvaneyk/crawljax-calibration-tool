package main.java;

import java.util.List;

import com.google.inject.Guice;
import com.google.inject.Inject;

import main.java.analysis.Analysis;
import main.java.analysis.AnalysisProcessorCsv;
import main.java.distributed.configuration.ConfigurationDao;

public class ShingleSizeAndThesholdRunner {	
	private static int[] websiteIds = new int[]{1};
	private static int type = 1;
	
	private static String filename = "results";

	private ConfigurationDao config;
	private ThresholdRunner runner;
	
	public static void main(String[] args) {
		Guice.createInjector(new TestingSuiteModule()).getInstance(ShingleSizeAndThesholdRunner.class).run();
	}
	
	@Inject
	public ShingleSizeAndThesholdRunner(ThresholdRunner runner, ConfigurationDao config) {
		this.runner = runner;
		this.config = config;
	}
	
	public void run() {		
		for (int i=1; i<3; i++) {
			config.updateConfiguration("common", "feature", "FeatureShingles;" + String.valueOf(i) + ";"+ String.valueOf(type), 6);
			List<Analysis> results = runner.analyseThresholds(0.0, 4.0, 0.1, websiteIds);
			for(Analysis analysis : results) {
				new AnalysisProcessorCsv(filename + type + "-" + i).apply(analysis);
			}
		}
		
	}
}