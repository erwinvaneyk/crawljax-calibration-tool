package main.java.analysis;

/**
 * Processors should be classes which manipulate the analysis in some way. Plugins should be used
 * here.
 */
public interface IAnalysisProcessor {

	/**
	 * Manipulates the analysis in some way, independent of the analysis.
	 * 
	 * @param analysisReport
	 *            the analysis.
	 */
	public void apply(Analysis analysisReport);

}
