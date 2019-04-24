package sapphire.query;

import java.util.Comparator;

public class TriplePriority implements Comparator<Triple> {
	
	RelaxerMain main = null;
	
	public TriplePriority(RelaxerMain main) {
		this.main = main;
	}

	@Override
	public int compare(Triple tripleA, Triple tripleB) {
		// TODO Auto-generated method stub
		int tripleAVal = main.costSoFar.get(tripleA);
		int tripleBVal = main.costSoFar.get(tripleB);
		return (tripleAVal - tripleBVal);
	}

}
