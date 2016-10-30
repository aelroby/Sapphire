package ayhay.main;

import java.io.IOException;

import ayhay.FileManagement.ParameterFileManager;
import ayhay.autoComplete.Warehouse;

public class CollectStatistics {

	public static void main(String[] args) {
		ParameterFileManager parameterManager = null;
        try {
        	System.out.println("Reading parameter file...");
			parameterManager = new ParameterFileManager("parameters");
			parameterManager.readParameterFile();
			Warehouse warehouse = new Warehouse();
			
			System.out.println("Initializing warehouse...");
			warehouse.initializeWarehouse(parameterManager.getLabelsFile(), parameterManager.getPredicatesFile());
			
			System.out.println("Writing length frequency histogram...");
			warehouse.writeLengthHistogram("literals_histogram.csv");
			System.out.println("Done!");
			
        }
        catch (IOException e) {
			e.printStackTrace();
		}
	}

}
