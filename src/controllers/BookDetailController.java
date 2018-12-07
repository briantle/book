package controllers;

import java.io.IOException;
import java.text.SimpleDateFormat;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import enums.ViewType;
import exceptions.GatewayException;
import gateways.AuthorBookTableGateway;
import gateways.BookTableGateway;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.Callback;
import javafx.util.converter.IntegerStringConverter;
import main.Launcher;
import model.Author;
import model.AuthorBook;
import model.Book;
import model.Publisher;
import singleton.ViewManager;
public class BookDetailController
{
	private static Logger log = LogManager.getLogger();
	private Book selectedBook;
	/**************************** FXML Variables *****************************/
	@FXML private Button saveButton, auditButton, addAuthorButton;
	@FXML private TextField tfTitle, tfSummary, tfYearPublished, tfISBN, royaltyTextField;
	@FXML private Label dateAdded;
	@FXML private ComboBox<Publisher> publisherComboBox;
	@FXML private ComboBox<Author> authorComboBox;
	@FXML TableView<AuthorBook> authorBookTable;
	@FXML TableColumn<AuthorBook, String> authorNameColumn;
	@FXML TableColumn<AuthorBook, Integer> royaltyColumn;
	@FXML TableColumn<AuthorBook, AuthorBook> deleteColumn;
	/****************** List Variables ******************/
	ObservableList<Publisher> publisherList;
	ObservableList<Author> authorList;
	ObservableList<AuthorBook> authorBookList;
	/*******************************************************************************
	* Constructor for book detail controller
	* @param book - the book to be displayed in the detail view
	* @param publisherList - the list of publishers that our database has
	*******************************************************************************/
	public BookDetailController(Book book, ObservableList<Publisher> publisherList){
		this.selectedBook = book;
		this.publisherList = publisherList;
		authorList = ViewManager.getInstance().getAuthorGateway().getAuthors();
	}
	/*******************************************************************
	* Populates the detail view with the values from the selected book.
	*********************************************************************/
	public void initialize() 
	{
		if (selectedBook != null)
			populateAuthorBookTable();
		// If we are adding a new book, we want to disable the audit button
		if (selectedBook == null)
		{
			// Disable the audit button from being clicked on
			auditButton.setDisable(true);
			// Set the selected book to be a new book with empty values
			selectedBook = new Book();
		}
		populateBookFields();
	}
	@FXML 
	/***********************************************
	* Handles the logic behind each button click.
	* @param action - the button that was pressed.
	* @throws IOException
	**************************************************/
	public void handleButtonAction(ActionEvent action) throws IOException 
	{
		if (action.getSource() == saveButton) 
		{
			log.info("Clicked on save button in book detail");
			try {
				saveBookChanges();
			} 
			// If an error occurred
			catch (GatewayException e)
			{
				// Display the error messages to the console
				e.printStackTrace();
				// Display an alert message in the book detail view
				ViewManager.getInstance().showAlert(AlertType.ERROR, "ERROR", e.getMessage());
			}
		}
		else if (action.getSource() == addAuthorButton)
		{
			try
			{
				if (authorComboBox.getSelectionModel().getSelectedItem() == null)
					ViewManager.getInstance().showAlert(AlertType.ERROR, "ERROR", "You must select an author from the combo box");
				else if (royaltyTextField.getText().isEmpty())
					ViewManager.getInstance().showAlert(AlertType.ERROR, "ERROR", "You must enter an amount for the royalty");
				else
				{
					isAuthorFieldsValid();
					addAuthorToTable();
				}
			}
			// The user either entered in words/letters or a number that is either too large to too small
			catch(NumberFormatException e)
			{
				e.printStackTrace();
				ViewManager.getInstance().showAlert(AlertType.ERROR, "ERROR", "You entered in an invalid royalty amount!");
			} catch (GatewayException e) {
				ViewManager.getInstance().showAlert(AlertType.ERROR, "ERROR", e.getMessage());
			}
		}
		// User clicked on audit trail button, so switch to the audit trail view
		else if (action.getSource() == auditButton) 
			ViewManager.getInstance().changeView(ViewType.AUDIT_TRAIL, selectedBook);
	}
	/***********************************************************************
	* Populate each of the book fields using values from the selected book.
	*************************************************************************/
	private void populateBookFields()
	{

		tfTitle.setText(selectedBook.getTitle());
		tfSummary.setText(selectedBook.getSummary());
		tfYearPublished.setText(String.valueOf(selectedBook.getYearPublished()));
		tfISBN.setText(selectedBook.getIsbn());
		// Displays the time stamp in Month/Day/Year format
		dateAdded.setText(new SimpleDateFormat("MM/dd/yyyy").format(selectedBook.getDateAdded()));
		// Populate the combo box with the list of publishers
		publisherComboBox.setItems(publisherList);
		populatePublisherComboBox();
		// Populate the combo box with the list of authors
		authorComboBox.setItems(authorList);
		populateAuthorComboBox();
		// Default the combo box value to whichever publisher the book has selected
		publisherComboBox.getSelectionModel().select(selectedBook.getPub().getId());
	}
	private void populateAuthorBookTable()
	{
		authorBookList = ViewManager.getInstance().getAuthorBookGateway().getAuthorsForBook(selectedBook, selectedBook.getId());
		populateTableColumns();
	}
	private void populateTableColumns()
	{
		authorBookTable.setItems(authorBookList);
		authorBookTable.setEditable(true);
		authorNameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getAuthor().getFirstName() 
				+ " " + cellData.getValue().getAuthor().getLastName()));
		royaltyColumn.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
		royaltyColumn.setCellValueFactory(new PropertyValueFactory<AuthorBook, Integer>("royalty"));
		addDeleteButtonToTableCell();
	}
	public void addDeleteButtonToTableCell()
	{
        Callback<TableColumn<AuthorBook, AuthorBook>, TableCell<AuthorBook, AuthorBook>> cellFactory = new Callback<TableColumn<AuthorBook, AuthorBook>, TableCell<AuthorBook, AuthorBook>>()
        {
            @Override
            public TableCell<AuthorBook, AuthorBook> call(final TableColumn<AuthorBook, AuthorBook> param) 
            {
                final TableCell<AuthorBook, AuthorBook> cell = new TableCell<AuthorBook, AuthorBook>()
                {
                    @Override
                    public void updateItem(AuthorBook authorBook, boolean empty)
                    {
                        super.updateItem(authorBook, empty);
                        Button deleteButton = new Button("Delete");
                        {
	                        if (empty) 
	                            setGraphic(null);
	                        else
	                        {
	                            setGraphic(deleteButton);
	                        	deleteButton.setOnAction((ActionEvent event) ->
	                            {
	                            	AuthorBook authorBookToBeRemoved = authorBookTable.getItems().get(super.getIndex());
	                                authorBookList.remove(authorBookToBeRemoved);
	                                // Reflect changes
	                                populateTableColumns();
	                            });
	                        }
                        }
                    }
                };
                return cell;
            }
        };
        deleteColumn.setCellFactory(cellFactory);
	}
	@FXML
	public void onEditChanged(TableColumn.CellEditEvent<AuthorBook, Integer> authorBookEditEvent)
	{
		AuthorBook authorBook = authorBookTable.getSelectionModel().getSelectedItem();
		authorBook.setRoyalty( ((double) authorBookEditEvent.getNewValue()) / 100000 );
		populateTableColumns();
	}
	/***************************************************************************
	* Populates the combo box with all the authors that our database houses.
	*****************************************************************************/
	private void populateAuthorComboBox()
	{
		Callback<ListView<Author>, ListCell<Author>> cellFactory = new Callback<ListView<Author>, ListCell<Author>>() 
		{
		    @Override
		    public ListCell<Author> call(ListView<Author> l) 
		    {
		        return new ListCell<Author>() 
		        {
		            @Override
		            protected void updateItem(Author author, boolean empty) 
		            {
		            	// Populate the combo box with the author
		                super.updateItem(author, empty);
		                if (author == null || empty) 
		                    setGraphic(null);
		                else 
		                    setText(author.getFirstName() + " " + author.getLastName());
		            }
		        };
		    }
		};
		authorComboBox.setButtonCell(cellFactory.call(null));
		authorComboBox.setCellFactory(cellFactory);
	}
	/***************************************************************************
	* Populates the combo box with all the publishers that our database houses.
	*****************************************************************************/
	private void populatePublisherComboBox()
	{
		Callback<ListView<Publisher>, ListCell<Publisher>> cellFactory = new Callback<ListView<Publisher>, ListCell<Publisher>>() 
		{
		    @Override
		    public ListCell<Publisher> call(ListView<Publisher> l) 
		    {
		        return new ListCell<Publisher>() 
		        {
		            @Override
		            protected void updateItem(Publisher pub, boolean empty) 
		            {
		            	// Populate the combo box with the publishers
		                super.updateItem(pub, empty);
		                if (pub == null || empty) 
		                    setGraphic(null);
		                else 
		                    setText(pub.getPublisherName());
		            }
		        };
		    }
		};
		publisherComboBox.setButtonCell(cellFactory.call(null));
		publisherComboBox.setCellFactory(cellFactory);
	}
	/**************************************************************************
	* Checks to see if the values of the book in the detail view has changed
	* @return false - book hasn't changed, true - book values have changed
	****************************************************************************/
	public boolean isBookDifferent()
	{
		/**************** Checks related to book values ***********************/
		// Empty so no need to check the rest of the values
		if (selectedBook.getSummary() == null && tfSummary.getText() == null)
			return false;
		
		// If one has an empty title and the other one doesn't
		if (selectedBook.getTitle() == "" && !tfTitle.getText().trim().isEmpty() || selectedBook.getTitle() != "" && tfTitle.getText().trim().isEmpty())
			return true;
		// If both have titles and they don't match
		else if (selectedBook.getTitle() != "" && !tfTitle.getText().trim().isEmpty() && selectedBook.getTitle().compareTo(tfTitle.getText()) != 0) 
			return true;
		// The summaries don't match
		if (selectedBook.getSummary().compareTo(tfSummary.getText()) != 0)
			return true;
		// If the year published don't match
		if (!Integer.valueOf(selectedBook.getYearPublished()).equals(Integer.valueOf(tfYearPublished.getText())))
			return true;
		// The ISBNs don't match
		if (selectedBook.getIsbn().compareTo(tfISBN.getText()) != 0)
			return true;
		// Publishers don't match
		if (selectedBook.getPub().getPublisherName().compareTo(publisherComboBox.getSelectionModel().getSelectedItem().getPublisherName()) != 0)
			return true;
		/**************************** Checks related to the author ***********************************/
		if (authorComboBox.getSelectionModel().getSelectedItem() != null)
			return true;
		if (!royaltyTextField.getText().isEmpty())
			return true;
		// Check the authorBookList to see if we've added a new author, or changed an author's royalty
		for (int i = 0; i < authorBookList.size(); i++) 
		{
			// The authorBook does not exist in the database
			if (authorBookList.get(i).isNewRecord())
				return true;
			// The authorBook exists in the database, so check if the royalty in the table has been changed from the one in the database
			else
			{
				int authorID = authorBookList.get(i).getAuthor().getId();
				int bookID = selectedBook.getId();
				int dbAuthorRoyalty = ViewManager.getInstance().getAuthorBookGateway().getAuthorBookByID(authorID, bookID).getRoyalty();
				if (dbAuthorRoyalty != authorBookList.get(i).getRoyalty())
					return true;
			}
		}
		// Check to see if we've removed an author from the authorBook list
		boolean isRemoved = false;
		ObservableList<AuthorBook> authorBookDB = ViewManager.getInstance().getAuthorBookGateway().getAuthorsForBook(selectedBook, selectedBook.getId());
		for (int i = 0; i < authorBookDB.size(); i++)
		{
			isRemoved = true;
			for (int j = 0; j < authorBookList.size(); j++)
			{
				if (authorBookDB.get(i).getAuthor().getId() == authorBookList.get(j).getAuthor().getId())
				{
					isRemoved = false;
					break;
				}
			}
			if (isRemoved)
				return true;
		}
		// All the fields are similar
		return false;
	}
	public void saveBookChanges() throws GatewayException
	{
		try
		{
			// Make a temporary book in case the validation fails
			Book newBook = new Book(selectedBook.getId(), tfTitle.getText(), tfSummary.getText(), Integer.valueOf(tfYearPublished.getText()), tfISBN.getText()
					               , selectedBook.getLastModified(), selectedBook.getDateAdded(), getPublisherSelection());
			
			/*************************** Error Checking ******************/
			// Make sure the values in the book are valid before saving them in the database
			newBook.validateBook();
			// Check if the user input any data to the author royalty or combo box and make sure they are valid
			isAuthorFieldsValid();
			// The book must have at least 1 author
			if (authorBookList.size() <= 0)
				throw new GatewayException("ERROR: Book must contain at least 1 author!");
			
			/************************** Save Changes Here ****************************/
			// If the user filled out the author royalty and combo box but didn't save them, save them and add to the authorBook list
			if (authorComboBox.getSelectionModel().getSelectedItem() != null && !royaltyTextField.getText().isEmpty())
				addAuthorToTable();
			// Get reference to the database
			BookTableGateway gateway = ViewManager.getInstance().getBookGateway();
			// This book doesn't exist in the database, so we are going to insert it into the database
			if (gateway.getBookByID(newBook.getId()) == null)
			{
				gateway.saveBook(newBook);
				for (int i = 0; i < authorBookList.size(); i++)
					ViewManager.getInstance().getAuthorBookGateway().addAuthorBook(authorBookList.get(i));
			}
			// The book already exists in the database, so let's update it
			else
			{
				gateway.updateBook(selectedBook, newBook, "Book is not up to date! Go back to the book list to get the updated version of the book.");
				AuthorBookTableGateway authorBookGateway = ViewManager.getInstance().getAuthorBookGateway();
				// Check if we have to add new authorBook records
				addNewRecords(authorBookGateway);
				// Check if we have to update or delete any authorBook records
				updateAndDeleteRecords(authorBookGateway);
			}
			// Copy the changes made to the original book
			selectedBook = newBook;
			// Refresh Table View
			populateAuthorBookTable();
			// If the audit trail button is disabled, enable it
			if (auditButton.isDisabled())
				auditButton.setDisable(false);
		}
		catch(NumberFormatException e) {
			e.printStackTrace();
			ViewManager.getInstance().showAlert(AlertType.ERROR, "ERROR", "You entered in an invalid royalty amount!");
		}
	}
	public void addNewRecords(AuthorBookTableGateway authorBookGateway)
	{
		for (int i = 0; i < authorBookList.size(); i++)
		{
			if (authorBookList.get(i).isNewRecord())
				authorBookGateway.addAuthorBook(authorBookList.get(i));
		}
	}
	public void updateAndDeleteRecords(AuthorBookTableGateway authorBookGateway)
	{
		// Used to check if we need to remove an authorbook from the database
		boolean isRemoved = false;
		ObservableList<AuthorBook> authorBookDB = authorBookGateway.getAuthorsForBook(selectedBook, selectedBook.getId());
		for (int i = 0; i < authorBookDB.size(); i++)
		{
			isRemoved = true;
			for (int j = 0; j < authorBookList.size(); j++)
			{
				if (authorBookDB.get(i).getAuthor().getId() == authorBookList.get(j).getAuthor().getId())
				{
					authorBookGateway.updateAuthorBook(authorBookList.get(j));
					isRemoved = false;
					break;
				}
			}
			if (isRemoved)
				authorBookGateway.deleteAuthorBook(authorBookDB.get(i));
		}
	}
	public void clearAddAuthorFields()
	{
		authorComboBox.getSelectionModel().clearSelection();
		royaltyTextField.clear();
	}
	public void isAuthorFieldsValid() throws GatewayException
	{
		if (authorComboBox.getSelectionModel().getSelectedItem() == null && !royaltyTextField.getText().isEmpty())
			throw new GatewayException("You must select an author from the combo box");
		else if (authorComboBox.getSelectionModel().getSelectedItem() != null && royaltyTextField.getText().isEmpty())
			throw new GatewayException("You must enter an amount for the royalty");
		else if (authorComboBox.getSelectionModel().getSelectedItem() != null && !royaltyTextField.getText().isEmpty())
		{
			// Check to see if the user actually passed in a number, an exception will occur if it's not a number
			Integer.parseInt(royaltyTextField.getText());
			// Check to see if user is trying to add an author that already exists in the authorBook list
			for (int i = 0; i < authorBookList.size(); i++)
			{
				// User has attempted to add an author that already exists, show an error message and leave function
				if (authorComboBox.getSelectionModel().getSelectedItem().getId() == authorBookList.get(i).getAuthor().getId())
				{
					throw new GatewayException("You are trying to add an author that is already associated with the book");
				}
			}
		}
	}
	public void addAuthorToTable()
	{
		// Create a new authorBook based on the combo box and text field
		AuthorBook authorBook = new AuthorBook();
		authorBook.setAuthor(authorComboBox.getSelectionModel().getSelectedItem());
		authorBook.setBook(selectedBook);
		authorBook.setNewRecord(true);
		authorBook.setRoyalty( ((double) Integer.parseInt(royaltyTextField.getText()) / 100000));
		// Add the new authorBook to the list and then refresh the table view to show the new authorBook
		authorBookList.add(authorBook);
		populateTableColumns();
		// Clear the fields after adding in a new author to the list
		clearAddAuthorFields();
	}
	/********************* Setters *******************/
	public void setSelectedBook(Book selectedBook) {
		this.selectedBook = selectedBook;
	}
	/******************** Getters ***********************/
	public Book getSelectedBook() {
		return selectedBook;
	}
	public TextField getTfTitle() {
		return tfTitle;
	}
	public TextField getTfSummary() {
		return tfSummary;
	}
	public TextField getTfYearPublished() {
		return tfYearPublished;
	}
	public TextField getTfISBN() {
		return tfISBN;
	}	
	public Publisher getPublisherSelection() {
		return publisherComboBox.getSelectionModel().getSelectedItem();
	}
}