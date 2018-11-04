package gateways;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;

import exceptions.GatewayException;

public class GatewayManager 
{
	private static Logger logger = LogManager.getLogger();
	private Connection conn = null;
	
	public GatewayManager() throws GatewayException
	{
		Properties prop = new Properties();
		FileInputStream fis = null;
		try 
		{
			fis = new FileInputStream("db.properties");
			prop.load(fis);
			fis.close();
			
			MysqlDataSource ds = new MysqlDataSource(); 
			ds.setURL(prop.getProperty("MYSQL_DB_URL"));
			ds.setUser(prop.getProperty("MYSQL_DB_USER"));
			ds.setPassword(prop.getProperty("MYSQL_DB_PASS"));
			
			conn = ds.getConnection();
			logger.info("Connected to database");
		} catch (IOException | SQLException e) {
			e.printStackTrace();
			throw new GatewayException(e);
		}	
	}
	// Closes connection to database
	public void closeConnection() 
	{
		// If we are still connected to the database, we want to log out of it
		if (conn != null)
		{
			try {
				conn.close();
				logger.info("Logged out of database");
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	public Connection getConn() {
		return conn;
	}
}
