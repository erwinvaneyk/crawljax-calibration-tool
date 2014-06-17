package suite;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;

import suite.analysis.Analysis;
import suite.analysis.AnalysisBuilder;
import suite.analysis.AnalysisProcessorCsv;
import suite.analysis.SpeedMetric;
import suite.analysis.StateAnalysisMetric;
import suite.crawljax.CrawlManager;
import suite.distributed.DatabaseUtils;
import suite.distributed.configuration.ConfigurationDao;
import suite.distributed.configuration.ConfigurationIni;
import suite.distributed.results.ResultProcessor;
import suite.distributed.results.ResultProcessorException;
import suite.distributed.workload.WorkTask;
import suite.distributed.workload.WorkloadDao;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;

public class CrawlRunner {
	@Inject private Injector injector;
	String[] additionalArgs = null;
	DatabaseUtils dbUtils;
	private ResultProcessor resultProcessor;
	private WorkloadDao workload;
	private CrawlManager crawlManager;
	private ConfigurationDao config;
	private AnalysisBuilder analysisFactory;

	public static void main(String[] args) {
		// Header
		System.out.println("Crawljax Testing Suite");
		System.out.println("---------------------------------");

		// Do stuff
		CrawlRunner cr =
		        Guice.createInjector(new TestingSuiteModule()).getInstance(CrawlRunner.class);
		cr.actOnArgs(args);
		// Finish up.
		System.out.println("Finished.");
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

	public void actOnArgs(String[] args) {
		// Parse Args
		String arg = "";
		if (args.length > 0) {
			arg = args[0];
			additionalArgs = Arrays.copyOfRange(args, 1, args.length);
		}
		// Actions
		if (arg.equals("-w") || arg.equals("--worker")) {
			actionWorker();
		} else if (arg.equals("-f") || arg.equals("--flush")) {
			if(!checkArgumentExists(additionalArgs, 1)) return;
			dbUtils.actionFlushWebsitesFile(new File(args[1]));
		} else if (arg.equals("-s") || arg.equals("--settings")) {
			if(!checkArgumentExists(additionalArgs, 1)) return;
			dbUtils.actionFlushSettingsFile(new File(args[1]));
		} else if (arg.equals("-l") || arg.equals("--local")) {
			if(!checkArgumentExists(additionalArgs, 1)) return;
			actionLocalCrawler(new File(args[1]));
		} else if (arg.equals("-a") || arg.equals("--analyse")) {
			actionAnalysis();
		} else {
			actionHelp();
		}
	}
	
	private boolean checkArgumentExists(String[] args, int count) {
		if(args.length < count) {
			System.out.println("Error: Argument missing");
			return false;
		}
		return true;
	}

	private void actionHelp() {
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
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("Crawljax-testing-suite CLI", options);
	}

	private void actionWorker() {
		try {
			System.out.println("Started client crawler/worker.");
			while (true) {
				// Get worktasks
				List<WorkTask> workTasks = workload.retrieveWork(1);
				while (workTasks.isEmpty()) {
					workTasks = workload.retrieveWork(1);
					if (workTasks.isEmpty()) {
						if (additionalArgs.length >= 1 && additionalArgs[0].equals("-finish"))
							return;
						Thread.sleep(1000 * 10); // sleep 10 seconds
					}
				}

				for (WorkTask task : workTasks) {
					try {
						List<String> sections = new ArrayList<String>();
						sections.add(task.getURL().getHost());
						sections.add(ConfigurationIni.INI_SECTION_COMMON);
						Map<String, String> args = config.getConfiguration(sections);
						File dir = CrawlManager.generateOutputDir(task.getURL());
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
		}
	}

	private void actionLocalCrawler(File websitePath) {
		System.out.println("Started local crawler");
		try {
			crawlManager.websitesFromFileToQueue(websitePath);
			crawlManager.crawlWebsitesFromQueue();
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
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
