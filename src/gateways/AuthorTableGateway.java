package gateways;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import model.Author;

public class AuthorTableGateway 
{
	private static Logger logger = LogManager.getLogger();
	private Connection conn = null;
	private ResultSet rs = null;
	private PreparedStatement prepStatement = null;
	
	public AuthorTableGateway(Connection conn) {
		this.conn = conn;
	}
	public Author getAuthor(int authorId)
	{
		Author author = null;
		try
		{
			prepStatement = conn.prepareStatement("select * from Author where id = ?");
			prepStatement.setInt(1, authorId);
			rs = prepStatement.executeQuery();
			if (rs.next())
			{
				author = new Author();
				author.setId(authorId);
				author.setFirstName(rs.getString("first_name"));
				author.setLastName(rs.getString("last_name"));
				author.setDateOfBirth(rs.getDate("dob").toLocalDate());
				author.setGender(rs.getString("gender"));
				author.setWebsite(rs.getString("website"));
			}
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
		return author;
	}
}
