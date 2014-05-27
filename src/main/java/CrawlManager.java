package main.java;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;

import main.java.distributed.configuration.ConfigurationIni;
import main.java.distributed.configuration.IConfigurationDAO;

import org.apache.commons.validator.routines.UrlValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.crawljax.core.CrawljaxRunner;
import com.crawljax.core.ExitNotifier.ExitStatus;
import com.crawljax.core.configuration.CrawljaxConfiguration;

/**
 * SuiteManager is responsible for running the actual crawler. Therefore it 
 * deals with the arguments needed by the CrawlJax CLI and websites to be crawled.
 */
public class CrawlManager {

	final Logger logger = LoggerFactory.getLogger(this.getClass());

	static final File DEFAULT_OUTPUT_DIR = new File(System.getProperty("user.dir") + "/output");
	static final String ARG_WEBSITE = "website";
	static final String ARG_OUTPUTDIR = "outputdir";
	
	private Queue<String> websiteQueue = new PriorityQueue<String>();

	/**
	 * Reads a file containing website (1 website per line) and adds them to the queue.
	 * @param websitesPath the path to the file containing websites.
	 * @throws IOException the file could not be found.
	 */
	public void websitesFromFile(File websitesPath) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(websitesPath.toString()));
		UrlValidator urlValidator = new UrlValidator();
		String line;
		while ((line = br.readLine()) != null) {
			if(urlValidator.isValid(line))
				websiteQueue.add(new URL(line).toString());
			else
				logger.warn("Website: " + line + " is an invalid url. Ignoring website.");
		}
		br.close();
		logger.info("Website-queue loaded.");
	}

	/**
	 * Crawl all websites in the queue.
	 * @return the outputdirs of the crawled websites
	 */
	public List<File> crawlWebsites() {
		List<File> outputdirs = new ArrayList<File>();
		try {
			IConfigurationDAO config = new ConfigurationIni();
			while(!websiteQueue.isEmpty()) {
				URL website = new URL(websiteQueue.poll());
				Map<String,String> args = config.getConfiguration(website.toString());
				File outputDir = generateOutputDir(website);
				runCrawler(website, outputDir, args);
				outputdirs.add(outputDir);
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return outputdirs;
	}


	/**
	 * Generate a unique outputdir-name for a website
	 * @param website the website needing a outputdir
	 * @return unique name for the dir
	 * @throws URISyntaxException website contains an invalid syntax
	 * @throws MalformedURLException 
	 */
	public static File generateOutputDir(URL website) {
		Date date= new Date();
		Timestamp timestamp = new Timestamp(date.getTime());
		return new File(DEFAULT_OUTPUT_DIR + "/" + website.getHost() + "-" + timestamp.getTime());
	}
	
	
	/**
	 * Run CrawlJax for a given set of args. Output can be found in args.get(ARG_OUTPUTDIR).
	 * @param args arguments which need to be send to crawljax.
	 */
	public boolean runCrawler(URL website, File outputdir, Map<String, String> args) {	
		CrawljaxConfiguration config = new ConfigurationMapper().convert(website, outputdir, args);

		CrawljaxRunner runner = new CrawljaxRunner(config);
		runner.call();
		ExitStatus reason = runner.getReason();
		logger.debug("Finished crawling " + args.get(ARG_WEBSITE) + ". Reason: " + reason.toString());
		return (!reason.equals(ExitStatus.ERROR));
	}
	
	/**
	 * Get website-queue.
	 * @return website-queue.
	 */
	public Queue<String> getWebsiteQueue() {
		return websiteQueue;
	}

	/**
	 * Set Website-queue
	 * @param websiteQueue the new website-queue.
	 */
	public void setWebsiteQueue(Queue<String> websiteQueue) {
		this.websiteQueue = websiteQueue;
	}
	
}
