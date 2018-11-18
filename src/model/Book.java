package model;

import validation.Validator;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import exceptions.GatewayException;
import javafx.collections.ObservableList;
import singleton.ViewManager;

public class Book 
{
	private int id;
	private int yearPublished;
	private String title;
	private String summary;
	private String isbn;
	private LocalDateTime lastModified;
	private Timestamp dateAdded;
	private Publisher pub;
	private ObservableList<AuditTrailEntry> auditTrailList;
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
		isbn = null;
		lastModified = null;
		dateAdded = new Timestamp(System.currentTimeMillis());
		pub = new Publisher();
	}
	/**
	 * Book constructor that is used when saving or updating a book
	 */
	public Book(int id, String title, String summary, int yearPublished, String isbn, LocalDateTime lastModified, Timestamp dateAdded, Publisher pub)
	{
		this.id = id;
		this.title = title;
		this.summary = summary;
		this.yearPublished = yearPublished;
		this.isbn = isbn;
		this.lastModified = lastModified;
		this.dateAdded = dateAdded;
		this.pub = pub;
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
	
	public Publisher getPub() {
		return pub;
	}
	public void setPub(Publisher pub) {
		this.pub = pub;
	}
	public ObservableList<AuditTrailEntry> getAuditTrailList()
	{
		this.auditTrailList = ViewManager.getInstance().getBookGateway().getAuditTrails(this.id);
		return this.auditTrailList;
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
	public ObservableList<AuthorBook> getAuthors() {
		return ViewManager.getInstance().getAuthorBookGateway().getAuthorsForBook(this, this.id);
	}
}
