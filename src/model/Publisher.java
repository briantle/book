package model;

public class Publisher 
{
	private int id;
	private String publisherName;
	
	public Publisher() {
		id = 0;
		publisherName = "Unknown";
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getPublisherName() {
		return publisherName;
	}

	public void setPublisherName(String publisherName) {
		this.publisherName = publisherName;
	}
	
}
