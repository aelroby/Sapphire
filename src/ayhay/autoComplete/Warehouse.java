package ayhay.autoComplete;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.json.simple.JSONArray;

import com.abahgat.suffixtree.GeneralizedSuffixTree;

import ayhay.dataStructures.StringScore;
import ayhay.utils.FileManager;
import ayhay.utils.StringScoreComparator;
import ayhay.utils.Timer;
import info.debatty.java.stringsimilarity.JaroWinkler;



public class Warehouse {

	// Lists to be searched 
	private ArrayList<String> predicatesList;
	private ArrayList<String> literalsList;
	private ArrayList<String> frequentLiteralsList;
	
	// indexes and lengths
	private ArrayList<Integer> indexes;
	private ArrayList<Integer> lengths;
	
	// Synchronized list used by threads
	private List<String> list;
	
	// Number of corse available in runtime
	private int numOfCores;
	
	// Number of results to be found for typeahead
	private int resultsToBeFound;
	
	// Suffix tree
	private GeneralizedSuffixTree in;
	
	// Similarity class
	private JaroWinkler jw;
	
	// Variables for statistics
	private int numOfSearchTasks;
	private int numOfIndexHits;
	
	/**
	 * 
	 * @author ahmed
	 * 
	 * The search task class for threads
	 */
	private class SearchTask implements Runnable {

		int minIndex, maxIndex;
		String query;
		
		public SearchTask(int minIndex, int maxIndex, String query) {
			this.minIndex = minIndex;
			this.maxIndex = maxIndex;
			this.query = query;
		}
		
		@Override
		public void run() {
			for(int i = minIndex; i < maxIndex; ++i){
				if(literalsList.get(i).toLowerCase().contains(query.toLowerCase())){
					System.out.println("Thread " + Thread.currentThread().getName() + 
							" found " + literalsList.get(i));
					list.add(literalsList.get(i));
					--resultsToBeFound;
					if(resultsToBeFound < 0)
						break;
				}
			}
		}
		
	}
	
	public Warehouse() {
		indexes = new ArrayList<Integer>();
		lengths = new ArrayList<Integer>();
		in = new GeneralizedSuffixTree();
		numOfCores = Runtime.getRuntime().availableProcessors();
		list = Collections.synchronizedList(new ArrayList<String>());
		numOfSearchTasks = numOfIndexHits = 0;
	}
	
	public ArrayList<String> getPredicatesList() {
		return predicatesList;
	}

	public void setPredicatesList(ArrayList<String> predicatesList) {
		this.predicatesList = predicatesList;
	}

	public ArrayList<String> getLiteralsList() {
		return literalsList;
	}

	public void setLiteralsList(ArrayList<String> literalsList) {
		this.literalsList = literalsList;
	}

	/**
	 * Analyze lengths of literals and divide in bins
	 */
	private void analyzeLiterals() {
		
		int lastLength = 0;
		for(int i = 0; i < literalsList.size(); ++i) {
			// Update lengths of literals array with indexes
			if(lastLength != literalsList.get(i).length() - 5) {
				indexes.add(i);
				lengths.add(literalsList.get(i).length() - 5); // -5 for ""@en
			}
			lastLength = literalsList.get(i).length() - 5;
			
		}
		
	}
	
	/**
	 * 
	 * @param literalsFile is the path of the literals file
	 * @param predicatesFile is the path of the predicates file
	 */
	public void initializeWarehouse(String literalsFile, 
			String predicatesFile, String mostFrequentLiteralsFile){
		
		Timer.start();
		
		System.out.println("Reading literals file...");
		literalsList = FileManager.readFileLineByLine(literalsFile);
		System.out.println("Reading literals file finished!");
		
		System.out.println("Reading predicates file...");
		predicatesList = FileManager.readFileLineByLine(predicatesFile);
		System.out.println("Reading predicates file finished!");
		
		System.out.println("Reading frequent literals file...");
		frequentLiteralsList = FileManager.readFileLineByLine(mostFrequentLiteralsFile);
		for(int i = 0; i < frequentLiteralsList.size(); ++i) {
			in.put(frequentLiteralsList.get(i).toLowerCase(), i);
		}
		System.out.println("Reading frequent literals file finished!");
		
		jw = new JaroWinkler();
		System.out.println("Analyzing literals...");
		analyzeLiterals();
		Timer.stop();
		System.out.println("Warehouse initialization took: " + Timer.getTime());
		
	}
	
	private void writeStatsToFile() {
		String fileName = "IndexStats.dat";
		String contents = "Typeahead_Tasks,Index_Hits,Hit_Ratio\n";
		contents += numOfSearchTasks + "," + numOfIndexHits + 
				"," + 1.0 * numOfIndexHits / numOfSearchTasks;
		FileManager.writeToFile(fileName, contents);
	}
	
	/**
	 * 
	 * @param fileName
	 */
	public void writeLengthHistogram(String fileName) {
		String contents = "Length,Frequency\n";
		int literalLength = 0;
		int frequency = 0;
		for(String literal : literalsList) {

			literal = literal.substring(literal.indexOf("\"")+1, literal.lastIndexOf("\""));
			
			if(literal.length() == literalLength) {
				frequency++;
			}
			else {
				contents += Integer.toString(literalLength) + "," + 
							Integer.toString(frequency) + "\n";
				frequency = 1;
				literalLength = literal.length();
			}
		}
		FileManager.writeToFile(fileName, contents);
	}
	
