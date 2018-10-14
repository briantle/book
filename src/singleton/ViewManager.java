package singleton;

import java.io.IOException;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import controllers.BookDetailController;
import enums.ViewType;
import exceptions.GatewayException;
import gateways.BookTableGateway;
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
	private BookDetailController currController = null;
	
	public BookDetailController getCurrController() {
		return currController;
	}

	public void changeView(ViewType view, Book book) throws GatewayException
	{	
		try
		{
			BorderPane newRoot = null;
			BorderPane currRoot = Launcher.getMainPane();
			if (view == ViewType.BOOK_LIST)
			{
				if (currController != null && currController.isBookDifferent())
				{
					Alert confirmAlert = new Alert(AlertType.NONE);
					confirmAlert.setHeaderText("Confirm Save Changes");
					confirmAlert.setContentText("The book has been modified. Do you want to save the changes?");
					confirmAlert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);
					// Gets the button that the user clicked on
					Optional<ButtonType> result = confirmAlert.showAndWait();
					if (result.get() == ButtonType.YES)
					{
						try
						{
								BookTableGateway gateway = new BookTableGateway();
								Book bdBook = currController.getSelectedBook();
								Book changedBook = new Book(bdBook.getId(), currController.getTfTitle().getText(), currController.getTfSummary().getText(), Integer.valueOf(currController.getTfYearPublished().getText()), currController.getTfISBN().getText(), bdBook.getLastModified(), bdBook.getDateAdded());
								// Book already exists in database, so lets update it
								if (gateway.isBookInDB(changedBook.getId()))
									gateway.updateBook(changedBook, "The changes made to the book could not be saved! Return to the book list and try again.");
								// It doesn't exist, so save it	
								else
									gateway.saveBook(changedBook);
								gateway.closeConnection();
								switchToListView(newRoot, currRoot);
							} catch (GatewayException e) {
								e.printStackTrace();
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
					Alert confirmAlert = new Alert(AlertType.NONE);
					confirmAlert.setHeaderText("Confirm Save Changes");
					confirmAlert.setContentText("The book has been modified. Do you want to save the changes?");
					confirmAlert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);
					// Gets the button that the user clicked on
					Optional<ButtonType> result = confirmAlert.showAndWait();
					if (result.get() != ButtonType.CANCEL)
					{
						if (result.get() == ButtonType.YES)
						{
							try
							{
									BookTableGateway gateway = new BookTableGateway();
									Book bdBook = currController.getSelectedBook();
									Book changedBook = new Book(bdBook.getId(), currController.getTfTitle().getText(), currController.getTfSummary().getText(), Integer.valueOf(currController.getTfYearPublished().getText()), currController.getTfISBN().getText(), bdBook.getLastModified(), bdBook.getDateAdded());
									// Book already exists in database, so lets update it
									if (gateway.isBookInDB(changedBook.getId()))
										gateway.updateBook(changedBook, "The changes made to the book could not be saved! Return to the book list and try again.");
									// It doesn't exist, so save it	
									else
										gateway.saveBook(changedBook);

									gateway.closeConnection();
									switchToListView(newRoot, currRoot);
								} 
								catch (GatewayException e) {
									e.printStackTrace();
								}
						}
						switchToDetailView(book, newRoot, currRoot);
					}
				}
				else
					switchToDetailView(book, newRoot, currRoot);
			}
		} catch (IOException ie)
		{
			logger.error("Failed to switch views");
			ie.printStackTrace();
		}
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
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/BookDetailView.fxml"));
		currController = new BookDetailController(book);
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
}
