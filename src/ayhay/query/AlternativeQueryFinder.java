package ayhay.query;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ayhay.dataStructures.AlternativeToken;
import ayhay.dataStructures.SPARQLQuery;
import ayhay.utils.AlternativeTokenComparator;

/**
 * Servlet implementation class AlternativeQueryFinder
 */
@WebServlet(description = "Finds alternative queries to the query sent by the user", urlPatterns = { "/AlternativeQueryFinder" })
public class AlternativeQueryFinder extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static AlternativeQueryGenerator altQueryGenerator;
       
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

        SPARQLQuery sparqlQuery = new SPARQLQuery(inputs);
        if(!sparqlQuery.isValid()) {
        	return;
        }
        
        ArrayList<AlternativeToken> alternativeTokens = altQueryGenerator.findSimilarQueries(sparqlQuery);
		
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
