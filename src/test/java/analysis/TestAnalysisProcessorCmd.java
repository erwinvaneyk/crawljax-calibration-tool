package test.java.analysis;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import main.java.analysis.AnalysisException;
import main.java.analysis.AnalysisProcessorCmd;
import main.java.analysis.Analysis;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

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
