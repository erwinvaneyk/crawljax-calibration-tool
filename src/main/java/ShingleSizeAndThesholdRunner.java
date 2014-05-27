package main.java;

import java.util.List;

import main.java.analysis.Analysis;
import main.java.analysis.AnalysisProcessorCsv;
import main.java.distributed.ConnectionManager;
import main.java.distributed.configuration.ConfigurationDAO;

public class ShingleSizeAndThesholdRunner {
	private static int[] websiteIds = new int[]{1};
	private static int type = 1;
	
	private static String filename = "results";
	
	
	public static void main(String[] args) {
		ThresholdRunner runner = new ThresholdRunner();
		
		ConfigurationDAO config = new ConfigurationDAO(new ConnectionManager());
		
		for (int i=1; i<3; i++) {
			config.updateConfiguration("common", "feature", "FeatureShingles;" + String.valueOf(i) + ";"+ String.valueOf(type), 6);
			List<Analysis> results = runner.analyseThresholds(0, 4, websiteIds);
			for(Analysis analysis : results) {
				new AnalysisProcessorCsv(filename + type + "-" + i).apply(analysis);
			}
		}
		
	}
}