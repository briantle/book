package gateways;

import java.sql.ResultSet;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mysql.jdbc.Statement;

import exceptions.GatewayException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.AuditTrailEntry;
import model.Author;
import model.AuthorBook;
import model.Book;
import model.Publisher;
import singleton.ViewManager;
public class BookTableGateway 
{
	private static Logger logger = LogManager.getLogger();
	private Connection conn = null;
	private ResultSet rs = null;
	private PreparedStatement prepStatement = null;
	
	public BookTableGateway(Connection conn) {
		this.conn = conn;
	}
	/**
	 * 
	 * @param bookToDelete
	 */
	public void deleteBook(Book bookToDelete) 
	{
		logger.info("In Delete Book");
		try {
			prepStatement = conn.prepareStatement("delete from Book where id = ?");
			prepStatement.setInt(1, bookToDelete.getId());
			prepStatement.executeUpdate();
		} catch (SQLException e) {
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
			// Insert book into database
			String query = "insert into Book (title, summary, year_published, publisher_id, isbn) values (?, ?, ?, ?, ?)";
			prepStatement = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
			prepStatement.setString(1, bookToSave.getTitle());
			prepStatement.setString(2, bookToSave.getSummary());
			prepStatement.setInt(3, bookToSave.getYearPublished());
			prepStatement.setInt(4, bookToSave.getPub().getId());
			prepStatement.setString(5, bookToSave.getIsbn());
			prepStatement.execute();
			// Get ID of the just inserted book model
			ResultSet key = prepStatement.getGeneratedKeys();
			if (key.next())
				// Store the id in the book model
				bookToSave.setId(key.getInt(1));
			
			// Set the last modified date for version control
			prepStatement = conn.prepareStatement("select * from Book where id = ?");
			prepStatement.setInt(1, bookToSave.getId());
			rs = prepStatement.executeQuery();
			
			if (rs.next()) 
				bookToSave.setLastModified(rs.getTimestamp("last_modified").toLocalDateTime());
			logger.info("Saved " + bookToSave.getTitle());
			
			// Audit Trail
			prepStatement = conn.prepareStatement("insert into BookAuditTrail (book_id, entry_msg) values (?, ?)");
			prepStatement.setInt(1, bookToSave.getId());
			prepStatement.setString(2, "Book Added");
			prepStatement.execute();
			logger.info("Created Audit Trail for " + bookToSave.getTitle());
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	/**
	 * 
	 * @param bookToUpdate
	 * @throws GatewayException 
	 */
	public void updateBook(Book ogBook, Book bookToUpdate, String errMsg) throws GatewayException
	{
		try 
		{
			logger.info("In Update Book");
			String selectQuery = "select * from Book where id = ?";
			String updateQuery = "update Book set title = ?, summary = ?, year_published = ?, publisher_id = ?, isbn = ? where id = ?";
			
			prepStatement = conn.prepareStatement(selectQuery);
			prepStatement.setInt(1, bookToUpdate.getId());
			rs = prepStatement.executeQuery();
			// Our query returned back a result
			if (rs.next()) 
			{
				prepStatement = conn.prepareStatement(updateQuery);
				// We are trying to update an out of date book
				if (!bookToUpdate.getLastModified().equals(rs.getTimestamp("last_modified").toLocalDateTime()))
					throw new GatewayException(errMsg);
				// Update the book record
				prepStatement.setString(1, bookToUpdate.getTitle());
				prepStatement.setString(2, bookToUpdate.getSummary());
				prepStatement.setInt(3, bookToUpdate.getYearPublished());
				prepStatement.setInt(4, bookToUpdate.getPub().getId());
				prepStatement.setString(5, bookToUpdate.getIsbn());
				prepStatement.setInt(6, bookToUpdate.getId());
				prepStatement.executeUpdate();
				
				// Update the last modified date for the book in the detail controller
				selectQuery = "select * from Book where id = ?";
				prepStatement = conn.prepareStatement(selectQuery);
				prepStatement.setInt(1, bookToUpdate.getId());
				rs = prepStatement.executeQuery();
				
				if (rs.next()) 
					bookToUpdate.setLastModified(rs.getTimestamp("last_modified").toLocalDateTime());
				
				/**************** Audit Trail *********************/
				// If the title has been changed
				if (ogBook.getTitle().compareTo(bookToUpdate.getTitle()) != 0) 
					changedValueAudit(bookToUpdate.getId(), "Title", ogBook.getTitle(), bookToUpdate.getTitle());
				// If the summary has been changed
				if (ogBook.getSummary().compareTo(bookToUpdate.getSummary()) != 0)
					changedValueAudit(bookToUpdate.getId(), "Summary", ogBook.getSummary(), bookToUpdate.getSummary());
				// If the year published has been changed
				if (!Integer.valueOf(ogBook.getYearPublished()).equals(Integer.valueOf(bookToUpdate.getYearPublished())))
					changedValueAudit(bookToUpdate.getId(), "Year Published", String.valueOf(ogBook.getYearPublished()), String.valueOf(bookToUpdate.getYearPublished()));
				// If the ISBN has been changed
				if (ogBook.getIsbn().compareTo(bookToUpdate.getIsbn()) != 0)
					changedValueAudit(bookToUpdate.getId(), "ISBN", ogBook.getIsbn(), bookToUpdate.getIsbn());
				// If the publisher has been changed
				if (ogBook.getPub().getPublisherName().compareTo(bookToUpdate.getPub().getPublisherName()) != 0)
					changedValueAudit(bookToUpdate.getId(), "Publisher", ogBook.getPub().getPublisherName(), bookToUpdate.getPub().getPublisherName());
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void changedValueAudit(int bookId, String attribute, String ogValue, String newValue)
	{
		String entryMsg = attribute + " Changed from " + ogValue + " to " + newValue;
		try
		{
			prepStatement = conn.prepareStatement("insert into BookAuditTrail (book_id, entry_msg) values (?, ?)");
			prepStatement.setInt(1, bookId);
			prepStatement.setString(2, entryMsg);
			prepStatement.execute();
		}
		catch(SQLException e) {
			e.printStackTrace();
		}	
	}
	/**
	 * 
	 * @param id
	 * @return
	 */
	public boolean isBookInDB(int id) 
	{
		try {
			prepStatement = conn.prepareStatement("select * from Book where id = ?");
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
		try 
		{
			prepStatement = conn.prepareStatement("select * from Book");
			rs = prepStatement.executeQuery();
			
			while(rs.next())
			{
				Book dbBook = new Book();
				dbBook.setId(rs.getInt("id"));
				dbBook.setTitle(rs.getString("title"));
				dbBook.setSummary(rs.getString("summary"));
				dbBook.setYearPublished(rs.getInt("year_published"));
				dbBook.setPub(ViewManager.getInstance().getPubGateway().fetchPubById(rs.getInt("publisher_id")));
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
	public ObservableList<AuditTrailEntry> getAuditTrails(int bookId)
	{
		//"select * from BookAuditTrail audit join Book book on audit.book_id = book.id where book.id = ? order by audit.date_added asc"
		logger.info("Getting Audit Trails for book at id: " + bookId);
		ObservableList<AuditTrailEntry> auditTrailList = FXCollections.observableArrayList();
		try 
		{
			prepStatement = conn.prepareStatement("select * from BookAuditTrail where book_id = ? order by date_added asc");
			prepStatement.setInt(1, bookId);
			rs = prepStatement.executeQuery();
			while (rs.next())
			{
				AuditTrailEntry audit = new AuditTrailEntry(rs.getString("entry_msg"));
				audit.setId(rs.getInt("id"));
				audit.setDateAdded(rs.getTimestamp("date_added"));
				auditTrailList.add(audit);
			}
		} 
		catch (SQLException e) {
			e.printStackTrace();
		}
		return auditTrailList;
	}
	public ObservableList<AuthorBook> getAuthorForBook(Book book, int bookId)
	{
		logger.info("Getting Authors for Book at id " + bookId);
		AuthorBook ab = null;
		ObservableList<AuthorBook> authorBookList = FXCollections.observableArrayList();
		try
		{
			prepStatement = conn.prepareStatement("select * from AuthorBook where book_id = ?");
			prepStatement.setInt(1, bookId);
			rs = prepStatement.executeQuery();
			while (rs.next())
			{
				ab = new AuthorBook();
				ab.setAuthor(ViewManager.getInstance().getAuthorGateway().getAuthor(rs.getInt("author_id")));
				ab.setBook(book);
				ab.setRoyalty((int) (rs.getDouble("royalty") * 100000));
				ab.setNewRecord(false);
				authorBookList.add(ab);
			}
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
		return authorBookList;
	}
}
