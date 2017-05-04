package ayhay.query;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Paint;
import java.awt.Stroke;
import java.io.File;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import javax.swing.JFrame;

import org.apache.commons.collections15.Transformer;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;

import ayhay.dataStructures.AlternativeToken;
import ayhay.dataStructures.SPARQLQuery;
import ayhay.utils.FileManager;
import ayhay.utils.RandomIDGenerator;
import ayhay.utils.SimpleTimestamp;
import ayhay.utils.Timer;
import edu.uci.ics.jung.algorithms.layout.CircleLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.shortestpath.DijkstraShortestPath;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.UndirectedSparseMultigraph;
import edu.uci.ics.jung.graph.util.Pair;
import edu.uci.ics.jung.visualization.BasicVisualizationServer;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.renderers.Renderer.VertexLabel.Position;


/**
 * This class suggests alternative queries to the one executed
 * This happens by finding alternatives to values in the triples
 * of the query.
 * It also mutates the structure of the query to find more alternatives
 * @author ahmed
 */
public class AlternativeQueryGenerator {

	private HashSet<String> expandedNodes = new HashSet<String>();
	
	// We use a map to map int id to an edge
	// This is because JUNG does not allow using an existing edge
	// to connect different vertices
	private HashMap<Integer, String> edgeMap = new HashMap<Integer, String>();
	private int edgeCntr = 0;
	
	/**
	 * Visualize the graph
	 * @param g The graph
	 */
	@SuppressWarnings({ "unchecked", "rawtypes", "unused" })
	private void visualizeGraph(Graph<String, Integer> g) {
		 // Layout<V, E>, BasicVisualizationServer<V,E>
		 Layout<String, Integer> layout = new CircleLayout(g);
		 layout.setSize(new Dimension(300,300));
		 BasicVisualizationServer<String, Integer> vv =
		 new BasicVisualizationServer<String, Integer>(layout);
		 vv.setPreferredSize(new Dimension(700,700));
		 // Setup up a new vertex to paint transformer...
		 Transformer<String,Paint> vertexPaint = new Transformer<String,Paint>() {
		 public Paint transform(String i) {
		 return Color.GREEN;
		 }
		 };
		 // Set up a new stroke Transformer for the edges
		 float dash[] = {10.0f};
		 final Stroke edgeStroke = new BasicStroke(1.0f, BasicStroke.CAP_BUTT,
		 BasicStroke.JOIN_MITER, 10.0f, dash, 0.0f);
		 Transformer<Integer, Stroke> edgeStrokeTransformer =
		 new Transformer<Integer, Stroke>() {
		 public Stroke transform(Integer s) {
		 return edgeStroke;
		 }
		 };
		 vv.getRenderContext().setVertexFillPaintTransformer(vertexPaint);
		 vv.getRenderContext().setEdgeStrokeTransformer(edgeStrokeTransformer);
		 vv.getRenderContext().setVertexLabelTransformer(new ToStringLabeller());
		 vv.getRenderContext().setEdgeLabelTransformer(new ToStringLabeller());
		 vv.getRenderer().getVertexLabelRenderer().setPosition(Position.CNTR);

		 JFrame frame = new JFrame("Simple Graph View 2");
		 frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		 frame.getContentPane().add(vv);
		 frame.pack();
		 frame.setVisible(true);
	}
	
