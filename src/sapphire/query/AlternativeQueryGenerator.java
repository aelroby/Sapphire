package sapphire.query;

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

import edu.uci.ics.jung.algorithms.layout.CircleLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.shortestpath.DijkstraShortestPath;
import edu.uci.ics.jung.algorithms.shortestpath.MinimumSpanningForest2;
import edu.uci.ics.jung.graph.DelegateForest;
import edu.uci.ics.jung.graph.DelegateTree;
import edu.uci.ics.jung.graph.Forest;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.UndirectedSparseMultigraph;
import edu.uci.ics.jung.graph.util.Pair;
import edu.uci.ics.jung.visualization.BasicVisualizationServer;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.renderers.Renderer.VertexLabel.Position;
import sapphire.dataStructures.AlternativeToken;
import sapphire.dataStructures.Path;
import sapphire.dataStructures.SPARQLQuery;
import sapphire.utils.FileManager;
import sapphire.utils.RandomIDGenerator;
import sapphire.utils.SimpleTimestamp;
import sapphire.utils.Timer;


/**
 * This class suggests alternative queries to the one executed
 * This happens by finding alternatives to values in the triples
 * of the query.
 * It also mutates the structure of the query to find more alternatives
 * @author ahmed
 */
public class AlternativeQueryGenerator {

	private HashSet<String> expandedNodes;
	
	private HashSet<String> similarPredicates;
	
	private ArrayList<String> queryPredicates;
	
	private int edgeCntr = 0;
	
	private int queryNum = 0;
	
	private int connectingNodesNum = 0;
	
	private int expandedNodesCntr = 0;
	
	private double branchingFactor = 0;
	
	public class CustomEdge {
		 private double weight;
		 private String label;
		 int id;

		 public CustomEdge(double weight, String label) {
			 this.id = edgeCntr++; // This is defined in the outer class.
			 this.setWeight(weight);
			 this.setLabel(label);
		 }
		 
		 public String toString() { 
			 return "E"+id;
		 }

		public double getWeight() {
			return weight;
		}

		public void setWeight(double weight) {
			this.weight = weight;
		}

		public String getLabel() {
			return label;
		}

