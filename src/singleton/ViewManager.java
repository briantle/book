package singleton;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import controllers.BookDetailController;
import enums.ViewType;
import javafx.fxml.FXMLLoader;
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

	public void changeView(ViewType view, Book book)
	{	
		try
		{
			currController = null;
			BorderPane newRoot = null;
			BorderPane currRoot = Launcher.getMainPane();
			if (view == ViewType.BOOK_LIST)
			{
				currController = null;
				newRoot = (BorderPane) FXMLLoader.load(getClass().getResource("/fxml/BookListView.fxml"));
			}
			else if (view == ViewType.BOOK_DETAIL)
			{
				FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/BookDetailView.fxml"));
				
				currController = new BookDetailController(book);
				loader.setController(currController);
				newRoot = loader.load();
			}
			// Clears the view in order to prevent overlap
			currRoot.setCenter(null);
			// Swap to new view
			currRoot.setCenter(newRoot);
		} catch (IOException ie)
		{
			logger.error("Failed to switch views");
			ie.printStackTrace();
		}
	}
	
	public static ViewManager getInstance()
	{
		if (instance == null)
			instance = new ViewManager();
		return instance;
	}
}