	/**
	 * Expands the given node to find resources it is connected to
	 * @param Queue<String> nodes A queue of the nodes to be expanded
	 * @param Graph<String, String> g The graph to be updated
	 */
	private void expandNodes (Queue<String> nodes, Graph<String, Integer> g) {
		
		QueryManager queryManager = QueryManager.getInstance();
		
		int queueSize = nodes.size();
		for(int i = 0; i < queueSize; ++i) {
			String node = nodes.poll();
			if(!expandedNodes.contains(node)) {
				expandedNodes.add(node);
				// if node is a URI
				if(node.startsWith("<http")) {
					// node as subject
					String query = "SELECT ?p ?o WHERE { " + node + " ?p ?o}";
					System.out.println("Query: " + query);
					int id = RandomIDGenerator.getID();
					ResultSet results = queryManager.executeQuery(id, query);
					// update the graph and expansion queue
					while(results.hasNext()) {
						QuerySolution sol = results.next();
						String predicate = "<" + sol.get("p").toString() + ">";
						String object = sol.get("o").toString();
						if(object.startsWith("http")) {
							object = "<" + object + ">";
						}
						else {
							if(object.length() > 30 || object.contains("\"")) continue;
							if(object.contains("@")) {
								String[] parts = object.split("@");
								if(parts.length > 2) continue;
								object = "\"" + parts[0] + "\"@" + parts[1];
							}
							else {
								object = "\"" + object + "\"";
							}
						}
						if(predicate.compareTo(
								"<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>") == 0) {
							expandedNodes.add(object);
						}
						g.addVertex(object);
						edgeMap.put(edgeCntr, predicate);
						g.addEdge(edgeCntr++, node, object);
						nodes.add(object);
					}
					
					// node as object
					query = "SELECT ?s ?p WHERE {?s ?p " + node + "}";
					System.out.println("Query: " + query);
					id = RandomIDGenerator.getID();
					results = queryManager.executeQuery(id, query);
					// update the graph and expansion queue
					while(results.hasNext()) {
						QuerySolution sol = results.next();
						String predicate = "<" + sol.get("p").toString() + ">";
						String subject = "<" + sol.get("s").toString() + ">";
						if(predicate.compareTo(
								"<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>") == 0) {
							expandedNodes.add(subject);
						}
						g.addVertex(subject);
						edgeMap.put(edgeCntr, predicate);
						g.addEdge(edgeCntr++, subject, node);
						nodes.add(subject);
					}
					
				}
				// else if it is a literal
				else {
					String query = "SELECT ?s ?p WHERE {?s ?p " + node + "}";
					System.out.println("Query: " + query);
					int id = RandomIDGenerator.getID();
					ResultSet results = queryManager.executeQuery(id, query);
					// update the graph and expansion queue
					while(results.hasNext()) {
						QuerySolution sol = results.next();
						String predicate = "<" + sol.get("p").toString() + ">";
						String subject = "<" + sol.get("s").toString() + ">";
						g.addVertex(subject);
						edgeMap.put(edgeCntr, predicate);
						g.addEdge(edgeCntr++, subject, node);
						nodes.add(subject);
					}
				}
			}
		}
	}
	
	/**
	 * Find a path between the seeds in the query or their similar literals
	 * @param seeds The literals in the query 
	 * @param groups The groups similar literals
	 * @param g The graph
	 * @return
	 */
	private ArrayList<List<Integer>> findPaths (ArrayList<String> seeds,
			HashMap<String, Set<String>> groups,
			Graph<String, Integer> g) {
		
		ArrayList<List<Integer>> paths = new ArrayList<List<Integer>>();
		DijkstraShortestPath<String, Integer> alg = new DijkstraShortestPath<String, Integer>(g);
		
		Set<String> firstSeedSet = groups.get(seeds.get(0));
		Set<String> secondSeedSet = groups.get(seeds.get(1));
		
		for(String firstSetLiteral : firstSeedSet) {
			
			for(String secondSetLiteral : secondSeedSet) {
				List<Integer> l = alg.getPath(firstSetLiteral, secondSetLiteral);
				if(l.size() > 0) {
					paths.add(l);
				}
			}
			
		}
		
		
		return paths;
	}
	