	public ArrayList<String> findSimilarStringsPredicates(String s){
		ArrayList<StringScore> matchesScores = new ArrayList<StringScore>(); 
		ArrayList<String> matches = new ArrayList<String>();
		String trimmedString;
		trimmedString = s.substring(s.lastIndexOf("/")+1, s.length()-1).toLowerCase();	// Whatever is after the last /
		double score;
		String currentPredicate;
		// Search in predicates
		for(int i = 0; i < predicatesList.size(); ++i){
			if(s.compareTo(predicatesList.get(i)) == 0)
				continue;
			currentPredicate = predicatesList.get(i);
			currentPredicate = currentPredicate.substring(currentPredicate.lastIndexOf("/")+1, currentPredicate.length()-1).toLowerCase();
			score = jw.similarity(currentPredicate, trimmedString); 
			if(score > 0.5){
				matchesScores.add(new StringScore(predicatesList.get(i), score));
			}
		}
		java.util.Collections.sort(matchesScores, new StringScoreComparator());
		for(int i = 0; i < 5 && i < matchesScores.size(); ++i){
			matches.add(matchesScores.get(i).getS());
		}
		return matches;
		
	}
	public ArrayList<String> findSimilarStringsLiterals(String s){
		ArrayList<StringScore> matchesScores = new ArrayList<StringScore>(); 
		ArrayList<String> matches = new ArrayList<String>();
		String trimmedString;
		// Check if it's not a literal. URIs support should be added later 
		if(!s.startsWith("\""))
			return matches;
		trimmedString = s.substring(s.indexOf("\"")+1, s.indexOf("\"", s.indexOf("\"")+1)).toLowerCase();	// Just choose what is between brackets
		
		int minLength = trimmedString.length();
		int maxLength = trimmedString.length() + 5;
		int minIndex = Integer.MAX_VALUE;
		int maxIndex = -1;
		
		for(int i = 0; i < lengths.size(); ++i){
			if(lengths.get(i) > maxLength) {
				maxIndex = indexes.get(i);
				break;
			}
			if(minLength >= lengths.get(i)) {
				minIndex = indexes.get(i);
			}
		}
		
		double score;
		String currentLiteral;
		// Search in LiteralsIndex
		for(int i = minIndex; i < maxIndex; ++i){
			if(s.compareTo(literalsList.get(i)) == 0)
				continue;
			if(literalsList.get(i).length() - s.length() > 10)
				break;
			if(s.length() - literalsList.get(i).length() > 10)
				continue;
			currentLiteral = literalsList.get(i);
			try{
				currentLiteral = currentLiteral.substring(currentLiteral.indexOf("\"")+1, currentLiteral.indexOf("\"", currentLiteral.indexOf("\"")+1)).toLowerCase();
			}
			catch(StringIndexOutOfBoundsException e){
				System.out.println("A non-literal value was found in literalsList: " + literalsList.get(i));
			}
			score = jw.similarity(currentLiteral, trimmedString); 
			if(score > 0.6){
				matchesScores.add(new StringScore(literalsList.get(i), score));
			}
		}
		java.util.Collections.sort(matchesScores, new StringScoreComparator());
		for(int i = 0; i < 5 && i < matchesScores.size(); ++i){
			matches.add(matchesScores.get(i).getS());
		}
		return matches;
	}
	
	@SuppressWarnings("unchecked")
	public JSONArray findMatches(String query){
		
		++numOfSearchTasks;
		
		Timer.start();
		resultsToBeFound = 10;
		list.clear();
		JSONArray arrayObj = new JSONArray();
		
		// Search in predicates
		for(int i = 0; i < predicatesList.size(); ++i){
			if(predicatesList.get(i).toLowerCase().contains(query.toLowerCase())){
				arrayObj.add(predicatesList.get(i));
			}
		}
		
		// Search in index
		HashSet<Integer> output = (HashSet<Integer>) in.search(query.toLowerCase());
		if(output.size() > 0) {
			++numOfIndexHits;
		}
		for(Integer index : output) {
    		arrayObj.add(frequentLiteralsList.get(index));
    		--resultsToBeFound;
    	}
		
		// If all results were found in index
		if(resultsToBeFound < 0) {
			writeStatsToFile();
			return arrayObj;
		}
		
		
		// In length bins
		int minLength = query.length();
		int maxLength = query.length() + 12;
		
		int minIndex = Integer.MAX_VALUE;
		int maxIndex = -1;
		
		for(int i = 0; i < lengths.size(); ++i){
			if(lengths.get(i) > maxLength) {
				maxIndex = indexes.get(i);
				break;
			}
			if(minLength >= lengths.get(i)) {
				minIndex = indexes.get(i);
			}
		}
		
		System.out.println("Searching between indexes: " + minIndex + 
				" corresponding to length " + (literalsList.get(minIndex).length()-5) +
				" and " + maxIndex + " corresponding to length " + 
				(literalsList.get(maxIndex).length()-5));
		
		// Assign threads to search tasks
		int indexesPerThread = (maxIndex - minIndex) / (numOfCores - 1);
		
		ArrayList<Thread> threads = new ArrayList<Thread>();
		
		for(int i = minIndex; i < maxIndex; i += indexesPerThread) {
			threads.add(new Thread(new SearchTask(minIndex, maxIndex, query)));
		}
		
		for(int i = 0; i < threads.size(); ++i) {
			threads.get(i).start();
		}
		
		// Join threads before continue
		for(int i = 0; i < threads.size(); ++i) {
			try {
				threads.get(i).join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		for(int i = 0; i < list.size(); ++i) {
			arrayObj.add(list.get(i));
		}
		
		Timer.stop();
		System.out.println("Time: " + Timer.getTime());
		writeStatsToFile();
		return arrayObj;
	}
}
