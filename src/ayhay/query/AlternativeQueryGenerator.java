package ayhay.query;

import java.util.ArrayList;

import ayhay.dataStructures.AlternativeToken;
import ayhay.dataStructures.SPARQLQuery;
import ayhay.main.MainServlet;
import ayhay.utils.RandomIDGenerator;


/**
 * This class suggests alternative queries to the one executed
 * This happens by finding alternatives to values in the triples
 * of the query.
 * It also mutates the structure of the query to find more alternatives
 * @author ahmed
 */
public class AlternativeQueryGenerator {

	
	/**
	 * Relax the query to find alternatives to its structure
	 * @param query The SPARQL query 
	 * @return ArrayList of alternative queries
	 */
	public ArrayList<SPARQLQuery> relaxQuery(SPARQLQuery query) {
		ArrayList<SPARQLQuery> alternativeQueries = 
				new ArrayList<SPARQLQuery>();
		
		// TODO: Magic happens here
		
		return alternativeQueries;
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
				ArrayList<String> alternatives = 
						ayhay.autoComplete.AutoComplete.warehouse.findSimilarStringsPredicates(clause.get(1));

				for(int j = 0; j < alternatives.size(); ++j){
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
			}
			
		}

		System.out.println("Finding alternative literals finished!");
		
		System.out.println("Finding answers to alternative queries...");
		for(int i = 0; i < alternativeQueries.size(); ++i){
			int id = RandomIDGenerator.getID();
			MainServlet.queryManager.executeQuery(id, alternativeQueries.get(i));
			int numOfRows = MainServlet.queryManager.getNumberOfResults(id);
			MainServlet.queryManager.closeQuery(id);
			alternativeTokens.get(i).setNumOfRows(numOfRows);
		}
		
		return alternativeTokens;
	}
	
}
