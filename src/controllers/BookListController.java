package controllers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import javafx.util.Callback;
import model.Book;
import enums.ViewType;
import exceptions.GatewayException;
import gateways.BookTableGateway;
import singleton.ViewManager;

public class BookListController 
{
	@FXML private ListView<Book> bookList;
	private static Logger log = LogManager.getLogger();
	
	public void initialize() throws GatewayException
	{
		// Get the list of books from the database and then display them on the listView
		resetListView();
		// is called when the user clicks somewhere on the list view
		bookList.setOnMouseClicked(new EventHandler<MouseEvent>()
		{
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
	/****************************************************
	*  Gets the list of books from the database and then 
	*  populates the list view with the booklist. 
	******************************************************/
	public void resetListView()
	{
		// Get the list of books from the database
		bookList.setItems(ViewManager.getInstance().getBookGateway().getBooks());
		// Populate the list view with the books
		populateListView();		
	}
	/********************************************
	* Populates the list view with the book list.
	**********************************************/
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
						{
							// Set the title of the book on the list view
							setText(b.getTitle());
							// Add a delete button to that book item, set it to the far right and display it
							Button btn = new Button("Delete");
							btn.setTranslateX(800);
							setGraphic(btn);
							// Handles the delete button when it's clicked on
							btn.setOnMouseClicked(new EventHandler<MouseEvent>()
							{
								@Override
								public void handle(MouseEvent click) 
								{
									// Get a refernce to the database
									BookTableGateway gw = ViewManager.getInstance().getBookGateway();
									// Delete the book
									gw.deleteBook(b);
									// Update the listview to reflect changes
									resetListView();
								}
							});
						}
					}
				};
				return bCell;
			}
		});
	}
}
