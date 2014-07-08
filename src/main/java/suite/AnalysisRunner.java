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

/**
 * The AnalysisRunner is responsible for running analyzes.
 */
public class AnalysisRunner {

	private static final String NAMESPACE = "test";

	private String filenamePrefix = "results";
	private ConfigurationDao config;
	private AnalysisBuilder factory;

	public static void main(String[] args) {
		AnalysisRunner ar = Guice.createInjector(new TestingSuiteModule(NAMESPACE))
		        .getInstance(AnalysisRunner.class);
		ar.run();
	}

	/**
	 * Constructor for AnalysisRunner used by Guice
	 * 
	 * @param factory
	 *            the factory to be used to generate the analysis
	 * @param config
	 *            the configurationDao to be used.
	 * @param speedMetric
	 * 			  measuring the runtime
	 * @param saMetric
	 * 			  measuring the duplicate and missed states of a test run
	 */
	@Inject
	AnalysisRunner(AnalysisBuilder factory, ConfigurationDao config, SpeedMetric speedMetric,
	        StateAnalysisMetric saMetric) {
		this.factory = factory;
		this.config = config;
		factory.addMetric(speedMetric);
		factory.addMetric(saMetric);
	}

	/**
	 * Using a given set of website-ids, threshold-range and featuresize-range, WorkTasks
	 */
	public void run() {
		// ############################
		// TODO: Should be read from input or external file..
		final int[] websiteIds = new int[] { 1, 2, 15, 48, 51, 53 };
		final double minThreshold = 1;
		final double maxThreshold = 1;
		final double stepsizeThreshold = 1;
		final int shingleType = FeatureShingles.ShingleType.WORDS.ordinal();
		final int minShingleSize = 1;
		final int maxShingleSize = 1;
		// #############################
		namespaceSetup();
		try {
			for (int i = minShingleSize; i <= maxShingleSize; i++) {
				config.updateConfiguration(
				        NAMESPACE,
				        "feature",
				        "FeatureShingles;" + String.valueOf(i) + ";"
				                + String.valueOf(shingleType));
				List<Analysis> results =
				        analyseThresholds(minThreshold, maxThreshold, stepsizeThreshold,
				                websiteIds);
				for (Analysis analysis : results) {
					new AnalysisProcessorCsv(filenamePrefix + shingleType + "-" + i)
					        .apply(analysis);
				}
			}
		} finally {
			namespaceCleanup();
		}
	}

	/**
	 * Runs crawler on a threshold-range.
	 * 
	 * @param from
	 *            lowerbound of threshold
	 * @param to
	 *            upperbound of threshold
	 * @param step
	 *            the stepsize of the threshold
	 * @param websiteids
	 *            the websites tht should be crawled each iteration
	 * @return A list of Analysis. An analysis for each threshold
	 */
	private List<Analysis> analyseThresholds(double from, double to, double step, int[] websiteids) {
		assert from <= to;
		assert websiteids.length > 0;
		Map<String, String> defaultSettings = config.getConfiguration(NAMESPACE);
		List<Analysis> results = new ArrayList<Analysis>((int) ((to - from) / step));

		for (double i = from; i <= to; i += step) {
			results.add(issueCrawl(websiteids, i));
		}
		config.updateConfiguration(NAMESPACE, "threshold", defaultSettings.get("threshold"));
		return results;
	}

	/**
	 * Issue a crawl for all workers
	 * 
	 * @param websiteids
	 *            the ids of the websites which should be recrawled
	 * @param threshold
	 *            the threshold
	 * @return an analysis
	 */
	private Analysis issueCrawl(int[] websiteids, double threshold) {
		config.updateConfiguration(NAMESPACE, "threshold", String.valueOf(threshold));
		return factory.getAnalysis("threshold-" + threshold, websiteids);
	}

	/**
	 * Creates a (temporary section for the NAMESPACE by copying the settings of the common section.
	 * This is done to keep the original common settings clean.
	 */
	private void namespaceSetup() {
		if (!NAMESPACE.equals(ConfigurationDao.SECTION_COMMON)) {
			Map<String, String> settings = config.getConfiguration(NAMESPACE);
			if (settings.size() == 0) {
				Map<String, String> commonSettings =
				        config.getConfiguration(ConfigurationDao.SECTION_COMMON);
				for (Entry<String, String> commonSetting : commonSettings.entrySet()) {
					config.updateConfiguration(NAMESPACE, commonSetting.getKey(),
					        commonSetting.getValue());
				}
			}
		}
	}

	/**
	 * Removes the (temporary) section of the NAMESPACE.
	 */
	private void namespaceCleanup() {
		if (!NAMESPACE.equals(ConfigurationDao.SECTION_COMMON)) {
			config.deleteConfiguration(NAMESPACE);
		}
	}

	public String getFilenamePrefix() {
		return filenamePrefix;
	}

	public void setFilenamePrefix(String filenamePrefix) {
		this.filenamePrefix = filenamePrefix;
	}
}