	/**
	 * Relax the query. Important assumption: we assume that there are two literals
	 * in the query. This is a design choice. More literals in the query will make 
	 * query relaxation more complicated to present to the user as suggestions.
	 * TODO: Develop a way to present suggestions to the user for more complex suggestions.
	 * @param query The original SPARQL query
	 * @return ArrayList of alternative tokens
	 */
	public ArrayList<AlternativeToken> relaxQuery (SPARQLQuery query) {
		
		expandedNodes.clear();
		expandedNodes.add("<http://www.w3.org/2002/07/owl#Thing>");
		edgeCntr = 0;
		
		ArrayList<AlternativeToken> alternativeTokens = new ArrayList<AlternativeToken>();
		
		Graph<String, Integer> g = new UndirectedSparseMultigraph<String, Integer>();
		
		// seeds in the query
		ArrayList<String> seeds = new ArrayList<String>();
		
		// Groups for seeds
		HashMap<String, Set<String>> groups = new HashMap<String, Set<String>>();
		
		// Expansion queue
		Queue<String> q = new ArrayDeque<String>();
		
		// Find seeds
		for(int i = 0; i < query.where.size(); ++i) {
			// If this triple contains a literal, that's a seed
			if(query.where.get(i).get(2).contains("@en")) {
				seeds.add(query.where.get(i).get(2));
				Set<String> newGroupSet = new HashSet<String>();
				newGroupSet.add(seeds.get(seeds.size()-1));
				newGroupSet.addAll(ayhay.autoComplete.AutoComplete.
						warehouse.findSimilarStringsLiterals(
								seeds.get(seeds.size()-1), 0.8));
				groups.put(seeds.get(seeds.size()-1), newGroupSet);
				g.addVertex(seeds.get(seeds.size()-1));
				q.add(seeds.get(seeds.size()-1));
				for(String similarLiteral : newGroupSet) {
					g.addVertex(similarLiteral);
					q.add(similarLiteral);
				}
			}
		}
		
		// Do only 2 levels of expansions
		ArrayList<List<Integer>> paths = null;
		for(int i = 0; i < 2; ++i) {
			expandNodes(q, g);
		}
		paths = findPaths(seeds, groups, g);
		
		Collections.sort(paths, new Comparator<List<Integer>>(){
		    public int compare(List<Integer> a1, List<Integer> a2) {
		        return a1.size() - a2.size(); // shortest to longest
		    }
		});
		
		int minLength = paths.get(0).size();
		int currentLength = minLength;
		
		for(int i = 0; i < paths.size(); ++i) {
			currentLength = paths.get(i).size();
//			if(currentLength > minLength) break;
			System.out.println("Shortest path is: ");
			for(Integer edge : paths.get(i)) {
				Pair<String> pair = g.getEndpoints(edge);
				System.out.println("(" + pair.getFirst() + ", " +
				edgeMap.get(edge) +
				", " + pair.getSecond() + ")");
			}
		}
		
		
		
		
		// Generate alternative tokens
		
		
		return alternativeTokens;
	}
	
	/**
	 * Relax the predicate by finding different predicates 
	 * that return answers
	 * @param query The SPARQL query
	 * @return ArrayList of alternative tokens
	 */
	public ArrayList<AlternativeToken> relaxPredicates(SPARQLQuery query) {
		ArrayList<AlternativeToken> alternativeTokens = new ArrayList<AlternativeToken>();
		QueryManager queryManager = QueryManager.getInstance();
		
		// Find seeds and relax them
		for(int i = 0; i < query.where.size(); ++i) {
			// The predicate is not a variable, relax it
			if(!query.where.get(i).get(1).startsWith("?")) {
				SPARQLQuery newQuery = query.copyObject();
				newQuery.where.get(i).set(1, "?p");
				newQuery.select.add("?p");
				newQuery.updateQueryString();
				int id = RandomIDGenerator.getID();
				ResultSet results = queryManager.executeUserQuery(id, newQuery);
				if(results.hasNext()) {
					Set<String> relaxedPredicates = new HashSet<String>();
					while(results.hasNext()) {
						String answer = "<" + results.next().get("p").toString() + ">";
						relaxedPredicates.add(answer);
					}
					for(String relaxedObject : relaxedPredicates) {
						AlternativeToken newToken = new AlternativeToken(query.getWhere().get(i).get(0),
								query.getWhere().get(i).get(1), query.getWhere().get(i).get(2),
								relaxedObject, "P");
						newToken.setNumOfRows(1);
						alternativeTokens.add(newToken);
					}
				}
			}
		}
		return alternativeTokens;
	}
	
