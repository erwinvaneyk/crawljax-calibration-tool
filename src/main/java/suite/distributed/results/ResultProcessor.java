package suite.distributed.results;

import java.io.File;

/**
 * ResultProcessorImpl should deal with the results of crawls, sending them to the server.
 */
public interface ResultProcessor {

	/**
	 * Save the resulting JSON file of a crawled website.
	 * 
	 * @param id
	 *            The id of the crawled website that generates the output folder
	 * @param dir
	 *            The directory that contains the output of the crawl
	 * @param duration
	 * 			  The duration of the crawl
	 */
	void uploadResults(int id, File dir, long duration);

	/**
	 * @param id
	 * 			  The id of the website which dom to upload
	 * @param dir
	 * 			  The directory of the output folder
	 */
	void uploadDom(int id, File dir);

	/**
	 * @param id
	 * 			   The id of the website which stripped-dom to upload
	 * @param dir
	 * 			   The directory of the output folder
	 */
	void uploadStrippedDom(int id, File dir);

	/**
	 * @param id
	 * 			   The id of the website which screenshot to upload
	 * @param dir
	 * 			   The directory of the output folder
	 */
	void uploadScreenshot(int id, File dir);

	/**
	 * @param id
	 * 			   The id of the website which screenshot to upload
	 * @param dir
	 * 			   The directory of the output folder
	 * @param duration
	 * 			   The duration of the crawl
	 * @return the new id of the website
	 */
	int uploadJson(int id, File dir, long duration);
}
