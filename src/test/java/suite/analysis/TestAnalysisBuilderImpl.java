package suite.analysis;

import org.junit.Test;

import suite.distributed.DatabaseUtils;
import suite.distributed.results.WebsiteResult;
import suite.distributed.workload.WorkloadDao;

import com.j256.ormlite.dao.Dao;

import static org.mockito.Mockito.*;

public class TestAnalysisBuilderImpl {

	@Test(expected = AnalysisException.class)
	public void testGetAnalysisIDsNull() {
		Dao<WebsiteResult, String> websiteDao = mock(Dao.class);
		WorkloadDao wl = mock(WorkloadDao.class);
		DatabaseUtils du = mock(DatabaseUtils.class);
		AnalysisBuilderImpl af = new AnalysisBuilderImpl(websiteDao, wl, du);
		af.getAnalysis("TestGetAnalysisIDsNull", null);
	}

	@Test(expected = AnalysisException.class)
	public void testGetAnalysisBothNull() {
		Dao<WebsiteResult, String> websiteDao = mock(Dao.class);
		WorkloadDao wl = mock(WorkloadDao.class);
		DatabaseUtils du = mock(DatabaseUtils.class);
		AnalysisBuilderImpl af = new AnalysisBuilderImpl(websiteDao, wl, du);
		af.getAnalysis("TestGetAnalysisBothNull", null);
	}

	@Test(expected = AnalysisException.class)
	public void testGetAnalysisIDsEmpty() {
		Dao<WebsiteResult, String> websiteDao = mock(Dao.class);
		WorkloadDao wl = mock(WorkloadDao.class);
		DatabaseUtils du = mock(DatabaseUtils.class);
		AnalysisBuilderImpl af = new AnalysisBuilderImpl(websiteDao, wl, du);
		af.getAnalysis("testGetAnalysisIDsEmpty", new int[] {});
	}

	@Test(expected = AnalysisException.class)
	public void testRetrieveWebsiteResultsByIdNull() {
		Dao<WebsiteResult, String> websiteDao = mock(Dao.class);
		WorkloadDao wl = mock(WorkloadDao.class);
		DatabaseUtils du = mock(DatabaseUtils.class);
		AnalysisBuilderImpl af = new AnalysisBuilderImpl(websiteDao, wl, du);
		af.retrieveWebsiteResultsById(null);
	}

	@Test(expected = AnalysisException.class)
	public void testRetrieveWebsiteResultsByIdEmpty() {
		Dao<WebsiteResult, String> websiteDao = mock(Dao.class);
		WorkloadDao wl = mock(WorkloadDao.class);
		DatabaseUtils du = mock(DatabaseUtils.class);
		AnalysisBuilderImpl af = new AnalysisBuilderImpl(websiteDao, wl, du);
		af.retrieveWebsiteResultsById(new int[] {});
	}
}
