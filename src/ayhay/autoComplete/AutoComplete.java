package ayhay.autoComplete;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;

import ayhay.FileManagement.ParameterFileManager;

/**
 * Servlet implementation class AutoComplete
 */
@WebServlet("/AutoComplete")
public class AutoComplete extends HttpServlet {
	private static final long serialVersionUID = 1L;
	public static Warehouse warehouse;
       
	
	public void init (ServletConfig config) throws ServletException{
       super.init(config);
       ParameterFileManager parameterManager = null;
       try {
			parameterManager = new ParameterFileManager("parameters");
			parameterManager.readParameterFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
       
       warehouse = new Warehouse();
       warehouse.initializeWarehouse(parameterManager.getLabelsFile(), parameterManager.getPredicatesFile());
    }
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public AutoComplete() {
    	
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		PrintWriter out = response.getWriter();
        response.setContentType("text/html");
        response.setHeader("Cache-control", "no-cache, no-store");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Expires", "-1");
        
        String query = request.getParameter("term");
        if (query.charAt(0)=='?'){
        	// if the term is a variable, ignore AutoComplete
        	System.out.println("variable term: " + query);
        	out.println("");
        	out.close();
        }
        else{
            System.out.println("Received term: " + query);
            query = query.toLowerCase();
            JSONArray arrayObj = warehouse.findMatches(query);
            System.out.println("found: " + arrayObj.toString());
            out.println(arrayObj.toString());
            out.close();        	
        }
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		//doGet(request, response);
	}

}
