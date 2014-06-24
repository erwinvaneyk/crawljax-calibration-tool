package suite.analysis;

import static org.junit.Assert.*;

import org.junit.Test;

// Stub test case for exception, for potential future features.
public class TestAnalysisExcp {

	@Test
	public void testAnalysisException() {
		String mess = "mock exception";
		AnalysisException exception = new AnalysisException(mess);
		assertEquals(mess, exception.getMessage());
	}

}
