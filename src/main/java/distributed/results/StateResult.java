package main.java.distributed.results;

import lombok.Data;
import lombok.NoArgsConstructor;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "DomResults")
@Data public class StateResult {
	
	@DatabaseField(canBeNull = false, foreign = true, uniqueCombo = true)
	private final WebsiteResult websiteId;
	
	@DatabaseField (uniqueCombo = true, canBeNull = false) 
	private final int stateId;
	
	@DatabaseField
	private final String dom;
	
	@DatabaseField
	private final String strippedDom;
	
	@DatabaseField(dataType = DataType.BYTE_ARRAY)
	private final byte[] screenshot;
	
	public StateResult() {
		websiteId = null;
		stateId = 0;
		dom = "";
		strippedDom = "";
		screenshot = null;
		
	}
}
