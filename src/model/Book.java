package model;

import validation.Validator;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import exceptions.GatewayException;

public class Book 
{
	private static Logger log = LogManager.getLogger();
	
	private int id;
	private int yearPublished;
	private Publisher publisher;
	private String title;
	private String summary;
	private String isbn;
	private LocalDateTime lastModified;
	private Timestamp dateAdded;
	
	/**
	 * Empty Book Constructor
	 * Called when clicking Add Book or getting book info from the database
	 */
	public Book()
	{
		id = 0;
		title = "";
		summary = null;
		yearPublished = 0;
		//publisher = new Publisher();
		isbn = null;
		lastModified = null;
		dateAdded = new Timestamp(System.currentTimeMillis());
	}
	/**
	 * Book constructor that is used when saving or updating a book
	 */
	public Book(int id, String title, String summary, int yearPublished, String isbn, LocalDateTime lastModified, Timestamp dateAdded)
	{
		this.id = id;
		this.title = title;
		this.summary = summary;
		this.yearPublished = yearPublished;
		this.isbn = isbn;
		this.lastModified = lastModified;
		this.dateAdded = dateAdded;
	}
	
	/**
	 * 
	 * @throws GatewayException
	 */
	public void validateBook() throws GatewayException 
	{
		Validator val = new Validator();
		if (!val.validTitle(this.getTitle()))
			throw new GatewayException("Invalid Book Title: Book not saved!");
		if (!val.validSummary(this.getSummary()))
			throw new GatewayException("Invalid Book Summary: Book not saved!");
		if (!val.validISBN(this.getIsbn()))
			throw new GatewayException("Invalid Book ISBN: Book not saved!");
		if (!val.validYear(this.getYearPublished()))
			throw new GatewayException("Invalid Year Published: Book not saved!");
	}
	
	public LocalDateTime getLastModified() {
		return lastModified;
	}
	public void setLastModified(LocalDateTime lastModified) {
		this.lastModified = lastModified;
	}
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public int getYearPublished() {
		return yearPublished;
	}

	public void setYearPublished(int yearPublished) {
		this.yearPublished = yearPublished;
	}

	public String getIsbn() {
		return isbn;
	}

	public void setIsbn(String isbn) {
		this.isbn = isbn;
	}

	public java.sql.Timestamp getDateAdded() {
		return dateAdded;
	}

	public void setDateAdded(java.sql.Timestamp dateAdded) {
		this.dateAdded = dateAdded;
	}
}
