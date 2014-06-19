package suite.analysis;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import suite.analysis.Analysis;
import suite.analysis.AnalysisException;
import suite.analysis.AnalysisProcessorCsv;
import suite.analysis.Metric;
import suite.analysis.SpeedMetric;
import suite.analysis.StateAnalysisMetric;
import suite.distributed.DatabaseUtils;
import suite.distributed.results.WebsiteResult;
import static org.mockito.Mockito.*;

import com.crawljax.core.state.duplicatedetection.NearDuplicateDetection;
import com.google.common.collect.ImmutableList;

public class TestAnalysisProcessorCsv {

	private static File dir;

	@BeforeClass
	public static void beforeClass() {
		dir = new File(System.getProperty("user.dir") + "/output/temp-test/");
		dir.mkdir();

	}

	//@AfterClass
	public static void afterClass() {
		try {
			FileUtils.deleteDirectory(dir);
		} catch (IOException e) {
			System.out.println("Error while deleting temp-directory." + e.getMessage());
		}
	}

	@Test(expected = NullPointerException.class)
	// temp
	public void testApply() throws IOException {
		Collection<WebsiteResult> websites = new ArrayList<WebsiteResult>();
		websites.add(mock(WebsiteResult.class));
		StateAnalysisMetric stateMetric =
		        new StateAnalysisMetric(mock(DatabaseUtils.class),
		                mock(NearDuplicateDetection.class));
		SpeedMetric speedMetric = new SpeedMetric();
		ImmutableList<Metric> metrics =
		        new ImmutableList.Builder<Metric>().add(stateMetric).add(speedMetric).build();
		Analysis analysis = new Analysis("Test-Analysis", websites, metrics);
		AnalysisProcessorCsv apc = new AnalysisProcessorCsv("Test-Analysis");
		apc.setOutputDir(dir);
		apc.apply(analysis);
		File f = new File(dir + "/" + apc.getFilename());
		assertTrue(f.exists());
	}

	@Test
	public void testApplyInvalidMetrics() throws IOException {
		Collection<WebsiteResult> websites = new ArrayList<WebsiteResult>();
		websites.add(mock(WebsiteResult.class));
		Analysis analysis =
		        new Analysis("Test-analysis-invalid-metrics", websites,
		                new ImmutableList.Builder<Metric>().build());
		AnalysisProcessorCsv apc = new AnalysisProcessorCsv("Test-analysis-invalid-metrics");
		apc.setOutputDir(dir);
		apc.apply(analysis);
		File f = new File(dir + "/" + apc.getFilename());
		assertFalse(f.exists());
	}

	@Test
	public void testGetFilename() {
		String name = "test filename2";
		AnalysisProcessorCsv apc = new AnalysisProcessorCsv(name);
		assertEquals(name + ".csv", apc.getFilename());
	}

	@Test(expected = AnalysisException.class)
	public void testAnalysisProcessorCsvNull() {
		String name = null;
		new AnalysisProcessorCsv(name);
	}

	@Test(expected = AnalysisException.class)
	public void testAnalysisProcessorCsvEmpty() {
		String name = "";
		new AnalysisProcessorCsv(name);
	}

	@Test(expected = NullPointerException.class)
	public void testApplyNull() {
		AnalysisProcessorCsv apc = new AnalysisProcessorCsv("test");
		apc.apply(null);
	}
}
