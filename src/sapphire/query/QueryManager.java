package sapphire.query;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QueryParseException;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;

import sapphire.dataStructures.SPARQLQuery;



public class QueryManager {
	private String queryString;
	private Query query;
	private QueryExecution qexec;
	public boolean initialized = false;
	private HashMap<Integer, ResultSet> resultSetMap;
	private String endpointURL;
	
	private static QueryManager singleton = new QueryManager();
	
	
	private QueryManager () {
		resultSetMap = new HashMap<Integer, ResultSet>();
		//default URL
		endpointURL = "http://dbpedia.org/sparql";
	}
	
	public void setEndpointURL(String endpointURL) {
		this.endpointURL = endpointURL; 
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
		try{
		query = QueryFactory.create(queryString);
		}
		catch(QueryParseException e) {
			System.out.println("Query Parse Exception for query: " + queryString);
			return null;
		}
		
		qexec = QueryExecutionFactory.sparqlService(endpointURL, query);
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
		resultSetMap.remove(id);
	}

	public static QueryManager getEndpointQMInstance() {
		return singleton;
	}

}
