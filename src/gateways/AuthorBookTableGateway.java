package gateways;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import exceptions.GatewayException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.Author;
import model.AuthorBook;
import model.Book;
import model.Publisher;
import singleton.ViewManager;

public class AuthorBookTableGateway 
{
	private static Logger logger = LogManager.getLogger();
	private Connection conn = null;
	private ResultSet rs = null;
	private PreparedStatement prepStatement = null;
	
	public AuthorBookTableGateway(Connection conn) {
		this.conn = conn;
	}
	public ObservableList<AuthorBook> getAuthorsForBook(Book book, int bookId)
	{
		//logger.info("Getting Authors for Book at id " + bookId);
		AuthorBook ab = null;
		Author author;
		ObservableList<AuthorBook> authorBookList = FXCollections.observableArrayList();
		try
		{
			prepStatement = conn.prepareStatement("select * from AuthorBook where book_id = ?");
			prepStatement.setInt(1, bookId);
			rs = prepStatement.executeQuery();
			while (rs.next())
			{
				ab = new AuthorBook();
				author = ViewManager.getInstance().getAuthorGateway().getAuthorByID(rs.getInt("author_id"));
				if (author == null)
					throw new GatewayException("Tried to find an author that doesn't exist in the gateway");
				ab.setAuthor(author);
				ab.setBook(book);
				ab.setRoyalty(rs.getDouble("royalty"));
				ab.setNewRecord(false);
				authorBookList.add(ab);
			}
		}
		catch (GatewayException | SQLException e) {
			e.printStackTrace();
		} 
		return authorBookList;
	}
	public ObservableList<AuthorBook> getAuthorBooksByPublisher(Publisher pub)
	{
		AuthorBook ab;
		Author author;
		Book book;
		ObservableList<AuthorBook> authorBookList = FXCollections.observableArrayList();
		try
		{
			prepStatement = conn.prepareStatement("select * from Book, AuthorBook where id = book_id and publisher_id = ?");
			prepStatement.setInt(1, pub.getId());
			rs = prepStatement.executeQuery();
			while (rs.next())
			{
				ab = new AuthorBook();
				author = ViewManager.getInstance().getAuthorGateway().getAuthorByID(rs.getInt("author_id"));
				ab.setAuthor(author);
				book = new Book(rs.getInt("id"), rs.getString("title"), rs.getString("summary"), rs.getInt("year_published")
						, rs.getString("isbn"), rs.getTimestamp("last_modified").toLocalDateTime(), rs.getTimestamp("date"), pub);
				ab.setBook(book);
				ab.setRoyalty(rs.getDouble("royalty"));
				ab.setNewRecord(false);
				authorBookList.add(ab);
			}
		}
		catch (SQLException e){
			e.printStackTrace();
		}
		return authorBookList;
	}
	public AuthorBook getAuthorBookByID(int authorID, int bookID)
	{
		//logger.info("Attempting to get authorBook at author id: " + authorID + " book id: " + bookID);
		Author author;
		Book book;
		AuthorBook authorBookdb = null;
		try
		{
			prepStatement = conn.prepareStatement("select * from AuthorBook where author_id = ? and book_id = ?");
			prepStatement.setInt(1, authorID);
			prepStatement.setInt(2, bookID);
			rs = prepStatement.executeQuery();
			if (rs.next())
			{
				authorBookdb = new AuthorBook();
				author = ViewManager.getInstance().getAuthorGateway().getAuthorByID(authorID);
				book = ViewManager.getInstance().getBookGateway().getBookByID(bookID);
				authorBookdb.setAuthor(author);
				authorBookdb.setBook(book);
				authorBookdb.setNewRecord(false);
				authorBookdb.setRoyalty(rs.getDouble("royalty"));
			}
		}
		catch(SQLException e){
			e.printStackTrace();
		}
		return authorBookdb;
	}
	public void addAuthorBook(AuthorBook authorBook)
	{
		//logger.info("In Add Author Book");
		try {
			prepStatement = conn.prepareStatement("insert into AuthorBook (author_id, book_id, royalty) values (?, ?, ?)");
			prepStatement.setInt(1, authorBook.getAuthor().getId());
			prepStatement.setInt(2, authorBook.getBook().getId());
			prepStatement.setDouble(3, ( ((double) authorBook.getRoyalty()) / 100000));
			prepStatement.execute();
			logger.info("Inserted Author Into Database");
			authorBook.setNewRecord(false);
			/******** Audit Trail ***********/
			insertAudit(authorBook.getBook().getId(), "Added Author: " + authorBook.getAuthor().getFirstName() + " " + authorBook.getAuthor().getLastName());
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	public void deleteAuthorBook(AuthorBook authorBook)
	{
		//logger.info("In Delete Author Book");
		try {
			prepStatement = conn.prepareStatement("delete from AuthorBook where author_id = ? and book_id = ?");
			prepStatement.setInt(1, authorBook.getAuthor().getId());
			prepStatement.setInt(2, authorBook.getBook().getId());
			prepStatement.executeUpdate();
			logger.info("Deleted Author from Database");
			/******** Audit Trail ***********/
			insertAudit(authorBook.getBook().getId(), "Deleted Author: " + authorBook.getAuthor().getFirstName() + " " + authorBook.getAuthor().getLastName());
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
	}
	public void updateAuthorBook(AuthorBook authorBook)
	{
		//logger.info("In Update Author Book");
		try
		{
			int oldRoyalty = getAuthorBookByID(authorBook.getAuthor().getId(), authorBook.getBook().getId()).getRoyalty();
			prepStatement = conn.prepareStatement("update AuthorBook set royalty = ? where author_id = ? and book_id = ?");
			prepStatement.setDouble(1, ( ((double) authorBook.getRoyalty()) / 100000));
			prepStatement.setInt(2, authorBook.getAuthor().getId());
			prepStatement.setInt(3, authorBook.getBook().getId());
			prepStatement.executeUpdate();
			/************************ Audit Trail **************************/
			String authorName = authorBook.getAuthor().getFirstName() + " " + authorBook.getAuthor().getLastName();
			if (oldRoyalty != authorBook.getRoyalty())
				insertAudit(authorBook.getBook().getId(), "Royalty for " + authorName + " changed from " + oldRoyalty + " to " + authorBook.getRoyalty());
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
	}
	public void insertAudit(int bookId, String entryMsg) throws SQLException
	{
		prepStatement = conn.prepareStatement("insert into BookAuditTrail (book_id, entry_msg) values (?, ?)");
		prepStatement.setInt(1, bookId);
		prepStatement.setString(2, entryMsg);
		prepStatement.execute();
	}
}