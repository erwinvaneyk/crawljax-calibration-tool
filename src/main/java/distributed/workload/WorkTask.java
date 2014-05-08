package main.java.distributed.workload;

import java.net.URL;


/**
 * A task for a worker to crawl a website
 */
public class WorkTask {
	private int id;
	private URL url;
	
	public WorkTask(int id, URL url) {
		assert url != null;
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
	public void setUrl(URL url) {
		assert url != null;
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
	public URL getUrl() {
		return url;
	}

	/**
	 * @return the string representation of this task.
	 */
	public String toString() {

		return "[" + id + ", " + url + "]";
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		WorkTask other = (WorkTask) obj;
		if (id != other.id)
			return false;
		if (url == null) {
			if (other.url != null)
				return false;
		} else if (!url.equals(other.url))
			return false;
		return true;
	}
	
	
}
