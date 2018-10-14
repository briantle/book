package controllers;

import java.io.IOException;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import enums.ViewType;
import exceptions.GatewayException;
import gateways.BookTableGateway;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Alert.AlertType;
import model.Book;
import singleton.ViewManager;

public class MenuController 
{
	private static Logger logger = LogManager.getLogger();
	@FXML private MenuItem menuQuit, bookList, addBook;
	
	@FXML private void handleMenuAction(ActionEvent event) throws IOException, GatewayException
	{
		// User clicks on quit button --> closes program
		if (event.getSource() == menuQuit)
		{
			BookDetailController bdController = ViewManager.getInstance().getCurrController();
			if (bdController != null && bdController.isBookDifferent())
			{
				Alert confirmAlert = new Alert(AlertType.NONE);
				confirmAlert.setHeaderText("Confirm Save Changes");
				confirmAlert.setContentText("The book has been modified. Do you want to save the changes?");
				confirmAlert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);
				// Gets the button that the user clicked on
				Optional<ButtonType> result = confirmAlert.showAndWait();
				if (result.get() != ButtonType.CANCEL)
				{
					if (result.get() == ButtonType.YES)
					{
						BookTableGateway gateway = new BookTableGateway();
						try
						{
								Book bdBook = bdController.getSelectedBook();
								Book changedBook = new Book(bdBook.getId(), bdController.getTfTitle().toString(), bdController.getTfSummary().toString(), Integer.valueOf(bdController.getTfYearPublished().toString()), bdController.getTfISBN().toString(), bdBook.getLastModified(), bdBook.getDateAdded());
								// Book already exists in database, so lets update it
								if (gateway.isBookInDB(changedBook.getId()))
									gateway.updateBook(changedBook, "The changes made to the book could not be saved! Return to the book list and try again.");
								// It doesn't exist, so save it	
								else
									gateway.saveBook(changedBook);
							} catch (GatewayException e) {
								e.printStackTrace();
							}
						gateway.closeConnection();
					}
					Platform.exit();
				}
			}
			else
				Platform.exit();
		}
		// Displays a list of books
		if (event.getSource() == bookList)
			ViewManager.getInstance().changeView(ViewType.BOOK_LIST ,null);
		// Opens up the book detail view with empty book information
		if (event.getSource() == addBook)
			ViewManager.getInstance().changeView(ViewType.BOOK_DETAIL, new Book());
	}
}
