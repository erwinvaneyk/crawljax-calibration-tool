package main.java.distributed.results;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import main.java.distributed.workload.WorkTask;

import com.j256.ormlite.dao.*;
import com.j256.ormlite.field.*;
import com.j256.ormlite.table.*;

@DatabaseTable(tableName = "WebsiteResults")
@RequiredArgsConstructor
@Data public class WebsiteResult {
	
	@DatabaseField(canBeNull = false, foreignAutoRefresh=true, foreign = true)
    private WorkTask workTask;
	
	 @DatabaseField(generatedId = true)
    private int id;
	
	@DatabaseField
	private final String jsonResults;
	
	@DatabaseField
	private final float duration;
	
	@ForeignCollectionField(eager = false)
    private ForeignCollection<StateResult> stateResults;
	
	public WebsiteResult() {
		jsonResults = null;
		duration = 0;
	}
}