package suite.analysis;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableList;

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
		Metric metric = mock(Metric.class);
		ImmutableList<Metric> metrics = new ImmutableList.Builder<Metric>().add(metric).build();
		ImmutableList<Statistic> stats = new ImmutableList.Builder<Statistic>().build();
		when(analysisReport.getMetrics()).thenReturn(metrics);
		when(analysisReport.getStatistics()).thenReturn(stats);

		new AnalysisProcessorCmd().apply(analysisReport);
		assertNotNull(outContent.toString());
	}

	@Test(expected = NullPointerException.class)
	public void testApplyNull() throws AnalysisException {
		new AnalysisProcessorCmd().apply(null);
		assertNotNull(outContent.toString());
	}
}
