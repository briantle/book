package singleton;

import java.io.IOException;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import controllers.AuditTrailController;
import controllers.BookDetailController;
import enums.ViewType;
import exceptions.GatewayException;
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
	private BookDetailController currController = null;
	private AuditTrailController auditController = null;
	private FXMLLoader loader = null;
	// Setup database connection
	private ViewManager()
	{
		try 
		{
			gwManager = new GatewayManager();
			bookGateway = new BookTableGateway(gwManager.getConn());
			pubGateway = new PublisherTableGateway(gwManager.getConn());
			authorGateway = new AuthorTableGateway(gwManager.getConn());
		} 
		catch (GatewayException e) {
			e.printStackTrace();
		}
	}
	
	public void changeView(ViewType view, Book book)
	{	
		try
		{
			BorderPane newRoot = null;
			BorderPane currRoot = Launcher.getMainPane();
			if (view == ViewType.BOOK_LIST)
			{
				if (currController != null && currController.isBookDifferent())
				{
					// Gets the button that the user clicked on
					Optional<ButtonType> result = getButtonResult();
					if (result.get() == ButtonType.YES)
					{
						try
						{
							saveBookChanges();
							switchToListView(newRoot, currRoot);
						} 
						catch (GatewayException e)
						{
							e.printStackTrace();
							showErrAlert(e.getMessage());
						}
					}
					else if (result.get() == ButtonType.NO)
						switchToListView(newRoot, currRoot);
				}
				else
					switchToListView(newRoot, currRoot);
			}
			else if (view == ViewType.BOOK_DETAIL)
			{
				if (currController != null && currController.isBookDifferent())
				{
					// Gets the button that the user clicked on
					Optional<ButtonType> result = getButtonResult();
					if (result.get() == ButtonType.YES)
					{
						try
						{
							saveBookChanges();
							switchToDetailView(book, newRoot, currRoot);
						} 
						catch (GatewayException e)
						{
							e.printStackTrace();
							showErrAlert(e.getMessage());
						}
					}
					else if (result.get() == ButtonType.NO)
						switchToDetailView(book, newRoot, currRoot);
				}
				else
					switchToDetailView(book, newRoot, currRoot);
			}
			else if (view == ViewType.AUDIT_TRAIL)
			{
				loader = new FXMLLoader(getClass().getResource("/fxml/AuditTrailView.fxml"));
				this.auditController = new AuditTrailController(currController.getSelectedBook(), currController.getSelectedBook().getAuditTrailList());
				loader.setController(this.auditController);
				newRoot = loader.load();
				// Clears the view in order to prevent overlap
				currRoot.setCenter(null);
				// Swap to new view
				currRoot.setCenter(newRoot);
				this.currController = null;
			}
		}
		catch (IOException ie)
		{
			logger.error("Failed to switch views");
			ie.printStackTrace();
		}
	}
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
	public void showErrAlert(String exceptionMsg)
	{
		Alert errAlert = new Alert(AlertType.ERROR);
		errAlert.setHeaderText("ERROR");
		errAlert.setContentText(exceptionMsg);
		errAlert.showAndWait();
	}
	public Optional<ButtonType> getButtonResult()
	{
		Alert confirmAlert = new Alert(AlertType.NONE);
		confirmAlert.setHeaderText("Confirm Save Changes");
		confirmAlert.setContentText("The book has been modified. Do you want to save the changes?");
		confirmAlert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);
		// Gets the button that the user clicked on
		Optional<ButtonType> result = confirmAlert.showAndWait();
		return result;
	}
	public void switchToListView(BorderPane newRoot, BorderPane currRoot) throws IOException
	{
		this.currController = null;
		newRoot = (BorderPane) FXMLLoader.load(getClass().getResource("/fxml/BookListView.fxml"));
		// Clears the view in order to prevent overlap
		currRoot.setCenter(null);
		// Swap to new view
		currRoot.setCenter(newRoot);
	}
	public void switchToDetailView(Book book, BorderPane newRoot, BorderPane currRoot) throws IOException
	{
		loader = new FXMLLoader(getClass().getResource("/fxml/BookDetailView.fxml"));
		currController = new BookDetailController(book, pubGateway.fetchPublishers());
		loader.setController(currController);
		newRoot = loader.load();
		// Clears the view in order to prevent overlap
		currRoot.setCenter(null);
		// Swap to new view
		currRoot.setCenter(newRoot);
	}
	public static ViewManager getInstance()
	{
		if (instance == null)
			instance = new ViewManager();
		return instance;
	}
	
	public BookDetailController getCurrController() {
		return currController;
	}
	public void setCurrController(BookDetailController currController) {
		this.currController = currController;
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
}
