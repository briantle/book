package validation;

import java.util.Calendar;
/**
 * 
 * Validator is used to determine if the data stored in the model is valid or not. If it's not valid,
 * the book model will not be saved and stored in the database.
 * 
 * @author Brian Le
 *
 */
public class Validator
{
	/**
	 * 
	 * @param title - the book title stored in the book model
	 * @return
	 */
	public boolean validTitle(String title)
	{
		if (title.length() < 1 || title.length() > 255)
			return false;
		return true;
	}
	/**
	 * 
	 * @param summary - the book summary stored in the book model
	 * @return
	 */
	public boolean validSummary(String summary)
	{
		if (summary.length() >= 65536)
			return false;
		return true;
	}
	/**
	 * The year published must range from 1900 to the current year 
	 * 
	 * validYear checks to see if the yearPublished stored in the model is at least published in the year 1900 and
	 * doesn't exceed the current year.
	 * 
	 * @param yearPublished - the year published stored in the book model
	 * @return false for invalid year, otherwise true
	 */
	public boolean validYear(int yearPublished)
	{
		// Get the current year
		int currYear = Calendar.getInstance().get(Calendar.YEAR);
		// Year published must range between 1900 to current year
		if (yearPublished < 1900 || yearPublished > currYear)
			return false;
		return true;
	}
	/**
	 * The isbn cannot be > 13 characters and validISBN checks for that.
	 * 
	 * @param isbn - the isbn stored in the book model
	 * @return false if the isbn is invalid, otherwise true
	 */
	public boolean validISBN(String isbn)
	{
		if (isbn.length() > 13)
			return false;
		return true;
	}
}
