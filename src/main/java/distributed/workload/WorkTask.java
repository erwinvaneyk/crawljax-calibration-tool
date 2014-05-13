package main.java.distributed.workload;

import java.net.URL;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import lombok.Data;
import lombok.RequiredArgsConstructor;


/**
 * A task for a worker to crawl a website
 */
@DatabaseTable(tableName="workload")
@RequiredArgsConstructor
@Data public class WorkTask {

	@DatabaseField(generatedId=true,canBeNull=false)
	private final int id;
	
	@DatabaseField(canBeNull=false)
	private final URL url;

	@DatabaseField
	private String crawler;

	@DatabaseField
	private boolean crawled;
	
	public WorkTask() {
		id = 0;
		url = null;
	}
}