	/**
	 * Relax the query to find alternatives to its structure
	 * @param query The SPARQL query 
	 * @return ArrayList of alternative tokens
	 */
	public ArrayList<AlternativeToken> relaxStructure(SPARQLQuery query) {
		ArrayList<AlternativeToken> alternativeTokens = 
				new ArrayList<AlternativeToken>();
		
		QueryManager queryManager = QueryManager.getInstance();
		
		// Find seeds and relax them
		for(int i = 0; i < query.where.size(); ++i) {
			// If this triple contains a literal, that's a seed
			if(query.where.get(i).get(2).contains("@en")) {
				ArrayList<String> literalMatches = new ArrayList<String>();
				literalMatches.add(query.where.get(i).get(2));
				literalMatches.addAll(ayhay.autoComplete.AutoComplete.warehouse.findSimilarStringsLiterals(
								query.where.get(i).get(2), 1));
				for(String literalMatch : literalMatches) {
					SPARQLQuery newQuery = query.copyObject();
					newQuery.getWhere().get(i).set(2, "?s");
					ArrayList<String> newTriple = new ArrayList<String>();
					newTriple.add("?s");
					newTriple.add("?p");
					newTriple.add(literalMatch);
					newQuery.getWhere().add(newTriple);
					newQuery.select.add("?s");
					newQuery.updateQueryString();
					
					// Execute this query
					int id = RandomIDGenerator.getID();
					ResultSet results = queryManager.executeUserQuery(id, newQuery);
					int numOfRows = 0;
					if(results.hasNext()) {
						++numOfRows;
						Set<String> relaxedObjects = new HashSet<String>();
						while(results.hasNext()) {
							String answer = "<" + results.next().get("s").toString() + ">";
							relaxedObjects.add(answer);
						}
						for(String relaxedObject : relaxedObjects) {
							AlternativeToken newToken = new AlternativeToken(query.getWhere().get(i).get(0),
									query.getWhere().get(i).get(1), query.getWhere().get(i).get(2),
									relaxedObject, "O");
							newToken.setNumOfRows(numOfRows);
							alternativeTokens.add(newToken);
						}
					}
				}
			}
		}
		return alternativeTokens;
	}
	
