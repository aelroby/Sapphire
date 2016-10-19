package ayhay.dataStructures;

import java.io.Serializable;

public class LiteralStat implements Serializable{

	private static final long serialVersionUID = 1L;
	private String literal;
	private int subjectTriples;
	
	public LiteralStat(String literal, int subjectTriples){
		this.literal = literal;
		this.subjectTriples = subjectTriples;
	}
	
	public String getLiteral() {
		return literal;
	}
	public void setLiteral(String literal) {
		this.literal = literal;
	}
	public int getSubjectTriples() {
		return subjectTriples;
	}
	public void setSubjectTriples(int subjectTriples) {
		this.subjectTriples = subjectTriples;
	}
	
	
}
