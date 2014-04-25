package main.java;

import java.io.IOException;
import java.sql.SQLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;

import main.java.distributed.ResultProcessor;
import main.java.distributed.WorkTask;
import main.java.distributed.WorkloadDistributor;
import main.java.distributed.WorkloadRunner;

import com.crawljax.cli.JarRunner;

public class SuiteRunner {

	public static void main(String[] args) {
		// Header
		System.out.println("Crawljax Functional Testing Suite");
		System.out.println("---------------------------------");
		System.out.println("Crawljax CLI details:");
		JarRunner.main(new String[] {"--version"});
		System.out.println("---------------------------------");
		
		// Do stuff
		new SuiteRunner(args);
		// Finish up.
		System.out.println("Finished.");
	}
	
	public SuiteRunner(String[] args) {
		// Parse Args
		String arg = "";
		String[] additionalArgs = null;
		if (args.length > 0) {
			arg = args[0];
			additionalArgs = Arrays.copyOfRange(args, 1, args.length);
		}
		// Actions
		if (arg.equals("-w") || arg.equals("--worker")) {
			actionWorker();
		} else if (arg.equals("-f") || arg.equals("--flush")) {
			actionFlushFile();
		} else if (arg.equals("-d") || arg.equals("--distributor")) {
			actionDistributor(additionalArgs);
		} else if (arg.equals("-l") || arg.equals("--local")) {
			actionLocalCrawler();
		} else {
			actionHelp();
		}
	}
	
	private void actionHelp() {
		Options options = new Options();
		options.addOption("w","worker", false, "Setup computer as slave/worker, polling the db continuously.");
		options.addOption("f","flush", false, "Flushes the website-file to the server. Nothing is crawled.");
		options.addOption("d","distributor", false, "Runs the commandline interface of the distributor.");
		options.addOption("l","local", false, "Do not use server-functionality. Read the website-file and crawl all.");
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("Cryptotrader CLI", options);
	}

	private void actionWorker() {
		try {
			ResultProcessor resultprocessor = new ResultProcessor();
			SuiteManager suite = new SuiteManager();
			WorkloadDistributor workload = new WorkloadDistributor();

			System.out.println("Started client crawler/worker.");
			while (true) {

				ArrayList<WorkTask> workTasks;
				workTasks = workload.retrieveWork(1, 1000 * 10); //poll server every 10 seconds
				
				try {
					for (WorkTask task : workTasks) {
						HashMap<String, String> args = suite.buildSettings(task.getUrl());
						
						suite.runCrawler(args);
						String dir = args.get(SuiteManager.ARG_OUTPUTDIR);
	
						resultprocessor.uploadOutputJson(task.getId(), dir);
						workload.checkoutWork(task.getUrl());
						System.out.println("crawl: " + task.getUrl() + " completed");
					}
				} catch (URISyntaxException e) {
					System.out.println("Crawl failed: invalid url");
				}
			}
		} catch (InterruptedException e) {
			System.out.println("Sleep interrupted; worker stopped.");
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}

	private void actionFlushFile() {
		try {
			WorkloadDistributor workload = new WorkloadDistributor();
			SuiteManager suite = new SuiteManager();
			
			suite.websitesFromFile(SuiteManager.DEFAULT_SETTINGS_DIR + "/websites.txt");
			String url;
			while((url = suite.getWebsiteQueue().poll()) != null) {
				workload.submitWork(url);
			}
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
		System.out.println("File flushed to server.");
	}
	
	private void actionLocalCrawler() {
		System.out.println("Started local crawler");
		try {
			SuiteManager suite = new SuiteManager();
			suite.websitesFromFile(SuiteManager.DEFAULT_SETTINGS_DIR + "/websites.txt");
			suite.crawlWebsites();
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}
	
	private void actionDistributor(String[] args) {
		System.out.println("Distributor CLI:");
		WorkloadRunner.main(args);
	}
}
