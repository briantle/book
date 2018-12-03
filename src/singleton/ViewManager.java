package singleton;

import java.io.IOException;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import controllers.AuditTrailController;
import controllers.AuthorDetailController;
import controllers.BookDetailController;
import enums.ViewType;
import exceptions.GatewayException;
import gateways.AuthorBookTableGateway;
import gateways.AuthorTableGateway;
import gateways.BookTableGateway;
import gateways.GatewayManager;
import gateways.PublisherTableGateway;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.BorderPane;
import main.Launcher;
import model.Author;
import model.Book;

public class ViewManager
{
	private static final Logger logger = LogManager.getLogger(ViewManager.class);
	private static ViewManager instance = null;
	private GatewayManager gwManager;
	private BookTableGateway bookGateway;
	private PublisherTableGateway pubGateway;
	private AuthorTableGateway authorGateway;
	private AuthorBookTableGateway authorBookGateway;
	private AuthorDetailController authorController = null;
	private BookDetailController currController = null;
	private AuditTrailController auditController = null;
	private FXMLLoader loader = null;
	/**********************************************************************
	 * Constructor that gets a reference to several tables in the database.
	 ***********************************************************************/
	private ViewManager()
	{
		// Connects to the database and get reference to each table in the database
		try 
		{
			gwManager = new GatewayManager();
			bookGateway = new BookTableGateway(gwManager.getConn());
			pubGateway = new PublisherTableGateway(gwManager.getConn());
			authorGateway = new AuthorTableGateway(gwManager.getConn());
			authorBookGateway = new AuthorBookTableGateway(gwManager.getConn());
		} 
		// Error occurred trying to connect to the database
		catch (GatewayException e) {
			e.printStackTrace();
		}
	}
	/**
	 * 
	 * @param view
	 * @param obj
	 */
	public void changeView(ViewType view, Object obj)
	{	
		try
		{
			// We are in the audit view, clicked on add author/book, or we haven't changed the values and therefore we just switch and don't have to check for 
			// unsaved changes
			if (auditController != null || currController == null && authorController == null 
					|| currController != null && !currController.isBookDifferent() || authorController != null && !authorController.isAuthDifferent())
				swapViews(obj, view);
			// If we are in the book detail view and have changed values
			else if (currController != null && currController.isBookDifferent())
				handleUnsavedChanges(obj, view, "BOOK", "The book has been modified. Do you want to save the changes?");
			// If we are in the author detail view and have changed values
			else if (authorController != null && authorController.isAuthDifferent())
				handleUnsavedChanges(obj, view, "AUTHOR", "The author has been modified. Do you want to save the changes?");
		}
		// Error occurred trying to switch views
		catch (IOException ie)
		{
			logger.error("Failed to switch views");
			ie.printStackTrace();
			showAlert(AlertType.ERROR, "ERROR", ie.getMessage());
		}
	}
	public void handleUnsavedChanges(Object obj, ViewType view, String objType, String unsavedMessage) throws IOException
	{
		// Gets the button that the user clicked on
		Optional<ButtonType> result = getButtonResult(unsavedMessage);
		// Nothing will happen if the user presses on the cancel button
		if (result.get() != ButtonType.CANCEL)
		{
			// We want to save the changes if the user clicks on YES
			if (result.get() == ButtonType.YES)
			{
				// Depending on the data type, we will either save book or author changes.
				try
				{
					if (objType == "BOOK")
						currController.saveBookChanges();
					else if (objType == "AUTHOR")
						authorController.saveAuthorChanges();
				} 
				catch (GatewayException e)
				{
					e.printStackTrace();
					showAlert(AlertType.ERROR, "ERROR", e.getMessage());
				}
			}
			// We will switch views if the user chose either YES or NO
			swapViews(obj, view);
		}
	}
	/**
	 * 
	 * @param book
	 * @param view
	 * @throws IOException
	 */
	public void swapViews(Object obj, ViewType view) throws IOException
	{
		BorderPane newRoot = null;
		// Related to Book
		if (view == ViewType.BOOK_LIST)
			switchToListView(newRoot, "/fxml/BookListView.fxml");
		else if (view == ViewType.BOOK_DETAIL)
			switchToDetailView((Book) obj, newRoot, "BOOK", "/fxml/BookDetailView.fxml");
		// Related to Author
		else if (view == ViewType.AUTHOR_LIST)
			switchToListView(newRoot, "/fxml/AuthorListView.fxml");
		else if (view == ViewType.AUTHOR_DETAIL)
			switchToDetailView((Author) obj, newRoot, "AUTHOR", "/fxml/AuthorDetailView.fxml");
		// Audit Trail
		else if (view == ViewType.AUDIT_TRAIL)
			switchToAuditTrail(newRoot);
	}
	/**
	 * 
	 * @param newRoot
	 */
	public void loadView(BorderPane newRoot)
	{
		BorderPane currRoot = Launcher.getMainPane();
		// Clears the view in order to prevent overlap
		currRoot.setCenter(null);
		// Swap to new view
		currRoot.setCenter(newRoot);
	}
	/*****************************************************************************
	* Displays an message through an alert window
	* Could either be an info alert, or a error alert
	*******************************************************************************/
	public void showAlert(AlertType alertType, String headerText, String msg)
	{
		Alert errAlert = new Alert(alertType);
		errAlert.setHeaderText(headerText);
		errAlert.setContentText(msg);
		errAlert.showAndWait();
	}
	/****************************************************************************************
	* Shows an alert to the user with 3 button options.
	* YES - the user wants to save changes, this option switches views
	* NO - the user doesn't want to save changes, this option switches views
	* CANCEL - the alert disappears and nothing changes. The user is still on the same view
	* @return the button the user clicked on, is either YES, NO, or CANCEL
	*****************************************************************************************/
	public Optional<ButtonType> getButtonResult(String unsavedMessage)
	{
		Alert confirmAlert = new Alert(AlertType.NONE);
		confirmAlert.setHeaderText("Confirm Save Changes");
		confirmAlert.setContentText(unsavedMessage);
		// Display 3 buttons to the alert, YES NO and CANCEL
		confirmAlert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);
		// Displays the alert and then waits for the user to click on one of the buttons
		Optional<ButtonType> result = confirmAlert.showAndWait();
		// Gets the button that the user clicked on
		return result;
	}
	/*******************************************************************
	* 
	* @param newRoot
	* @throws IOException 
	*********************************************************************/
	public void switchToAuditTrail(BorderPane newRoot) throws IOException
	{
		loader = new FXMLLoader(getClass().getResource("/fxml/AuditTrailView.fxml"));
		this.auditController = new AuditTrailController(currController.getSelectedBook(), currController.getSelectedBook().getAuditTrailList());
		loader.setController(this.auditController);
		newRoot = loader.load();
		loadView(newRoot);
		this.currController = null;
		this.authorController = null;
	}
	/*********************************************************************
	* Switches to the list view. It could either be the author list view
	* or the book list view.
	**********************************************************************/
	public void switchToListView(BorderPane newRoot, String listViewPath) throws IOException
	{
		setControllersNull();
		newRoot = (BorderPane) FXMLLoader.load(getClass().getResource(listViewPath));
		loadView(newRoot);
	}
	/***********************************************************************
	* Switches to the detail view
	*************************************************************************/
	public void switchToDetailView(Object obj, BorderPane newRoot, String dataType, String fxmlPath) throws IOException
	{
		// Load the detail fxml
		loader = new FXMLLoader(getClass().getResource(fxmlPath));
		if (dataType == "BOOK")
		{
			this.authorController = null;
			// Create a new book detail controller with our specified book
			currController = new BookDetailController((Book) obj, pubGateway.fetchPublishers());
			// Set up the controller for our fxml
			loader.setController(currController);
		}
		else if (dataType == "AUTHOR")
		{
			this.currController = null;
			authorController = new AuthorDetailController((Author) obj);
			loader.setController(authorController);
		}
		this.auditController = null;
		newRoot = loader.load();
		loadView(newRoot);
	}
	public void setControllersNull()
	{
		this.currController = null;
		this.auditController = null;
		this.authorController = null;
	}
	/*****************************
	 * Sets up singleton
	 * @return singleton instance
	 ******************************/
	public static ViewManager getInstance()
	{
		// If we don't have a singleton reference to our view manager, create one
		if (instance == null)
			instance = new ViewManager();
		return instance;
	}
	/***************** Setters ************************/
	public void setCurrController(BookDetailController currController) {
		this.currController = currController;
	}
	/******************** Getters ********************/
	public BookDetailController getCurrController() {
		return currController;
	}
	public AuthorDetailController getAuthorController() {
		return authorController;
	}
	public GatewayManager getGwManager() {
		return gwManager;
	}
	public BookTableGateway getBookGateway() {
		return bookGateway;
	}
	public PublisherTableGateway getPubGateway() {
		return pubGateway;
	}
	public AuthorTableGateway getAuthorGateway() {
		return authorGateway;
	}
	public AuthorBookTableGateway getAuthorBookGateway() {
		return authorBookGateway;
	}
}