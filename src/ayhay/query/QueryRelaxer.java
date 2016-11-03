package ayhay.query;

import java.util.ArrayList;

import ayhay.dataStructures.SPARQLQuery;

public class QueryRelaxer {

	
	private SPARQLQuery parseQuery(String query) {
		SPARQLQuery sQuery = new SPARQLQuery(query);
		return sQuery;
	}
	
	public ArrayList<String> relaxQuery(String query) {
		ArrayList<String> relaxedQueries = new ArrayList<String>();
		SPARQLQuery sQuery = parseQuery(query);
		
		return relaxedQueries;
	}
	
}
