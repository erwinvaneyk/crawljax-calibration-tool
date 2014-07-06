package suite.crawljax;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.Timestamp;
import java.util.*;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.validator.routines.UrlValidator;

import suite.distributed.configuration.ConfigurationDao;
import suite.distributed.configuration.ConfigurationIni;

import com.crawljax.core.CrawljaxRunner;
import com.crawljax.core.ExitNotifier.ExitStatus;
import com.crawljax.core.configuration.CrawljaxConfiguration;

/**
 * SuiteManager is responsible for running the actual crawler. Therefore it deals with the arguments
 * needed by the CrawlJax-core and websites to be crawled.
 */
@Slf4j
public class CrawlManager {

	static final File DEFAULT_OUTPUT_DIR = new File(System.getProperty("user.dir") + "/output");
	static final String ARG_WEBSITE = "website";
	static final String ARG_OUTPUTDIR = "outputdir";

	private Queue<String> websiteQueue = new PriorityQueue<String>();

	/**
	 * Reads a file containing website (1 website per line) and adds them to the queue.
	 * 
	 * @param websitesPath
	 *            the path to the file containing websites.
	 * @throws IOException
	 *             the file could not be found.
	 */
	public void websitesFromFileToQueue(File websitesPath) {
		try (BufferedReader br = new BufferedReader(new FileReader(websitesPath.toString()))) {
			UrlValidator urlValidator = new UrlValidator();
			String line = br.readLine();
			while (line != null) {
				if (urlValidator.isValid(line)) {
					websiteQueue.add(new URL(line).toString());
				} else {
					log.warn("Website: {} is an invalid url. Ignoring website.", line);
				}
				line = br.readLine();
			}
			log.info("Website-queue loaded.");
		} catch (IOException e) {
			log.error("Reading website-file failed, because {}.", e.getMessage());
		}
	}

	/**
	 * Crawl all websites in the queue.
	 * 
	 * @return the outputdirs of the crawled websites
	 */
	public List<File> crawlWebsitesFromQueue() {
		List<File> outputdirs = new ArrayList<File>(websiteQueue.size());
		ConfigurationDao config = new ConfigurationIni();
		while (!websiteQueue.isEmpty()) {
			String rawUrl = websiteQueue.poll();
			try {
				URL website = new URL(rawUrl);
				Map<String, String> args = config.getConfiguration(website.toString());
				File outputDir = generateOutputDir(website);
				runCrawler(website, outputDir, args);
				outputdirs.add(outputDir);
			} catch (MalformedURLException e) {
				log.error("Invalid URL provided: {}. Continuing with the next url. ", rawUrl);
				log.debug("Exception caught while reading URL: {}", e.getMessage());
			}
		}
		return outputdirs;
	}

	/**
	 * Generate a unique outputdir-name for a website
	 * 
	 * @param website
	 *            the website needing a outputdir
	 * @return unique name for the dir
	 * @throws URISyntaxException
	 *             website contains an invalid syntax
	 * @throws MalformedURLException
	 */
	public File generateOutputDir(URL website) {
		Date date = new Date();
		Timestamp timestamp = new Timestamp(date.getTime());
		return new File(DEFAULT_OUTPUT_DIR + "/" + website.getHost() + "-" + timestamp.getTime());
	}

	/**
	 * Run CrawlJax for a given set of args. Output can be found in args.get(ARG_OUTPUTDIR).
	 * 
	 * @param args
	 *            arguments which need to be send to crawljax.
	 */
	public boolean runCrawler(URL website, File outputdir, Map<String, String> args) {
		CrawljaxConfiguration config =
		        new ConfigurationMapper().convert(website, outputdir, args);

		CrawljaxRunner runner = new CrawljaxRunner(config);
		runner.call();
		ExitStatus reason = runner.getReason();
		log.debug("Finished crawling {}. Reason: {}", args.get(ARG_WEBSITE), reason.toString());
		return !reason.equals(ExitStatus.ERROR);
	}

	/**
	 * Get website-queue.
	 * 
	 * @return website-queue.
	 */
	public Queue<String> getWebsiteQueue() {
		return websiteQueue;
	}

	/**
	 * Set Website-queue
	 * 
	 * @param websiteQueue
	 *            the new website-queue.
	 */
	public void setWebsiteQueue(Queue<String> websiteQueue) {
		this.websiteQueue = websiteQueue;
	}

	@Override
	public String toString() {
		return "CrawlManager [websiteQueue=" + websiteQueue + "]";
	}
}
