package ayhay.test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import com.abahgat.suffixtree.GeneralizedSuffixTree;

import ayhay.FileManagement.ParameterFileManager;
import ayhay.autoComplete.Warehouse;
import ayhay.utils.Timer;

public class SuffixTreeTest {

	public static void testPerformance() {
		ParameterFileManager parameterManager = null;
        try {
        	System.out.println("Reading parameter file...");
			parameterManager = new ParameterFileManager("parameters");
			parameterManager.readParameterFile();
			Warehouse warehouse = new Warehouse();
			
			System.out.println("Initializing warehouse...");
			warehouse.initializeWarehouse(parameterManager.getLabelsFile(),
					parameterManager.getPredicatesFile(),
					parameterManager.getFrequentLiteralsFile());
		    
		    
		    
		    GeneralizedSuffixTree in = new GeneralizedSuffixTree();
		    
		    ArrayList<String> literalsList = warehouse.getLiteralsList();
		    
		    
		    System.out.println("Building suffix tree...");
		    for(int i = 0; i < 200000; ++i) {
		    	if(i % 10000 == 0) {
		    		System.out.println("Processed " + i + " literal");
		    	}
		    	if(i == 199999) {
		    		System.out.println("Literal list: " + literalsList.get(i).length());
		    	}
		    	in.put(literalsList.get(i).toLowerCase(), i);
		    }
		    
		    
		    System.out.println("Testing performance...");
		    // Test performance
		    
		    // Suffix tree
		    System.out.println("Suffix tree...");
		    Timer.start();
		    for(int i = 0; i < 200000; ++i) {
		    	if(i % 10000 == 0) {
		    		System.out.println("Processed " + i + " literal");
		    	}
		    	String query = literalsList.get(i);
		    	Collection<Integer> output = in.search(query.toLowerCase());
		    	if(output.size() == 0) { 
		    		System.out.println("This shit isn't working");
		    	}
		    }
		    Timer.stop();
		    System.out.println("Suffix tree time: " + Timer.getTimeInSeconds() + " seconds");
		    
		    // Correctness test
		    HashSet<Integer> output = (HashSet<Integer>) in.search("abc");
		    if(output.size() > 0) {
		    	for(Integer index : output) {
		    		System.out.println("Index: " + index + " String: " + literalsList.get(index));
		    	}
		    }
		    else { 
		    	System.out.println("This shit isn't working");
		    }
		    
		    System.out.println("Done");
		    
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	// Test if two entries would return different indexes
	public static void testFunctionality() {
		GeneralizedSuffixTree in = new GeneralizedSuffixTree();
		in.put("barack obama", 0);
		in.put("barack obama", 1);
		HashSet<Integer> output = (HashSet<Integer>) in.search("barack obama");
		for(Integer index : output) {
    		System.out.println("Index: " + index);
    	}
	}
	
	public static void main(String[] args) {
        testFunctionality();
	}

}
