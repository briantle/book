package controllers;

import java.io.IOException;

import exceptions.GatewayException;
import gateways.AuthorTableGateway;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import model.Author;
import singleton.ViewManager;

public class AuthorDetailController 
{
	@FXML private TextField firstNameTF, lastNameTF, websiteTF;
	@FXML private ChoiceBox<String> genderChoiceBox;
	@FXML private DatePicker dobPicker;
	@FXML private Button saveButton;
	private Author selectedAuthor;
	
	public AuthorDetailController(Author author) {
		this.selectedAuthor = author;
	}
	public void initialize()
	{
		if (selectedAuthor == null)
			selectedAuthor = new Author();
		populateChoiceBox();
		// Display the values
		firstNameTF.setText(selectedAuthor.getFirstName());
		lastNameTF.setText(selectedAuthor.getLastName());
		dobPicker.setValue(selectedAuthor.getDateOfBirth());
		websiteTF.setText(selectedAuthor.getWebsite());
		genderChoiceBox.getSelectionModel().select(selectedAuthor.getGender());
	}
	@FXML
	public void handleButtonAction(ActionEvent action) throws IOException 
	{
		if (action.getSource() == saveButton)
			try {
				saveAuthorChanges();
			} catch (GatewayException e) {
				ViewManager.getInstance().showAlert(AlertType.ERROR, "Error", e.getMessage());
			}
	}
	public void saveAuthorChanges() throws GatewayException
	{
		// Create an author based on the values in the detail view
		Author currAuthor = new Author(selectedAuthor.getId(), firstNameTF.getText(), lastNameTF.getText()
				, dobPicker.getValue(), genderChoiceBox.getSelectionModel().getSelectedItem(), websiteTF.getText());
		currAuthor.validateAuthor();
		// Used to checked if the author exists in the database, and either saves/updates the author
		AuthorTableGateway authorGW = ViewManager.getInstance().getAuthorGateway();
		// The author doesn't exist in the database, so insert into the database
		if (authorGW.getAuthorByID(currAuthor.getId()) == null)
			authorGW.saveAuthor(currAuthor);
		// Author already exists in the database, so update the author
		else
			authorGW.updateAuthor(currAuthor);
		// Set our author to reflect the new author changes
		selectedAuthor = currAuthor;
	}
	/************************************************************
	* Populate the gender choice box with MALE and FEMALE option
	**************************************************************/
	public void populateChoiceBox()
	{
		// Remove all item choices
		genderChoiceBox.getItems().clear();
		// Insert 2 item choices, M for male, F for female
		genderChoiceBox.getItems().addAll("M", "F");
	}
	/******************************************************************************************
	* Checks to see if the values in the detail view are different than what it originally was
	* @return true if the author has been changed, false if the author hasn't been changed
	*********************************************************************************************/
	public boolean isAuthDifferent()
	{
		if (!selectedAuthor.getFirstName().equals(firstNameTF.getText().trim()))
			return true;
		if (!selectedAuthor.getLastName().equals(lastNameTF.getText().trim()))
			return true;
		if (selectedAuthor.getDateOfBirth() != null && dobPicker.getValue() == null || selectedAuthor.getDateOfBirth() == null && dobPicker.getValue() != null || 
		selectedAuthor.getDateOfBirth() != null && dobPicker.getValue() != null && !selectedAuthor.getDateOfBirth().toString().equals(dobPicker.getValue().toString()))
			return true;
		if (!selectedAuthor.getGender().equals(genderChoiceBox.getSelectionModel().getSelectedItem())) 
			return true;
		if (selectedAuthor.getWebsite() != null && websiteTF.getText() == null || selectedAuthor.getWebsite() == null && websiteTF.getText() != null || 
				selectedAuthor.getWebsite() != null && websiteTF.getText() != null && !selectedAuthor.getWebsite().equals(websiteTF.getText().trim()))
			return true;
		return false;
	}
	/************************ Getters ************************/
	public TextField getFirstNameTF() {
		return firstNameTF;
	}
	public TextField getLastNameTF() {
		return lastNameTF;
	}
	public TextField getWebsiteTF() {
		return websiteTF;
	}
	public ChoiceBox<String> getGenderChoiceBox() {
		return genderChoiceBox;
	}
	public DatePicker getDobPicker() {
		return dobPicker;
	}
	public Button getSaveButton() {
		return saveButton;
	}
	public Author getSelectedAuthor() {
		return selectedAuthor;
	}
}
