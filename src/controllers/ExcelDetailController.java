package controllers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import gateways.PublisherTableGateway;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
import model.Author;
import model.AuthorBook;
import model.Book;
import model.Publisher;
import singleton.ViewManager;

public class ExcelDetailController 
{
	@FXML private Button saveButton;
	@FXML private TextField saveField;
	@FXML private ComboBox<Publisher> publisherComboBox;
	@FXML private Pane paneId;
	
	ObservableList<Publisher> publisherList = FXCollections.observableArrayList();
	ObservableList<AuthorBook> authorBookList = FXCollections.observableArrayList();
	
	public ExcelDetailController(PublisherTableGateway pubGateway) {
		publisherList = pubGateway.fetchPublishers();
	}
	
	public void initialize()
	{
		publisherComboBox.setItems(publisherList);
		populateComboBox();
	}
	
	public void populateComboBox()
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
	
	@FXML
	public void saveHandler()
	{
		//Create blank workbook
		XSSFWorkbook workbook = new XSSFWorkbook(); 
		//Create a blank sheet
		XSSFSheet spreadsheet = workbook.createSheet("Publisher Royalty Report");
	    // Create row object
		XSSFRow row;
		int rowId = 0;
		Cell cell;
		String publisherName = publisherComboBox.getSelectionModel().getSelectedItem().getPublisherName();
		String timeStamp = new SimpleDateFormat("MMMM dd, yyyy HH:mm").format(Calendar.getInstance().getTime());
		String headings[] = {"Royalty Report", "Publisher: " + publisherName, "Report generated on " + timeStamp};
		String bookRows[] = {"Book Title", "ISBN", "Author", "Royalty"};
		authorBookList = ViewManager.getInstance().getAuthorBookGateway().getAuthorBooksByPublisher(publisherComboBox.getSelectionModel().getSelectedItem());
		for (int i = 0; i < 3; i++)
		{
			row = spreadsheet.createRow(rowId++);
			cell = row.createCell(0);
			cell.setCellValue(headings[i]);
		}
		row = spreadsheet.createRow(rowId++);
		row = spreadsheet.createRow(rowId++);
		for (int i = 1; i < 5; i++)
		{
			cell = row.createCell(i-1);
			cell.setCellValue(bookRows[i-1]);
		}
		String prevBookName = "";
		Book book;
		Author author;
		double currRoyalty = 0.0;
		double totalRoyalty = 0.0;
		for (int i = 0; i < authorBookList.size(); i++)
		{
			book = authorBookList.get(i).getBook();
			author = authorBookList.get(i).getAuthor();
			if (book.getTitle().compareTo(prevBookName) == 0)
			{
				cell = row.createCell(2);
				cell.setCellValue(author.getFirstName() + " " + author.getLastName());
				cell = row.createCell(3);
				currRoyalty = ((double) authorBookList.get(i).getRoyalty()) / 1000;
				totalRoyalty += currRoyalty;
				cell.setCellValue(currRoyalty + "%");
			}
			else
			{
				// Display the total royalty only if we have finished displaying the first book
				if (i != 0)
				{
					displayTotalRoyalty(spreadsheet, row, totalRoyalty);
					// Create an empty row to separate the books
					row = spreadsheet.createRow(rowId++);
				}
				// Reset royalty amounts
				totalRoyalty = 0.0;
				currRoyalty = 0.0;
				row = spreadsheet.createRow(rowId++);
				cell = row.createCell(0);
				cell.setCellValue(book.getTitle());
				cell = row.createCell(1);
				cell.setCellValue(book.getIsbn());
				cell = row.createCell(2);
				cell.setCellValue(author.getFirstName() + " " + author.getLastName());
				cell = row.createCell(3);
				currRoyalty = ((double) authorBookList.get(i).getRoyalty()) / 1000;
				totalRoyalty += currRoyalty;
				cell.setCellValue(currRoyalty + "%");
			}
			prevBookName = book.getTitle();
			row = spreadsheet.createRow(rowId++);
		}
		// Display the total royalties for the last book
		displayTotalRoyalty(spreadsheet, row, totalRoyalty);
		// Create an output file that will be saved to the directory the user has specified
		FileOutputStream fout;
		try
		{
			fout = new FileOutputStream(new File(saveField.getText()), false);
			workbook.write(fout);
			fout.close();
		} 
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	@FXML
	public void browseHandler()
	{
		final DirectoryChooser dirChooser = new DirectoryChooser();
		Stage stage = (Stage) paneId.getScene().getWindow();
		File file = dirChooser.showDialog(stage);
		
		if (file != null)
			saveField.setText(file.getAbsolutePath() + "\\" + "file.xlsx");
	}
	public void displayTotalRoyalty(XSSFSheet spreadsheet, XSSFRow row, double totalRoyalty)
	{
		Cell cell;
		cell = row.createCell(2);
		cell.setCellValue("Total Royalty");
		cell = row.createCell(3);
		cell.setCellValue(totalRoyalty + "%");
	}
}
