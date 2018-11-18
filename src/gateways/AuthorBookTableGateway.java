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
				Author author = ViewManager.getInstance().getAuthorGateway().getAuthor(rs.getInt("author_id"));
				if (author == null)
					throw new GatewayException("Tried to find an author that doesn't exist in the gateway");
				ab.setAuthor(author);
				ab.setBook(book);
				ab.setRoyalty((int) (rs.getDouble("royalty") * 100000));
				ab.setNewRecord(false);
				authorBookList.add(ab);
			}
		}
		catch (GatewayException | SQLException e) {
			e.printStackTrace();
		} 
		return authorBookList;
	}
	public void addAuthor(AuthorBook authorBook)
	{
		logger.info("In Add Author");
		try {
			prepStatement = conn.prepareStatement("insert into AuthorBook (author_id, book_id, royalty) values (?, ?, ?)");
			prepStatement.setInt(1, authorBook.getAuthor().getId());
			prepStatement.setInt(2, authorBook.getBook().getId());
			prepStatement.setDouble(3, authorBook.getRoyalty());
			prepStatement.executeUpdate();
			logger.info("Inserted Author Into Database");
			/******** Audit Trail ***********/
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	public void deleteAuthor(AuthorBook authorBook)
	{
		logger.info("In Delete Author");
		try {
			prepStatement = conn.prepareStatement("delete from AuthorBook where author_id = ? and book_id = ?");
			prepStatement.setInt(1, authorBook.getAuthor().getId());
			prepStatement.setInt(2, authorBook.getBook().getId());
			prepStatement.executeUpdate();
			logger.info("Deleted Author from Database");
			/**************** Audit Trail ******************/
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
	}
	public void updateAuthor(AuthorBook authorBook)
	{
		
	}
}
