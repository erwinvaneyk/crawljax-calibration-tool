package suite.distributed.results;

import lombok.*;

import com.j256.ormlite.field.*;
import com.j256.ormlite.table.DatabaseTable;

/**
 * The stateResult-class stores a single state from a crawl. It stores the DOM, StrippedDOM and
 * additionally the hash. It should not exist without a related websiteResult.
 */
@DatabaseTable(tableName = "DomResults")
@ToString(exclude = { "dom", "strippedDom", "websiteResult", "screenshot" })
@RequiredArgsConstructor
@EqualsAndHashCode(exclude= {"strippedDomHash", "screenshot", "strippedDom"})
public class StateResult {

	@DatabaseField(canBeNull = false, foreign = true, uniqueCombo = true)
	@Getter private final WebsiteResult websiteResult;

	@DatabaseField(uniqueCombo = true, canBeNull = false)
	@Getter private final String stateId;

	@DatabaseField
	@Getter private final String dom;

	@DatabaseField
	@Getter private final String strippedDom;

	@DatabaseField
	@Getter private final String strippedDomHash;

	@DatabaseField(dataType = DataType.BYTE_ARRAY)
	@Getter private final byte[] screenshot;

	public StateResult() {
		websiteResult = null;
		stateId = "";
		dom = "";
		strippedDom = "";
		strippedDomHash = null;
		screenshot = null;
	}
}
