package ayhay.main;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;

public class TestClass {

	public static void main(String[] args) {
		Query query;
		for(int i = 0; i < 50; ++i) {
			query = QueryFactory.create("SELECT DISTINCT ?s ?p WHERE { ?s ?p \"Cave\"@en }");
			QueryExecution qexec = QueryExecutionFactory.sparqlService("http://dbpedia.org/sparql", query);
			ResultSet results = qexec.execSelect();
			ResultSetFormatter.out(System.out, results, query);
		}
	}

}
