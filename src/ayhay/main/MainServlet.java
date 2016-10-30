package ayhay.main;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import ayhay.FileManagement.ParameterFileManager;
import ayhay.query.QueryManager;

/**
 * Servlet implementation class MainServlet
 */
@WebServlet("/MainServlet")
public class MainServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private String TDBDirectory;
    public static QueryManager queryManager;
//    private String lastQuery;
    
    
    
    public void init (ServletConfig config) throws ServletException{
        super.init(config);
        ParameterFileManager parameterManager = null;
        try {
			parameterManager = new ParameterFileManager("parameters");
			parameterManager.readParameterFile();
			this.TDBDirectory = parameterManager.getTDBDirectory();
		} catch (IOException e) {
			e.printStackTrace();
		}
        queryManager = new QueryManager();
    	queryManager.initializeDataset(TDBDirectory);
//    	lastQuery = "";
    }
    /**
     * Default constructor. 
     */
    public MainServlet() {
    	
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		if(!queryManager.initialized)
			queryManager.initializeDataset(TDBDirectory);
		String[] inputs = request.getParameterValues("myInputs[]");
        String query = "SELECT " + inputs[0] + " WHERE{ ";
        for(int i = 1; i < inputs.length; ){
        	query += inputs[i++] + " " + inputs[i++] + " " + inputs[i++] + ".";
        }
        query += "}";
        	
        int id = (int) Math.random() * 100;
		queryManager.executeQuery(id, query);
        String answers = queryManager.getResultsAsJSON(id);
        queryManager.closeQuery(id);
        
        
        response.setContentType("text/plain");  
        response.setCharacterEncoding("UTF-8"); // You want world domination, huh?
        response.getWriter().write(answers);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		
		if(!queryManager.initialized)
			queryManager.initializeDataset(TDBDirectory);
		String[] inputs = request.getParameterValues("myInputs[]");
		
        String query = "";
        if (inputs[inputs.length - 1].equalsIgnoreCase("count"))
        	query = "SELECT (count(*) as ?counting ) WHERE{ ";        	
        else
        	query = "SELECT " + inputs[0] + " WHERE{ ";

        
        for(int i = 1; i < inputs.length -1; ){
        	query += inputs[i++] + " " + inputs[i++] + " " + inputs[i++] + ".";
        }
        query += "}";
        // If this query is the same as last call, ignore. Commented because it is done in the frontend now.
        /*if(query.compareTo(lastQuery) == 0){
        	System.out.println("Same as last query. Ignore!");
        	return;
        }
        lastQuery = query;*/
        
        // Check the query syntax
 		for(int i = 2; i < inputs.length; i += 3){
 			if(!(inputs[i].startsWith("<") && inputs[i].endsWith(">") || inputs[i].startsWith("?")))
 				return;
 		}
 		for(int i = 3; i < inputs.length; i += 3){
 			if(!((inputs[i].startsWith("<") && inputs[i].endsWith(">")) || inputs[i].startsWith("\"") || inputs[i].startsWith("?")))
 				return;
 		}
        
        System.out.println("Answering Query: " + query);
        int id = (int) Math.random() * 100;
		queryManager.executeQuery(id, query);
        String answers = queryManager.getResultsAsJSON(id);
        queryManager.closeQuery(id);
        System.out.println("Answers:" + answers);
        
        response.setContentType("text/plain");  // Set content type of the response so that jQuery knows what it can expect.
        response.setCharacterEncoding("UTF-8"); // You want world domination, huh?
        response.getWriter().write(answers);
 
        
	}

}
