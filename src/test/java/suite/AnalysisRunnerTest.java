package suite;

import static org.mockito.Mockito.*;

import org.junit.Test;

import suite.AnalysisRunner;
import suite.analysis.AnalysisBuilder;
import suite.analysis.SpeedMetric;
import suite.analysis.StateAnalysisMetric;
import suite.distributed.configuration.ConfigurationDao;

public class AnalysisRunnerTest {

	@Test
	public void testAnalysisRunner() {
		AnalysisBuilder ab = mock(AnalysisBuilder.class);
		ConfigurationDao cd = mock(ConfigurationDao.class);
		SpeedMetric sm = mock(SpeedMetric.class);
		StateAnalysisMetric am = mock(StateAnalysisMetric.class);
		AnalysisRunner ar = new AnalysisRunner(ab, cd, sm, am);
		ar.getFilenamePrefix();
		ar.setFilenamePrefix("test");
		ar.run();
	}
}
