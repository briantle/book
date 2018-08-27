package model;

public class Book 
{
	private String title;
	private String summary;
	private int yearPublished;
	private String isbn;
	private String dateAdded;
	
	public Book(String title, String summary, int yearPublished, String isbn, String dateAdded)
	{
		this.title = title;
		this.summary = summary;
		this.yearPublished = yearPublished;
		this.isbn = isbn;
		this.dateAdded = dateAdded;
	}

	public String getTitle() {
		return title;
	}

	public String getSummary() {
		return summary;
	}

	public int getYearPublished() {
		return yearPublished;
	}

	public String getIsbn() {
		return isbn;
	}

	public String getDateAdded() {
		return dateAdded;
	}
	
}
