package main.java.analysis;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import main.java.distributed.IConnectionManagerORM;
import main.java.distributed.results.WebsiteResult;
import main.java.distributed.DatabaseUtils;
import main.java.distributed.workload.IWorkloadDAO;

/**
 * The AnalysisBuilder is responsible for properly constructing an analysis from benchmarks. *
 */
@Slf4j
public class AnalysisBuilder implements IAnalysisBuilder {

	@Getter
	private ImmutableList<IMetric> metrics = new ImmutableList.Builder<IMetric>()
	        .build();
	private IConnectionManagerORM connMgr;
	private IWorkloadDAO workload;
	private DatabaseUtils dbUtils;

	/**
	 * Add a metric-type to the list of metrics used to analyse the results
	 * 
	 * @param metric
	 */
	public void addMetric(IMetric metric) {
		metrics = new ImmutableList.Builder<IMetric>()
		        .addAll(metrics)
		        .add(metric)
		        .build();
		log.info("Metric added to analysis: " + metric.getMetricName());
	}
	
	@Inject
	public AnalysisBuilder(IConnectionManagerORM connMgr, IWorkloadDAO workload, DatabaseUtils dbUtils) {
		this.connMgr = connMgr;
		this.workload = workload;
		this.dbUtils = dbUtils; 
	}

	/**
	 * Basic factory for constructing Analyses. Retrieves benchmark-results by id, re-crawls the
	 * websites and generates a report. Finally it removes all websiteResults from the DB.
	 * 
	 * @param title
	 *            The (unique) title of the analysis
	 * @param websiteids
	 *            The ids of websiteResults used as benchmarks
	 * @return A completed analysisReport
	 */
	public Analysis getAnalysis(String title, int[] websiteids) {
		List<WebsiteResult> benchmarkedWebsites = retrieveWebsiteResultsById(websiteids);
		if (benchmarkedWebsites.isEmpty())
			log.warn("No websiteResults found for websiteids: " + Arrays.toString(websiteids));
		List<WebsiteResult> testWebsites = updateWebsiteResults(benchmarkedWebsites);
		log.debug("Benchmarked websites have been crawled");
		Analysis analyse = new Analysis(title, benchmarkedWebsites, metrics);
		analyse.runAnalysis(testWebsites);
		log.debug("Results have been analysed");
		removeWebsiteResultsFromDB(testWebsites);
		log.debug("Removed results from database");
		return analyse;
	}

	/**
	 * Retrieves all websites equal to the websiteids.
	 * 
	 * @param websiteids
	 *            ids of the websites to retrieve
	 * @return The list of corresponding websites, if none found return a empty list.
	 * @throws AnalysisException
	 *             Invalid parameters or an sql exception occured
	 */
	@VisibleForTesting
	public List<WebsiteResult> retrieveWebsiteResultsById(int[] websiteids)
	        throws AnalysisException {
		if (websiteids == null || websiteids.length == 0) {
			throw new AnalysisException("Invalid number websiteids provided; should be > 0.");
		}
		try {
			Dao<WebsiteResult, String> websiteResultDAO = DaoManager.createDao(connMgr.getConnectionORM(), WebsiteResult.class);
			QueryBuilder<WebsiteResult, String> builder = websiteResultDAO.queryBuilder();
			Where<WebsiteResult, String> where = builder.where();
			for (int id : websiteids) {
				where.eq("id", id);
			}
			if (websiteids.length > 1) {
				where.or(websiteids.length);
			}
			builder.prepare();
			return builder.query();
		} catch (SQLException e) {
			throw new AnalysisException("SQL exception caught while retrieving websiteResults: "
			        + e.getMessage());
		}
	}

	/**
	 * Given a list of websitesResults, submit and recrawl these websites
	 * 
	 * @param benchmarkedWebsites
	 *            the websiteResults to crawl again
	 * @return a list of websiteResults resulting from the recrawl
	 * @throws AnalysisException
	 *             Invalid parameters or an sql exception occured
	 */
	private List<WebsiteResult> updateWebsiteResults(List<WebsiteResult> benchmarkedWebsites)
	        throws AnalysisException {
		if (benchmarkedWebsites == null || benchmarkedWebsites.isEmpty()) {
			throw new AnalysisException(
			        "Invalid number benchmarkedWebsites provided; should be > 0.");
		}
		List<Integer> newids = resubmitWebsitesForCrawling(benchmarkedWebsites);
		try {
			// Build query to retrieve newly crawled websiteResults.
			Dao<WebsiteResult, String> websiteResultDAO =
			        DaoManager.createDao(connMgr.getConnectionORM(),
			                WebsiteResult.class);
			QueryBuilder<WebsiteResult, String> builder = websiteResultDAO.queryBuilder();
			Where<WebsiteResult, String> where = builder.where();
			for (Integer newid : newids) {
				where.eq("workTask_id", newid);
			}
			where.or(benchmarkedWebsites.size());
			builder.prepare();
			return waitForResults(builder, benchmarkedWebsites.size(), 1000 * 10);
		} catch (SQLException e) {
			throw new AnalysisException("SQL exception caught while re-crawling websiteResults: "
			        + e.getMessage());
		}
	}

	private List<WebsiteResult> waitForResults(QueryBuilder<WebsiteResult, String> builder,
	        int expectedResultCount, int interval) {
		List<WebsiteResult> retrieveTestedWebsites;
		try {
			retrieveTestedWebsites = builder.query();
			log.info("Waiting for crawling to finish...");
			while (retrieveTestedWebsites.size() < expectedResultCount) {
				Thread.sleep(interval);
				retrieveTestedWebsites = builder.query();
			}
		} catch (InterruptedException e) {
			throw new AnalysisException(
			        "Thread was interrupted while waiting for the results of the re-crawls");
		} catch (SQLException e) {
			throw new AnalysisException("SQL exception caught while re-crawling websiteResults: "
			        + e.getMessage());
		}
		return retrieveTestedWebsites;
	}

	private List<Integer> resubmitWebsitesForCrawling(List<WebsiteResult> benchmarkedWebsites) {
		List<Integer> newids = new ArrayList<Integer>(benchmarkedWebsites.size());
		for (WebsiteResult baseWebsite : benchmarkedWebsites) {
			log.debug("Work to submit: {}", baseWebsite.getWorkTask());
			int newId = workload.submitWork(baseWebsite.getWorkTask().getURL(), false);
			newids.add(newId);
			if (newId > -1) {
				log.info("Work submitted: {} (id: {})", baseWebsite.getWorkTask().getURL(), newId);
			} else {
				log.warn("Failed to submit work: {} (id: {})",
				        baseWebsite.getWorkTask().getURL(), newId);
			}
		}
		return newids;
	}

	private void removeWebsiteResultsFromDB(Collection<WebsiteResult> websites) {
		for (WebsiteResult website : websites) {
			dbUtils.deleteAllResultsById(website.getId());
		}
	}
}
