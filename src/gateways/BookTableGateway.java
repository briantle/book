package gateways;

import java.sql.ResultSet;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mysql.jdbc.Statement;
import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;

import exceptions.GatewayException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.Book;
public class BookTableGateway 
{
	private static Logger logger = LogManager.getLogger();
	private Connection conn = null;
	private ResultSet rs = null;
	private PreparedStatement prepStatement = null;
	
	/**
	 * Constructor for BookTableGateway that attempts to connect to
	 * the Book database using login info from a properties file.
	 * 
	 * @throws GatewayException
	 */
	public BookTableGateway() throws GatewayException
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
	/**
	 * 
	 * @param bookToDelete
	 */
	public void deleteBook(Book bookToDelete) {
		logger.info("In Delete Book");
		String query = "delete from Book where id = ?";
		
		try {
			prepStatement = conn.prepareStatement(query);
			prepStatement.setInt(1, bookToDelete.getId());
			prepStatement.executeUpdate();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/**
	 * 
	 * @param bookToSave
	 */
	public void saveBook(Book bookToSave)
	{
		logger.info("In Save Book");
		try 
		{
			String query = "insert into Book (title, summary, year_published, isbn) values (?, ?, ?, ?)";
			prepStatement = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
			prepStatement.setString(1, bookToSave.getTitle());
			prepStatement.setString(2, bookToSave.getSummary());
			prepStatement.setInt(3, bookToSave.getYearPublished());
			prepStatement.setString(4, bookToSave.getIsbn());
			
			prepStatement.execute();
			
			// Get ID of the just inserted book model
			ResultSet key = prepStatement.getGeneratedKeys();
			if (key.next())
				// Store the id in the book model
				bookToSave.setId(key.getInt(1));
			
			// Audit Trail
			query = "select * from Book where id = ?";
			prepStatement = conn.prepareStatement(query);
			prepStatement.setInt(1, bookToSave.getId());
			rs = prepStatement.executeQuery();
			
			if (rs.next()) 
				bookToSave.setLastModified(rs.getTimestamp("last_modified").toLocalDateTime());
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	/**
	 * 
	 * @param bookToUpdate
	 * @throws GatewayException 
	 */
	public void updateBook(Book bookToUpdate) throws GatewayException
	{
		try 
		{
			logger.info("In Update Book");
			String selectQuery = "select * from Book where id = ?";
			String updateQuery = "update Book set title = ?, summary = ?, year_published = ?, isbn = ? where id = ?";
			
			prepStatement = conn.prepareStatement(selectQuery);
			prepStatement.setInt(1, bookToUpdate.getId());
			rs = prepStatement.executeQuery();
			if (rs.next()) 
			{
				prepStatement = conn.prepareStatement(updateQuery);
				
				// 
				if (!bookToUpdate.getLastModified().equals(rs.getTimestamp("last_modified").toLocalDateTime()))
					throw new GatewayException("Book is not up to date! Go back to the book list to get the updated version of the book.");
				
				prepStatement.setString(1, bookToUpdate.getTitle());
				prepStatement.setString(2, bookToUpdate.getSummary());
				prepStatement.setInt(3, bookToUpdate.getYearPublished());
				prepStatement.setString(4, bookToUpdate.getIsbn());
				prepStatement.setInt(5, bookToUpdate.getId());
				prepStatement.executeUpdate();
				
				// Update the last modified date for the book in the detail controller
				selectQuery = "select * from Book where id = ?";
				prepStatement = conn.prepareStatement(selectQuery);
				prepStatement.setInt(1, bookToUpdate.getId());
				rs = prepStatement.executeQuery();
				
				if (rs.next()) 
					bookToUpdate.setLastModified(rs.getTimestamp("last_modified").toLocalDateTime());
				
			}
			// Audit trail
			
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	/**
	 * 
	 * @param id
	 * @return
	 */
	public boolean isBookInDB(int id) {
		String query = "select * from Book where id = ?";
		try {
			prepStatement = conn.prepareStatement(query);
			prepStatement.setInt(1, id);
			rs = prepStatement.executeQuery();
			// Book does not currently exist in database
			if (rs.next() == false)
				return false;
			return true;
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	/**
	 * 
	 * Stores all the book records from the database into a list and then returns that list
	 * so that the book table view can display each book in the view.
	 * 
	 * @return a list of books from the database that will be displayed on the list view
	 */
	public ObservableList<Book> getBooks()
	{
		ObservableList<Book> bookList = FXCollections.observableArrayList();
		try {
			prepStatement = conn.prepareStatement("select * from Book");
			rs = prepStatement.executeQuery();
			
			while(rs.next())
			{
				Book dbBook = new Book();
				dbBook.setId(rs.getInt("id"));
				dbBook.setTitle(rs.getString("title"));
				dbBook.setSummary(rs.getString("summary"));
				dbBook.setYearPublished(rs.getInt("year_published"));
				dbBook.setIsbn(rs.getString("isbn"));
				dbBook.setDateAdded(rs.getTimestamp("date"));
				dbBook.setLastModified(rs.getTimestamp("last_modified").toLocalDateTime());
				
				bookList.add(dbBook);
			}
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
		return bookList;
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
}
