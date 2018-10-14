package gateways;

import model.Publisher;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;

import exceptions.GatewayException;

public class PublisherTableGateway 
{
	private static Logger log = LogManager.getLogger();
	Connection conn = null;
	PreparedStatement prepStatement = null;
	ResultSet rs = null;
	
	public PublisherTableGateway() throws GatewayException
	{
		Properties prop = new Properties();
		FileInputStream fis = null;
		try {
			fis = new FileInputStream("db.properties");
			prop.load(fis);
			fis.close();
			
			MysqlDataSource ds = new MysqlDataSource();
			ds.setURL("MYSQL_DB_URL");
			ds.setUser("MYSQL_DB_USER");
			ds.setPassword("MYSQL_DB_PASS");
			
			conn = ds.getConnection();
			log.info("Connected to database in Publisher Table Gateway");
		}
		catch(IOException | SQLException e) {
			e.printStackTrace();
			throw new GatewayException(e);
		}
	}
	
	public List<Publisher> fetchPublishers()
	{
		List<Publisher> pubList = new ArrayList<Publisher>();
		String query = "select * from Publisher";
		try 
		{
			prepStatement = conn.prepareStatement(query);
			rs = prepStatement.executeQuery();
			while (rs.next())
			{
				Publisher pub = new Publisher();
				pub.setId(rs.getInt("id"));
				pub.setPublisherName(rs.getString("publisher_name"));
				pubList.add(pub);
			}
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
		return pubList;
	}
	public void closeConnection() 
	{
		if (conn != null)
		{
			try {
				conn.close();
				log.info("Logged out of database in Publisher Table Gateway");
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
}
