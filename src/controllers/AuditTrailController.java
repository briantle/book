package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import model.AuditTrailEntry;

public class AuditTrailController 
{
	@FXML
	private TextField bookAuditTrail;
	@FXML
	private ListView<AuditTrailEntry> auditTrailList;
	@FXML 
	private Button goBack;
}
