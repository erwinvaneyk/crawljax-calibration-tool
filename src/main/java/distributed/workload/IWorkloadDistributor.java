package main.java.distributed.workload;

import java.util.ArrayList;

/**
 * The WorkloadDistributor is responsible for managing the workload of the clients. 
 * It should enable to send/retrieve/claim/unclaim/finish urls.
 * 
 * A url or workunit can have three different states:
 * - unclaimed/Available:	The url has not been crawled and has no worker assigned.
 * - claimed:				A worker has been assigned, but has not finished crawling.
 * - Checked out/finished:	A worker has been assigned and has finished the crawling.
 *
 */
public interface IWorkloadDistributor {
	
	/**
	 * Retrieve and claim a number of urls (if nothing is available, an empty ArrayList is returned).
	 * @param maxcount the maximum number of urls to retrieve.
	 * @return a list with claimed urls
	 */
	public ArrayList<WorkTask> retrieveWork(int maxcount);
	
	/**
	 * Registering a succesful crawl.
	 * @param url The url to be checked out.
	 * @return true if checkout was succesful, else false. 
	 */
	public boolean checkoutWork(String url);
	
	/**
	 * Submit a new url/workunit to the queue/server/container to crawl.
	 * @param url the url to be crawled
	 * @return true if no errors occurred, else false.
	 */
	public boolean submitWork(String url);
	
	/**
	 * Reverts previously checked out or claimed work to the available state.
	 * @param url the url to be reverted
	 * @return true if successful, else false.
	 */
	public boolean revertWork(String url);
	
	/**
	 * Attempts to retrieve a number of urls. If nothing is available sleep and try again.
	 * @param maxcount the maximum number of urls to retrieve.
	 * @param sleepMilisecs the number of milliseconds to sleep
	 * @return a list with claimed urls
	 * @throws InterruptedException thrown when the worker is unexpectedly awakened
	 */
	public ArrayList<WorkTask> retrieveWork(int maxcount, int sleepMilisecs) throws InterruptedException;
	
}
