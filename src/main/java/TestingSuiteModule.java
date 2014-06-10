package main.java;

import java.util.ArrayList;
import java.util.List;

import main.java.analysis.AnalysisBuilderImpl;
import main.java.analysis.AnalysisBuilder;
import main.java.distributed.ConnectionManagerOrmImpl;
import main.java.distributed.ConnectionManager;
import main.java.distributed.ConnectionManagerOrm;
import main.java.distributed.configuration.ConfigurationDaoImpl;
import main.java.distributed.configuration.ConfigurationDao;
import main.java.distributed.results.ResultProcessor;
import main.java.distributed.results.ResultProcessorImpl;
import main.java.distributed.workload.WorkloadDao;
import main.java.distributed.workload.WorkloadDaoImpl;

import com.crawljax.core.state.duplicatedetection.DuplicateDetectionModule;
import com.crawljax.core.state.duplicatedetection.FeatureShingles;
import com.crawljax.core.state.duplicatedetection.FeatureType;
import com.google.inject.AbstractModule;

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

}
