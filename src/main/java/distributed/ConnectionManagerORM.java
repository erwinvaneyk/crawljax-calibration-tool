package main.java.distributed;

import java.sql.SQLException;

import com.j256.ormlite.support.ConnectionSource;

public interface ConnectionManagerOrm extends ConnectionManager {


	public ConnectionSource getConnectionORM() throws SQLException;

}
