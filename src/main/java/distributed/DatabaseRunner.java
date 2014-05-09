package main.java.distributed;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseRunner {

	public static void main(String[] args) {
		try {
			cleanDatabase();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private static void cleanDatabase() throws SQLException {
		IConnectionManager cm = new ConnectionManager();
		Connection conn = cm.getConnection();
		Statement stat = conn.createStatement();
		stat.executeUpdate("DELETE FROM DomResults");
		stat.executeUpdate("DELETE FROM TestResults");
		stat.executeUpdate("DELETE FROM workload");
		conn.close();
	}

}
