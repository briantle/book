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
				// Gets the button that the user clicked on
				Optional<ButtonType> result = ViewManager.getInstance().getButtonResult();
				if (result.get() == ButtonType.YES)
				{
					try
					{
						ViewManager.getInstance().saveBookChanges();
						Platform.exit();
					} catch (GatewayException e) {
						e.printStackTrace();
					}
				}
				else if (result.get() == ButtonType.NO)
					Platform.exit();
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
