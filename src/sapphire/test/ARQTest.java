package sapphire.test;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.tdb.TDBFactory;

import sapphire.utils.Timer;

public class ARQTest {

	public static void main(String[] args) {
		// TDB on server: /home/ahmed/Datasets/DBpedia_TDB
		// TDB on laptop: /media/ahmed/TOSHIBA-HD/Work/Datasets/DBpedia_TDB/
		String directory = "/home/ahmed/Datasets/DBpedia_TDB" ;
		Dataset dataset = TDBFactory.createDataset(directory) ;
		dataset.begin(ReadWrite.READ);
		Model model = dataset.getDefaultModel();
		Query query = QueryFactory.create("SELECT ?s ?p ?o WHERE { ?s ?p ?o } LIMIT 10");
		Timer.start();
		try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
		    ResultSet results = qexec.execSelect() ;
		    for ( ; results.hasNext() ; )
		    {
		    	System.out.println(results.next().get("s").toString()); 
		    }
		  }
		Timer.stop();
		System.out.println("Finished in " + Timer.getTimeInSeconds() + " seconds.");
	}

}
