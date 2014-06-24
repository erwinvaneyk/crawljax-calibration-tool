package suite.analysis;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestAnalysisProcessorFile {

	private AnalysisProcessorFile apf;

	private static File DIR = new File(System.getProperty("user.dir") + "/output/temp/");

	@BeforeClass
	public static void setUpBeforeClass() {
		DIR.mkdir();
	}

	@AfterClass
	public static void tearDownAfterClass() {
		DIR.delete();
	}

	@Before
	public void setUp() {
		apf = new AnalysisProcessorFile();
		apf.setOutputDir(DIR);
	}

	@Test
	public void testOutputDirSet() {
		assertNotNull(apf.getOutputDir());
	}

	@Test
	public void testOutputDirSetGet() {
		File newDir = new File("/test/");
		apf.setOutputDir(newDir);
		assertEquals(apf.getOutputDir(), newDir);
	}

	@Test(expected = NullPointerException.class)
	public void testApplyNull() {
		apf.apply(null);
	}
}
