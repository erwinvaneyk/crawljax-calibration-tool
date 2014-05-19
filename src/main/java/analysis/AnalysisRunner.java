package main.java.analysis;

public class AnalysisRunner {

	public static void main(String[] args) {
		try {
			// Build factory
			AnalysisFactory factory = new AnalysisFactory();
			factory.addMetric(new SpeedMetric());
			factory.addMetric(new StateAnalysisMetric());
			
			// Generate report
			Analysis analysis = factory.getAnalysis("analysis", new int[]{40});

			// Generate file
			new AnalysisProcessorFile().apply(analysis);
			// Output to cmd
			new AnalysisProcessorCmd().apply(analysis);
		} catch (AnalysisException e) {
			System.out.println(e.getMessage());
		}
	}
}
