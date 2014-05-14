package test.java.distributed.workload;

import static org.junit.Assert.*;

import java.net.MalformedURLException;
import java.net.URL;

import main.java.distributed.workload.WorkTask;

import org.junit.Test;

public class TestWorkTask {

	@Test
	public void testWorkTask() throws MalformedURLException {
		URL url = new URL("http://test.com");
		WorkTask wt = new WorkTask(1, url);
		assertEquals(wt.getId(), 1);
		assertEquals(wt.getURL(), url);
	}

	@Test(expected=AssertionError.class)
	public void testWorkTaskInvalidURL() {
		new WorkTask(1,(URL) null);
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

}
