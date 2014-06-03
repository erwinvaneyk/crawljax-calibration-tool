package main.java.distributed.results;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import main.java.distributed.workload.WorkTask;

import com.j256.ormlite.dao.*;
import com.j256.ormlite.field.*;
import com.j256.ormlite.table.*;

/**
 * The WebsiteResult-class stores a single website-result from a crawl. It stores the json-results, id and
 * additionally the crawl-duration. It cannot exist without a related websiteResult.
 */
@DatabaseTable(tableName = "WebsiteResults")
@RequiredArgsConstructor
@ToString(exclude = { "workTask", "jsonResults", "stateResults" })
@Data
public class WebsiteResult {

	@DatabaseField(canBeNull = false, foreignAutoRefresh = true, foreign = true)
	private WorkTask workTask;

	@DatabaseField(generatedId = true)
	private int id;

	@DatabaseField
	private final String jsonResults;

	@DatabaseField
	private final float duration;

	@ForeignCollectionField(eager = true)
	private ForeignCollection<StateResult> stateResults;

	public WebsiteResult() {
		jsonResults = null;
		duration = 0;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		WebsiteResult other = (WebsiteResult) obj;
		if (Float.floatToIntBits(duration) != Float
		        .floatToIntBits(other.duration))
			return false;
		if (id != other.id)
			return false;
		if (jsonResults == null) {
			if (other.jsonResults != null)
				return false;
		} else if (!jsonResults.equals(other.jsonResults))
			return false;
		return true;
	}
}
