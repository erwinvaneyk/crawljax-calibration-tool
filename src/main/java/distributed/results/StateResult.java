package main.java.distributed.results;

import lombok.Data;
import lombok.NoArgsConstructor;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "DomResults")
@Data public class StateResult {
	
	@DatabaseField(canBeNull = false, foreign = true, uniqueCombo = true)
	private final WebsiteResult websiteResult;
	
	@DatabaseField (uniqueCombo = true, canBeNull = false) 
	private final int stateId;
	
	@DatabaseField
	private final String dom;
	
	@DatabaseField
	private final String strippedDom;
	
	@DatabaseField(dataType = DataType.BYTE_ARRAY)
	private final byte[] screenshot;
	
	public StateResult() {
		websiteResult = null;
		stateId = 0;
		dom = "";
		strippedDom = "";
		screenshot = null;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		StateResult other = (StateResult) obj;
		if (dom == null) {
			if (other.dom != null)
				return false;
		} else if (!dom.equals(other.dom))
			return false;
		if (strippedDom == null) {
			if (other.strippedDom != null)
				return false;
		} else if (!strippedDom.equals(other.strippedDom))
			return false;
		return true;
	}	
}
