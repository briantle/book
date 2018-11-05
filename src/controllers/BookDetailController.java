package controllers;

import java.io.IOException;
import java.text.SimpleDateFormat;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import enums.ViewType;
import exceptions.GatewayException;
import gateways.BookTableGateway;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.util.Callback;
import model.Book;
import model.Publisher;
import singleton.ViewManager;
public class BookDetailController
{
	private static Logger log = LogManager.getLogger();
	private Book selectedBook;
	
	@FXML private Button saveButton, auditButton;
	@FXML private TextField tfTitle, tfSummary, tfYearPublished, tfISBN;
	@FXML private Label dateAdded;
	@FXML private ComboBox<Publisher> publisherComboBox;
	ObservableList<Publisher> publisherList;
	
	public BookDetailController(Book book, ObservableList<Publisher> publisherList)
	{
		this.selectedBook = book;
		this.publisherList = publisherList;
	}
	private void populateComboBox()
	{
		Callback<ListView<Publisher>, ListCell<Publisher>> cellFactory = new Callback<ListView<Publisher>, ListCell<Publisher>>() 
		{
		    @Override
		    public ListCell<Publisher> call(ListView<Publisher> l) 
		    {
		        return new ListCell<Publisher>() 
		        {
		            @Override
		            protected void updateItem(Publisher pub, boolean empty) 
		            {
		            	// Populate the combo box with the publishers
		                super.updateItem(pub, empty);
		                if (pub == null || empty) 
		                    setGraphic(null);
		                else 
		                    setText(pub.getPublisherName());
		            }
		        };
		    }
		};
		publisherComboBox.setButtonCell(cellFactory.call(null));
		publisherComboBox.setCellFactory(cellFactory);
	}
	public void initialize() 
	{
		// If we are adding a new book, we want to disable the audit button
		if (selectedBook == null)
		{
			auditButton.setDisable(true);
			selectedBook = new Book();
		}
		tfTitle.setText(selectedBook.getTitle());
		tfSummary.setText(selectedBook.getSummary());
		tfYearPublished.setText(String.valueOf(selectedBook.getYearPublished()));
		tfISBN.setText(selectedBook.getIsbn());
		// Displays the time stamp in Month/Day/Year format
		dateAdded.setText(new SimpleDateFormat("MM/dd/yyyy").format(selectedBook.getDateAdded()));
		// Populate the combo box with the list of publishers
		publisherComboBox.setItems(publisherList);
		populateComboBox();
		// Default the combo box value to whichever publisher the book has selected
		publisherComboBox.getSelectionModel().select(selectedBook.getPub().getId());
	}
	@FXML 
	public void handleButtonAction(ActionEvent action) throws IOException 
	{
		if (action.getSource() == saveButton) 
		{
			log.info("Clicked on save button");
			try 
			{
				// Make a temporary book in case the validation fails
				Book newBook = new Book(selectedBook.getId(), tfTitle.getText(), tfSummary.getText(), Integer.valueOf(tfYearPublished.getText()), tfISBN.getText()
						               , selectedBook.getLastModified(), selectedBook.getDateAdded(), getPublisherSelection());
				
				// Make sure the values in the book are valid before saving them in the database
				newBook.validateBook();
				// Get reference to the database
				BookTableGateway gateway = ViewManager.getInstance().getBookGateway();
				// This book doesn't exist in the database, so we are going to insert it into the database
				if (!gateway.isBookInDB(newBook.getId()))
					gateway.saveBook(newBook);
				// The book already exists in the database, so let's update it
				else
					gateway.updateBook(selectedBook, newBook, "Book is not up to date! Go back to the book list to get the updated version of the book.");
				// Copy the changes made to the original book
				selectedBook = newBook;
				// If the audit trail button is disable, enable it
				if (auditButton.isDisabled())
					auditButton.setDisable(false);
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
		else if (action.getSource() == auditButton) 
			ViewManager.getInstance().changeView(ViewType.AUDIT_TRAIL, selectedBook);
	}
	public boolean isBookDifferent()
	{
		// Empty so no need to check
		if (selectedBook.getSummary() == null && tfSummary.getText() == null)
			return false;
		
		// If one has an empty title and the other one doesn't
		if (selectedBook.getTitle() == "" && !tfTitle.getText().trim().isEmpty() || selectedBook.getTitle() != "" && tfTitle.getText().trim().isEmpty())
			return true;
		// If both have titles and they don't match
		else if (selectedBook.getTitle() != "" && !tfTitle.getText().trim().isEmpty() && selectedBook.getTitle().compareTo(tfTitle.getText()) != 0) 
			return true;
		// The summaries don't match
		if (selectedBook.getSummary().compareTo(tfSummary.getText()) != 0)
			return true;
		// If the year published don't match
		if (!Integer.valueOf(selectedBook.getYearPublished()).equals(Integer.valueOf(tfYearPublished.getText())))
			return true;
		// The ISBNs don't match
		if (selectedBook.getIsbn().compareTo(tfISBN.getText()) != 0)
			return true;
		// Publishers don't match
		if (selectedBook.getPub().getPublisherName().compareTo(publisherComboBox.getSelectionModel().getSelectedItem().getPublisherName()) != 0)
			return true;
		// All the fields are similar
		return false;
	}
	/********************* Setters *******************/
	public void setSelectedBook(Book selectedBook) {
		this.selectedBook = selectedBook;
	}
	/******************** Getters ***********************/
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
	public Publisher getPublisherSelection() {
		return publisherComboBox.getSelectionModel().getSelectedItem();
	}
}