package main.java;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;

import main.java.analysis.*;
import main.java.distributed.configuration.ConfigurationDao;

public class ThresholdRunner {
	
	private final static String FILENAME = "2wordsShingleBroder";
	
	private final static int[] WEBSITE_IDS = new int[]{1,};

	private static Injector injector;

	public static void main(String[] args) {
		injector = Guice.createInjector(new TestingSuiteModule());
		ThresholdRunner tr = injector.getInstance(ThresholdRunner.class);
		List<Analysis> results = tr.analyseThresholds(1, 1, 1, WEBSITE_IDS);
		for(Analysis analysis : results) {
			new AnalysisProcessorCsv(FILENAME).apply(analysis);
		}
	}

	private ConfigurationDao config;

	private AnalysisBuilder factory;

	@Inject
	public ThresholdRunner(ConfigurationDao config, AnalysisBuilder factory) {
		injector = Guice.createInjector(new TestingSuiteModule());
		this.config = config;
		this.factory = factory;
	}
	
	public List<Analysis> analyseThresholds(double from, double to, double step, int[] websiteids) {
		assert from <= to;
		assert websiteids.length > 0;
		Map<String, String> defaultSettings = config.getConfiguration("common");
		// Build factory
		factory.addMetric(injector.getInstance(SpeedMetric.class));
		factory.addMetric(injector.getInstance(StateAnalysisMetric.class));
		List<Analysis> results = new ArrayList<Analysis>((int)((to - from)/step));
		
		for(double i = from; i <= to; i+=step) {
			// update setting
			config.updateConfiguration("common", "threshold", String.valueOf(i), 6);
			// run crawler
			results.add(factory.getAnalysis("threshold-" + i,websiteids));
		}
		config.updateConfiguration("common", "threshold", defaultSettings.get("threshold"), 6);
		System.out.println("Finished!");
		return results;
	}
}
