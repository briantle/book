package controllers;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import model.Book;
import singleton.ViewManager;

public class BookDetailController
{
	private static Logger log = LogManager.getLogger();
	private Book selectedBook;
	
	@FXML private Button saveButton;
	@FXML private TextField tfTitle, tfSummary, tfYearPublished, tfISBN;
	@FXML private Label dateAdded;
	
	public void initialize() {
		tfTitle.setText(selectedBook.getTitle());
		tfSummary.setText(selectedBook.getSummary());
		tfYearPublished.setText(String.valueOf(selectedBook.getYearPublished()));
		tfISBN.setText(selectedBook.getIsbn());
		dateAdded.setText(selectedBook.getDateAdded());
	}
	
	
	@FXML public void handleButtonAction(ActionEvent action) throws IOException {
		if (action.getSource() == saveButton)
			log.info("Clicked on save button");
	}

	public void setSelectedBook(Book selectedBook) {
		this.selectedBook = selectedBook;
	}
}
