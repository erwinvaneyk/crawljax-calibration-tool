package main.java.distributed;

import java.sql.SQLException;

import com.j256.ormlite.support.ConnectionSource;

public interface IConnectionManagerORM extends IConnectionManager {


	public ConnectionSource getConnectionORM() throws SQLException;

}
