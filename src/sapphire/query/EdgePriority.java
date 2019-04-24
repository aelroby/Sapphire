package sapphire.query;

import java.util.Comparator;

public class EdgePriority implements Comparator<Triple> {
	
	RelaxerMain main;
	
	public EdgePriority(RelaxerMain main) {
		this.main = main;
	}
	
	@Override
	public int compare(Triple edge1, Triple edge2) {
		int edge1Val = main.edgeCost.get(edge1);
		int edge2Val = main.edgeCost.get(edge2);
		return (edge1Val - edge2Val);
	}

}
