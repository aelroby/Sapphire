package sapphire.main;

import java.io.IOException;
import java.util.ArrayList;

import sapphire.FileManagement.ParameterFileManager;
import sapphire.autoComplete.Warehouse;
import sapphire.utils.FileManager;

public class TypeaheadEvaluation {

	public static void main(String[] args) {
		
		// Read parameter file
		System.out.println("Reading parameter file...");
		ParameterFileManager parameterManager = null;
        try {
			parameterManager = new ParameterFileManager("parameters");
			parameterManager.readParameterFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
        
        // Initialize warehouse
        System.out.println("Initializing warehouse...");
        Warehouse warehouse = new Warehouse();
        warehouse.initializeWarehouse(parameterManager.getLabelsFile(),
     		   parameterManager.getPredicatesFile(),
     		   parameterManager.getFrequentLiteralsFile(),
     		   parameterManager.getLemonSummaryFile(),
     		   40000);
        
        // Read typeahead log
        System.out.println("Reading words...");
        ArrayList<String> words = FileManager.readFileLineByLine("TypeaheadWords.dat");
        
        // Benchmark Warehouse
        System.out.println("Benchmarking Warehouse...");
        long start = System.nanoTime();
        for(String word : words) {
        	warehouse.findMatches(word);
        }
        long time = System.nanoTime() - start;
        System.out.println("Finished in " + (1.0 * time / 1000000000));
        System.out.println("Size of list = " + words.size());
        double avgTime = (1.0 * time / 1000000000) / words.size();
        System.out.println("Finished benchmarking warehouse. Avg. Time = " + avgTime);
	}

}
