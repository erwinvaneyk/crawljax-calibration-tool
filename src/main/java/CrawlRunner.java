package main.java;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;

import main.java.analysis.Analysis;
import main.java.analysis.AnalysisException;
import main.java.analysis.AnalysisFactory;
import main.java.analysis.AnalysisProcessorCmd;
import main.java.analysis.AnalysisProcessorCsv;
import main.java.analysis.AnalysisProcessorFile;
import main.java.analysis.SpeedMetric;
import main.java.analysis.StateAnalysisMetric;
import main.java.distributed.ConnectionManager;
import main.java.distributed.DatabaseUtils;
import main.java.distributed.IConnectionManager;
import main.java.distributed.configuration.ConfigurationDAO;
import main.java.distributed.configuration.ConfigurationIni;
import main.java.distributed.configuration.IConfigurationDAO;
import main.java.distributed.results.IResultProcessor;
import main.java.distributed.results.ResultProcessor;
import main.java.distributed.results.ResultProcessorException;
import main.java.distributed.results.UploadResult;
import main.java.distributed.workload.IWorkloadDAO;
import main.java.distributed.workload.WorkloadDAO;
import main.java.distributed.workload.WorkTask;
import main.java.distributed.workload.WorkloadRunner;

import com.crawljax.cli.JarRunner;

public class CrawlRunner {
	String[] additionalArgs = null;
	DatabaseUtils dbUtils;

	public static void main(String[] args) {
		// Header
		System.out.println("Crawljax Functional Testing Suite");
		System.out.println("---------------------------------");
		System.out.println("Crawljax CLI details:");
		JarRunner.main(new String[] {"--version"});
		System.out.println("---------------------------------");
		
		// Do stuff
		new CrawlRunner(args);
		// Finish up.
		System.out.println("Finished.");
	}
	
	public CrawlRunner(String[] args) {
		dbUtils = new DatabaseUtils(new ConnectionManager());
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
			dbUtils.actionFlushWebsitesFile();
		} else if (arg.equals("-s") || arg.equals("--settings")) {
			dbUtils.actionFlushSettingsFile();
		} else if (arg.equals("-d") || arg.equals("--distributor")) {
			actionDistributor(additionalArgs);
		} else if (arg.equals("-l") || arg.equals("--local")) {
			actionLocalCrawler();
		} else if (arg.equals("-a") || arg.equals("--analyse")) {
			actionAnalysis(true);
		} else if (arg.equals("-c") || arg.equals("--controler")) {
			actionAnalysis(false);
		} else {
			actionHelp();
		}
	}
	
	private void actionHelp() {
		Options options = new Options();
		options.addOption("w","worker", false, "Setup computer as slave/worker, polling the db continuously.");
		options.addOption("f","flush", false, "Flushes the website-file to the server. Nothing is crawled.");
		options.addOption("s","settings", false, "Flushes setting-file to the server. Nothing is crawled.");
		options.addOption("d","distributor", false, "Runs the commandline interface of the distributor.");
		options.addOption("l","local", false, "Do not use server-functionality. Read the website-file and crawl all.");
		options.addOption("c", "controller", false, "Run the analysis only as  controller, so that system will not help crawling");
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("Crawljax-testing-suite CLI", options);
	}

	private void actionWorker() {
		try {
			IConnectionManager conn = new ConnectionManager();
			IResultProcessor resultprocessor = new ResultProcessor(new UploadResult(conn));
			CrawlManager suite = new CrawlManager();
			IWorkloadDAO workload = new WorkloadDAO(conn);
			IConfigurationDAO config = new ConfigurationDAO(conn);
			while (true) {
				// Get worktasks
				List<WorkTask> workTasks = workload.retrieveWork(1);
				while(workTasks.isEmpty()) {
					workTasks = workload.retrieveWork(1);
					if (workTasks.isEmpty()) {
						if(additionalArgs.length >= 1 && additionalArgs[0].equals("-finish")) return;
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
						boolean hasNoError = suite.runCrawler(task.getURL(), dir, args);
						if(!hasNoError) {
							throw new Exception("Crawljax returned an error code");
						}
						try {
							resultprocessor.uploadResults(task.getId(), dir.toString(), new Date().getTime() - timeStart);
						} catch(ResultProcessorException e) {
							System.out.println(e.getMessage());
						}
						workload.checkoutWork(task);
						System.out.println("crawl: " + task.getURL() + " completed");
					} catch (Exception e) {
						System.out.println(e.getMessage());
						workload.revertWork(task.getId());						
						System.out.println("crawl: " + task.getURL() + " failed. Claim reverted.");
					}
				}
			}
		} catch (InterruptedException e) {
			System.out.println("Sleep interrupted; worker stopped.");
		} 
	}
	
	private void actionLocalCrawler() {
		System.out.println("Started local crawler");
		try {
			CrawlManager suite = new CrawlManager();
			suite.websitesFromFile(new File(ConfigurationIni.DEFAULT_SETTINGS_DIR + "/websites.txt"));
			suite.crawlWebsites();
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}
	
	private void actionDistributor(String[] args) {
		System.out.println("Distributor CLI:");
		WorkloadRunner.main(args);
	}
	
	private void actionAnalysis(boolean helpCrawling) {
		try {
			// Build factory
			AnalysisFactory factory = new AnalysisFactory();
			factory.addMetric(new SpeedMetric());
			factory.addMetric(new StateAnalysisMetric());
			
			// Generate report
			Analysis analysis = factory.getAnalysis("analysis",new int[]{2}, helpCrawling);

			new AnalysisProcessorFile().apply(analysis);
			new AnalysisProcessorCsv("test").apply(analysis);
			// Output to cmd
			new AnalysisProcessorCmd().apply(analysis);
		} catch (AnalysisException e) {
			System.out.println(e.getMessage());
		}
	}
}
