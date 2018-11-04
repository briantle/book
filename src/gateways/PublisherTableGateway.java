package gateways;

import model.Publisher;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class PublisherTableGateway 
{
	private static Logger log = LogManager.getLogger();
	Connection conn = null;
	PreparedStatement prepStatement = null;
	ResultSet rs = null;
	
	public PublisherTableGateway(Connection conn)
	{
		this.conn = conn;
	}
	
	public ObservableList<Publisher> fetchPublishers()
	{
		ObservableList<Publisher> pubList = FXCollections.observableArrayList();
		String query = "select * from Publisher";
		Publisher pub;
		try 
		{
			prepStatement = conn.prepareStatement(query);
			rs = prepStatement.executeQuery();
			while (rs.next())
			{
				pub = new Publisher();
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
}
