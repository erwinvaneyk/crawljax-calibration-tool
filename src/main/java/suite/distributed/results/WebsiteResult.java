package suite.distributed.results;

import java.util.Collection;

import suite.distributed.workload.WorkTask;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import com.j256.ormlite.field.*;
import com.j256.ormlite.table.*;

/**
 * The WebsiteResult-class stores a single website-result from a crawl. It stores the json-results,
 * id and additionally the crawl-duration. It cannot exist without a related websiteResult.
 */
@DatabaseTable(tableName = "WebsiteResults")
@RequiredArgsConstructor
@EqualsAndHashCode(exclude = { "workTask", "stateResults" })
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
	private Collection<StateResult> stateResults;

	public WebsiteResult() {
		jsonResults = null;
		duration = 0;
	}
}
