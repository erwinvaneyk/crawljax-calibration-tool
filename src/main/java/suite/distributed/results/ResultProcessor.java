package suite.distributed.results;

import java.io.File;

/**
 * ResultProcessorImpl should deal with the results of crawls, sending them to the server.
 */
public interface ResultProcessor {

	/**
	 * Save the resulting JSON file of a crawled website.
	 * 
	 * @param website
	 *            The crawled website that generates the output folder
	 * @param dir
	 *            The directory that contains the output of the crawl
	 * @throws ResultProcessorException
	 */
	public void uploadResults(int id, File dir, long duration);

	public void uploadDom(int id, File dir);

	public void uploadStrippedDom(int id, File dir);

	public void uploadScreenshot(int id, File dir);

	public int uploadJson(int id, File dir, long duration);
}
