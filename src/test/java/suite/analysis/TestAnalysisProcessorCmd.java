package suite.analysis;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import suite.analysis.Analysis;
import suite.analysis.AnalysisException;
import suite.analysis.AnalysisProcessorCmd;

public class TestAnalysisProcessorCmd {

	private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();

	@Before
	public void setUpStreams() {
		System.setOut(new PrintStream(outContent));
	}

	@After
	public void cleanUpStreams() {
		System.setOut(null);
	}

	@Test
	public void testApply() throws AnalysisException {
		Analysis analysisReport = mock(Analysis.class);
		new AnalysisProcessorCmd().apply(analysisReport);
		assertNotNull(outContent.toString());
	}

	@Test
	public void testApplyNull() throws AnalysisException {
		new AnalysisProcessorCmd().apply(null);
		assertNotNull(outContent.toString());
	}
}
