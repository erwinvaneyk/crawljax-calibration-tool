package main.java.analysis;

public class AnalysisRunner {

	public static void main(String[] args) {
		try {
			// Build factory
			AnalysisFactory factory = new AnalysisFactory();
			factory.addMetric(new SpeedMetric());
			
			// Generate report
			Analysis analysis = factory.getAnalysis("analysis", new int[]{2});

			// Generate file
			new AnalysisProcessorFile().apply(analysis);
			// Output to cmd
			new AnalysisProcessorCmd().apply(analysis);
		} catch (AnalysisException e) {
			e.printStackTrace();
		}
	}
}
