package ayhay.query;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.apache.jena.query.ResultSet;

import ayhay.dataStructures.AlternativeToken;
import ayhay.dataStructures.SPARQLQuery;
import ayhay.utils.RandomIDGenerator;
import ayhay.utils.Timer;


/**
 * This class suggests alternative queries to the one executed
 * This happens by finding alternatives to values in the triples
 * of the query.
 * It also mutates the structure of the query to find more alternatives
 * @author ahmed
 */
public class AlternativeQueryGenerator {

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
				int numOfRows = 0;
				if(results.hasNext()) {
					Set<String> relaxedPredicates = new HashSet<String>();
					while(results.hasNext()) {
						++numOfRows;
						String answer = "<" + results.next().get("p").toString() + ">";
						relaxedPredicates.add(answer);
					}
					for(String relaxedObject : relaxedPredicates) {
						AlternativeToken newToken = new AlternativeToken(query.getWhere().get(i).get(0),
								query.getWhere().get(i).get(1), query.getWhere().get(i).get(2),
								relaxedObject, "P");
						newToken.setNumOfRows(numOfRows);
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
	public ArrayList<AlternativeToken> relaxQuery(SPARQLQuery query) {
		ArrayList<AlternativeToken> alternativeTokens = 
				new ArrayList<AlternativeToken>();
		
		QueryManager queryManager = QueryManager.getInstance();
		
		// Find seeds and relax them
		for(int i = 0; i < query.where.size(); ++i) {
			// If this triple contains a literal, that's a seed
			if(query.where.get(i).get(2).contains("@en")) {
				SPARQLQuery newQuery = query.copyObject();
				newQuery.getWhere().get(i).set(2, "?s");
				ArrayList<String> newTriple = new ArrayList<String>();
				newTriple.add("?s");
				newTriple.add("?p");
				newTriple.add(query.where.get(i).get(2));
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
		return alternativeTokens;
	}
	
	/**
	 * Find alternatives to the query seeds (values of predicates and literal)
	 * @param query The SPARQL query
	 * @return ArrayList of alternative tokens
	 */
	public ArrayList<AlternativeToken> findSimilarQueries(SPARQLQuery query) {
		ArrayList<AlternativeToken> alternativeTokens = 
				new ArrayList<AlternativeToken>();
		ArrayList<String> alternativeQueries = 
				new ArrayList<String>();
		
		ArrayList<ArrayList<String>> where = query.getWhere();
		for(int i = 0; i < where.size(); ++i){
			ArrayList<String> clause = where.get(i);
			// If predicate and object in this clause are variables --> skip
			if(clause.get(1).startsWith("?") && 
					clause.get(2).startsWith("?")) {
				continue;
			}
			
			// Find alternatives for predicate
			if(!clause.get(1).startsWith("?")) {
				Timer.start();
				ArrayList<String> alternatives = 
						ayhay.autoComplete.AutoComplete.warehouse.findSimilarStringsPredicates(clause);

				for(int j = 0; j < alternatives.size(); ++j){
					SPARQLQuery newQuery = query.copyObject();
					newQuery.getWhere().get(i).set(1, alternatives.get(j));
					newQuery.updateQueryString();
					alternativeQueries.add(newQuery.getQueryString());
					alternativeTokens.add(new AlternativeToken(clause.get(0), 
							clause.get(1), clause.get(2), alternatives.get(j), "P"));
				}
				Timer.stop();
				System.out.println("Found alternatives for predicates in " + Timer.getTimeInSeconds() + " seconds");
			}
			
			
			// Find alternatives for literals
			if(clause.get(2).startsWith("\"")) {
				Timer.start();
				ArrayList<String> alternatives = 
						ayhay.autoComplete.AutoComplete.warehouse.findSimilarStringsLiterals(clause.get(2));
				
				for(int j = 0; j < alternatives.size(); ++j){
					SPARQLQuery newQuery = query.copyObject();
					newQuery.getWhere().get(i).set(2, alternatives.get(j));
					newQuery.updateQueryString();
					alternativeQueries.add(newQuery.getQueryString());
					alternativeTokens.add(new AlternativeToken(clause.get(0), 
							clause.get(1), clause.get(2), alternatives.get(j), "O"));
				}
				Timer.stop();
				System.out.println("Found alternatives for literals in " + Timer.getTimeInSeconds() + " seconds");
			}
			
		}

		System.out.println("Finding alternative literals finished!");
		
		System.out.println("Finding answers to " + alternativeQueries.size() + " alternative queries...");
		Timer.start();
		QueryManager queryManager = QueryManager.getInstance();
		for(int i = 0; i < alternativeQueries.size(); ++i){
			int id = RandomIDGenerator.getID();
			queryManager.executeQuery(id, alternativeQueries.get(i));
			int numOfRows = queryManager.getNumberOfResults(id);
			queryManager.closeQuery(id);
			alternativeTokens.get(i).setNumOfRows(numOfRows);
		}
		Timer.stop();
		System.out.println("Answered alternatives in " + Timer.getTimeInSeconds() + " seconds");
		
		return alternativeTokens;
	}
	
}
