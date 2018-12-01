package controllers;

import java.io.IOException;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;

public class AuthorDetailController 
{
	@FXML private TextField firstNameTF, lastNameTF, websiteTF;
	@FXML private ChoiceBox genderChoiceBox;
	@FXML private DatePicker dobPicker;
	@FXML private Button saveButton;
	
	@FXML
	public void handleButtonAction(ActionEvent action) throws IOException 
	{
		
	}
}
