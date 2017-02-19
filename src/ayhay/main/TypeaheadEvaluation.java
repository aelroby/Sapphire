package ayhay.main;

import java.io.IOException;
import java.util.ArrayList;

import ayhay.FileManagement.ParameterFileManager;
import ayhay.autoComplete.Warehouse;
import ayhay.utils.FileManager;

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
     		   10000);
        
        // Read typeahead log
        System.out.println("Reading words...");
        ArrayList<String> words = FileManager.readFileLineByLine("TypeaheadWords.dat");
        
        // Benchmark Warehouse
        System.out.println("Benchmarking Warehouse...");
        for(String word : words) {
        	warehouse.findMatches(word);
        }
		
        System.out.println("Finished benchmarking warehouse. Check the stats file now.");
	}

}
