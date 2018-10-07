package main;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import controllers.MenuController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
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
