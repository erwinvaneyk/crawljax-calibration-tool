package main.java.analysis;

import java.util.Collection;
import lombok.Data;


@Data public class Statistic {
	
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
	
	public boolean hasDetails() {
		return details != null && !details.isEmpty();
	}
}