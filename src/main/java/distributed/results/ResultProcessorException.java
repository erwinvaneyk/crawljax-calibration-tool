package main.java.distributed.results;

@SuppressWarnings("serial")
/**
 * The class for all the possible exceptions in the ResultProcessor class
 * In order to show the exceptions friendly to the user
 */
public class ResultProcessorException extends RuntimeException {
	
	public ResultProcessorException() {
		super();
	}
	
	public ResultProcessorException(String message) {
		super(message);
	}
}
