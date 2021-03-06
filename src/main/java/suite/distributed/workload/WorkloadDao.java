package suite.distributed.workload;

import java.net.URL;
import java.util.List;

/**
 * The WorkloadDistributor is responsible for managing the workload of the clients. It should enable
 * to send/retrieve/claim/unclaim/finish urls. A url or workunit can have three different states: -
 * unclaimed/Available: The url has not been crawled and has no worker assigned. - claimed: A worker
 * has been assigned, but has not finished crawling. - Checked out/finished: A worker has been
 * assigned and has finished the crawling.
 */
public interface WorkloadDao {

	/**
	 * Retrieve and claim a number of urls (if nothing is available, an empty ArrayList is
	 * returned).
	 * 
	 * @param maxcount
	 *            the maximum number of urls to retrieve.
	 * @return a list with claimed urls
	 */
	List<WorkTask> retrieveWork(int maxcount);

	/**
	 * Registering a succesful crawl.
	 * 
	 * @param id
	 *            The id of the worktask to be checked out.
	 * @return true if checkout was succesful, else false.
	 */
	boolean checkoutWork(WorkTask id);

	/**
	 * Submit a new url/workunit to the queue/server/container to crawl.
	 * 
	 * @param url
	 *            the url to be crawled
	 * @param claim
	 * 			  if true, prevent others from taking this task
	 * @return true if no errors occurred, else false.
	 */
	int submitWork(URL url, boolean claim);

	/**
	 * Reverts previously checked out or claimed work to the available state.
	 * 
	 * @param id
	 *            the id of the website to be reverted
	 * @return true if successful, else false.
	 */
	boolean revertWork(int id);

}
