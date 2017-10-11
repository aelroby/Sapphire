package sapphire.query;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import sapphire.dataStructures.AlternativeToken;
import sapphire.dataStructures.SPARQLQuery;
import sapphire.utils.AlternativeTokenComparator;
import sapphire.utils.FileManager;
import sapphire.utils.SimpleTimestamp;
import sapphire.utils.Timer;

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
        altQueryGenerator = new AlternativeQueryGenerator();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		String[] inputs = request.getParameterValues("myInputs[]");
		String[] filters = request.getParameterValues("myFilters[]");

        SPARQLQuery sparqlQuery = new SPARQLQuery(inputs,filters);
        if(!sparqlQuery.isValid()) {
        	return;
        }
        
        String logContents = SimpleTimestamp.getFormattedTimestamp() + 
        		"Finding alternatives for query \"" + sparqlQuery.queryString + "\":";
        
        FileManager.appendToFileWithNewLine("AlternativeQueriesLog.dat", logContents);
        
		ArrayList<AlternativeToken> alternativeTokens = new ArrayList<AlternativeToken>();
		
		// Finding alternatives for predicates and literals
        alternativeTokens.addAll(altQueryGenerator.findSimilarQueries(sparqlQuery));
		
        if(alternativeTokens.size() > 0) {
        	java.util.Collections.sort(alternativeTokens, new AlternativeTokenComparator());
        }
        
        // Query relaxation too
        Timer.start();
        alternativeTokens.addAll(altQueryGenerator.relaxQuery(sparqlQuery));
        Timer.stop();
        System.out.println("Relaxed query in " + Timer.getTimeInSeconds() + " seconds");
        
    	// Literals first
    	/*Timer.start();
    	alternativeTokens.addAll(altQueryGenerator.relaxStructure(sparqlQuery));
    	Timer.stop();
    	FileManager.appendToFileNoNewLine("AlternativeQueriesTimeStatsSeconds.dat",
				Double.toString(Timer.getTimeInSeconds()) + ",");
    	System.out.println("Relaxed literals in " + Timer.getTimeInSeconds() + " seconds");
    	
    	// Predicates
    	Timer.start();
    	alternativeTokens.addAll(altQueryGenerator.relaxPredicates(sparqlQuery));
    	Timer.stop();
    	FileManager.appendToFileWithNewLine("AlternativeQueriesTimeStatsSeconds.dat",
				Double.toString(Timer.getTimeInSeconds()));
    	System.out.println("Relaxed literals in " + Timer.getTimeInSeconds() + " seconds");*/
    	
    	FileManager.appendToFileWithNewLine("AlternativeQueriesLog.dat", "++++++++++++++++++++++++++++++++++++++");
		
		String result = "{ \"results\": { \"suggestions\": [ ";
		for(int i = 0; i < alternativeTokens.size(); ++i){
			AlternativeToken thisToken = alternativeTokens.get(i);
			if(thisToken.getType().compareTo("X") == 0) {
				result += "{\"type\": \"" + thisToken.getType() + "\", \"example\":\"" + thisToken.getExample().replace("\"","\\\"") + "\"} ,";
			}
			else {
				if(thisToken.getNumOfRows() > 0){
					System.out.println("Type: " + thisToken.getType() + ", Old Subject: " + thisToken.getSubject() + ", Old Predicate: " + thisToken.getPredicate() + ", Old Object: " + thisToken.getObject() + ", New Value: " + thisToken.getNewValue() + ", Number of answers: " + thisToken.getNumOfRows());
					result += "{\"type\": \"" + thisToken.getType() + "\", \"subject\":\"" + thisToken.getSubject().replace("\"","\\\"") + "\", \"predicate\":\"" + thisToken.getPredicate().replace("\"","\\\"") + "\", \"object\":\"" + thisToken.getObject().replace("\"","\\\"") + "\", \"newValue\":\"" + thisToken.getNewValue().replace("\"","\\\"") + "\", \"resultCount\":\"" + thisToken.getNumOfRows()+"\"} ,";
				}
			}
		}
		result += "]}}";
			
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
