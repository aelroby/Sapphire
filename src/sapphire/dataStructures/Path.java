package sapphire.dataStructures;

import java.util.List;

import sapphire.query.AlternativeQueryGenerator.CustomEdge;

public class Path {
	
	private List<CustomEdge> path;
	private double cost;
	
	public Path(List<CustomEdge> path, double cost) {
		this.path = path;
		this.cost = cost;
	}

	public List<CustomEdge> getPath() {
		return path;
	}

	public double getCost() {
		return cost;
	}
	
	
	
}
