package ayhay.query;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ayhay.dataStructures.AlternativeToken;
import ayhay.main.MainServlet;
import ayhay.utils.AlternativeTokenComparator;

/**
 * Servlet implementation class AlternativeQueryFinder
 */
@WebServlet(description = "Finds alternative queries to the query sent by the user", urlPatterns = { "/AlternativeQueryFinder" })
public class AlternativeQueryFinder extends HttpServlet {
	private static final long serialVersionUID = 1L;
//	private String lastQuery = "";
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public AlternativeQueryFinder() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		String[] inputs = request.getParameterValues("myInputs[]");
		ArrayList<String> alternatives = new ArrayList<String>();
		ArrayList<String> alternativeQueries = new ArrayList<String>();
		ArrayList<AlternativeToken> alternativeTokens = new ArrayList<AlternativeToken>();
		String query;
        query = "SELECT " + inputs[0] + " WHERE{ ";
        for(int i = 1; i < inputs.length - 1; ){
        	query += inputs[i++] + " " + inputs[i++] + " " + inputs[i++] + ".";
        }
        query += "}";
		
		// Check the query syntax
		for(int i = 2; i < inputs.length -1 ; i += 3){
			if(!(inputs[i].startsWith("<") && inputs[i].endsWith(">") || inputs[i].startsWith("?")))
				return;
		}
		for(int i = 3; i < inputs.length -1; i += 3){
			if(!((inputs[i].startsWith("<") && inputs[i].endsWith(">")) || inputs[i].startsWith("\"") || inputs[i].startsWith("?")))
				return;
		}
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
			int id = (int) Math.random();
			MainServlet.queryManager.executeQuery(id, alternativeQueries.get(i));
			int numOfRows = MainServlet.queryManager.getNumberOfResults(id);
			MainServlet.queryManager.closeQuery(id);
			alternativeTokens.get(i).setNumOfRows(numOfRows);
		}
		
		java.util.Collections.sort(alternativeTokens, new AlternativeTokenComparator());
		System.out.println("Finding answers to alternative queries finished");
		
		String result = "{ \"results\": { \"suggestions\": [ ";
		for(int i = 0; i < alternativeTokens.size(); ++i){
			System.out.println("Alternative Token " + i + ":");
			AlternativeToken thisToken = alternativeTokens.get(i);
			if(thisToken.getNumOfRows() > 0){
				System.out.println("Type: " + thisToken.getType() + ", Old Subject: " + thisToken.getSubject() + ", Old Predicate: " + thisToken.getPredicate() + ", Old Object: " + thisToken.getObject() + ", New Value: " + thisToken.getNewValue() + ", Number of answers: " + thisToken.getNumOfRows());
				result = result +"{\"type\": \"" + thisToken.getType() + "\", \"subject\":\"" + thisToken.getSubject().replace("\"","\\\"") + "\", \"predicate\":\"" + thisToken.getPredicate().replace("\"","\\\"") + "\", \"object\":\"" + thisToken.getObject().replace("\"","\\\"") + "\", \"newValue\":\"" + thisToken.getNewValue().replace("\"","\\\"") + "\", \"resultCount\":\"" + thisToken.getNumOfRows()+"\"} ,";
			}
		}
		result = result + "]}}";
			
		System.out.println(result);
	    response.setContentType("text/plain");  // Set content type of the response so that jQuery knows what it can expect.
	    response.setCharacterEncoding("UTF-8"); // You want world domination, huh?
	    response.getWriter().write(result);
	        
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException {
		doGet(request, response);

	}
	

}
