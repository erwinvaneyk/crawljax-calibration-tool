package main.java.distributed.results;

public class ResultProcessorException extends Exception {

	/**
	 * The class for all the possible exceptions in the ResultProcessor class
	 * In order to show the exceptions friendly to the user
	 */
	private static final long serialVersionUID = 1452456742552682243L;

	public ResultProcessorException() {
		super();
	}
	
	public ResultProcessorException(String message) {
		super(message);
	}
}
