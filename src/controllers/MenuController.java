package controllers;

import java.io.IOException;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import enums.ViewType;
import exceptions.GatewayException;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ButtonType;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Alert.AlertType;
import singleton.ViewManager;

public class MenuController 
{
	private static Logger logger = LogManager.getLogger();
	@FXML private MenuItem menuQuit, bookList, addBook, authorList, addAuthor, createExcel;
	
	@FXML private void handleMenuAction(ActionEvent event) throws IOException, GatewayException
	{
		// User clicks on quit button --> closes program
		if (event.getSource() == menuQuit)
		{
			String unsavedMessage = "";
			BookDetailController bdController = ViewManager.getInstance().getCurrController();
			AuthorDetailController adController = ViewManager.getInstance().getAuthorController();
			ExcelDetailController excelController = ViewManager.getInstance().getExcelController();
			boolean bookBool = bdController != null && bdController.isBookDifferent();
			boolean authorBool = adController != null && adController.isAuthDifferent();
			boolean excelBool = excelController != null && excelController.isChanged();
			if (bookBool)
				unsavedMessage = "The book has been modified. Do you want to save the changes?";
			else if (authorBool)
				unsavedMessage = "The author has been modified. Do you want to save the changes?";
			else if (excelBool)
				unsavedMessage = "The excel detail view has been modified. Do you want to save the changes?";				
			if (bookBool || authorBool || excelBool)
			{
				// Gets the button that the user clicked on
				Optional<ButtonType> result = ViewManager.getInstance().getButtonResult(unsavedMessage);
				if (result.get() != ButtonType.CANCEL)
				{
					if (result.get() == ButtonType.YES)
					{
						try
						{
							if (bookBool)
								bdController.saveBookChanges();
							else if (authorBool)
								adController.saveAuthorChanges();
							else if (excelBool)
								excelController.saveHandler();
						}
						catch (GatewayException e) {
							e.printStackTrace();
							ViewManager.getInstance().showAlert(AlertType.ERROR, "ERROR", e.getMessage());
						}
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
			ViewManager.getInstance().changeView(ViewType.BOOK_DETAIL, null);
		// Display a list of authors
		if (event.getSource() == authorList)
			ViewManager.getInstance().changeView(ViewType.AUTHOR_LIST, null);
		// Open up author detail view with empty author information
		if (event.getSource() == addAuthor)
			ViewManager.getInstance().changeView(ViewType.AUTHOR_DETAIL, null);
		if (event.getSource() == createExcel)
			ViewManager.getInstance().changeView(ViewType.EXCEL_SPREADSHEET, null);
	}
}