package suite;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import suite.analysis.AnalysisBuilder;
import suite.analysis.AnalysisBuilderImpl;
import suite.distributed.ConnectionManager;
import suite.distributed.ConnectionManagerOrm;
import suite.distributed.ConnectionManagerOrmImpl;
import suite.distributed.configuration.ConfigurationDao;
import suite.distributed.configuration.ConfigurationDaoImpl;
import suite.distributed.results.ResultProcessor;
import suite.distributed.results.ResultProcessorImpl;
import suite.distributed.results.WebsiteResult;
import suite.distributed.workload.WorkloadDao;
import suite.distributed.workload.WorkloadDaoImpl;

import com.crawljax.core.state.duplicatedetection.DuplicateDetectionModule;
import com.crawljax.core.state.duplicatedetection.FeatureShingles;
import com.crawljax.core.state.duplicatedetection.FeatureType;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;

public class TestingSuiteModule extends AbstractModule {

	@Override
	protected void configure() {
		// Use Near-duplicate Module
		List<FeatureType> ft = new ArrayList<FeatureType>();
		ft.add(new FeatureShingles(1, FeatureShingles.SizeType.CHARS));
		install(new DuplicateDetectionModule(1, ft));

		// Analysis
		bind(AnalysisBuilder.class).to(AnalysisBuilderImpl.class);

		// Distributed
		bind(ConnectionManager.class).to(ConnectionManagerOrmImpl.class);
		bind(ConnectionManagerOrm.class).to(ConnectionManagerOrmImpl.class);

		// Configuration
		bind(ConfigurationDao.class).to(ConfigurationDaoImpl.class);

		// Results
		bind(ResultProcessor.class).to(ResultProcessorImpl.class);

		// Workload
		bind(WorkloadDao.class).to(WorkloadDaoImpl.class);
	}

	@Provides
	public Dao<WebsiteResult, String> getWebsiteDao(ConnectionManagerOrm connMgr)
	        throws SQLException {
		return DaoManager.createDao(connMgr.getConnectionORM(), WebsiteResult.class);
	}

}
