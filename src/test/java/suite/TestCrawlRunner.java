package suite;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.junit.Test;

import static org.mockito.Mockito.*;
import suite.CrawlRunner;
import suite.crawljax.CrawlManager;
import suite.distributed.configuration.ConfigurationDao;
import suite.distributed.results.ResultProcessor;
import suite.distributed.workload.WorkTask;
import suite.distributed.workload.WorkloadDao;

public class TestCrawlRunner {

	@Test
	public void testWorkerGoodRun() throws MalformedURLException {
		ResultProcessor resultProcessor = mock(ResultProcessor.class);
		WorkloadDao workload = mock(WorkloadDao.class);
		CrawlManager crawlManager = mock(CrawlManager.class);
		ConfigurationDao config = mock(ConfigurationDao.class);

		List<WorkTask> emptyWorktask = new ArrayList<>(1);
		emptyWorktask.add(null);

		List<WorkTask> worktask = new ArrayList<>(5);
		worktask.add(new WorkTask(1, "http://demo.crawljax.com"));
		when(workload.retrieveWork(anyInt())).thenReturn(worktask, emptyWorktask);
		when(config.getConfiguration()).thenReturn(new HashMap<String, String>());
		when(crawlManager.generateOutputDir(new URL("http://demo.crawljax.com"))).thenReturn(
		        new File("testFileWorker"));
		when(
		        crawlManager.runCrawler(new URL("http://demo.crawljax.com"),
		                new File("testFile"), new HashMap<String, String>())).thenReturn(true);
		when(workload.revertWork(anyInt())).thenReturn(true);
		CrawlRunner runner = new CrawlRunner(resultProcessor, crawlManager, workload, config);
		runner.actionWorker("test", true);
	}

	@Test
	public void testWorkerBadRun() throws MalformedURLException {
		ResultProcessor resultProcessor = mock(ResultProcessor.class);
		WorkloadDao workload = mock(WorkloadDao.class);
		CrawlManager crawlManager = mock(CrawlManager.class);
		ConfigurationDao config = mock(ConfigurationDao.class);

		List<WorkTask> emptyWorktask = new ArrayList<>(1);
		emptyWorktask.add(null);

		List<WorkTask> worktask = new ArrayList<>(5);
		worktask.add(new WorkTask(1, "http://demo.crawljax.com"));
		when(workload.retrieveWork(anyInt())).thenReturn(worktask, emptyWorktask);
		when(config.getConfiguration()).thenReturn(new HashMap<String, String>());
		when(crawlManager.generateOutputDir(new URL("http://demo.crawljax.com"))).thenReturn(
		        new File("testFileWorker"));
		when(
		        crawlManager.runCrawler(new URL("http://demo.crawljax.com"),
		                new File("testFile"), new HashMap<String, String>())).thenReturn(false);
		when(workload.revertWork(anyInt())).thenReturn(true);
		CrawlRunner runner = new CrawlRunner(resultProcessor, crawlManager, workload, config);
		runner.actionWorker(null, true);
	}
}
