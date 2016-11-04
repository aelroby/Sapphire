package ayhay.dataStructures;

import java.util.ArrayList;
import java.util.Arrays;

public class SPARQLQuery {
	private ArrayList<String> select;
	private ArrayList<ArrayList<String>> where;
	private ArrayList<String> modifiers;
	private boolean isValid;
	
	private String queryString;
	
	public String getQueryString() {
		return queryString;
	}

	public boolean isValid() {
		return isValid;
	}
	
	public void setQueryString(String queryString) {
		this.queryString = queryString;
	}
	
	public ArrayList<String> getSelect() {
		return select;
	}

	public void setSelect(ArrayList<String> select) {
		this.select = select;
	}

	public ArrayList<ArrayList<String>> getWhere() {
		return where;
	}

	public void setWhere(ArrayList<ArrayList<String>> where) {
		this.where = where;
	}

	public ArrayList<String> getModifiers() {
		return modifiers;
	}

	public void setModifiers(ArrayList<String> modifiers) {
		this.modifiers = modifiers;
	}

	public SPARQLQuery(String[] query) {
		
		// Check the query syntax
		isValid = true;
 		for(int i = 2; i < query.length; i += 3){
 			if(!(query[i].startsWith("<") && query[i].endsWith(">") || query[i].startsWith("?")))
 				isValid = false;
 		}
 		for(int i = 3; i < query.length; i += 3){
 			if(!((query[i].startsWith("<") && query[i].endsWith(">")) || query[i].startsWith("\"") || query[i].startsWith("?")))
 				isValid = false;
 		}
 		
 		if(!isValid)
 			return;
		
		// First element is the select clause
		select = new ArrayList<String> (Arrays.asList(query[0].split(" ")));
		queryString = "SELECT " + query[0] + " WHERE{ ";
		
		where = new ArrayList<ArrayList<String>>();
		// If there are no modifiers
		if((query.length - 2) % 3 == 0){
			for(int i = 1; i < query.length -1; ){
				ArrayList<String> singleClause = new ArrayList<String>();
				singleClause.add(query[i]);
				queryString += query[i++] + " ";
				singleClause.add(query[i]);
				queryString += query[i++] + " ";
				singleClause.add(query[i]);
				queryString += query[i++] + ".";
				where.add(singleClause);
			}
			queryString += "}";
		}
		// There are modifiers
		else {
			for(int i = 1; i < query.length -2; ){
				ArrayList<String> singleClause = new ArrayList<String>();
				singleClause.add(query[i++]);
				singleClause.add(query[i++]);
				singleClause.add(query[i++]);
				where.add(singleClause);
			}
			// TODO: Handle modifier here
			
		}
	}
}
