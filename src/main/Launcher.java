package main;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class Launcher extends Application 
{

	private static Logger log = LogManager.getLogger(Launcher.class);
	@Override
	public void start(Stage primaryStage) throws Exception 
	{
		
	}

	
	public static void main(String[] args)
	{
		launch(args);
	}
}
