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
public class AuditTrailController 
{
	private Logger log = LogManager.getLogger();
	private ObservableList<AuditTrailEntry> auditTrailList;
	@FXML
	private TextField bookAuditTrail;
	@FXML
	private ListView<AuditTrailEntry> auditListView;
	@FXML 
	private Button goBackButton;
	private Book selectedBook;
	
	public AuditTrailController(Book book, ObservableList<AuditTrailEntry> auditTrailList)
	{
		this.selectedBook = book;
		this.auditTrailList = auditTrailList;
		ViewManager.getInstance().setCurrController(null);
	}
	
	public void initialize()
	{
		auditListView.setItems(auditTrailList);
		populateListView();
		bookAuditTrail.setText("Audit Trail for " + selectedBook.getTitle());
	}
	@FXML
	public void handleButtonAction(ActionEvent action)
	{
		if (action.getSource() == goBackButton)
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
