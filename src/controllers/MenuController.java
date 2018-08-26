package controllers;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import enums.ViewType;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.MenuItem;
import singleton.ViewManager;

public class MenuController 
{
	private static Logger logger = LogManager.getLogger();
	
	@FXML private MenuItem menuQuit;
	@FXML private MenuItem bookList;
	
	@FXML private void handleMenuAction(ActionEvent event) throws IOException
	{
		// User clicks on quit button --> closes program
		if (event.getSource() == menuQuit)
			Platform.exit();
		// Displays a list of books
		if (event.getSource() == bookList)
			ViewManager.getInstance().changeView(ViewType.BOOK_LIST ,null);
	}
}
