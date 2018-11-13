package model;

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
	public void setRoyalty(int royalty) {
		this.royalty = royalty;
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
