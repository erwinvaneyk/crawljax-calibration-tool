package main.java.distributed.results;

/**
 * ResultProcessor should deal with the results of crawls, sending them to the server. 
 */
public interface IResultProcessor {
	
	/**
	 * Save the resulting JSON file of a crawled wesite.
	 * @param website The crawled website that genarates the output folder
	 * @param dir The directory that contains the output of the crawl 
	 * @throws ResultProcessorException 
	 */
	public void uploadResults(int id, String dir, long duration) throws ResultProcessorException;

	public void uploadDom(int id, String dir) throws ResultProcessorException;
		
	public void uploadStrippedDom(int id, String dir) throws ResultProcessorException;
	
	public void uploadScreenshot(int id, String dir) throws ResultProcessorException;
}
