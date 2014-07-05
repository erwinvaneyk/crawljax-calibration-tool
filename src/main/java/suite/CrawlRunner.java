package suite;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.cli.*;

import suite.crawljax.CrawlManager;
import suite.distributed.DatabaseUtils;
import suite.distributed.configuration.ConfigurationDao;
import suite.distributed.results.ResultProcessor;
import suite.distributed.results.ResultProcessorException;
import suite.distributed.workload.WorkTask;
import suite.distributed.workload.WorkloadDao;

import com.google.inject.Guice;
import com.google.inject.Inject;

@Slf4j
public class CrawlRunner {
	private static String NAMESPACE;
	private static final String APPLICATION_NAME = "Crawljax Testing Suite";
	private static final long WAIT_INTERVAL = 1000 * 10;
	
	private DatabaseUtils dbUtils;
	private ResultProcessor resultProcessor;
	private WorkloadDao workload;
	private CrawlManager crawlManager;
	private ConfigurationDao config;

	public static void main(String[] args) {
		try {
			// Header
			System.out.println(APPLICATION_NAME + System.lineSeparator() + "---------------------------------");
	
			// Parse commandline
			CommandLine cmd = new BasicParser().parse(buildOptions(), args);	
			NAMESPACE = cmd.getOptionValue("n");
			
			// Setup Crawlrunner
			CrawlRunner cr =
			        Guice.createInjector(new TestingSuiteModule(NAMESPACE)).getInstance(CrawlRunner.class);
			cr.actOnArgs(cmd);
			
			// Finish up.
			System.out.println("Finished.");
        } catch (ParseException e) {
	        System.out.println("Error while parsing arguments: " + e.getMessage());
        }
	}	

	private static Options buildOptions() {
		Options options = new Options();
		options.addOption("w", "worker", false,
		        "Setup computer as slave/worker, polling the db continuously for work.");
		options.addOption("f", "flush", true,
		        "Flushes the provided website-file to the server. Nothing is crawled.");
		options.addOption("s", "settings", true,
		        "Flushes the provided setting-file to the server. Nothing is crawled.");
		options.addOption("l", "local", true,
		        "Do not use server-functionality. Read the website-file and crawl all.");
		options.addOption("n", "NAMESPACE", true,
		        "Sets a custom NAMESPACE for the instance. This enables multiple crawl-sessions on a single database");
		options.addOption("noWaiting", false,
		        "Prevents a worker-instance from waiting on new tasks. If no tasks are left, the worker stops.");
		return options;
	}

	@Inject
	public CrawlRunner(ResultProcessor resultprocessor, CrawlManager suite,
	        WorkloadDao workload, ConfigurationDao config) {
		this.resultProcessor = resultprocessor;
		this.crawlManager = suite;
		this.workload = workload;
		this.config = config;
	}

	public void actOnArgs(CommandLine cmd) {
		if (cmd.hasOption("worker")) {
			actionWorker(NAMESPACE, cmd.hasOption("noWaiting"));
		} else if (cmd.hasOption("flush")) {
			dbUtils.actionFlushWebsitesFile(new File(cmd.getOptionValue("flush")));
		} else if (cmd.hasOption("settings")) {
	        dbUtils.actionFlushSettingsFile(new File(cmd.getOptionValue("settings")));
		} else if (cmd.hasOption("local")) {
			actionLocalCrawler(new File(cmd.getOptionValue("local")));
		} else {
			actionHelp();
		}
	}

	private void actionHelp() {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp(APPLICATION_NAME, buildOptions());
	}

	private void actionWorker(String namespace, boolean noWaiting) {
		while (true) {
			WorkTask task = getWorkTask(noWaiting);
			if(task == null) return;
			Map<String, String> args = getConfigurationForUrl(task.getURL(), namespace);
			crawlWorkTask(task, args);
			workload.checkoutWork(task);
			System.out.println("crawl: " + task.getURL() + " completed");
		}
	}
	
	private void actionLocalCrawler(File websitePath) {
		crawlManager.websitesFromFileToQueue(websitePath);
		crawlManager.crawlWebsitesFromQueue();
	}
	
	private Map<String, String> getConfigurationForUrl(URL url, String namespace) {
		List<String> sections = new ArrayList<String>();
		sections.add(url.getHost());
		if(namespace != null) {
			sections.add(namespace);
		} else {
			sections.add(ConfigurationDao.SECTION_COMMON);
		}
		return config.getConfiguration(sections);
	}
	
	private WorkTask getWorkTask(boolean noWaiting) {
		WorkTask workTasks = workload.retrieveWork(1).get(0);
		while (workTasks == null) {
			if (noWaiting) return null;
			try {
                Thread.sleep(WAIT_INTERVAL);
            } catch (InterruptedException e) {
    			System.out.println("Sleep interrupted; worker stopped.");
    			log.error("Session interupted, reason: {}", e.getMessage());
    		}
			workTasks = workload.retrieveWork(1).get(0);
		}
		return workTasks;
	}
	
	private void crawlWorkTask(WorkTask task, Map<String, String> args) {
		File dir = crawlManager.generateOutputDir(task.getURL());
		long timeStart = new Date().getTime();
		boolean hasNoError = crawlManager.runCrawler(task.getURL(), dir, args);
		try {
			if (!hasNoError) {
				System.out.println("Crawljax returned an error code.");
				workload.revertWork(task.getId());
			}
			long duration = new Date().getTime() - timeStart;
			resultProcessor.uploadResults(task.getId(), dir, duration);
		} catch (ResultProcessorException e) {
			System.out.println(e.getMessage());
			workload.revertWork(task.getId());
		}
	}
}
