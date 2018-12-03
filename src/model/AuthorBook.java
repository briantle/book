package model;

import javafx.scene.control.Alert.AlertType;
import singleton.ViewManager;

public class AuthorBook 
{
	private Author author;
	private Book book;
	private int royalty;
	private boolean newRecord = true;
	
	public AuthorBook()
	{
		author = null;
		book = null;
		royalty = 0;
		newRecord = true;
	}
	/******************* Setters **********************/
	public void setAuthor(Author author) {
		this.author = author;
	}
	public void setBook(Book book) {
		this.book = book;
	}
	public void setRoyalty(double royalty) 
	{
		if (royalty < 0.0)
		{
			this.royalty = 0;
			ViewManager.getInstance().showAlert(AlertType.INFORMATION, "ALERT", "Your royalty amount has been set to 0 since it was a negative number!");
		}
		else if (royalty > 1.0)
		{
			this.royalty = 100000;
			ViewManager.getInstance().showAlert(AlertType.INFORMATION, "ALERT", "Your royalty amount has been set to 100000 since it exceeded 100000");
		}
		else
			this.royalty = (int) (royalty * 100000);
	}
	public void setNewRecord(boolean newRecord) {
		this.newRecord = newRecord;
	}
	/****************** Getters *********************/
	public Author getAuthor() {
		return author;
	}
	public Book getBook() {
		return book;
	}
	public int getRoyalty() {
		return royalty;
	}
	public boolean isNewRecord() {
		return newRecord;
	}
}
