package controllers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import javafx.util.Callback;
import model.Book;
import enums.ViewType;
import exceptions.GatewayException;
import gateways.BookTableGateway;
import singleton.ViewManager;

public class BookListController 
{
	@FXML private ListView<Book> bookListView;
	@FXML Text fetched;
	@FXML TextField searchTF;
	@FXML Button search, first, prev, next, last;
	private BookTableGateway bGateway;
	private static Logger log = LogManager.getLogger();
	ObservableList<Book> bookList = FXCollections.observableArrayList();
	private String lowerCaseFilter = "";
	private static int maxRecord = 0;
	/**************************************************
	* Displays the books in the list view
	* @throws GatewayException
	****************************************************/
	public void initialize() throws GatewayException
	{
		bGateway = ViewManager.getInstance().getBookGateway();
		// By default we start on the page that shows the first 50 books so we don't need a previous button
		prev.setDisable(true);
		resetXandY();
		bookList = bGateway.getBooks(0, 50, lowerCaseFilter);
		maxRecord = bookList.size();
		searchHandler();
		populateListView();
		// is called when the user clicks somewhere on the list view
		bookListView.setOnMouseClicked(new EventHandler<MouseEvent>()
		{
			@Override
			public void handle(MouseEvent click) 
			{
				// If the user double clicks on a book title in the list view
				if (click.getClickCount() == 2 && bookListView.getSelectionModel().getSelectedItem() != null)
				{
					// Get the book that was clicked on
					Book selectedBook = bookListView.getSelectionModel().getSelectedItem();
					log.info("Double clicked on: " + selectedBook.getTitle());
					// Switch to detail view and display the values of the selected book.
					ViewManager.getInstance().changeView(ViewType.BOOK_DETAIL, selectedBook);
				}
			}
		});
	}
	/****************************************************
	*  Gets the list of books from the database and then 
	*  populates the list view with the booklist. 
	******************************************************/
	public void resetListView(int x, int y)
	{
		bookList = bGateway.getBooks(x, y, lowerCaseFilter);
		// Get the list of books from the database
		//bookListView.setItems(bookList);
		searchHandler();
		// Populate the list view with the books
		populateListView();
	}
	/********************************************
	* Populates the list view with the book list.
	**********************************************/
	public void populateListView()
	{
		// Display the data in the list view
		bookListView.setCellFactory(new Callback<ListView<Book>, ListCell<Book>>()
		{
			@Override
			public ListCell<Book> call(ListView<Book> bList) 
			{
				ListCell<Book> bCell = new ListCell<Book>()
				{
					@Override
					protected void updateItem(Book book, boolean empty)
					{	
						// Checks if we passed in a null object
						super.updateItem(book, empty);
						// If the book we passed in is not null, then we can display the book's title
						// to the list view
						if (book != null)
						{
							// Set the title of the book on the list view
							setText(book.getTitle());
							// Add a delete button to that book item, set it to the far right and display it
							Button btn = new Button("Delete");
							setGraphic(btn);
							// Handles the delete button when it's clicked on
							btn.setOnMouseClicked(new EventHandler<MouseEvent>()
							{
								@Override
								public void handle(MouseEvent click) 
								{
									// Delete the book from the database
									bGateway.deleteBook(book);
									// Update the listview to reflect changes
									resetListView(1, 0);
								}
							});
						}
					}
				};
				return bCell;
			}
		});
	}
	@FXML
	public void handleButtonAction(ActionEvent event)
	{
		if (event.getSource() == first)
		{
			if (bGateway.minVal <= 0)
				return;
			prev.setDisable(true);
			next.setDisable(false);
			resetXandY();
			resetListView(0, 50);
			maxRecord = bookList.size();
			displayFetched();
		}
		else if (event.getSource() == prev)
		{
			if (bGateway.minVal <= 0)
				return;
			next.setDisable(false);
			resetListView(-50, 0);
			maxRecord -= bookList.size();
			displayFetched();
		}
		else if (event.getSource() == next)
		{
			if (bGateway.minVal >= bGateway.getCount(lowerCaseFilter)-50)
				return;
			prev.setDisable(false);
			resetListView(50, 0);
			maxRecord += bookList.size();
			displayFetched();
		}
		else if (event.getSource() == last)
		{
			if (bookListView.getItems().size() < 50)
				return;
			next.setDisable(true);
			prev.setDisable(false);
			bGateway.minVal = 0;
			resetListView(bGateway.getCount(lowerCaseFilter)-50, 0);	
			maxRecord = bGateway.getCount(lowerCaseFilter);
			displayFetched();
		}
		else if (event.getSource() == search)
			searchHandler();
	}
	public void searchHandler()
	{
		// p-> true is a predicate that must be true. This is a lambda expression
		FilteredList<Book> filteredBooks = new FilteredList<>(bookList, p-> true);
		filteredBooks.setPredicate(book ->
		{
			// if filter is empty, display all books
			if(searchTF.getText() == null || searchTF.getText().isEmpty()) {
				lowerCaseFilter = "";
				return true;
			}
			lowerCaseFilter = searchTF.getText().toLowerCase();
			// filter matches, display books that contains that filter
			if(book.getTitle().toLowerCase().contains(lowerCaseFilter)) 
				return true;
			return false;
		});
		// Adds the remaining books to the list until it reaches 50
		if (filteredBooks.size() < 50 && bGateway.getCount(lowerCaseFilter) >= 50) 
			bookListView.setItems(bGateway.getBooks(0, 0, lowerCaseFilter));
		// Start at the beginning
		else if (filteredBooks.size() < 50 && bGateway.getCount(lowerCaseFilter) < 50)
		{
			resetXandY();
			bookListView.setItems(bGateway.getBooks(0, 50, lowerCaseFilter));
		}
		else
			bookListView.setItems(filteredBooks);
		populateListView();
		displayFetched();
	}
	public void displayFetched()
	{
		if (bookListView.getItems().size() < 50 && bookListView.getItems().size() >= 1)
			fetched.setText("Fetched records 1 to " + bookListView.getItems().size() + " out of " + bookListView.getItems().size());
		else if (bookListView.getItems().size() <= 0)
			fetched.setText("Fetched 0 records");
		else
		{
			if (maxRecord > bGateway.getCount(lowerCaseFilter))
				maxRecord = bGateway.getCount(lowerCaseFilter);
			fetched.setText("Fetched records " + (maxRecord-50) + " to " + maxRecord + " out of " + bGateway.getCount(lowerCaseFilter));
		}
	}
	public void resetXandY()
	{
		bGateway.minVal = 0;
		bGateway.maxVal = 0;
	}
}
