package controllers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import enums.ViewType;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.util.Callback;
import model.AuditTrailEntry;
import model.Book;
import singleton.ViewManager;
import gateways.BookTableGateway;
public class AuditTrailController 
{
	private Logger log = LogManager.getLogger();
	@FXML
	private TextField bookAuditTrail;
	@FXML
	private ListView<AuditTrailEntry> auditListView;
	@FXML 
	private Button goBack;
	private Book selectedBook;
	
	public AuditTrailController(Book book, ObservableList<AuditTrailEntry> auditTrailList){
		this.selectedBook = book;
		this.auditListView.setItems(auditTrailList);
	}
	
	public void initialize(){
		bookAuditTrail.setText("Audit Trail for " + selectedBook.getTitle());
		populateListView();
	}
	@FXML
	public void handleButtonAction(ActionEvent action)
	{
		if (action.getSource() == goBack)
			ViewManager.getInstance().changeView(ViewType.BOOK_DETAIL, selectedBook);
	}
	public void populateListView()
	{
		auditListView.setCellFactory(new Callback<ListView<AuditTrailEntry>, ListCell<AuditTrailEntry>>()
		{
			@Override
			public ListCell<AuditTrailEntry> call(ListView<AuditTrailEntry> auditList)
			{
					ListCell<AuditTrailEntry> auditCell = new ListCell<AuditTrailEntry>()
					{
						@Override
						protected void updateItem(AuditTrailEntry audit, boolean empty)
						{
							super.updateItem(audit, empty);
							if (audit != null)
								setText(audit.getDateAdded() + " " + audit.getMessage());
						}
					};
					return auditCell;
			}
		});
	}
}
