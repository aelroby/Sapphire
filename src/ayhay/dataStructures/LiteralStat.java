package ayhay.dataStructures;

import java.io.Serializable;

public class LiteralStat implements Serializable{

	private static final long serialVersionUID = 1L;
	private String literal;
	private int index;
	private int frequency;
	
	public LiteralStat(String literal, int index, int frequency){
		this.literal = literal;
		this.index = index;
		this.frequency = frequency;
	}
	
	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}
	
	public String getLiteral() {
		return literal;
	}
	
	public void setLiteral(String literal) {
		this.literal = literal;
	}
	
	public int getFrequency() {
		return frequency;
	}
	
	public void setFrequency(int subjectTriples) {
		this.frequency = subjectTriples;
	}
	
	
}
