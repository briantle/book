package main;


import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import controllers.BookDetailController;
import controllers.MenuController;
import exceptions.GatewayException;
import gateways.BookTableGateway;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import model.Book;
import singleton.ViewManager;
/**
 * 
 * CS 4743 Assignment2 by Brian Le
 * 
 * @author Brian
 *
 */
public class Launcher extends Application 
{
	private static Logger log = LogManager.getLogger(Launcher.class);
	public static Stage stage;
	public static BorderPane mainPane;
	
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
				BookDetailController detailController = ViewManager.getInstance().getCurrController();
				// We are in the book detail view
				if (detailController != null)
				{
					if (detailController.isBookDifferent()) 
					{
						Alert confirmAlert = new Alert(AlertType.NONE);
						confirmAlert.setHeaderText("Confirm Save Changes");
						confirmAlert.setContentText("The book has been modified. Do you want to save the changes?");
						confirmAlert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);
						// Gets the button that the user clicked on
						Optional<ButtonType> result = confirmAlert.showAndWait();
						// We don't want to save changes or close the program
						if (result.get() == ButtonType.CANCEL) 
							// Stops the application from closingS
							event.consume();
						// We want to close the program
						else
						{
								// We want to save the changes made to the book
								if (result.get() == ButtonType.YES)
								{
									try
									{
										BookTableGateway gateway = new BookTableGateway();
										Book bdBook = detailController.getSelectedBook();
										Book changedBook = new Book(bdBook.getId(), bdBook.getTitle(), bdBook.getSummary(), bdBook.getYearPublished(), bdBook.getIsbn(), bdBook.getLastModified(), bdBook.getDateAdded());
										// Book already exists in database, so lets update it
										if (gateway.isBookInDB(changedBook.getId()))
											gateway.updateBook(changedBook, "The changes made to the book could not be saved! Return to the book list and try again.");
										// It doesn't exist, so save it	
										else
											gateway.saveBook(changedBook);
										gateway.closeConnection();
										Platform.exit();
									}
									catch (GatewayException e) {
										e.printStackTrace();
									}
								}
						}
					}
				}
				else
					Platform.exit();
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
}
