package ayhay.dataStructures;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * This class is a representation of the SPARQL query.
 * It contains the following:
 * SELECT values
 * WHERE Triples
 * Modifiers
 * SPARQL query as a String
 * @author ahmed
 *
 */
public class SPARQLQuery {
	public ArrayList<String> select;
	public ArrayList<ArrayList<String>> where;
	public ArrayList<String> modifiers;
	public boolean isValid;		// This query is valid and can be executed
	public boolean hasModifiers;	// This query has modifiers
	public String queryString;
	
	/**
	 * 
	 * @return A copy of this SPARQL query object
	 */
	public SPARQLQuery copyObject() {
		SPARQLQuery newObject = new SPARQLQuery();
		
		// Copy select
		ArrayList<String> newSelect = new ArrayList<String>();
		for(String s : select) {
			newSelect.add(s);
		}
		newObject.setSelect(newSelect);
		
		// Copy where
		ArrayList<ArrayList<String>> newWhere = 
				new ArrayList<ArrayList<String>>();
		for(ArrayList<String> clause : where) {
			ArrayList<String> newClause = 
					new ArrayList<String>();
			for(String s : clause) {
				newClause.add(s);
			}
			newWhere.add(newClause);
		}
		newObject.setWhere(newWhere);
		
		// Copy modifiers
		if(hasModifiers){
			ArrayList<String> newModifiers = 
					new ArrayList<String>();
			for(String s : modifiers) {
				newModifiers.add(s);
			}
			newObject.setModifiers(newModifiers);
		}
		
		// Copy valid flag
		newObject.setValid(isValid);
		
		// Copy query string
		newObject.setQueryString(queryString);
		
		return newObject;
	}
	
	public void setValid(boolean isValid) {
		this.isValid = isValid;
	}
	
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

	/* TODO: Handle modifiers*/
	public void updateQueryString() {
		queryString = "SELECT ";
		for(String s : select) {
			queryString += s + " ";
		}
		queryString += "WHERE{ ";
		
		for(ArrayList<String> clause : where) {
			
			for(String s : clause) {
				queryString += s + " ";
			}
			
			queryString += ".";
		}
		queryString += "}";
	}
	
	public SPARQLQuery(String[] query, String[] filters) {
		
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
 		// There's a whitespace at the beginning from the client. It is removed
		select = new ArrayList<String> (Arrays.asList(query[0].substring(1).split("\\s+")));
		queryString = "SELECT " + query[0] + " WHERE{ ";
		
		where = new ArrayList<ArrayList<String>>();
		// If there are no modifiers
		if((query.length - 2) % 3 == 0){
			hasModifiers = false;
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
			
			System.out.println(filters.length);
			System.out.println(filters[0]);
			System.out.println(filters[1]);
			
			if (filters.length>0 && filters.length%3==0 && !filters[0].equalsIgnoreCase("-1") && !filters[1].equalsIgnoreCase("-1") ){

				queryString += " FILTER (";
				

				if(filters[1].equalsIgnoreCase("n") || filters[1].equalsIgnoreCase("v")){
					queryString += filters[0] + " ";
					queryString += filters[2] + " "; // operator
					queryString += filters[3] + " "; // value
				}else if(filters[1].equalsIgnoreCase("YEAR") || filters[1].equalsIgnoreCase("MONTH")){
					queryString += filters[1]+"(" + filters[0] + ") "; // YEAR
					queryString += filters[2] + " "; // operator
					queryString += filters[3] + " "; // value
				}else if(filters[1].equalsIgnoreCase("MONTH") || filters[1].equalsIgnoreCase("MONTH")){
					queryString += filters[1]+"(" + filters[0] + ") "; // YEAR
					queryString += filters[2] + " "; // operator
					queryString += filters[4] + " "; // month
				}else if(filters[1].equalsIgnoreCase("d")){
					queryString += filters[0] + " ";
					queryString += filters[2] + " "; // operator
					queryString += "\""+filters[5] +"\"^^<http://www.w3.org/2001/XMLSchema#date> "; // date value
				}else{
					System.out.println("ERROR: filter type is not recognized");
				}
				
				
				queryString += ").";
			}
			queryString += "}";
		}
		// There are modifiers
		else {
			hasModifiers = true;
			for(int i = 1; i < query.length -2; ){
				ArrayList<String> singleClause = new ArrayList<String>();
				singleClause.add(query[i++]);
				singleClause.add(query[i++]);
				singleClause.add(query[i++]);
				where.add(singleClause);
			}
			// TODO: Handle modifier here
			
		}
		
		if(filters.length >0){
			// TODO: apply filters
			
		}
		System.out.println("------+++++====+++++++-----");
		System.out.println(queryString);
	}

	public SPARQLQuery() {
		
	}
}
