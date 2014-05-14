package main.java.analysis;

import java.sql.SQLException;

public class AnalysisRunner {

	public static void main(String[] args) {
		try {
			// Generate report
			AnalysisReport analysis = new AnalysisFactory().getAnalysis("analysis", new int[]{3});

			// Generate file
			new AnalysisProcessorFile().apply(analysis);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
