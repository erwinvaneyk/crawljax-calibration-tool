package suite.analysis;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.io.FileUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import suite.distributed.DatabaseUtils;
import suite.distributed.results.WebsiteResult;
import static org.mockito.Mockito.*;

import com.crawljax.core.state.duplicatedetection.NearDuplicateDetection;
import com.google.common.collect.ImmutableList;

@Slf4j
public class TestAnalysisProcessorCsv {

	private static File DIR;

	@BeforeClass
	public static void beforeClass() {
		DIR = new File(System.getProperty("user.dir") + "/output/temp-test/");
		DIR.mkdir();

	}

	// @AfterClass
	public static void afterClass() {
		try {
			FileUtils.deleteDirectory(DIR);
		} catch (IOException e) {
			log.error("Error while deleting temp-directory." + e.getMessage());
		}
	}

	@Test(expected = NullPointerException.class)
	// temp
	public void testApply() {
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
		apc.setOutputDir(DIR);
		apc.apply(analysis);
		File f = new File(DIR + "/" + apc.getFilename());
		assertTrue(f.exists());
	}

	@Test
	public void testApplyInvalidMetrics() {
		Collection<WebsiteResult> websites = new ArrayList<WebsiteResult>();
		websites.add(mock(WebsiteResult.class));
		Analysis analysis =
		        new Analysis("Test-analysis-invalid-metrics", websites,
		                new ImmutableList.Builder<Metric>().build());
		AnalysisProcessorCsv apc = new AnalysisProcessorCsv("Test-analysis-invalid-metrics");
		apc.setOutputDir(DIR);
		apc.apply(analysis);
		File f = new File(DIR + "/" + apc.getFilename());
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
