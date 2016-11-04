package ayhay.query;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.tdb.TDBFactory;

import ayhay.dataStructures.SPARQLQuery;



public class QueryManager {
	private String queryString;
	private Query query;
	private Dataset dataset;
	private Model model;
	private QueryExecution qexec;
	public boolean initialized = false;
	private HashMap<Integer, ResultSet> resultSetMap;
	
	
	public void test(){
/*		long startTime = System.currentTimeMillis();
		String directory = "/media/windows/Work/Programs/apache-jena-fuseki-2.3.0/DB/";
		initializeDataset(directory);
		String queryString = "SELECT DISTINCT ?o (count(distinct ?object) as ?count) WHERE{?s ?p ?o. ?s ?predicate ?object FILTER(isLiteral(?o) && langMatches(lang(?o), \"EN\"))} GROUP BY ?o";
//		Query query = QueryFactory.create(queryString);
		@SuppressWarnings("unused")
		ResultSet results = executeQuery(queryString);
		String answers = getResultsAsText();
		BufferedWriter writer;
		try {
			writer = new BufferedWriter(new FileWriter("output"));
			writer.write (answers);
		} catch (IOException e) {
			e.printStackTrace();
		}
		long stopTime = System.currentTimeMillis();
		double elapsedTime = stopTime - startTime;
		elapsedTime = elapsedTime / 60000;
		System.out.println("Warehouse initialization took: " + elapsedTime + " minutes");*/
	}
	
	public void initializeDataset(String TDBDirectory){
		dataset = TDBFactory.createDataset(TDBDirectory);
		model = dataset.getDefaultModel();
		initialized = true;
		resultSetMap = new HashMap<Integer, ResultSet>();
	}
	
	public synchronized int getNumberOfResults(int id){
		int n = 0;
		ResultSet results = resultSetMap.get(id);
		while(results.hasNext()){
			++n;
			results.next();
		}
		return n;
	}
	
	public synchronized ResultSet executeUserQuery(int id, SPARQLQuery sparqlQuery){
		return executeQuery(id, sparqlQuery.getQueryString());
	}
	
	public synchronized ResultSet executeQuery(int id, String queryString){
		this.queryString = queryString;
		query = QueryFactory.create(queryString);
		qexec = QueryExecutionFactory.sparqlService("http://dbpedia.org/sparql", query);
		ResultSet results = qexec.execSelect();
		resultSetMap.put(id, results);
		return results;
	}
	
	public synchronized void printLastResultSet(int id){
		System.out.println("Query: " + queryString);
		System.out.println("Result Set:");
		ResultSetFormatter.out(System.out, resultSetMap.get(id), query);
	}
	
	public synchronized String getResultsAsJSON(int id){
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ResultSetFormatter.outputAsJSON(baos, resultSetMap.get(id));
		return baos.toString();
	}
	
	public synchronized String getResultsAsText(int id){
		return ResultSetFormatter.asText(resultSetMap.get(id));
	}
	
	public synchronized void closeQuery(int id){
		qexec.close();
		resultSetMap.remove(id);
	}
}
