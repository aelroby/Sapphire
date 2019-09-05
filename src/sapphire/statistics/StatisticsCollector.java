package sapphire.statistics;

import java.io.IOException;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.tdb.TDBFactory;

import sapphire.FileManagement.ParameterFileManager;

public class StatisticsCollector {

	public static void main(String[] args) {
		ParameterFileManager parameterManager = null;
        try {
			parameterManager = new ParameterFileManager("parameters");
			parameterManager.readParameterFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
        Dataset dataset = TDBFactory.createDataset(parameterManager.getTDBDirectory());
		dataset.begin(ReadWrite.READ);
		Model model = dataset.getDefaultModel();
        
	}

}
