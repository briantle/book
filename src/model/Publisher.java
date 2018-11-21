package model;

public class Publisher 
{
	private int id;
	private String publisherName;
	/*******************************************************
	* Constructor that defaults the publisher to be unknown
	*********************************************************/
	public Publisher() {
		id = 0;
		publisherName = "Unknown";
	}
	/*************** Setters ******************/
	public void setId(int id) {
		this.id = id;
	}
	public void setPublisherName(String publisherName) {
		this.publisherName = publisherName;
	}
	/************************* Getters ****************/
	public int getId() {
		return id;
	}
	public String getPublisherName() {
		return publisherName;
	}
}
