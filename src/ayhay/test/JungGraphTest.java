package ayhay.test;

import java.util.List;


import edu.uci.ics.jung.algorithms.shortestpath.DijkstraShortestPath;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.UndirectedSparseMultigraph;

/**
 * Test class for the more efficient JUNG graph library.
 * This test should be similar to how it is used in Sapphire
 * @author ahmed
 *
 */
public class JungGraphTest { 

	public static void main(String[] args) {
		
        Graph<Integer, String> g = new UndirectedSparseMultigraph<Integer, String>();
        // Add some vertices. From above we defined these to be type Integer.
        g.addVertex((Integer)1);
        g.addVertex((Integer)2);
        g.addVertex((Integer)3); 
        // Add some edges. From above we defined these to be of type String
        // Note that the default is for undirected edges.
        g.addEdge("Edge-A", 1, 2); // Note that Java 1.5 auto-boxes primitives
        g.addEdge("Edge-B", 2, 3);
        g.addEdge("Edge-C", 2, 3);
        // Let's see what we have. Note the nice output from the SparseMultigraph<V,E> toString() method
        System.out.println("The graph g = " + g.toString());

        DijkstraShortestPath<Integer, String> alg = new DijkstraShortestPath<Integer, String>(g);
        List<String> l = alg.getPath(1, 3);
        System.out.println("The shortest unweighted path from 1 to 3 is:");
	    System.out.println(l.toString());
    
	}
}
