package model;

import java.sql.Timestamp;
/**
 * 
 * Audit Trails should be creates for all changes except delete
 * When a book is inserted, the message should be:
 * 		Book added
 * When a book is updated, the message should be:
 * 		"ﬁeld name" changed from "old value" to "new value"
 * @author Brian
 *
 */
public class AuditTrailEntry 
{
	int id;
	Timestamp dateAdded;
	String message;
	
	public AuditTrailEntry(String message) {
		this.message = message;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Timestamp getDateAdded() {
		return dateAdded;
	}

	public void setDateAdded(Timestamp dateAdded) {
		this.dateAdded = dateAdded;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
	
	
}
