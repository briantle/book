package controllers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import enums.ViewType;
import exceptions.GatewayException;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import javafx.util.Callback;
import model.Author;
import singleton.ViewManager;

public class AuthorListController 
{
	@FXML private ListView<Author> authorListView;
	private static Logger log = LogManager.getLogger();
	/*******************************************************************************
	* Will automatically populate the list view upon loading up the scene.
	* Will handle actions related to user double clicking on an author in the view
	* @throws GatewayException
	********************************************************************************/
	public void initialize() throws GatewayException
	{
		// Get the list of authors from the database and then display them on the listView
		resetListView();
		// is called when the user clicks somewhere on the list view
		authorListView.setOnMouseClicked(new EventHandler<MouseEvent>()
		{
			@Override
			public void handle(MouseEvent click) 
			{
				// If the user double clicks on an author's name in the list view
				if (click.getClickCount() == 2 && authorListView.getSelectionModel().getSelectedItem() != null)
				{
					// Get the author that was clicked on
					Author selectedAuthor = authorListView.getSelectionModel().getSelectedItem();
					log.info("Double clicked on: " + selectedAuthor.getFirstName() + " " + selectedAuthor.getLastName());
					// Switch to detail view and display the values of the selected author.
					ViewManager.getInstance().changeView(ViewType.AUTHOR_DETAIL, selectedAuthor);
				}
			}
		});
	}
	/****************************************************
	*  Gets the list of authors from the database and then 
	*  populates the list view with the author list 
	******************************************************/
	public void resetListView()
	{
		// Get the list of authors from the database
		authorListView.setItems(ViewManager.getInstance().getAuthorGateway().getAuthors());
		// Populate the list view with the authors
		populateListView();		
	}
	/**********************************************
	* Populates the list view with the author list.
	************************************************/
	public void populateListView()
	{
		// Display the data in the list view
		authorListView.setCellFactory(new Callback<ListView<Author>, ListCell<Author>>()
		{
			@Override
			public ListCell<Author> call(ListView<Author> authorList) 
			{
				ListCell<Author> authorCell = new ListCell<Author>()
				{
					@Override
					protected void updateItem(Author author, boolean empty)
					{	
						// Checks if we passed in a null object
						super.updateItem(author, empty);
						// If the author we passed in is not null, then we can display the author's full name
						// to the list view
						if (author != null)
						{
							
							// Set the name of the author on the list view
							setText(author.getFirstName() + " " + author.getLastName());
							
							// Add a delete button to that author item, set it to the far right and display it
							Button deleteButton = new Button("Delete");
							deleteButton.setTranslateX(800);
							setGraphic(deleteButton);
							
							// Handles the delete button when it's clicked on
							deleteButton.setOnMouseClicked(new EventHandler<MouseEvent>()
							{
								@Override
								public void handle(MouseEvent click) 
								{
									// Delete the author from the database
									ViewManager.getInstance().getAuthorGateway().deleteAuthor(author.getId());
									// Update the list view to reflect changes
									resetListView();
								}
							});
							
						}
					}
				};
				return authorCell;
			}
		});
	}
}
