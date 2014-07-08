package suite.distributed.results;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import org.junit.Test;

public class TestStateResult {

	@Test
	public void testStateResult() {
		WebsiteResult wr = mock(WebsiteResult.class);
		byte[] image = new byte[10];
		StateResult sr = new StateResult(wr, "state1", "DOM", "SDOM", "0101", image);
		sr.hashCode();
		assertEquals(wr, sr.getWebsiteResult());
		assertEquals("state1", "state1");
		assertEquals("DOM", sr.getDom());
		assertEquals("SDOM", sr.getStrippedDom());
		assertEquals("0101", sr.getStrippedDomHash());
		assertEquals(image, sr.getScreenshot());
		assertNotNull(sr.toString());
	}

	@Test
	public void testEqualsObject() {
		WebsiteResult wr = mock(WebsiteResult.class);
		byte[] image = new byte[10];
		StateResult sr1 = new StateResult(wr, "state1", "DOM", "SDOM", "0101", image);
		StateResult sr2 = new StateResult(wr, "state1", "DOM", "SDOM", "0101", image);
		StateResult sr3 = new StateResult();
		assertTrue(sr1.equals(sr2));
		assertFalse(sr1.equals(null));
		assertFalse(sr1.equals(sr3));
		assertFalse(sr1.equals(""));
	}
}
