package controllers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.TextAlignment;
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
	BookTableGateway gw = null;
	
	public void initialize() throws GatewayException
	{
		// Get a reference to the database and connect to it
		gw = new BookTableGateway();
		// Get the list of books from the database and then display them on the listView
		bookList.setItems(gw.getBooks());
		populateListView();
		// Log out of database
		gw.closeConnection();
		
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
					try {
						ViewManager.getInstance().changeView(ViewType.BOOK_DETAIL, selectedBook);
					} catch (GatewayException e) {
						e.printStackTrace();
					}
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
						{
							setText(b.getTitle());
							Button btn = new Button("Delete");
							btn.setTranslateX(800);
							setGraphic(btn);
							btn.setOnMouseClicked(new EventHandler<MouseEvent>()
							{
								@Override
								public void handle(MouseEvent click) 
								{
									try {
										// Get a refernce to the database
										BookTableGateway gw = new BookTableGateway();
										// Delete the book
										gw.deleteBook(b);
										// Close connection to database
										gw.closeConnection();
										// Update the listview to reflect changes
										ViewManager.getInstance().changeView(ViewType.BOOK_LIST, null);
									} catch (GatewayException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
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
