package main.java;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import main.java.analysis.*;
import main.java.distributed.ConnectionManager;
import main.java.distributed.configuration.ConfigurationDAO;

public class ThresholdRunner {
	
	private static String filename = "2wordsShingleBroder";
	
	private static int[] websiteIds = new int[]{1,};

	public static void main(String[] args) {
		ThresholdRunner tr = new ThresholdRunner();
		List<Analysis> results = tr.analyseThresholds(1, 1, 1, websiteIds);
		for(Analysis analysis : results) {
			new AnalysisProcessorCsv(filename).apply(analysis);
		}
	}
	
	public List<Analysis> analyseThresholds(double from, double to, double step, int[] websiteids) {
		assert from <= to;
		assert websiteids.length > 0;
		ConfigurationDAO config = new ConfigurationDAO(new ConnectionManager());
		Map<String, String> defaultSettings = config.getConfiguration("common");
		// Build factory
		AnalysisFactory factory = new AnalysisFactory();
		factory.addMetric(new SpeedMetric());
		factory.addMetric(new StateAnalysisMetric());
		List<Analysis> results = new ArrayList<Analysis>();
		
		for(double i = from; i <= to; i+=step) {
			try {
				// update setting
				config.updateConfiguration("common", "threshold", String.valueOf(i), 6);
				// run crawler
				results.add(factory.getAnalysis("threshold-" + i,websiteids));

			} catch (AnalysisException e) {
				e.printStackTrace();
			}
		}
		config.updateConfiguration("common", "threshold", defaultSettings.get("threshold"), 6);
		System.out.println("Finished!");
		return results;
	}
}
