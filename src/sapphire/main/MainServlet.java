package sapphire.main;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import sapphire.FileManagement.ParameterFileManager;
import sapphire.dataStructures.SPARQLQuery;
import sapphire.query.QueryManager;
import sapphire.utils.FileManager;
import sapphire.utils.SimpleTimestamp;
import sapphire.utils.Timer;

/**
 * Servlet implementation class MainServlet
 */
@WebServlet("/MainServlet")
public class MainServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
    private QueryManager queryManager;
    
    private int queryNum = 0;
    
    public void init (ServletConfig config) throws ServletException{
        super.init(config);
        ParameterFileManager parameterManager = null;
        try {
			parameterManager = new ParameterFileManager("parameters");
			parameterManager.readParameterFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
        queryManager = QueryManager.getInstance();
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
		String[] filters = request.getParameterValues("myFilters[]");
		
		SPARQLQuery sparqlQuery = new SPARQLQuery(inputs, filters);
		if(!sparqlQuery.isValid())
			return;
		String query = sparqlQuery.getQueryString();
		
		String logContent = SimpleTimestamp.getFormattedTimestamp() + 
				"query " + queryNum++ + ": " + query;
		
		FileManager.appendToFileWithNewLine("MainQueryLog.dat", logContent);
        
        System.out.println("Answering Query: " + query);
        int id = (int) Math.random() * 10000;
        Timer.start();
		queryManager.executeUserQuery(id, sparqlQuery);
		Timer.stop();
		FileManager.appendToFileWithNewLine("QueryTimeStatsMelliseconds.dat",
				Double.toString(Timer.getTimeInMelliseconds()));
		String answers = queryManager.getResultsAsJSON(id);
        queryManager.closeQuery(id);
        System.out.println("Answers:" + answers);
        
        FileManager.appendToFileWithNewLine("MainQueryLog.dat", "++++++++++++++++++++++++++++++++++++++");
        
        response.setContentType("text/plain");  // Set content type of the response so that jQuery knows what it can expect.
        response.setCharacterEncoding("UTF-8"); // You want world domination, huh?
        response.getWriter().write(answers);
	}

}
