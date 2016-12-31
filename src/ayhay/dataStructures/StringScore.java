package ayhay.dataStructures;

public class StringScore {

	private String s;
	private int index;
	private double score;
	
	public StringScore(String s, double score){
		this.s = s;
		this.score = score;
	}
	
	public StringScore(String string, double score, int index) {
		this.s = string;
		this.score = score;
		this.index = index;
	}

	public int getIndex() {
		return index;
	}
	
	public String getS() {
		return s;
	}
	public void setS(String s) {
		this.s = s;
	}
	public double getScore() {
		return score;
	}
	public void setScore(double score) {
		this.score = score;
	}
	
	
}
