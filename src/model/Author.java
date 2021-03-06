package model;

import java.time.LocalDate;

import exceptions.GatewayException;
import validation.Validator;

public class Author 
{
	private int id;
	private String firstName;
	private String lastName;
	private LocalDate dateOfBirth;
	private String gender;
	private String website;
	/************************************************
	* A No Argument Constructor for the Author model
	**************************************************/
	public Author()
	{
		id = 0;
		firstName = "";
		lastName = "";
		dateOfBirth = null;
		gender = "";
		website = "";
	}
	/************************************************************
	* A constructor for the Author model that contains arguments
	**************************************************************/
	public Author(int id, String firstName, String lastName, LocalDate dob, String gender, String website)
	{
		this.id = id;
		this.firstName = firstName;
		this.lastName = lastName;
		this.dateOfBirth = dob;
		this.gender = gender;
		this.website = website;
	}
	public void validateAuthor() throws GatewayException 
	{
		Validator val = new Validator();
		if (!val.validName(this.firstName, this.lastName))
			throw new GatewayException("Invalid Author Name: Author not saved!");
		if (!val.validDate(this.dateOfBirth))
			throw new GatewayException("Invalid Date of Birth: Author not saved!");
		if (!val.ValidWebsite(this.website))
			throw new GatewayException("Invalid Website: Author not saved!");
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
