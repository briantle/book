package controllers;

import java.io.IOException;
import java.text.SimpleDateFormat;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import exceptions.GatewayException;
import gateways.BookTableGateway;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import model.Book;

public class BookDetailController
{
	private static Logger log = LogManager.getLogger();
	private Book selectedBook;
	
	@FXML private Button saveButton;
	@FXML private TextField tfTitle, tfSummary, tfYearPublished, tfISBN;
	@FXML private Label dateAdded;
	
	public BookDetailController(Book book) {
		this.selectedBook = book;
	}
	
	public void initialize() 
	{
		tfTitle.setText(selectedBook.getTitle());
		tfSummary.setText(selectedBook.getSummary());
		tfYearPublished.setText(String.valueOf(selectedBook.getYearPublished()));
		tfISBN.setText(selectedBook.getIsbn());
		// Displays the time stamp in Month/Day/Year format
		dateAdded.setText(new SimpleDateFormat("MM/dd/yyyy").format(selectedBook.getDateAdded()));
	}
	
	@FXML public void handleButtonAction(ActionEvent action) throws IOException 
	{
		if (action.getSource() == saveButton) 
		{
			log.info("Clicked on save button");
			try 
			{
				// Make a temporary book in case the validation fails
				Book newBook = selectedBook;
				newBook.setTitle(tfTitle.getText());
				newBook.setSummary(tfSummary.getText());
				newBook.setYearPublished(Integer.valueOf(tfYearPublished.getText()));
				newBook.setIsbn(tfISBN.getText());
				
				// Make sure the values in the book are valid before saving them in the database
				newBook.validateBook();
				// Gain access to the database
				BookTableGateway gateway = new BookTableGateway();
				// This book doesn't exist in the database, so we are going to insert it into the database
				if (!gateway.isBookInDB(newBook.getId()))
					gateway.saveBook(newBook);
				// The book already exists in the database, so let's update it
				else
					gateway.updateBook(newBook, "Book is not up to date! Go back to the book list to get the updated version of the book.");
				// Close connection to database
				gateway.closeConnection();
				// Copy the changes made to the original book
				selectedBook = newBook;
			} 
			catch (GatewayException e) 
			{
				e.printStackTrace();
				// Display an alert message in the book detail view
				Alert alert = new Alert(AlertType.ERROR);
				alert.setTitle("Error Dialog");
				alert.setHeaderText("Failed to save book");
				// Get the message explaining why the save failed
				alert.setContentText(e.getMessage());
				// Display the error message
				alert.showAndWait();
			}
		}
	}
	public boolean isBookDifferent()
	{
		// If one has an empty title and the other one doesn't
		if (selectedBook.getTitle() == "" && !tfTitle.getText().trim().isEmpty() || selectedBook.getTitle() != "" && tfTitle.getText().trim().isEmpty()) 
			return true;
		// If both have titles and they don't match
		else if (selectedBook.getTitle() != "" && !tfTitle.getText().trim().isEmpty() && selectedBook.getTitle() != tfTitle.getText())
			return true;
		// The summaries don't match
		if (selectedBook.getSummary() != tfSummary.getText())
			return true;
		// If the year published don't match
		if (!Integer.valueOf(selectedBook.getYearPublished()).equals(Integer.valueOf(tfYearPublished.getText())))
			return true;
		// The ISBNs don't match
		if (selectedBook.getIsbn() != tfISBN.getText())
			return true;
		// All the fields are similar
		return false;
	}
	public void setSelectedBook(Book selectedBook) {
		this.selectedBook = selectedBook;
	}
	public Book getSelectedBook() {
		return selectedBook;
	}

	public TextField getTfTitle() {
		return tfTitle;
	}

	public TextField getTfSummary() {
		return tfSummary;
	}

	public TextField getTfYearPublished() {
		return tfYearPublished;
	}

	public TextField getTfISBN() {
		return tfISBN;
	}
	
}
