package suite;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.cli.*;

import suite.analysis.*;
import suite.crawljax.CrawlManager;
import suite.distributed.DatabaseUtils;
import suite.distributed.configuration.ConfigurationDao;
import suite.distributed.results.ResultProcessor;
import suite.distributed.results.ResultProcessorException;
import suite.distributed.workload.WorkTask;
import suite.distributed.workload.WorkloadDao;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;

@Slf4j
public class CrawlRunner {
	private static String NAMESPACE;
	private static final String APPLICATION_NAME = "Crawljax Testing Suite";
	private static final long WAIT_INTERVAL = 1000 * 10;
	
	@Inject private Injector injector;
	private DatabaseUtils dbUtils;
	private ResultProcessor resultProcessor;
	private WorkloadDao workload;
	private CrawlManager crawlManager;
	private ConfigurationDao config;
	private AnalysisBuilder analysisFactory;

	public static void main(String[] args) {
		try {
			// Header
			System.out.println(APPLICATION_NAME + System.lineSeparator() + "---------------------------------");
	
			// Parse commandline
			CommandLine cmd = new BasicParser().parse(buildOptions(), args);	
			NAMESPACE = cmd.hasOption("n") ? cmd.getOptionValue("n") : "";
			
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
		options.addOption("a", "analyse", false,
		        "Run the analysis-manager. This system will not help crawling");
		options.addOption("n", "NAMESPACE", true,
		        "Sets a custom NAMESPACE for the instance. This enables multiple crawl-sessions on a single database");
		options.addOption("noWaiting", false,
		        "Prevents a worker-instance from waiting on new tasks. If no tasks are left, the worker stops.");
		options.addOption("useCommonSettings", false,
		        "Lets a worker use the default settings, as defined by ConfigurationDAO. Generally the common-section.");
		return options;
	}

	@Inject
	public CrawlRunner(ResultProcessor resultprocessor, CrawlManager suite,
	        WorkloadDao workload, ConfigurationDao config, DatabaseUtils dbUtils, 
			AnalysisBuilder factory) {
		this.resultProcessor = resultprocessor;
		this.crawlManager = suite;
		this.workload = workload;
		this.config = config;
		this.dbUtils = dbUtils;
		this.analysisFactory = factory;
	}

	public void actOnArgs(CommandLine cmd) {
		if (cmd.hasOption("worker")) {
			actionWorker(NAMESPACE, cmd.hasOption("noWaiting"), cmd.hasOption("useCommonSettings"));
		} else if (cmd.hasOption("flush")) {
			dbUtils.actionFlushWebsitesFile(new File(cmd.getOptionValue("flush")));
		} else if (cmd.hasOption("settings")) {
	        dbUtils.actionFlushSettingsFile(new File(cmd.getOptionValue("settings")));
		} else if (cmd.hasOption("local")) {
			actionLocalCrawler(new File(cmd.getOptionValue("local")));
		} else if (cmd.hasOption("analysis")) {
			actionAnalysis();
		} else {
			actionHelp();
		}
	}

	private void actionHelp() {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp(APPLICATION_NAME, buildOptions());
	}

	private void actionWorker(String namespace, boolean noWaiting, boolean useDefaults) {
		try {
			System.out.println("Started client crawler/worker.");
			while (true) {
				// Get worktasks
				List<WorkTask> workTasks = workload.retrieveWork(1);
				while (workTasks.isEmpty()) {
					workTasks = workload.retrieveWork(1);
					if (workTasks.isEmpty()) {
						if (noWaiting) return;
						Thread.sleep(WAIT_INTERVAL); // sleep 10 seconds
					}
				}

				for (WorkTask task : workTasks) {
					try {
						List<String> sections = new ArrayList<String>();
						sections.add(task.getURL().getHost());
						if(useDefaults) {
							sections.add(ConfigurationDao.SECTION_COMMON);
							System.out.println("Worker uses default settings.");
						} else {
							sections.add(namespace);
						}
						Map<String, String> args = config.getConfiguration(sections);
						if(args.isEmpty())
							System.out.println("No configuration found for NAMESPACE: \"" + namespace + "\". Using Crawljax' default settings!");
						File dir = crawlManager.generateOutputDir(task.getURL());
						// Crawl
						long timeStart = new Date().getTime();
						boolean hasNoError = crawlManager.runCrawler(task.getURL(), dir, args);
						if (!hasNoError) {
							throw new Exception("Crawljax returned an error code");
						}
						try {
							resultProcessor.uploadResults(task.getId(), dir, new Date().getTime()
							        - timeStart);
						} catch (ResultProcessorException e) {
							System.out.println(e.getMessage());
						}
						workload.checkoutWork(task);
						System.out.println("crawl: " + task.getURL() + " completed");
					} catch (Exception e) {
						System.out.println(e.getMessage());
						workload.revertWork(task.getId());
						System.out
						        .println("crawl: " + task.getURL() + " failed. Claim reverted.");
					}
				}
			}
		} catch (InterruptedException e) {
			System.out.println("Sleep interrupted; worker stopped.");
			log.error("Session interupted, reason: {}", e.getMessage());
		}
	}

	private void actionLocalCrawler(File websitePath) {
		System.out.println("Started local crawler");
		crawlManager.websitesFromFileToQueue(websitePath);
		crawlManager.crawlWebsitesFromQueue();
	}

	private void actionAnalysis() {
		// Add metrics
		analysisFactory.addMetric(injector.getInstance(SpeedMetric.class));
		analysisFactory.addMetric(injector.getInstance(StateAnalysisMetric.class));
		
		// Settings
		Map<String, String> settings = config.getConfiguration("common");
		String threshold = settings.get("threshold");
		String feature = settings.get("feature").replace(";","|");

		// Generate report
		Analysis analysis = analysisFactory.getAnalysis("analysis-" + threshold + "-" + feature, new int[] { 1, 2, 15}); //, 48, 51, 53 });

		// Output results
		new AnalysisProcessorCsv("analysis-results").apply(analysis);
	}
}
