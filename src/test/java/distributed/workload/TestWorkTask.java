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
		assertEquals(wt.getUrl(), url);
	}

	@Test(expected=AssertionError.class)
	public void testWorkTaskInvalidURL() {
		new WorkTask(1, null);
	}

	@Test
	public void testSetId() throws MalformedURLException {
		URL url = new URL("http://test.com");
		WorkTask wt = new WorkTask(1, url);
		assertEquals(wt.getId(), 1);
		wt.setId(42);
		assertEquals(wt.getId(), 42);
	}

	@Test
	public void testSetUrl() throws MalformedURLException {
		URL url = new URL("http://test.com");
		WorkTask wt = new WorkTask(1, url);
		URL newUrl = new URL("http://newtest.com");
		assertEquals(wt.getUrl(), url);
		wt.setUrl(newUrl);
		assertEquals(wt.getUrl(), newUrl);
	}
	
	@Test(expected=AssertionError.class)
	public void testSetInvalidUrl() throws MalformedURLException {
		URL url = new URL("http://test.com");
		WorkTask wt = new WorkTask(1, url);
		URL newUrl = null;
		assertEquals(wt.getUrl(), url);
		wt.setUrl(newUrl);
		assertEquals(wt.getUrl(), newUrl);
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
