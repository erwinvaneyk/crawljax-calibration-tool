package main.java.analysis;

public interface IAnalysisFactory {
	
	public void addMetric(IMetric metric);
	
	public Analysis getAnalysis(String title, int[] websiteids) throws AnalysisException;

}
