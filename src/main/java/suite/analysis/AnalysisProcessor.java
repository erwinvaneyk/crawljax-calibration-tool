package suite.analysis;

/**
 * Processors should be classes which manipulate the analysis in some way. Plugins should be used
 * here.
 */
public interface AnalysisProcessor {

	/**
	 * Manipulates the analysis in some way, independent of the analysis.
	 * 
	 * @param analysisReport
	 *            the analysis.
	 */
	void apply(Analysis analysisReport);

}
