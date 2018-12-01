package gateways;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mysql.jdbc.Statement;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.Author;
import singleton.ViewManager;

public class AuthorTableGateway 
{
	private static Logger logger = LogManager.getLogger();
	private Connection conn = null;
	private ResultSet rs = null;
	private PreparedStatement prepStatement = null;
	
	public AuthorTableGateway(Connection conn) {
		this.conn = conn;
	}
	/***********************************************************
	* Gets all authors from the database, stores them in a list 
	* and returns that list
	**************************************************************/
	public ObservableList<Author> getAuthors()
	{
		ObservableList<Author> authorList = FXCollections.observableArrayList();
		Author author;
		try
		{
			prepStatement = conn.prepareStatement("select * from Author");
			rs = prepStatement.executeQuery();
			while (rs.next())
			{
				author = new Author();
				author.setId(rs.getInt("id"));
				author.setFirstName(rs.getString("first_name"));
				author.setLastName(rs.getString("last_name"));
				author.setDateOfBirth(rs.getDate("dob").toLocalDate());
				author.setGender(rs.getString("gender"));
				author.setWebsite(rs.getString("web_site"));
				
				authorList.add(author);
			}
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
		return authorList;
	}
	/*******************************************
	* Gets an author from the database based on
	* an id.
	*********************************************/
	public Author getAuthorByID(int authorId)
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
				author.setWebsite(rs.getString("web_site"));
			}
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
		return author;
	}
	/*********************************************
	** 
	* 
	************************************************/
	public void saveAuthor(Author author)
	{
		logger.info("Inserting a new author");
		try {
			String query = "insert into Author (first_name, last_name, dob, gender, web_site) values (?, ?, ?, ?, ?)";
			prepStatement = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
			prepStatement.setString(1, author.getFirstName());
			prepStatement.setString(2, author.getLastName());
			prepStatement.setDate(3, Date.valueOf(author.getDateOfBirth()));
			prepStatement.setString(4, author.getGender());
			prepStatement.setString(5, author.getWebsite());
			prepStatement.execute();
			// Get ID of the just inserted author model
			ResultSet key = prepStatement.getGeneratedKeys();
			if (key.next())
				// Store the id in the author model
				author.setId(key.getInt(1));
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
	}
	/*****************************************************************************
	* Deletes an author from the database
	* @param authorId - the id of the author we want to delete from the database
	******************************************************************************/
	public void deleteAuthor(int authorId)
	{
		logger.info("In Delete Author");
		try 
		{
			prepStatement = conn.prepareStatement("delete from Author where id = ?");
			prepStatement.setInt(1, authorId);
			prepStatement.executeUpdate();
		}
		catch (SQLException e) {
			e.printStackTrace();
			ViewManager.getInstance().showErrAlert(e.getMessage());
		}
	}
	public void updateAuthor(Author ogAuthor, Author updatedAuthor)
	{
		logger.info("Updating an author");
		try {
			prepStatement = conn.prepareStatement("update Author set first_name = ?, last_name = ?, dob = ?, gender = ?, web_site = ? where id = ?");
			// Update the author record
			prepStatement.setString(1, updatedAuthor.getFirstName());
			prepStatement.setString(2, updatedAuthor.getLastName());
			prepStatement.setDate(3, Date.valueOf(updatedAuthor.getDateOfBirth()));
			prepStatement.setString(4, updatedAuthor.getGender());
			prepStatement.setString(5, updatedAuthor.getWebsite());
			prepStatement.setInt(6, ogAuthor.getId());
			prepStatement.executeUpdate();
		}
		catch (SQLException e){
			e.printStackTrace();
		}
	}
}
