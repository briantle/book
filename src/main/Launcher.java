package main;

import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import controllers.AuthorDetailController;
import controllers.BookDetailController;
import controllers.ExcelDetailController;
import exceptions.GatewayException;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import singleton.ViewManager;
/**
 * 
 * CS 4743 Assignment 4 by Brian Le
 * 
 * @author Brian Le
 *
 */
public class Launcher extends Application 
{
	private static Logger log = LogManager.getLogger(Launcher.class);
	public static Stage stage;
	public static BorderPane mainPane;
	
	@Override
	public void init() throws Exception
	{
		super.init();
		// Connect to the database
		ViewManager.getInstance();
	}
	
	@Override
	public void stop()
	{
		// Stop connection to database
		ViewManager.getInstance().getGwManager().closeConnection();
	}
	@Override
	public void start(Stage primaryStage) throws Exception
	{
		// Get a reference to the main menu view
		BorderPane root = (BorderPane)FXMLLoader.load(getClass().getResource("/fxml/Menu.fxml"));
		// Set the resolution and title of the menu
		primaryStage.setScene(new Scene(root, 1000, 600));
		primaryStage.setTitle("Book Record");
		// Display the menu
		primaryStage.show();
		this.mainPane = root;
		
		// This handles the case of the user clicking the red X in the top right of the program
		primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>()
		{
			@Override
			public void handle(WindowEvent event) 
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
					// We don't want to save changes or close the program
					if (result.get() == ButtonType.CANCEL) 
						// Stops the application from closingS
						event.consume();
					// We want to close the program
					else
					{
						// We want to save the changes made to the book/author
						if (result.get() == ButtonType.YES)
						{
							// Save the changes in the database
							try
							{
								if (bookBool)
									bdController.saveBookChanges();
								else if (authorBool)
									adController.saveAuthorChanges();
								else if (excelBool)
									excelController.saveHandler();
							}
							// An issue occurred when trying to save the book in the database
							catch (GatewayException e)
							{
								// Display the error in the console
								e.printStackTrace();
								// Prevent the application from closing
								event.consume();
								// Display the error message through an alert
								ViewManager.getInstance().showAlert(AlertType.ERROR, "ERROR", e.getMessage());
							}
						}
					}
				}
			}
		});
	}
	public static void main(String[] args){
		launch(args);
	}
	public static BorderPane getMainPane() {
		return mainPane;
	}
	public static void setMainPane(BorderPane mainPane) {
		Launcher.mainPane = mainPane;
	}

	public static Stage getStage() {
		return stage;
	}	
}