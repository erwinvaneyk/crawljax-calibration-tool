package main.java.distributed.results;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

import com.j256.ormlite.field.*;
import com.j256.ormlite.table.DatabaseTable;

/**
 * The stateResult-class stores a single state from a crawl. It stores the DOM, StrippedDOM and
 * additionally the hash. It should not exist without a related websiteResult.
 */
@DatabaseTable(tableName = "DomResults")
@ToString(exclude = { "dom", "strippedDom", "websiteResult", "screenshot" })
@AllArgsConstructor
@Data
public class StateResult {

	@DatabaseField(canBeNull = false, foreign = true, uniqueCombo = true)
	private final WebsiteResult websiteResult;

	@DatabaseField(uniqueCombo = true, canBeNull = false)
	private final String stateId;

	@DatabaseField
	private final String dom;

	@DatabaseField
	private final String strippedDom;

	@DatabaseField
	private final String strippedDomHash;

	@DatabaseField(dataType = DataType.BYTE_ARRAY)
	private final byte[] screenshot;

	public StateResult() {
		websiteResult = null;
		stateId = "";
		dom = "";
		strippedDom = "";
		strippedDomHash = null;
		screenshot = null;
	}

	public int getStrippedDomHash() {
		return (int) Long.parseLong(strippedDomHash, 2);
	}
}
