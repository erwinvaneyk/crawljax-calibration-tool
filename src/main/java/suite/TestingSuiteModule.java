package suite;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
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

import com.crawljax.core.state.duplicatedetection.FeatureShingles;
import com.crawljax.core.state.duplicatedetection.FeatureType;
import com.crawljax.core.state.duplicatedetection.HashGenerator;
import com.crawljax.core.state.duplicatedetection.NearDuplicateDetection;
import com.crawljax.core.state.duplicatedetection.NearDuplicateDetectionCrawlhash;
import com.crawljax.core.state.duplicatedetection.XxHashGenerator;
import com.google.common.collect.ImmutableList;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.name.Names;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;

@Slf4j
public class TestingSuiteModule extends AbstractModule {
	
	private String namespace = "";
	
	public TestingSuiteModule(String namespace) {
		this.namespace  = namespace;
		log.info("Namespace used: \"" + namespace + "\"");
	}

	@Override
	protected void configure() {		
		// Use Near-duplicate Detection instance
		List<FeatureType> ft = new ArrayList<FeatureType>();
		ft.add(new FeatureShingles(1, FeatureShingles.SizeType.CHARS));
		bind(NearDuplicateDetection.class).toInstance(
				new NearDuplicateDetectionCrawlhash(1, ImmutableList.copyOf(ft), new XxHashGenerator()));
		bind(HashGenerator.class).to(XxHashGenerator.class);
		
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
		
		// Namespace binding
		bind(String.class).annotatedWith(Names.named("namespace")).toInstance(namespace);
	}

	@Provides
	public Dao<WebsiteResult, String> getWebsiteDao(ConnectionManagerOrm connMgr)
	        throws SQLException {
		return DaoManager.createDao(connMgr.getConnectionORM(), WebsiteResult.class);
	}

}
