package ayhay.query;

import java.util.ArrayList;

import ayhay.dataStructures.AlternativeToken;
import ayhay.dataStructures.SPARQLQuery;
import ayhay.main.MainServlet;

public class AlternativeQueryGenerator {

	public ArrayList<SPARQLQuery> relaxQuery(SPARQLQuery query) {
		ArrayList<SPARQLQuery> alternativeQueries = 
				new ArrayList<SPARQLQuery>();
		
		/* Magic happens here */
		
		return alternativeQueries;
	}
	
	public ArrayList<AlternativeToken> findSimilarQueries(SPARQLQuery query) {
		ArrayList<AlternativeToken> alternativeQueries = 
				new ArrayList<AlternativeToken>();
		
		/* Magic happens here */
		System.out.println("Finding alternative predicates...");
		// Find alternatives for each triple predicate
		for(int i = 2; i < inputs.length -1 ; i += 3){
			if(inputs[i].startsWith("?"))	// variable. Don't find alternative for that
				continue;
			alternatives = ayhay.autoComplete.AutoComplete.warehouse.findSimilarStringsPredicates(inputs[i]);
			for(int j = 0; j < alternatives.size(); ++j){
				query = "SELECT " + inputs[0] + " WHERE{ ";
		        for(int k = 1; k < inputs.length - 1; ){
		        	query += inputs[k++] + " ";
		        	if(k == i){
		        		query += alternatives.get(j) + " ";
		        		++k;
		        	}
		        	else{
		        		query += inputs[k++] + " ";
		        	}
		        	query += inputs[k++] + ".";
		        }
		        query += "}";
		        alternativeQueries.add(query);
		        alternativeTokens.add(new AlternativeToken(inputs[i-1], inputs[i], inputs[i+1], alternatives.get(j), "P"));
			}
		}
		System.out.println("Finding alternative predicates finished!");
		
		// Find alternatives for each triple literal
		System.out.println("Finding alternative literals...");
		for(int i = 3; i < inputs.length -1; i += 3){
			if(inputs[i].startsWith("?"))	// variable. Don't find alternative for that
				continue;
			alternatives = ayhay.autoComplete.AutoComplete.warehouse.findSimilarStringsLiterals(inputs[i]);
			for(int j = 0; j < alternatives.size(); ++j){
				query = "SELECT " + inputs[0] + " WHERE{ ";
		        for(int k = 1; k < inputs.length - 1; ){
		        	query += inputs[k++] + " ";
		        	query += inputs[k++] + " ";
		        	if(k == i){
		        		query += alternatives.get(j) + " ";
		        		++k;
		        	}
		        	else{
		        		query += inputs[k++] + " ";
		        	}
		        	query += ".";
		        }
		        query += "}";
		        alternativeQueries.add(query);
		        alternativeTokens.add(new AlternativeToken(inputs[i-2], inputs[i-1], inputs[i], alternatives.get(j), "O"));
			}
		}
		System.out.println("Finding alternative literals finished!");
		
		System.out.println("Finding answers to alternative queries...");
		for(int i = 0; i < alternativeQueries.size(); ++i){
			int id = (int) Math.random() * 10000;
			MainServlet.queryManager.executeQuery(id, alternativeQueries.get(i));
			int numOfRows = MainServlet.queryManager.getNumberOfResults(id);
			MainServlet.queryManager.closeQuery(id);
			alternativeTokens.get(i).setNumOfRows(numOfRows);
		}
		
		return alternativeQueries;
	}
	
}
