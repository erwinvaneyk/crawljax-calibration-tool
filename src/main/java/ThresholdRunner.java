package main.java;

import java.util.Map;

import main.java.analysis.*;
import main.java.distributed.ConnectionManager;
import main.java.distributed.configuration.ConfigurationDAO;

public class ThresholdRunner {
	
	private static String filename = "3wordsShingle";
	
	private static int[] websiteIds = new int[]{1,2,15,48,51,53};

	public static void main(String[] args) {
		ConfigurationDAO config = new ConfigurationDAO(new ConnectionManager());
		Map<String, String> defaultSettings = config.getConfiguration("common");
		// Build factory
		AnalysisFactory factory = new AnalysisFactory();
		factory.addMetric(new SpeedMetric());
		factory.addMetric(new StateAnalysisMetric());
		
		for(int i = 1; i <= 5; i++) {
			try {
				// update setting
				config.updateConfiguration("common", "threshold", String.valueOf(i), 6);
				// run crawler
				Analysis analysis = factory.getAnalysis("threshold-" + i,websiteIds);
				// Generate report
				new AnalysisProcessorCsv(filename).apply(analysis);
			} catch (AnalysisException e) {
				e.printStackTrace();
			}
		}
		config.updateConfiguration("common", "threshold", defaultSettings.get("threshold"), 6);
		System.out.println("Finished!");
	}

}
