package test.java.analysis;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.IOException;

import main.java.analysis.AnalysisProcessorFile;
import main.java.analysis.Analysis;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestAnalysisProcessorFile {
	
	private AnalysisProcessorFile apf;
	
	private Analysis ap;
	
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
		ap = mock(Analysis.class);
	}
	
	@Test
	public void testApplyNull() {
		apf.apply(null);
		assertNull(apf.getOutput());
	}
}
