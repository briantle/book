package controllers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import javafx.util.Callback;
import model.Book;
import enums.ViewType;
import singleton.ViewManager;

public class BookListController 
{
	@FXML private ListView<Book> bookList;
	private static Logger log = LogManager.getLogger();
	
	public void initialize()
	{
		ObservableList<Book> items = bookList.getItems();
		
		// create some fake data for assignment 1
		// and populate the list with that data
		for (int i = 1; i <= 3; i++)
		{
			Book book = new Book("Book " + i, "Summary", 1984, "4589", "03/03/2018");
			items.add(book);
		}
		populateListView();
		
		// is called when the user clicks somewhere on the list view
		bookList.setOnMouseClicked(new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent click) 
			{
				// If the user double clicks on a book title in the list view
				if (click.getClickCount() == 2 && bookList.getSelectionModel().getSelectedItem() != null)
				{
					Book selectedBook = bookList.getSelectionModel().getSelectedItem();
					log.info("Double clicked on: " + selectedBook.getTitle());
					ViewManager.getInstance().changeView(ViewType.BOOK_DETAIL, selectedBook);
				}
			}
			
		});
	}
	
	public void populateListView()
	{
		// Display the data in the list view
		bookList.setCellFactory(new Callback<ListView<Book>, ListCell<Book>>()
		{
			@Override
			public ListCell<Book> call(ListView<Book> bList) 
			{
				ListCell<Book> bCell = new ListCell<Book>()
				{
					@Override
					protected void updateItem(Book b, boolean empty)
					{
						// Checks if we passed in a null object
						super.updateItem(b, empty);
						// If the book we passed in is not null, then we can display the book's title
						// to the list view
						if (b != null)
							setText(b.getTitle());
					}
				};
				return bCell;
			}
		});
	}
}
