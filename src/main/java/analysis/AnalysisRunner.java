package main.java.analysis;

public class AnalysisRunner {

	public static void main(String[] args) {
		try {
			// Build factory
			AnalysisFactory factory = new AnalysisFactory();
			factory.addMetric(new SpeedMetric());
			factory.addMetric(new StateAnalysisMetric(1));
			
			// Generate report
			Analysis analysis = factory.getAnalysis("analysis", new int[]{19});

			// Generate file
			new AnalysisProcessorFile().apply(analysis);
			// Output to cmd
			new AnalysisProcessorCmd().apply(analysis);
		} catch (AnalysisException e) {
			e.printStackTrace();
		}
	}
}
