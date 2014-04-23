package main.java.datastore;

import java.sql.*;

// import sql
public class Test 
{
	public static void main(String[] args) {
		  String url = "jdbc:mysql://sql.ewi.tudelft.nl:3306/";
		  String dbName = "crawljaxsuite";
		  String driver = "com.mysql.jdbc.Driver";
		  String userName = "erwin"; 
		  String password = "crawljax";
		  try {
		  Class.forName(driver).newInstance();
		  Connection conn = DriverManager.getConnection(url+dbName,userName,password);
		  
		  conn.close();
		  } catch (Exception e) {
		  e.printStackTrace();
		  }
	}
}