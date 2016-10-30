package ayhay.FileManagement;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.jena.query.ResultSet;

import ayhay.dataStructures.LiteralStat;
import ayhay.query.QueryManager;
import ayhay.utils.FileManager;
import ayhay.utils.LengthComparator;

public class MetadataPreprocessor {
	
	private static ArrayList<String> predicatesList;
	private static Set<String> labels;
	private static ArrayList<String> labelsList;
	private static ArrayList<LiteralStat> literalsStats;
	private static ArrayList<String> labelsLines;
	private static ParameterFileManager paramManager;

	public static void main(String[] args) {
		long startTime, stopTime, startTimeRoutine, stopTimeRoutine;
		double elapsedTime;
		
		paramManager = null;
		try {
			paramManager = new ParameterFileManager("parameters");
			paramManager.readParameterFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		String predicatesFile = paramManager.getPredicatesFile();
		String labelsDirectory = paramManager.getLabelsFile();
		
		startTime = System.currentTimeMillis();
		labels = new HashSet<String>();
		
		// Reading and setting up predicates
		startTimeRoutine = System.currentTimeMillis();
		System.out.println("Reading predicate file...");
		predicatesList = FileManager.readFileLineByLine(predicatesFile);
		System.out.println("Predicates file has been read!");
		System.out.println("Setting up predicates for lookup...");
		setPredicates();
		System.out.println("Setting up predicates for lookup finished!");
		System.out.println("Ranking predicates...");
		java.util.Collections.sort(predicatesList, new LengthComparator());
		System.out.println("Ranking predicates finished!");
		// Writing predicates to file
		System.out.println("Writing refined predicates to disk...");
		FileWriter writer;
		try {
			writer = new FileWriter(predicatesFile + "_refined");
			for(int i = 0; i < predicatesList.size(); ++i){
				writer.write(predicatesList.get(i) + System.lineSeparator());
			}
			writer.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Writing refined predicates to disk finished!");
		predicatesList.clear();
		stopTimeRoutine = System.currentTimeMillis();
		elapsedTime = stopTimeRoutine - startTimeRoutine;
		elapsedTime = elapsedTime / 60000;
		System.out.println("Predicates preprocessing finished in: " + elapsedTime + " minutes");
		
		// Reading and setting up labels
		System.out.println("Reading labels files...");
		File folder = new File(labelsDirectory);
		File[] listOfFiles = folder.listFiles();
		
		startTimeRoutine = System.currentTimeMillis();
		for(int i = 0; i < listOfFiles.length; ++i){
			System.out.println("Reading label file: " + labelsDirectory + listOfFiles[i].getName());
			labelsLines = FileManager.readFileLineByLine(labelsDirectory + listOfFiles[i].getName());
			System.out.println("Labels file " + labelsDirectory + listOfFiles[i].getName() + " has been read!");
			int fileNum = i+1;
			System.out.println("Setting up labels for lookup (" + fileNum + " of " + listOfFiles.length + ")...");
			setLabels();
			System.out.println("Setting up labels for lookup (" + fileNum + " of " + listOfFiles.length + ")finished!");
		}

		System.out.println("Finished setting up labels!");
		stopTimeRoutine = System.currentTimeMillis();
		elapsedTime = stopTimeRoutine - startTimeRoutine;
		elapsedTime = elapsedTime / 60000;
		System.out.println("Reading and setting up labels finished in: " + elapsedTime + " minutes");
		
		labelsList = new ArrayList<String>(labels);
		labels.clear();
		java.util.Collections.sort(labelsList, new LengthComparator());
		
		// This part for adding count of objects. Will not use it
		/*startTimeRoutine = System.currentTimeMillis();
		System.out.println("Setting stats of labels...");
		setLiteralsStats();
		System.out.println("Setting stats of labels finished!");
		stopTimeRoutine = System.currentTimeMillis();
		elapsedTime = stopTimeRoutine - startTimeRoutine;
		elapsedTime = elapsedTime / 60000;
		System.out.println("setting stats of labels finished in: " + elapsedTime + " minutes");
		
		startTimeRoutine = System.currentTimeMillis();
		System.out.println("Ranking labels...");
		java.util.Collections.sort(literalsStats, new ExtendedLengthComparator());
		System.out.println("Finished ranking labels!");
		stopTimeRoutine = System.currentTimeMillis();
		elapsedTime = stopTimeRoutine - startTimeRoutine;
		elapsedTime = elapsedTime / 60000;
		System.out.println("Ranking labels finished in: " + elapsedTime + " minutes");*/
	
		// Writing refined data structures to files
		startTimeRoutine = System.currentTimeMillis();
		System.out.println("Writing labels file to disk...");
		try {
			writer = new FileWriter(labelsDirectory + "literals_refined");
			for(int i = 0; i < labelsList.size(); ++i){
				writer.write(labelsList.get(i) + System.lineSeparator());
			}
			writer.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		labelsList.clear();
		System.out.println("Writing labels file to disk finished!");
		stopTimeRoutine = System.currentTimeMillis();
		elapsedTime = stopTimeRoutine - startTimeRoutine;
		elapsedTime = elapsedTime / 60000;
		System.out.println("Writing labels file to disk finished in: " + elapsedTime + " minutes");
		
		stopTime = System.currentTimeMillis();
		elapsedTime = stopTime - startTime;
		elapsedTime = elapsedTime / 60000;
		System.out.println("Warehouse initialization took: " + elapsedTime + " minutes");
		
	}
	private static void setPredicates(){
		String oldString;
		String newString;
		for(int i = 0; i < predicatesList.size(); ++i){
			oldString = predicatesList.get(i);
			newString = "<" + oldString + ">";
			predicatesList.set(i, newString);
		}
		
	}
	
	private static void setLabels(){
		String currentLine;
		String [] splits;
		for (Iterator<String> iterator = labelsLines.iterator(); iterator.hasNext();) {
			currentLine = iterator.next();
			iterator.remove();
			splits = currentLine.split(" ");
			String label = "";
			for(int i = 2; i < splits.length-1; ++i)
				label += splits[i] + " ";
			labels.add(label);
		}
	}
	
	@SuppressWarnings("unused")
	private static void setLiteralsStats(){
		literalsStats = new ArrayList<LiteralStat>();
		ResultSet results;
		int id = 0;
		QueryManager queryManager = new QueryManager();
		queryManager.initializeDataset(paramManager.getTDBDirectory());
		int counter = 0;
		int step = 0;
		int listSize = labelsList.size();
		for (Iterator<String> iterator = labelsList.iterator(); iterator.hasNext();) {
			++counter;
			if(counter > 1.0*step/100*listSize){
				System.out.println(step + "% completed");
				++step;
			}
		    String literal = iterator.next();
		    String query = "SELECT (count(distinct ?o) as ?count) { ?s ?label " + literal + " . ?s ?p ?o}";
		    results = queryManager.executeQuery(0, query);
		    String answer = results.nextSolution().get("count").toString();
		    answer = answer.substring(0, answer.indexOf('^'));
		    int frequency = Integer.parseInt(answer); 
		    LiteralStat recordStat = new LiteralStat(literal, frequency);
		    literalsStats.add(recordStat);
	        iterator.remove();
		}
	}
}
