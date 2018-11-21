package singleton;

import java.io.IOException;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import controllers.AuditTrailController;
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
	 * @param book
	 */
	public void changeView(ViewType view, Book book)
	{	
		try{
			checkForUnsavedChanges(book, view);
		}
		catch (IOException ie)
		{
			logger.error("Failed to switch views");
			ie.printStackTrace();
		}
	}
	/**
	 * 
	 * @param book
	 * @param view
	 * @throws IOException
	 */
	public void checkForUnsavedChanges(Book book, ViewType view) throws IOException
	{
		BorderPane newRoot = null;
		// If the detail view is loaded and the book values have changed from the original values
		if (currController != null && currController.isBookDifferent())
		{
			// Gets the button that the user clicked on
			Optional<ButtonType> result = getButtonResult();
			if (result.get() == ButtonType.YES)
			{
				try
				{
					saveBookChanges();
					swapViews(book, view);
				} 
				catch (GatewayException e)
				{
					e.printStackTrace();
					showErrAlert(e.getMessage());
				}
			}
			else if (result.get() == ButtonType.NO)
				swapViews(book, view);
		}
		// No changes were made, so we don't need to save/update the book
		else
			swapViews(book, view);

	}
	/**
	 * 
	 * @param book
	 * @param view
	 * @throws IOException
	 */
	public void swapViews(Book book, ViewType view) throws IOException
	{
		BorderPane newRoot = null;
		if (view == ViewType.BOOK_LIST)
			switchToListView(newRoot);
		else if (view == ViewType.BOOK_DETAIL)
			switchToDetailView(book, newRoot);
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
	/******************************************************
	* 
	* @throws GatewayException
	*****************************************************/
	public void saveBookChanges() throws GatewayException
	{
		Book bdBook = currController.getSelectedBook();
		Book changedBook = new Book(bdBook.getId(), currController.getTfTitle().getText(), currController.getTfSummary().getText()
		, Integer.valueOf(currController.getTfYearPublished().getText()) 
		, currController.getTfISBN().getText(), bdBook.getLastModified(), bdBook.getDateAdded(), currController.getPublisherSelection());
		// Before we insert or update the book, we want to validate the input first
		changedBook.validateBook();
		// Book already exists in database, so lets update it
		if (bookGateway.isBookInDB(changedBook.getId()))
			bookGateway.updateBook(bdBook, changedBook, "The changes made to the book could not be saved! Return to the book list and try again.");
		// It doesn't exist, so save it	
		else
			bookGateway.saveBook(changedBook);
	}
	/*****************************************************************************
	* Displays an error message through an alert window
	* @param exceptionMsg - the message to displayed from the error that occurred
	*******************************************************************************/
	public void showErrAlert(String exceptionMsg)
	{
		Alert errAlert = new Alert(AlertType.ERROR);
		errAlert.setHeaderText("ERROR");
		errAlert.setContentText(exceptionMsg);
		errAlert.showAndWait();
	}
	/****************************************************************************************
	* Shows an alert to the user with 3 button options.
	* YES - the user wants to save changes, this option switches views
	* NO - the user doesn't want to save changes, this option switches views
	* CANCEL - the alert disappears and nothing changes. The user is still on the same view
	* @return the button the user clicked on, is either YES, NO, or CANCEL
	*****************************************************************************************/
	public Optional<ButtonType> getButtonResult()
	{
		Alert confirmAlert = new Alert(AlertType.NONE);
		confirmAlert.setHeaderText("Confirm Save Changes");
		confirmAlert.setContentText("The book has been modified. Do you want to save the changes?");
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
	}
	/**********************************************************************************
	* Switches to the list view
	* @param newRoot - the view to be switched to
	* @throws IOException
	**********************************************************************************/
	public void switchToListView(BorderPane newRoot) throws IOException
	{
		this.currController = null;
		newRoot = (BorderPane) FXMLLoader.load(getClass().getResource("/fxml/BookListView.fxml"));
		loadView(newRoot);
	}
	/***********************************************************************
	* Switches to the book detail view
	* @param book - the specified book to be displayed in the detail view
	* @param newRoot - the new view
	* @throws IOException
	*************************************************************************/
	public void switchToDetailView(Book book, BorderPane newRoot) throws IOException
	{
		// Load the book detail fxml
		loader = new FXMLLoader(getClass().getResource("/fxml/BookDetailView.fxml"));
		// Create a new book detail controller with our specified book
		currController = new BookDetailController(book, pubGateway.fetchPublishers());
		// Set up the controller for our fxml
		loader.setController(currController);
		newRoot = loader.load();
		loadView(newRoot);
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