	/**
	 * Find alternatives to the query seeds (literals in query)
	 * @param query The SPARQL query
	 * @return ArrayList of alternative tokens
	 */
	public ArrayList<AlternativeToken> findSimilarQueries(SPARQLQuery query) {
		
		// Initialize the log file
		File altTimeStatFile = new File("AlternativeQueriesTimeStatsSeconds.dat");
		if(!altTimeStatFile.exists()) {
			FileManager.writeToFile("AlternativeQueriesTimeStatsSeconds.dat",
					"AltPredicates,AltLiterals,AnswersAlternatives,RelaxQuery,RelaxPredicates\n");
		}		
		
		ArrayList<AlternativeToken> alternativeTokens = 
				new ArrayList<AlternativeToken>();
		ArrayList<String> alternativeQueries = 
				new ArrayList<String>();
		
		ArrayList<ArrayList<String>> where = query.getWhere();
		double timeForPredicates = 0;
		double timeForLiterals = 0;
		for(int i = 0; i < where.size(); ++i){
			ArrayList<String> clause = where.get(i);
			// If predicate and object in this clause are variables --> skip
			if(clause.get(1).startsWith("?") && 
					clause.get(2).startsWith("?")) {
				continue;
			}
			
			// Find alternatives for predicate
			if(!clause.get(1).startsWith("?")) {
				FileManager.appendToFileWithNewLine("AlternativeQueriesLog.dat", 
						SimpleTimestamp.getFormattedTimestamp() + 
						"Alternatives for predicate <" + clause.get(1) + ">:");
				Timer.start();
				ArrayList<String> alternatives = 
						ayhay.autoComplete.AutoComplete.warehouse.findSimilarStringsPredicates(clause);
				Timer.stop();
				timeForPredicates += Timer.getTimeInSeconds();
				System.out.println("Found alternatives for predicates in " + Timer.getTimeInSeconds() + " seconds");

				for(int j = 0; j < alternatives.size(); ++j){
					
					// Logging
					if(j == alternatives.size() - 1) {
						FileManager.appendToFileWithNewLine("AlternativeQueriesLog.dat", 
								alternatives.get(j));
					}
					else {
						FileManager.appendToFileNoNewLine("AlternativeQueriesLog.dat", 
								alternatives.get(j) + ",");
					}
					
					SPARQLQuery newQuery = query.copyObject();
					newQuery.getWhere().get(i).set(1, alternatives.get(j));
					newQuery.updateQueryString();
					alternativeQueries.add(newQuery.getQueryString());
					alternativeTokens.add(new AlternativeToken(clause.get(0), 
							clause.get(1), clause.get(2), alternatives.get(j), "P"));
				}
				
			}
			
			
			// Find alternatives for literals
			if(clause.get(2).startsWith("\"")) {
				FileManager.appendToFileWithNewLine("AlternativeQueriesLog.dat", 
						SimpleTimestamp.getFormattedTimestamp() + "Alternatives for literal \"" + clause.get(2) + "\":");
				Timer.start();
				ArrayList<String> alternatives = 
						ayhay.autoComplete.AutoComplete.warehouse.findSimilarStringsLiterals(clause.get(2), 0.7);
				Timer.stop();
				timeForLiterals += Timer.getTimeInSeconds();
				System.out.println("Found alternatives for literals in " + Timer.getTimeInSeconds() + " seconds");
				
				for(int j = 0; j < alternatives.size(); ++j){
					// Logging
					if(j == alternatives.size() - 1) {
						FileManager.appendToFileWithNewLine("AlternativeQueriesLog.dat", 
								alternatives.get(j));
					}
					else {
						FileManager.appendToFileNoNewLine("AlternativeQueriesLog.dat", 
								alternatives.get(j) + ",");
					}
					SPARQLQuery newQuery = query.copyObject();
					newQuery.getWhere().get(i).set(2, alternatives.get(j));
					newQuery.updateQueryString();
					alternativeQueries.add(newQuery.getQueryString());
					alternativeTokens.add(new AlternativeToken(clause.get(0), 
							clause.get(1), clause.get(2), alternatives.get(j), "O"));
				}
				
			}
			
		}
		
		FileManager.appendToFileNoNewLine("AlternativeQueriesTimeStatsSeconds.dat",
				timeForPredicates + ",");
		
		FileManager.appendToFileNoNewLine("AlternativeQueriesTimeStatsSeconds.dat",
				timeForLiterals + ",");

		System.out.println("Finding alternative literals finished!");
		
		System.out.println("Finding answers to " + alternativeQueries.size() + " alternative queries...");
		Timer.start();
		QueryManager queryManager = QueryManager.getInstance();
		for(int i = 0; i < alternativeQueries.size(); ++i){
			System.out.println("Query " + i + ": " + alternativeQueries.get(i));
			FileManager.appendToFileWithNewLine("AlternativeQueriesLog.dat", 
					SimpleTimestamp.getFormattedTimestamp() + 
					"Answering query \"" + alternativeQueries.get(i) + "\"");
			int id = RandomIDGenerator.getID();
			queryManager.executeQuery(id, alternativeQueries.get(i));
			int numOfRows = queryManager.getNumberOfResults(id);
			queryManager.closeQuery(id);
			alternativeTokens.get(i).setNumOfRows(numOfRows);
		}
		Timer.stop();
		FileManager.appendToFileNoNewLine("AlternativeQueriesTimeStatsSeconds.dat",
				Double.toString(Timer.getTimeInSeconds()) + ",");
		System.out.println("Answered alternatives in " + Timer.getTimeInSeconds() + " seconds");
		
		return alternativeTokens;
	}
	
}
