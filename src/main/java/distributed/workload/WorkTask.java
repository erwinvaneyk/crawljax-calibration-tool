package main.java.distributed.workload;

import java.net.MalformedURLException;
import java.net.URL;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import lombok.Data;
import lombok.RequiredArgsConstructor;

/**
 * A task for a worker to crawl a website
 */
@DatabaseTable(tableName = "workload")
@RequiredArgsConstructor
@Data
public class WorkTask {

	@DatabaseField(generatedId = true, canBeNull = false)
	private final int id;

	@DatabaseField(canBeNull = false)
	private final String url;

	@DatabaseField(defaultValue = "", canBeNull = false)
	private String worker;

	@DatabaseField
	private boolean crawled;

	public WorkTask() {
		id = 0;
		url = null;
	}

	public WorkTask(int id, URL url) {
		this.id = id;
		this.url = url.toString();
	}

	public URL getURL() {
		try {
			return new URL(url);
		} catch (MalformedURLException e) {
			throw new RuntimeException("Could not get URL-instance, because " + e.getMessage());
		}
	}
}
