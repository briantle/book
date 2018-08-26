package singleton;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import enums.ViewType;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.BorderPane;
import main.Launcher;

public class ViewManager
{
	private static final Logger logger = LogManager.getLogger(ViewManager.class);
	private static ViewManager instance = null;
	
	public void changeView(ViewType view, Object data)
	{	
		try
		{
			BorderPane newRoot = null;
			BorderPane currRoot = Launcher.getMainPane();
			if (view == ViewType.BOOK_LIST)
				newRoot = (BorderPane) FXMLLoader.load(getClass().getResource("/fxml/BookListView.fxml"));
			else if (view == ViewType.BOOK_DETAIL)
				newRoot = (BorderPane) FXMLLoader.load(getClass().getResource("/fxml/BookDetailView.fxml"));
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
