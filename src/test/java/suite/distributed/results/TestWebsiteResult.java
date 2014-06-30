package suite.distributed.results;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import suite.distributed.workload.WorkTask;

public class TestWebsiteResult {

	@Test
	public void testWebsiteResult() {
		WebsiteResult wr = new WebsiteResult("JSON RESULT",4.0F);
		wr.setId(42);
		List<StateResult> stateResults = new ArrayList<StateResult>();
		wr.setStateResults(stateResults);
		WorkTask wt = mock(WorkTask.class);
		wr.hashCode();
		wr.setWorkTask(wt);
		assertEquals(42, wr.getId());
		assertEquals(4.0F, wr.getDuration(), 0.01);
		assertEquals("JSON RESULT", wr.getJsonResults());
		assertEquals(wt, wr.getWorkTask());
		assertEquals(stateResults, wr.getStateResults());
		assertNotNull(wr.toString());
	}

	@Test
	public void testWebsiteResultEquals() {
		WebsiteResult wr1 = new WebsiteResult("JSON RESULT",4.0F);
		WebsiteResult wr2 = new WebsiteResult("JSON RESULT",4.0F);
		WebsiteResult wr3 = new WebsiteResult();
		assertTrue(wr1.equals(wr2));
		assertTrue(wr1.equals(wr1));
		assertFalse(wr1.equals(wr3));
		assertFalse(wr1.equals(null));
	}
}
