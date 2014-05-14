package main.java.distributed.results;

import lombok.Data;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "DomResults")
@Data public class StateResult {
	
	@DatabaseField(canBeNull = false, foreign = true, uniqueCombo = true)
	private final WebsiteResult websiteResult;
	
	@DatabaseField (uniqueCombo = true, canBeNull = false) 
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
}
