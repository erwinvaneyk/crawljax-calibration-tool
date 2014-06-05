package main.java;

import java.util.ArrayList;
import java.util.List;

import main.java.analysis.AnalysisBuilder;
import main.java.analysis.IAnalysisBuilder;
import main.java.distributed.ConnectionManagerORM;
import main.java.distributed.IConnectionManager;
import main.java.distributed.configuration.ConfigurationDAO;
import main.java.distributed.configuration.IConfigurationDAO;
import main.java.distributed.results.IResultProcessor;
import main.java.distributed.results.ResultProcessor;
import main.java.distributed.workload.IWorkloadDAO;
import main.java.distributed.workload.WorkloadDAO;

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
		bind(IAnalysisBuilder.class).to(AnalysisBuilder.class);
		
		// Distributed
		bind(IConnectionManager.class).to(ConnectionManagerORM.class);
		
		// Configuration
		bind(IConfigurationDAO.class).to(ConfigurationDAO.class);
		
		// Results
		bind(IResultProcessor.class).to(ResultProcessor.class);
		
		// Workload
		bind(IWorkloadDAO.class).to(WorkloadDAO.class);
		
    }

}
