package suite.analysis;

import java.util.Collection;
import lombok.Data;

/**
 * Data class containing the details a statistic. A statistic always has a name/key and a primary
 * value. Additionally details can be stored in the details-collection.
 */
@Data
public class Statistic {

	private final String name;
	private final String value;
	private final Collection<?> details;

	public Statistic(String name, String value) {
		this.name = name;
		this.value = value;
		this.details = null;
	}

	public Statistic(String name, String value, Collection<?> details) {
		this.name = name;
		this.value = value;
		this.details = details;
	}

	/**
	 * Checks if the current statistic has details.
	 * 
	 * @return true if the collection of details is not empty or false if empty or null.
	 */
	public boolean hasDetails() {
		return details != null && !details.isEmpty();
	}
}