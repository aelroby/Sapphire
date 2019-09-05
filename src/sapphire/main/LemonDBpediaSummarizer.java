package sapphire.main;

import java.util.ArrayList;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;

import sapphire.query.QueryManager;
import sapphire.utils.FileManager;

public class LemonDBpediaSummarizer {

	public static void main(String[] args) {
		QueryManager queryManager = QueryManager.getEndpointQMInstance();
		queryManager.setEndpointURL("http://localhost:3030/ds/");
		ResultSet results = queryManager.executeQuery(0, "SELECT ?o WHERE {?s ?p ?o. FILTER isLiteral(?o) }");
		ArrayList<String> allLiteralsList = new ArrayList<String>();
		String contents = "";
		if(results != null){
			while(results.hasNext()) {
				QuerySolution sol = results.next();
				allLiteralsList.add(sol.get("o").toString());
			}
			for(int i = 0; i < allLiteralsList.size(); ++i) {
				String substitutedString = allLiteralsList.get(i);
				String[] parts = substitutedString.split("@");
				if(parts.length != 2) {
					continue;
				}
				parts[0] = "\"" + parts[0] + "\"";
				substitutedString = parts[0] + "@" + parts[1];
				System.out.println("Literal: " + substitutedString);
				String queryString = "SELECT DISTINCT ?l WHERE { ?s1 <http://lemon-model.net/lemon#writtenRep> " + substitutedString + ". ?s2 <http://lemon-model.net/lemon#canonicalForm> ?s1. ?s2 <http://lemon-model.net/lemon#sense> ?o1. ?o1 <http://lemon-model.net/lemon#reference> ?o2. ?s3 <http://lemon-model.net/lemon#reference> ?o2. ?s4 <http://lemon-model.net/lemon#sense> ?s3. ?s4 <http://lemon-model.net/lemon#canonicalForm> ?o3. ?o3 <http://lemon-model.net/lemon#writtenRep> ?l.  FILTER isLiteral(?l).}";
				System.out.println("Query: " + queryString);
				results = queryManager.executeQuery(0, queryString);
				if(results != null) {
					if(results.hasNext()) {
						contents += allLiteralsList.get(i).substring(0, allLiteralsList.get(i).indexOf("@"));
						while(results.hasNext()) {
							contents += ",";
							QuerySolution sol = results.next();
							contents += sol.get("l").toString().substring(0, sol.get("l").toString().indexOf("@"));;
						}
						contents += "\n";
					}
				}
			}
			FileManager.writeToFile("lemon_summary.txt", contents);
		}
	}

}
