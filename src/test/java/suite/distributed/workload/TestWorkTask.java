package suite.distributed.workload;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.net.MalformedURLException;
import java.net.URL;

import org.junit.Test;

import suite.distributed.workload.WorkTask;

public class TestWorkTask {

	@Test
	public void testWorkTask() throws MalformedURLException {
		URL url = new URL("http://test.com");
		WorkTask wt = new WorkTask(1, url);
		assertEquals(wt.getId(), 1);
		assertEquals(wt.getURL(), url);
	}

	@Test
	public void testWorkTask2() throws MalformedURLException {
		WorkTask wt = new WorkTask();
		wt.setCrawled(true);
		wt.setWorker("henk");
		assertEquals(wt.getWorker(), "henk");
		assertTrue(wt.isCrawled());
	}

	@Test
	public void testEqualsObjectSame() throws MalformedURLException {
		WorkTask wt = new WorkTask(1, new URL("http://test.com"));
		WorkTask wt2 = new WorkTask(1, new URL("http://test.com"));
		assertTrue(wt.equals(wt2));
	}

	@Test
	public void testEqualsObjectNull() throws MalformedURLException {
		WorkTask wt = new WorkTask(1, new URL("http://test.com"));
		WorkTask wt2 = null;
		assertFalse(wt.equals(wt2));
	}

	@Test
	public void testEqualsObjectDiffId() throws MalformedURLException {
		WorkTask wt = new WorkTask(1, new URL("http://test.com"));
		WorkTask wt2 = new WorkTask(2, new URL("http://test.com"));
		assertFalse(wt.equals(wt2));
	}

	@Test
	public void testEqualsObjectDiffUrl() throws MalformedURLException {
		WorkTask wt = new WorkTask(1, new URL("http://test.com"));
		WorkTask wt2 = new WorkTask(1, new URL("http://notest.com"));
		assertFalse(wt.equals(wt2));
	}

	@Test
	public void testEqualsObjectAllDif() throws MalformedURLException {
		WorkTask wt = new WorkTask(1, new URL("http://test.com"));
		WorkTask wt2 = new WorkTask(2, new URL("http://notest.com"));
		assertFalse(wt.equals(wt2));
	}

	@Test(expected = RuntimeException.class)
	public void testGetUrlBadUrl() throws MalformedURLException {
		WorkTask wt = new WorkTask(1, "Infjdhlafijiwjcd338428%%667><");
		wt.getURL();
	}

	@Test
	public void testHashcode() throws MalformedURLException {
		WorkTask wt = new WorkTask(1, "http://test.com");
		wt.hashCode();
	}

}