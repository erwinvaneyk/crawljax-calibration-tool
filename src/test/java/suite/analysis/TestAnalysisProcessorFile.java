package suite.analysis;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import suite.analysis.AnalysisProcessorFile;

public class TestAnalysisProcessorFile {

	private AnalysisProcessorFile apf;

	private static File testDir = new File(System.getProperty("user.dir") + "/output/temp/");

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		testDir.mkdir();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		testDir.delete();
	}

	@Before
	public void setUp() throws Exception {
		apf = new AnalysisProcessorFile();
		apf.setOutputDir(testDir);
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
