package sapphire.main;

import java.io.IOException;

import sapphire.FileManagement.ParameterFileManager;
import sapphire.autoComplete.Warehouse;

public class CollectStatistics {

	public static void main(String[] args) {
		ParameterFileManager parameterManager = null;
        try {
        	System.out.println("Reading parameter file...");
			parameterManager = new ParameterFileManager("parameters");
			parameterManager.readParameterFile();
			Warehouse warehouse = new Warehouse();
			
			System.out.println("Initializing warehouse...");
			warehouse.initializeWarehouse(parameterManager.getLabelsFile(),
					parameterManager.getPredicatesFile(),
					parameterManager.getFrequentLiteralsFile(),
					parameterManager.getLemonSummaryFile(),
					-1);
			
			System.out.println("Writing length frequency histogram...");
			warehouse.writeLengthHistogram("literals_histogram.csv");
			System.out.println("Done!");
			
        }
        catch (IOException e) {
			e.printStackTrace();
		}
	}

}
