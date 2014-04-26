package main.java.distributed.workload;


/**
 * A task for a worker to crawl a website
 */
public class WorkTask {
	private int id;
	private String url;
	
	public WorkTask() {
		id = -1;
		url = "";
	}

	public WorkTask(int id, String url) {
		this.id = id;
		this.url = url;
	}
	
	/**
	 * Set the id of the task.
	 * @param id The identification for the task
	 */
	public void setId(int id) {
		this.id = id;
	}
	
	/**
	 * Set the url for the task.
	 * @param url The website to crawl
	 */
	public void setUrl(String url) {
		this.url = url;
	}
	
	/**
	 * @return The id of the task.
	 */
	public int getId() {
		return id;
	}
	
	/**
	 * @return The url of this task
	 */
	public String getUrl() {
		return url;
	}
	
	/**
	 * @return the string representation of this task.
	 */
	public String toString() {

		return id + ": " + url;
	}
}
