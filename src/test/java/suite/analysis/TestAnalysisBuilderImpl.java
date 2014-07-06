package suite.analysis;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import suite.distributed.DatabaseUtils;
import suite.distributed.results.WebsiteResult;
import suite.distributed.workload.WorkTask;
import suite.distributed.workload.WorkloadDao;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;

import static org.junit.Assert.*;
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

	@Test
	public void testGetAnalysis() throws SQLException {
		List<WebsiteResult> results = new ArrayList<WebsiteResult>();
		WebsiteResult websiteResult = mock(WebsiteResult.class);
		WorkTask worktask = mock(WorkTask.class);
		when(websiteResult.getWorkTask()).thenReturn(worktask);
		results.add(websiteResult);

		Dao<WebsiteResult, String> websiteDao = mock(Dao.class);
		QueryBuilder<WebsiteResult, String> builder = mock(QueryBuilder.class);
		Where<WebsiteResult, String> where = mock(Where.class);
		when(websiteDao.queryBuilder()).thenReturn(builder);
		when(builder.where()).thenReturn(where);
		when(builder.query()).thenReturn(results);

		WorkloadDao wl = mock(WorkloadDao.class);
		DatabaseUtils du = mock(DatabaseUtils.class);
		AnalysisBuilderImpl af = new AnalysisBuilderImpl(websiteDao, wl, du);
		af.getAnalysis("MOCK TEST", new int[] { 1, 2, 3 });
		assertNotNull(af.toString());
	}
}
