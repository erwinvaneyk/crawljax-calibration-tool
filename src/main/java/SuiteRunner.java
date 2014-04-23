package main.java;

import java.io.IOException;

import com.crawljax.cli.JarRunner;

public class SuiteRunner {

	public static void main(String[] args) {
		System.out.println("Crawljax Functional Testing Suite");
		System.out.println("---------------------------------");
		System.out.println("Crawljax CLI details:");
		JarRunner.main(new String[] {"--version"});
		System.out.println("---------------------------------");
		try {
			SuiteManager suite = new SuiteManager();
			//suite.runCrawler("http://demo.crawljax.com", "test");
			suite.crawlWebsites();
		} catch (IOException e) {
			// Settings file could not be retrieved
			e.printStackTrace();
		}
		System.out.println("Finished.");
	}

}
