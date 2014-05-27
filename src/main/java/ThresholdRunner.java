package main.java;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import main.java.analysis.*;
import main.java.distributed.ConnectionManager;
import main.java.distributed.configuration.ConfigurationDAO;

public class ThresholdRunner {
	
	private static String filename = "3wordsShingle";
	
	private static int[] websiteIds = new int[]{1,2,15,48,51,53};

	public static void main(String[] args) {
		ThresholdRunner tr = new ThresholdRunner();
		List<Analysis> results = tr.analyseThresholds(1, 1, websiteIds);
		for(Analysis analysis : results) {
			new AnalysisProcessorCsv(filename).apply(analysis);
		}
	}
	
	public List<Analysis> analyseThresholds(int from, int to, int[] websiteids) {
		assert from <= to;
		assert websiteids.length > 0;
		ConfigurationDAO config = new ConfigurationDAO(new ConnectionManager());
		Map<String, String> defaultSettings = config.getConfiguration("common");
		// Build factory
		AnalysisFactory factory = new AnalysisFactory();
		factory.addMetric(new SpeedMetric());
		factory.addMetric(new StateAnalysisMetric());
		List<Analysis> results = new ArrayList<Analysis>();
		
		for(int i = from; i <= to; i++) {
			try {
				// update setting
				config.updateConfiguration("common", "threshold", String.valueOf(i), 6);
				// run crawler
				results.add(factory.getAnalysis("threshold-" + i,websiteids, false));

			} catch (AnalysisException e) {
				e.printStackTrace();
			}
		}
		config.updateConfiguration("common", "threshold", defaultSettings.get("threshold"), 6);
		System.out.println("Finished!");
		return results;
	}
}
