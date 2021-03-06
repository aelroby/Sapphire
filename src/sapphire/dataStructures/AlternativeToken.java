package sapphire.dataStructures;

import java.util.ArrayList;

/**
 * This class describes an alternative token for the query.
 * A token is a triple.
 * This class keeps the original subject, predicate, and objects
 * and the new value of the changed value.
 * 
 * @author ahmed
 */
public class AlternativeToken {
	
	private String subject, predicate, object, newValue, example;
	private String type;	// Possible values for type are "S" for subject,
							// "P" for predicate, "O" for object, and "X" for example
							// The newValue variable holds the alternative value
							// for the modified type except for the case of an example
	
	public String getExample() {
		return example;
	}
	
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	private int numOfRows;
	
	/**
	 * Original triple and new value
	 * @param subject original subject
	 * @param predicate original predicate
	 * @param object original object
	 * @param newValue new value of subject, predicate, or object
	 * @param type type of the new value "S", "P", or "O"
	 */
	public AlternativeToken(String subject, String predicate, String object, String newValue, String type){
		this.subject = subject;
		this.predicate = predicate;
		this.object = object;
		this.newValue = newValue;
		this.type = type;
	}
	
	public AlternativeToken(String example, String type) {
		this.example = example;
		this.type = type;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getPredicate() {
		return predicate;
	}

	public void setPredicate(String predicate) {
		this.predicate = predicate;
	}

	public String getObject() {
		return object;
	}

	public void setObject(String object) {
		this.object = object;
	}

	public String getNewValue() {
		return newValue;
	}
	public void setNewValue(String newValue) {
		this.newValue = newValue;
	}
	public int getNumOfRows() {
		return numOfRows;
	}
	public void setNumOfRows(int numOfRows) {
		this.numOfRows = numOfRows;
	}

}
