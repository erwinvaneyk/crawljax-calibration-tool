package main.java.distributed.results;

import java.io.FileInputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import main.java.distributed.IConnectionManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class UploadResult {
	private static final String TABLE_STATE_RESULTS = "DomResults";
	private static final String COLUMN_ID_WEBSITE = "websiteResult_id";
	private static final String COLUMN_ID_STATE = "stateId";
	private static final String COLUMN_DOM = "dom";
	private static final String COLUMN_STRIPPEDDOM = "strippedDom";
	private static final String COLUMN_SCREENSHOT = "screenshot";
	
	private static final String TABLE_WEBSITE_RESULTS = "WebsiteResults";
	private static final String COLUMN_ID_WORKTASK = "workTask_id";
	private static final String COLUMN_RESULTS_JSON = "jsonResults";
	private static final String COLUMN_DURATION = "duration";
	
	
	private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
	private IConnectionManager con;
	public UploadResult(IConnectionManager con) {
		this.con = con;
	}
	
	public int uploadJson(int id, String fileContent, long duration) throws ResultProcessorException {
		int ret = -1;
		try {
			if (this.tableContainsJson(id)) {
				LOGGER.warn("There already excist a result.json file of this website_id in the database, so this result.json will be discarded");
			} else {
				String sql = "INSERT INTO " + TABLE_WEBSITE_RESULTS + "(" + COLUMN_ID_WORKTASK 
						+ "," + COLUMN_RESULTS_JSON + "," + COLUMN_DURATION + ") VALUES(?,?,?)";
				PreparedStatement statement = con.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
				
				statement.setInt(1, id);
				statement.setString(2, null);
				statement.setFloat(3, duration);
				int insert = statement.executeUpdate();	
				
				if (insert == 1) {
					LOGGER.info("The result.json file is sent to the database");
				} else {
					LOGGER.warn("The result.json file is NOT sent to the database");
					throw new ResultProcessorException("Can not insert the json-file");
				}
				
				// Get generated key
				ResultSet generatedkeys = statement.getGeneratedKeys();
		        if (generatedkeys.next()) {
		            ret = generatedkeys.getInt(1);
		        }
			}
		} catch (SQLException e) {
			LOGGER.error("SQLException: " + e.getMessage());
			throw new ResultProcessorException("SQLException during the upload of the json-file");
		} 
		return ret;
	}
	
	public void uploadDomAction(int id, String fileContent, String stateId) throws ResultProcessorException {
		try {
			
			if (!this.tableContainsTuple(id, stateId)) {
				this.makeTuple(id, stateId);
			}
			this.insertInTuple(COLUMN_DOM, fileContent, id, stateId);
			
		} catch (SQLException e) {
			LOGGER.error("SQLException: " + e.getMessage());
			throw new ResultProcessorException("SQLException during the upload of the json-file");
		}
	}
	
	
	public void uploadStrippedDom(int id, String fileContent, String stateId) throws ResultProcessorException {
		try {
			if (!this.tableContainsTuple(id, stateId)) {
				this.makeTuple(id, stateId);
			}
			
			String update  = "UPDATE " + TABLE_STATE_RESULTS + " SET " + COLUMN_STRIPPEDDOM + "=?, " + " WHERE " + COLUMN_ID_WEBSITE + "=? AND " + COLUMN_ID_STATE + "=?";
			PreparedStatement statement = con.getConnection().prepareStatement(update, Statement.RETURN_GENERATED_KEYS);
			
			statement.setString(1, fileContent);
			statement.setInt(2, id);
			statement.setString(3, stateId);
			
			int updateSt = statement.executeUpdate();
			
			if(updateSt != 1) {
				LOGGER.warn("A problem while inserting a screenshot into the database.");
			}
		} catch (SQLException e) {
			LOGGER.error("SQLException during upload of stripped-dom " + id + ". Message: " + e.getMessage());
			throw new ResultProcessorException("IOException during the upload of a stripped-dom");
		}
	}
	
	public void uploadScreenshotAction(int id, FileInputStream fr, String stateId) throws ResultProcessorException {
		try {
			
			if (!stateId.contains("small")) {
				if (!this.tableContainsTuple(id, stateId)) {
					this.makeTuple(id, stateId);
				}
				String sql = "UPDATE " + TABLE_STATE_RESULTS + " SET " + COLUMN_SCREENSHOT + " = ? WHERE " + COLUMN_ID_WEBSITE + " = ? AND " + COLUMN_ID_STATE + " = ?";
				PreparedStatement prepStat = con.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
				
				prepStat.setBinaryStream(1, fr);
				prepStat.setInt(2, id);
				prepStat.setString(3, stateId);
				
				int result = prepStat.executeUpdate();
				if(result != 1) {
					LOGGER.warn("A problem while inserting a screenshot into the database.");
				}
			}
		} catch (SQLException e) {
			LOGGER.error("SQLException during upload screenshot " + id + ". Message: " + e.getMessage());
			throw new ResultProcessorException("IOException during the upload of a screenshot");
		}
	}
	
	private void makeTuple(int id, String stateId) throws ResultProcessorException {
		try {
			String sql = "INSERT INTO " + TABLE_STATE_RESULTS + "(" + COLUMN_ID_WEBSITE + "," + COLUMN_ID_STATE+") VALUES(?,?)";
			PreparedStatement statement = (PreparedStatement) con.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			
			statement.setInt(1, id);
			statement.setString(2, stateId);
			
			int result = statement.executeUpdate();	
			if(result != 1) {
				LOGGER.info("A problem while insterted a dom in the database");
			}
		
		} catch (SQLException e) {
			LOGGER.error("SQLException: " + e.getMessage());
			throw new ResultProcessorException("SQLException: can not make new tupe with id=" + id + " and StateId=" + stateId);
		}
	}
	
	private boolean tableContainsJson(int id) {
		boolean res = false;
		
		try {
			String select = "SELECT * FROM " + TABLE_WEBSITE_RESULTS + " WHERE " + COLUMN_ID_WORKTASK + " = ?";
			PreparedStatement selectSt = con.getConnection().prepareStatement(select, Statement.RETURN_GENERATED_KEYS);
			selectSt.setInt(1, id);
			
			ResultSet resSet = selectSt.executeQuery();
			
			if (resSet.next()) {
				res = true;
			} else {
				res = false;
			}
		} catch (SQLException e) {
			LOGGER.error("SQLException: It is not possible to check if the table contains a the Json with id=" + id + ". Message: " + e.getMessage());
		}
		
		return res;
	}
	
	private boolean tableContainsTuple(int id, String stateId) throws ResultProcessorException {
		boolean res = false;
		
		try {
			String select = "SELECT * FROM " + TABLE_STATE_RESULTS + " WHERE " + COLUMN_ID_WEBSITE + " = ? AND " + COLUMN_ID_STATE +" = ?";
			PreparedStatement selectSt = con.getConnection().prepareStatement(select, Statement.RETURN_GENERATED_KEYS);
			selectSt.setInt(1, id);
			selectSt.setString(2, stateId);
			
			ResultSet resSet = selectSt.executeQuery();
			if (resSet.next()) {
				res = true;
			} else {
				res = false;
			}
		} catch (SQLException e) {
			LOGGER.error("SQLException: It is not possible to check if the table contains a tuple with id=" + id + " and StateId=" + stateId + ". Message: " + e.getMessage());
			throw new ResultProcessorException("SQLException during the search of excisting tuple");
		}
		return res;
	}
	
	
	private int insertInTuple(String column, String content, int websiteId, String stateId) throws SQLException {
		String update  = "UPDATE " + TABLE_STATE_RESULTS + " SET " + column + " = ? WHERE " + COLUMN_ID_WEBSITE + " = ? AND " + COLUMN_ID_STATE +" = ?";
		PreparedStatement statement = con.getConnection().prepareStatement(update, Statement.RETURN_GENERATED_KEYS);
		
		statement.setString(1, content);
		statement.setInt(2, websiteId);
		statement.setString(3, stateId);
		
		return statement.executeUpdate();
	}
	
	public void closeConnection() {
		con.closeConnection();
	}
}
