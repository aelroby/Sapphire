package ayhay.main;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import ayhay.FileManagement.ParameterFileManager;
import ayhay.dataStructures.SPARQLQuery;
import ayhay.query.QueryManager;

/**
 * Servlet implementation class MainServlet
 */
@WebServlet("/MainServlet")
public class MainServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
    public static QueryManager queryManager;
    
    
    
    public void init (ServletConfig config) throws ServletException{
        super.init(config);
        ParameterFileManager parameterManager = null;
        try {
			parameterManager = new ParameterFileManager("parameters");
			parameterManager.readParameterFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
        queryManager = new QueryManager();
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
		
		
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		
		String[] inputs = request.getParameterValues("myInputs[]");
		
		SPARQLQuery sparqlQuery = new SPARQLQuery(inputs);
		if(!sparqlQuery.isValid())
			return;
		String query = sparqlQuery.getQueryString();
        
        
        
        System.out.println("Answering Query: " + query);
        int id = (int) Math.random() * 10000;
		queryManager.executeUserQuery(id, sparqlQuery);
        String answers = queryManager.getResultsAsJSON(id);
        queryManager.closeQuery(id);
        System.out.println("Answers:" + answers);
        
        response.setContentType("text/plain");  // Set content type of the response so that jQuery knows what it can expect.
        response.setCharacterEncoding("UTF-8"); // You want world domination, huh?
        response.getWriter().write(answers);
 
        
	}

}
