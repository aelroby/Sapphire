package sapphire.test;

import org.apache.jena.query.ResultSet;

import sapphire.query.QueryManager;

public class QueryManagerTest {

	public static void main(String[] args) {
		QueryManager manager = QueryManager.getEndpointQMInstance();
		System.out.println("Answering query: SELECT ?s ?p WHERE {?s ?p \"Barack Obama\"@en }");
		ResultSet results = manager.executeQuery(15, "SELECT ?s ?p WHERE {?s ?p \"Barack Obama\"@en }");
		while(results.hasNext()) {
			System.out.println("Answers (?p): ");
			System.out.println(results.next().get("p").toString());
		}
		
	}

}
