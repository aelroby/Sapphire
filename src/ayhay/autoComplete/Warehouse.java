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


/**
 * @author ahmed
 *
 * This class serves as the warehouse for predicates
 * and literals used for lookup in typeahead and
 * suggestions
 */
public class Warehouse {

	// Lists to be searched 
	private ArrayList<String> predicatesList;
	private ArrayList<String> literalsList;
	private ArrayList<String> frequentLiteralsList;
	
	// indexes and lengths
	private ArrayList<Integer> indexes;
	private ArrayList<Integer> lengths;
	
	// Synchronized list used by threads
	private List<String> listForTypeahead;
	private List<StringScore> listForSuggestions;
	
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
					listForTypeahead.add(literalsList.get(i));
					--resultsToBeFound;
					if(resultsToBeFound < 0)
						break;
				}
			}
		}
		
	}
	
	/**
	 * 
	 * @author ahmed
	 * 
	 * The search task class for threads
	 */
	private class SuggestionTask implements Runnable {

		int minIndex, maxIndex;
		String query, trimmedString;
		
		public SuggestionTask(int minIndex, int maxIndex,
				String query, String trimmedString) {
			this.minIndex = minIndex;
			this.maxIndex = maxIndex;
			this.query = query;
			this.trimmedString = trimmedString;
		}
		
		@Override
		public void run() {
			double score;
			String currentLiteral;
			for(int i = minIndex; i < maxIndex; ++i){
				// If the literal is the same as the given string, continue
				if(query.compareTo(literalsList.get(i)) == 0)
					continue;
				
				currentLiteral = literalsList.get(i);
				try{
					currentLiteral = currentLiteral.substring(currentLiteral.indexOf("\"")+1,
							currentLiteral.indexOf("\"", currentLiteral.indexOf("\"")+1)).toLowerCase();
				}
				catch(StringIndexOutOfBoundsException e){
					System.out.println("A non-literal value was found in literalsList: " + literalsList.get(i));
				}
				// Get the score
				score = jw.similarity(currentLiteral, trimmedString);
				
				// If score is above threshold, add it to list
				if(score > 0.6){
					listForSuggestions.add(new StringScore(literalsList.get(i), score));
				}
			}
		}
		
	}
	
	// Constructor of the Warehouse class
	public Warehouse() {
		indexes = new ArrayList<Integer>();
		lengths = new ArrayList<Integer>();
		
		// Initialize suffix tree
		in = new GeneralizedSuffixTree();
		
		// Get the number of cores available at runtime
		numOfCores = Runtime.getRuntime().availableProcessors();
		
		// This list has to be synchronized because it gets filled
		// up by multiple threads
		listForTypeahead = Collections.synchronizedList(new ArrayList<String>());
		
		// This list has to be synchronized because it gets filled
		// up by multiple threads
		listForSuggestions = Collections.synchronizedList(new ArrayList<StringScore>());
		
		// Stats variables initialized
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
	 * Reads the predicates and literals from files
	 * and divide literals in bins
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
	
	/**
	 * Write stats to file
	 */
	private void writeStatsToFile() {
		String fileName = "IndexStats.dat";
		String contents = "Typeahead_Tasks,Index_Hits,Hit_Ratio\n";
		contents += numOfSearchTasks + "," + numOfIndexHits + 
				"," + 1.0 * numOfIndexHits / numOfSearchTasks;
		FileManager.writeToFile(fileName, contents);
	}
	
	/**
	 * Write histogram data (length of literals) into a file
	 * @param fileName name of the file to write the histogram data
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
	
	/**
	 * Find similar predicates to the one sent to this function
	 * @param s The predicate to find similar predicates for
	 * @return An Arraylist of similar predicates
	 */
	public ArrayList<String> findSimilarStringsPredicates(String s){
		ArrayList<StringScore> matchesScores = new ArrayList<StringScore>(); 
		ArrayList<String> matches = new ArrayList<String>();
		String trimmedString;

		// Whatever is after the last /
		trimmedString = s.substring(s.lastIndexOf("/")+1, s.length()-1).toLowerCase();	
		double score;
		String currentPredicate;
		
		// Search in predicates
		for(int i = 0; i < predicatesList.size(); ++i){
			// If the predicate is the same as the given predicate, continue
			if(s.compareTo(predicatesList.get(i)) == 0)
				continue;
			
			currentPredicate = predicatesList.get(i);

			// Trim predicate
			currentPredicate = currentPredicate.substring(currentPredicate.lastIndexOf("/")+1,
					currentPredicate.length()-1).toLowerCase();
			
			score = jw.similarity(currentPredicate, trimmedString); 
			
			// If score is above threshold, add it to the candidate matches list
			if(score > 0.5){
				matchesScores.add(new StringScore(predicatesList.get(i), score));
			}
		}

		// Sort the candidate matches based on score and return top 5
		java.util.Collections.sort(matchesScores, new StringScoreComparator());
		for(int i = 0; i < 5 && i < matchesScores.size(); ++i){
			matches.add(matchesScores.get(i).getS());
		}
		return matches;
		
	}
	
	/**
	 * Find similar literals to the one sent to this function
	 * @param s The literal to find similar predicates for
	 * @return An Arraylist of similar literals
	 */
	public ArrayList<String> findSimilarStringsLiterals(String s){
		// ArrayList for match scores
		ArrayList<StringScore> matchesScores = new ArrayList<StringScore>(); 
		
		// ArrayList for the matches
		ArrayList<String> matches = new ArrayList<String>();
		
		// The trimmed string variable
		String trimmedString;
		
		// Check if it's not a literal. URIs support should be added later 
		if(!s.startsWith("\""))
			return matches;
		
		// Extract what's between double quotes
		trimmedString = s.substring(s.indexOf("\"")+1, s.indexOf("\"", s.indexOf("\"")+1)).toLowerCase();	// Just choose what is between brackets
		
		// Find similar literals withing 5 characters
		int minLength = trimmedString.length();
		int maxLength = trimmedString.length() + 5;
		
		int minIndex = Integer.MAX_VALUE;
		int maxIndex = -1;
		
		// Determine indices for search in bins
		for(int i = 0; i < lengths.size(); ++i){
			if(lengths.get(i) > maxLength) {
				maxIndex = indexes.get(i);
				break;
			}
			if(minLength >= lengths.get(i)) {
				minIndex = indexes.get(i);
			}
		}
		
		// Assign threads to search tasks
		int indexesPerThread = (maxIndex - minIndex) / (numOfCores - 1);
		
		// Arraylist to keep track of threads
		ArrayList<Thread> threads = new ArrayList<Thread>();
		
		for(int i = minIndex; i < maxIndex; i += indexesPerThread) {
			threads.add(new Thread(new SuggestionTask(minIndex, maxIndex, s, trimmedString)));
		}
		
		// Start threads
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
		
		// Copy contents of the synchronized list into the matchesScores list
		for(int i = 0; i < listForSuggestions.size(); ++i) {
			matchesScores.add(listForSuggestions.get(i));
		}
		
		// Sort the candidate matches based on score and return top 5
		java.util.Collections.sort(matchesScores, new StringScoreComparator());
		for(int i = 0; i < 5 && i < matchesScores.size(); ++i){
			matches.add(matchesScores.get(i).getS());
		}
		return matches;
	}
	
	/**
	 * Find typeahead matches to the string typed so far
	 * @param query the string to find exact matches for
	 * @return A JSON array that contains the suggested words
	 */
	@SuppressWarnings("unchecked")
	public JSONArray findMatches(String query){
		
		// Increment the search tasks counter
		++numOfSearchTasks;
		
		// Start the timer for this task
		Timer.start();
		
		// Number of needed matches
		resultsToBeFound = 10;
		
		// Clear the matches list
		listForTypeahead.clear();
		
		// The returned JSON array
		JSONArray arrayObj = new JSONArray();
		
		// Search in all predicates
		for(int i = 0; i < predicatesList.size(); ++i){
			if(predicatesList.get(i).toLowerCase().contains(query.toLowerCase())){
				arrayObj.add(predicatesList.get(i));
			}
		}
		
		// Search in index first
		HashSet<Integer> output = (HashSet<Integer>) in.search(query.toLowerCase());

		// If found a match, that's a hit
		if(output.size() > 0) {
			++numOfIndexHits;
		}
		
		for(Integer index : output) {
    		arrayObj.add(frequentLiteralsList.get(index));
    		--resultsToBeFound;
    	}
		
		// If all results were found in suffix tree
		if(resultsToBeFound < 0) {
			writeStatsToFile();
			return arrayObj;
		}
		
		
		// In length bins
		// Search in bins with a minimum length of the query
		// and a maximum of the query length + 12
		int minLength = query.length();
		int maxLength = query.length() + 12;
		
		int minIndex = Integer.MAX_VALUE;
		int maxIndex = -1;
		
		// Determine which indices of the literals array
		// to search within
		for(int i = 0; i < lengths.size(); ++i){
			if(lengths.get(i) > maxLength) {
				maxIndex = indexes.get(i);
				break;
			}
			if(minLength >= lengths.get(i)) {
				minIndex = indexes.get(i);
			}
		}
		
		System.out.println("Searching between indeces: " + minIndex + 
				" corresponding to length " + (literalsList.get(minIndex).length()-5) +
				" and " + maxIndex + " corresponding to length " + 
				(literalsList.get(maxIndex).length()-5));
		
		// Assign threads to search tasks
		int indexesPerThread = (maxIndex - minIndex) / (numOfCores - 1);
		
		// Arraylist to keep track of threads
		ArrayList<Thread> threads = new ArrayList<Thread>();
		
		for(int i = minIndex; i < maxIndex; i += indexesPerThread) {
			threads.add(new Thread(new SearchTask(minIndex, maxIndex, query)));
		}
		
		// Start threads
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
		
		// Fill the array
		for(int i = 0; i < listForTypeahead.size(); ++i) {
			arrayObj.add(listForTypeahead.get(i));
		}
		
		// Stop timer
		Timer.stop();
		System.out.println("Time: " + Timer.getTime());
		
		// Update the stats file with number of tasks and hits
		writeStatsToFile();
		
		return arrayObj;
	}
}
