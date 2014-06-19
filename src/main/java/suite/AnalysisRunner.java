package suite;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import suite.analysis.Analysis;
import suite.analysis.AnalysisBuilder;
import suite.analysis.AnalysisProcessorCsv;
import suite.analysis.SpeedMetric;
import suite.analysis.StateAnalysisMetric;
import suite.distributed.configuration.ConfigurationDao;

import com.crawljax.core.state.duplicatedetection.FeatureShingles;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;

public class AnalysisRunner {
	private static int[] WEBSITE_IDS = new int[] { 1, 2, 15, 48, 51, 53 };
	
	private static String FILENAME = "results";
	private static final String NAMESPACE = "test";

	private ConfigurationDao config;
	private AnalysisBuilder factory;
	@Inject private Injector injector;

	public static void main(String[] args) {
		AnalysisRunner ar = Guice.createInjector(new TestingSuiteModule(NAMESPACE))
		        .getInstance(AnalysisRunner.class);
		ar.run();
	}

	@Inject
	public AnalysisRunner(AnalysisBuilder factory, ConfigurationDao config) {
		this.factory = factory;
		this.config = config;
	}

	public void run() {
		// ############################
		double minThreshold = 1;
		double maxThreshold = 1;
		double stepsizeThreshold = 1;
		int shingleType = FeatureShingles.SizeType.WORDS.ordinal();
		int minShingleSize = 1;
		int maxShingleSize = 1;
		// #############################
		namespaceSetup();
		try {
			for (int i = minShingleSize; i <= maxShingleSize; i++) {
				config.updateConfiguration(NAMESPACE, "feature",
				        "FeatureShingles;" + String.valueOf(i) + ";" + String.valueOf(shingleType), 6);
				List<Analysis> results = analyseThresholds(minThreshold, maxThreshold, stepsizeThreshold, WEBSITE_IDS);
				for (Analysis analysis : results) {
					new AnalysisProcessorCsv(FILENAME + shingleType + "-" + i).apply(analysis);
				}
			}
		} finally {
			namespaceCleanup();
		}
	}
	
	public List<Analysis> analyseThresholds(double from, double to, double step, int[] websiteids) {
		assert from <= to;
		assert websiteids.length > 0;
		Map<String, String> defaultSettings = config.getConfiguration(NAMESPACE);
		// Build factory
		factory.addMetric(injector.getInstance(SpeedMetric.class));
		factory.addMetric(injector.getInstance(StateAnalysisMetric.class));
		List<Analysis> results = new ArrayList<Analysis>((int) ((to - from) / step));

		for (double i = from; i <= to; i += step) {
			// update setting
			config.updateConfiguration(NAMESPACE, "threshold", String.valueOf(i), 6);
			// run crawler
			results.add(factory.getAnalysis("threshold-" + i, websiteids));
		}
		config.updateConfiguration(NAMESPACE, "threshold", defaultSettings.get("threshold"), 6);
		System.out.println("Finished!");
		return results;
	}
	

	private void namespaceSetup() {
		if(!NAMESPACE.equals(ConfigurationDao.SECTION_COMMON)) {
			Map<String, String> settings = config.getConfiguration(NAMESPACE);
			if(settings.size() == 0) {
				Map<String, String> commonSettings = config.getConfiguration(ConfigurationDao.SECTION_COMMON);
				for(Entry<String, String> commonSetting : commonSettings.entrySet()) {
					config.updateConfiguration(NAMESPACE, commonSetting.getKey(), commonSetting.getValue(), 6);
				}
			}
		}
	}
	
	private void namespaceCleanup() {
		if(!NAMESPACE.equals(ConfigurationDao.SECTION_COMMON)) {
			config.deleteConfiguration(NAMESPACE);
		}
	}
}