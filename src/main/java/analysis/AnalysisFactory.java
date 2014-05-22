package main.java.analysis;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import main.java.CrawlRunner;
import main.java.distributed.ConnectionManager;
import main.java.distributed.ConnectionManagerORM;
import main.java.distributed.results.WebsiteResult;
import main.java.distributed.workload.WorkloadDAO;

/**
 * The AnalysisFactory is responsible for properly constructing an analysis from benchmarks. *
 */
@Slf4j
public class AnalysisFactory {
	
	@Getter private ImmutableList<IMetric> metrics
    = new ImmutableList.Builder<IMetric>()
        .build();
	
	/**
	 * Add a metric-type to the list of metrics used to analyse the results
	 * @param metric
	 */
	public void addMetric(IMetric metric) {
		metrics = new ImmutableList.Builder<IMetric>()
			.addAll(metrics)
	        .add(metric)
	        .build();
		log.info("Metric added to analysis: " + metric.getMetricName());
	}
	
	/**
	 * Basic factory for constructing Analyses
	 * @param title The (unique) title of the analysis 
	 * @param websiteids The ids of websiteResults used as benchmarks
	 * @return A completed analysisReport
	 * @throws AnalysisException
	 */
	public Analysis getAnalysis(String title, int[] websiteids) throws AnalysisException {
		List<WebsiteResult> benchmarkedWebsites = retrieveWebsiteResultsById(websiteids);
		if(benchmarkedWebsites.isEmpty()) log.warn("No websiteResults found for websiteids: " + Arrays.toString(websiteids));
		List<WebsiteResult> testWebsites = updateWebsiteResults(benchmarkedWebsites);
		log.debug("Benchmarked websites have been crawled");
		Analysis analyse = new Analysis(title, benchmarkedWebsites, metrics);
		analyse.runAnalysis(testWebsites);
		log.debug("Results have been analysed");
		return analyse;
	}
	
	/**
	 * Retrieves all websites equal to the websiteids.
	 * @param websiteids ids of the websites to retrieve
	 * @return The list of corresponding websites, if none found return a empty list.
	 * @throws AnalysisException Invalid parameters or an sql exception occured
	 */
	public List<WebsiteResult> retrieveWebsiteResultsById(int[] websiteids) throws AnalysisException {
		if(websiteids == null || websiteids.length == 0) {
			throw new AnalysisException("Invalid number websiteids provided; should be > 0.");
		}
		try {
			Dao<WebsiteResult, String> websiteResultDAO = DaoManager.createDao(
					new ConnectionManagerORM().getConnectionORM(), WebsiteResult.class);
			QueryBuilder<WebsiteResult, String> builder = websiteResultDAO.queryBuilder();
			Where<WebsiteResult, String> where = builder.where();
			for(int id : websiteids) {
				where.eq("id",id);
			}
			if(websiteids.length > 1) {
				where.or(websiteids.length);
			}
			builder.prepare();
			return builder.query();
		} catch (SQLException e) {
			throw new AnalysisException("SQL exception caught while retrieving websiteResults: " + e.getMessage());
		}
	}
	
	/**
	 * Given a list of websitesResults, submit and recrawl these websites
	 * @param benchmarkedWebsites the websiteResults to crawl again
	 * @return a list of websiteResults resulting from the recrawl
	 * @throws AnalysisException Invalid parameters or an sql exception occured
	 */
	public List<WebsiteResult> updateWebsiteResults(List<WebsiteResult> benchmarkedWebsites) throws AnalysisException {
		if(benchmarkedWebsites == null || benchmarkedWebsites.isEmpty()) {
			throw new AnalysisException("Invalid number benchmarkedWebsites provided; should be > 0.");
		}
		try {
			Dao<WebsiteResult, String> websiteResultDAO = DaoManager.createDao(new ConnectionManagerORM().getConnectionORM(), WebsiteResult.class);
			WorkloadDAO workload = new WorkloadDAO(new ConnectionManager());
			// get websites
			QueryBuilder<WebsiteResult, String> builder = websiteResultDAO.queryBuilder();
			Where<WebsiteResult, String> where = builder.where();
			for(WebsiteResult baseWebsite : benchmarkedWebsites) {
				log.debug("Work to submit: {}", baseWebsite.getWorkTask());
				int newId = workload.submitWork(baseWebsite.getWorkTask().getURL(), true);
				if(newId > -1) {
					where.eq("workTask_id",newId);
					log.info("Work submitted: {} (id: {})",  baseWebsite.getWorkTask().getURL(), newId);
				} else {
					log.warn("Failed to submit work: {} (id: {})",  baseWebsite.getWorkTask().getURL(), newId);
				}
			}
			// wait until websites have been crawled
			new CrawlRunner(new String[]{"-w","-finish"});
			where.or(benchmarkedWebsites.size());
			builder.prepare();
			List<WebsiteResult> retrieveTestedWebsites = builder.query();
			while(retrieveTestedWebsites.size() < benchmarkedWebsites.size()) {
				Thread.sleep(1000 * 10);
				log.info("Waiting for crawling to finish...");
				retrieveTestedWebsites = builder.query();
			}
			return retrieveTestedWebsites;
		} catch (SQLException e) {
			throw new AnalysisException("SQL exception caught while re-crawling websiteResults: " + e.getMessage());
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return null;
	}
}