		public void setLabel(String label) {
			this.label = label;
		}
	 }
	
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
	private void expandNodes (Queue<String> nodes, Graph<String, CustomEdge> g) {
		
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
					queryNum++;
					ResultSet results = queryManager.executeQuery(id, query);
					// update the graph and expansion queue
					if(results != null){
						while(results.hasNext()) {
							++connectingNodesNum;
							QuerySolution sol = results.next();
							String predicate = "<" + sol.get("p").toString() + ">";
							String object = sol.get("o").toString();
							if(object.startsWith("http")) {
								object = "<" + object + ">";
								if(predicate.compareTo("<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>") == 0) {
									expandedNodes.add(object);
								}
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
							double defaultWeight = 3;
							// whatever after the last / without the >
							String trimmedPredicate = predicate.substring(
									predicate.lastIndexOf("/")+1,
									predicate.length()-1).toLowerCase();
							for(int j = 0; j < queryPredicates.size(); ++ j) {
								String trimmedQueryPredicate = queryPredicates.get(j).substring(
										queryPredicates.get(j).lastIndexOf("/")+1,
										queryPredicates.get(j).length()-1).toLowerCase();
								if(trimmedQueryPredicate.compareTo(trimmedPredicate) == 0) {
									defaultWeight = 1;
								}
							}
							for(String synsetString : similarPredicates) {
								String trimmedQueryPredicate = synsetString.substring(
										synsetString.lastIndexOf("/")+1,
										synsetString.length()-1).toLowerCase();
								if(trimmedQueryPredicate.compareTo(trimmedPredicate) == 0) {
									defaultWeight = 1;
								}
							}
							g.addEdge(new CustomEdge(defaultWeight, predicate), node, object);
							nodes.add(object);
						}
					}
					
					// node as object
					query = "SELECT ?s ?p WHERE {?s ?p " + node + "}";
					System.out.println("Query: " + query);
					id = RandomIDGenerator.getID();
					queryNum++;
					results = queryManager.executeQuery(id, query);
					// update the graph and expansion queue
					while(results.hasNext()) {
						++connectingNodesNum;
						QuerySolution sol = results.next();
						String predicate = "<" + sol.get("p").toString() + ">";
						String subject = "<" + sol.get("s").toString() + ">";
						if(predicate.compareTo(
								"<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>") == 0) {
							expandedNodes.add(subject);
						}
						g.addVertex(subject);
						double defaultWeight = 3;
						// whatever after the last / without the >
						String trimmedPredicate = predicate.substring(
								predicate.lastIndexOf("/")+1,
								predicate.length()-1).toLowerCase();
						for(int j = 0; j < queryPredicates.size(); ++ j) {
							String trimmedQueryPredicate = queryPredicates.get(j).substring(
									queryPredicates.get(j).lastIndexOf("/")+1,
									queryPredicates.get(j).length()-1).toLowerCase();
							if(trimmedQueryPredicate.compareTo(trimmedPredicate) == 0) {
								defaultWeight = 1;
							}
						}
						for(String synsetString : similarPredicates) {
							String trimmedQueryPredicate = synsetString.substring(
									synsetString.lastIndexOf("/")+1,
									synsetString.length()-1).toLowerCase();
							if(trimmedQueryPredicate.compareTo(trimmedPredicate) == 0) {
								defaultWeight = 1;
							}
						}
						g.addEdge(new CustomEdge(defaultWeight, predicate), subject, node);
						nodes.add(subject);
					}
					++expandedNodesCntr;
				}
				// else if it is a literal
				else {
					String query = "SELECT ?s ?p WHERE {?s ?p " + node + "}";
					System.out.println("Query: " + query);
					int id = RandomIDGenerator.getID();
					queryNum++;
					ResultSet results = queryManager.executeQuery(id, query);
					// update the graph and expansion queue
					while(results.hasNext()) {
						++connectingNodesNum;
						QuerySolution sol = results.next();
						String predicate = "<" + sol.get("p").toString() + ">";
						String subject = "<" + sol.get("s").toString() + ">";
						g.addVertex(subject);
						double defaultWeight = 3;
						// whatever after the last / without the >
						String trimmedPredicate = predicate.substring(
								predicate.lastIndexOf("/")+1,
								predicate.length()-1).toLowerCase();
						for(int j = 0; j < queryPredicates.size(); ++ j) {
							String trimmedQueryPredicate = queryPredicates.get(j).substring(
									queryPredicates.get(j).lastIndexOf("/")+1,
									queryPredicates.get(j).length()-1).toLowerCase();
							if(trimmedQueryPredicate.compareTo(trimmedPredicate) == 0) {
								defaultWeight = 1;
							}
						}
						for(String synsetString : similarPredicates) {
							String trimmedQueryPredicate = synsetString.substring(
									synsetString.lastIndexOf("/")+1,
									synsetString.length()-1).toLowerCase();
							if(trimmedQueryPredicate.compareTo(trimmedPredicate) == 0) {
								defaultWeight = 1;
							}
						}
						g.addEdge(new CustomEdge(defaultWeight, predicate), subject, node);
						nodes.add(subject);
					}
					++expandedNodesCntr;
				}
			}
		}
		System.out.println("Number of queries in expansion: " + queryNum);
		branchingFactor = 1.0 * connectingNodesNum / expandedNodesCntr;
		System.out.println("Branching Factor: " + branchingFactor);
	}
	
	/**
	 * Find the Steiner tree between the seeds in the query or their similar literals
	 * @param seeds The literals in the query 
	 * @param groups The groups similar literals
	 * @param g The graph
	 * @return
	 */
	private ArrayList<Path> findSteinerTree (ArrayList<String> seeds,
			HashMap<String, Set<String>> groups,
			Graph<String, CustomEdge> g) {
		
		ArrayList<Path> paths = new ArrayList<Path>();
		
		Transformer<CustomEdge, Double> wtTransformer = new Transformer<CustomEdge,Double>() {
			 public Double transform(CustomEdge edge) {
				 return edge.weight;
			 }
		};
		
//		ArrayList<Set<String>> seedsSets = new ArrayList<Set<String>>();
//		for(int i = 0; i < seeds.size(); ++i) {
//			seedsSets.add(groups.get(seeds.get(i)));
//		}
//		
//		boolean notDone = true;
//		while(notDone){
//			MinimumSpanningForest2<String, CustomEdge> spanningTree = new MinimumSpanningForest2<String, CustomEdge>(g,
//					new DelegateForest<String, CustomEdge>(), DelegateTree.<String, CustomEdge>getFactory(),wtTransformer);
//			
//			Forest<String, CustomEdge> tree = spanningTree.getForest();
//			
//			ArrayList<CustomEdge> edges = new ArrayList<CustomEdge>(tree.getEdges());
//			
//			paths.add(edges);
//			
//		}
		
		
		
		
		DijkstraShortestPath<String, CustomEdge> alg = new DijkstraShortestPath<String, CustomEdge>(g,
				wtTransformer);
		
		Set<String> firstSeedSet = groups.get(seeds.get(0));
		Set<String> secondSeedSet = groups.get(seeds.get(1));
		
		for(String firstSetLiteral : firstSeedSet) {
			
			for(String secondSetLiteral : secondSeedSet) {
				List<CustomEdge> l = alg.getPath(firstSetLiteral, secondSetLiteral);
				if(l.size() > 0) {
					double distance = (double) alg.getDistance(firstSetLiteral, secondSetLiteral);
					paths.add(new Path(l, distance));
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
		
		expandedNodes = new HashSet<String>();
		edgeCntr = 0;
		
		ArrayList<AlternativeToken> alternativeTokens = new ArrayList<AlternativeToken>();
		
		Graph<String, CustomEdge> g = new UndirectedSparseMultigraph<String, CustomEdge>();
		
		// seeds in the query
		ArrayList<String> seeds = new ArrayList<String>();
		// predicates in the query
		queryPredicates = new ArrayList<String>();
		
		similarPredicates = new HashSet<String>();
		
		// Groups for seeds
		HashMap<String, Set<String>> groups = new HashMap<String, Set<String>>();
		
		// Expansion queue
		Queue<String> q = new ArrayDeque<String>();
		
		// Find seeds
		for(int i = 0; i < query.where.size(); ++i) {
			// update predicates
			queryPredicates.add(query.where.get(i).get(1));
			
			similarPredicates.addAll(
					sapphire.autoComplete.AutoComplete.warehouse.findSimilarStringsPredicates(
							query.where.get(i).get(1)));
			
			queryPredicates.addAll(similarPredicates);
			
			// If this triple contains a literal, that's a seed
			if(query.where.get(i).get(2).contains("@en")) {
				seeds.add(query.where.get(i).get(2));
				Set<String> newGroupSet = new HashSet<String>();
				newGroupSet.add(seeds.get(seeds.size()-1));
				newGroupSet.addAll(sapphire.autoComplete.AutoComplete.
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
		ArrayList<Path> paths = null;
		for(int i = 0; i < 2; ++i) {
			expandNodes(q, g);
		}
		System.out.println("Number of vertices: " + g.getVertexCount());
		System.out.println("Number of edges: " + g.getEdgeCount());
		paths = findSteinerTree(seeds, groups, g);
		
		Collections.sort(paths, new Comparator<Path>(){
		    public int compare(Path a1, Path a2) {
		        return (int) (a1.getCost() - a2.getCost()); // shortest to longest
		    }
		});
		
		for(int i = 0; i < paths.size(); ++i) {
			String example = "";
			System.out.println("Shortest path is: ");
			for(CustomEdge edge : paths.get(i).getPath()) {
				Pair<String> pair = g.getEndpoints(edge);
				example += pair.getFirst() + "--" +
						edge.getLabel() +
						"--" + pair.getSecond() + ".";
				System.out.println(example);
			}
			alternativeTokens.add(new AlternativeToken(example, "X"));
		}
		
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
				literalMatches.addAll(sapphire.autoComplete.AutoComplete.warehouse.findSimilarStringsLiterals(
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
						sapphire.autoComplete.AutoComplete.warehouse.findSimilarStringsPredicates(clause.get(1));
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
						sapphire.autoComplete.AutoComplete.warehouse.findSimilarStringsLiterals(clause.get(2), 0.7);
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
