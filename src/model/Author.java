package model;

import java.time.LocalDate;

public class Author 
{
	private int id;
	private String firstName;
	private String lastName;
	private LocalDate dateOfBirth;
	private String gender;
	private String website;
	
	public Author()
	{
		id = 0;
		firstName = "";
		lastName = "";
		dateOfBirth = null;
		gender = "";
		website = null;
	}
	/**************** Setters **********************/
	public void setId(int id) {
		this.id = id;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public void setDateOfBirth(LocalDate dateOfBirth) {
		this.dateOfBirth = dateOfBirth;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public void setWebsite(String website) {
		this.website = website;
	}
	/****************** Getters *********************/
	public int getId() {
		return id;
	}
	public String getFirstName() {
		return firstName;
	}
	public String getLastName() {
		return lastName;
	}
	public LocalDate getDateOfBirth() {
		return dateOfBirth;
	}
	public String getGender() {
		return gender;
	}
	public String getWebsite() {
		return website;
	}
	
}
