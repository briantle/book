package controllers;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;
import model.Book;

public class BookListController 
{
	@FXML private ListView<Book> bookList;
	
	public void initialize()
	{
		ObservableList<Book> items = bookList.getItems();
		
		// create some fake data for assignment 1
		for (int i = 1; i <= 3; i++)
		{
			Book book = new Book("Book " + i, "Summary", 1984, "4589", "03/03/2018");
			items.add(book);
		}
		
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
