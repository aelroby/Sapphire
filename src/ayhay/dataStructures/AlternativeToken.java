package ayhay.dataStructures;


/**
 * This class described ana alternative token for the query.
 * A token is a triple.
 * This class keeps the original subject, predicate, and objects
 * and the new value of the changed value.
 * 
 * @author ahmed
 */
public class AlternativeToken {
	
	private String subject, predicate, object, newValue;
	private String type;
	
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	private int numOfRows;
	
	public AlternativeToken(String subject, String predicate, String object, String newValue, String type){
		this.subject = subject;
		this.predicate = predicate;
		this.object = object;
		this.newValue = newValue;
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
