package sapphire.autoComplete;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.json.simple.JSONArray;

import com.abahgat.suffixtree.GeneralizedSuffixTree;

import me.xdrop.fuzzywuzzy.FuzzySearch;
import sapphire.dataStructures.StringScore;
import sapphire.utils.FileManager;
import sapphire.utils.LengthComparator;
import sapphire.utils.StringScoreComparator;
import sapphire.utils.StringScoreLengthComparator;
import sapphire.utils.Timer;


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
	private Set<String> setForTypeahead;
	private Set<StringScore> setForSuggestions;
	private Set<StringScore> setForPredicates;
	
	private Map<String, ArrayList<String>> semanticRelationsMap;
	
	// Number of cores available in runtime
	private int numOfCores;
	
	// Number of results to be found for typeahead
	private int resultsToBeFound;
	
	// Suffix tree
	private GeneralizedSuffixTree in;
	
	// Variables for statistics
	private int numOfSearchTasks;
	private int numOfIndexHits;
	private double pruningPercentage;
	private double suffixTreeTime;
	private double totalTime;
	
	private class PredicateSearchTask implements Runnable {
		int minIndex, maxIndex;
		String originalPredicate;
		
		public PredicateSearchTask(int min, int max, String originalPredicate) {
			minIndex = min;
			maxIndex = max;
			this.originalPredicate = originalPredicate;
		}
		public void run() {
			for(int i = minIndex; i < maxIndex; ++i){
				// If the predicate is the same as the given predicate, continue
				if(originalPredicate.compareTo(predicatesList.get(i)) == 0)
					continue;
				
				String currentPredicate = predicatesList.get(i);
				
				// Trim predicate
				currentPredicate = currentPredicate.substring(currentPredicate.lastIndexOf("/")+1,
						currentPredicate.length()-1).toLowerCase();
				
				String trimmedString = originalPredicate.substring(originalPredicate.lastIndexOf("/")+1,
						originalPredicate.length()-1).toLowerCase();
				
				double score = 1.0 * FuzzySearch.ratio(trimmedString, currentPredicate)/100;
				
				// If score is above threshold, add it to the candidate matches list
				if(score > 0.7){
					setForPredicates.add(new StringScore(predicatesList.get(i), score, i));
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
					setForTypeahead.add(literalsList.get(i));
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
		double threshold;
		
		public SuggestionTask(int minIndex, int maxIndex,
				String query, String trimmedString, double threshold) {
			this.minIndex = minIndex;
			this.maxIndex = maxIndex;
			this.query = query;
			this.trimmedString = trimmedString;
			this.threshold = threshold;
		}
		
		@Override
		public void run() {
			double score;
			String currentLiteral;
			for(int i = minIndex; i < maxIndex; ++i) {
				
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
				score = 1.0 * FuzzySearch.ratio(currentLiteral, trimmedString)/100;
				
				// If score is above threshold, add it to list
				if(score >= threshold){
					setForSuggestions.add(new StringScore(literalsList.get(i), score));
				}
			}
		}
		
	}
	
	// Fill in relations based on lemon
	private void fillSemanticRelations(String summaryFilePath) {
		
		ArrayList<String> lines = FileManager.readFileLineByLine(summaryFilePath);
		for(int i = 0; i < lines.size(); ++i) {
			String[] parts = lines.get(i).split(",");
			ArrayList<String> list = new ArrayList<String>();
			for(int j = 1; j < parts.length; ++j) {
				list.add(parts[j]);
			}
			semanticRelationsMap.put(parts[0], list);
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
		setForTypeahead = Collections.synchronizedSet(new HashSet<String>());
		
		// This list has to be synchronized because it gets filled
		// up by multiple threads
		setForSuggestions = Collections.synchronizedSet(new HashSet<StringScore>());
		
		// This list has to be synchronized because it gets filled
		// up by multiple threads
		setForPredicates = Collections.synchronizedSet(new HashSet<StringScore>());
		
		
		// Map for semantic relations for words
		semanticRelationsMap = new HashMap<String, ArrayList<String>> ();
		
		// Stats variables initialized
		numOfSearchTasks = numOfIndexHits = 0;
		pruningPercentage = totalTime = suffixTreeTime = 0.0;
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
	 * Clean the given String ArrayList from extra space at the end
	 * of each String, if it exists
	 * @param list
	 */
	public void removeSpaceFromEndOfString(ArrayList<String> list) {
		
		for(int i = 0; i < list.size(); ++i) {
			if(list.get(i).charAt(list.get(i).length()-1) == ' ') {
				list.set(i, list.get(i).substring(0, list.get(i).length()-1));
			}
		}
	}
	
	/**
	 * Reads the predicates and literals from files
	 * and divide literals in bins
	 * 
	 * @param literalsFile is the path of the literals file
	 * @param predicatesFile is the path of the predicates file
	 * @param mostFrequentLiteralsFile location of the file that contains most frequent literals
	 * @param numOfMostFrequent the number of literals that need to be indexed. If you don't know what this is, -1 is the value for you
	 */
	public void initializeWarehouse(String literalsFile, 
			String predicatesFile, String mostFrequentLiteralsFile, 
			String lemonSummaryFile, int numOfMostFrequent){
		
		Timer.start();
		
		System.out.println("Reading summary file...");
		fillSemanticRelations(lemonSummaryFile);
		System.out.println("Reading summary file finished!");
		
		System.out.println("Reading literals file...");
		literalsList = FileManager.readFileLineByLine(literalsFile);
		removeSpaceFromEndOfString(literalsList);
		System.out.println("Reading literals file finished!");
		
		System.out.println("Reading predicates file...");
		predicatesList = FileManager.readFileLineByLine(predicatesFile);
		removeSpaceFromEndOfString(predicatesList);
		System.out.println("Reading predicates file finished!");
		
		System.out.println("Reading frequent literals file...");
		frequentLiteralsList = FileManager.readFileLineByLine(mostFrequentLiteralsFile);
		removeSpaceFromEndOfString(frequentLiteralsList);
		
		int numOfLiteralsIndexed;
		if(numOfMostFrequent == -1) {
			numOfLiteralsIndexed = frequentLiteralsList.size();
		}
		else {
			numOfLiteralsIndexed = Math.min(numOfMostFrequent,
					frequentLiteralsList.size());
		}
		
		for(int i = 0; i < numOfLiteralsIndexed; ++i) {
			in.put(frequentLiteralsList.get(i).toLowerCase(), i);
		}
		System.out.println("Reading frequent literals file finished!");
		
//		jw = new JaroWinkler();
		System.out.println("Analyzing literals...");
		analyzeLiterals();
		Timer.stop();
		System.out.println("Warehouse initialization took: " + Timer.getTimeInSeconds());
		
	}
	
	/**
	 * Write stats to file
	 */
	private void writeStatsToFile() {
		String fileName = "TypeaheadStats.dat";
		String contents = "Typeahead_Tasks,Index_Hits,Hit_Ratio,Avg_Time,Suffix_Time,Pruning\n";
		contents += numOfSearchTasks + 
				"," + numOfIndexHits + 
				"," + 1.0 * numOfIndexHits / numOfSearchTasks + 
				"," + totalTime / numOfSearchTasks +
				"," + suffixTreeTime / numOfSearchTasks + 
				"," + pruningPercentage / numOfSearchTasks;
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
	 * @param originalPredicate The predicate to find similar predicates for
	 * @return An Arraylist of similar predicates
	 */
	public ArrayList<String> findSimilarStringsPredicates(String predicate){
		
		ArrayList<StringScore> matchesScores = new ArrayList<StringScore>(); 
		ArrayList<String> matches = new ArrayList<String>();
		String trimmedString;
		
		
		// Whatever is after the last /
		trimmedString = predicate.substring(predicate.lastIndexOf("/")+1, predicate.length()-1).toLowerCase();	
		String currentPredicate;

		// Semantic relations
		ArrayList<String> list = semanticRelationsMap.get(trimmedString);
		
		if(list != null) {
			for(int i = 0; i < predicatesList.size(); ++i){ 
				currentPredicate = predicatesList.get(i);
				// Trim predicate
				currentPredicate = currentPredicate.substring(currentPredicate.lastIndexOf("/")+1,
						currentPredicate.length()-1).toLowerCase();
				
				for(String element : list) {
					if(currentPredicate.compareTo(element) == 0) {
						matchesScores.add(new StringScore(predicatesList.get(i), 1));
					}
				}
				
			}
		}
		
		System.out.println("Searching in all predicates...");
		// Search in predicates
		int minIndex = 0;
		int maxIndex = predicatesList.size() - 1;
		
		// Assign threads to search tasks
		int indexesPerThread = (maxIndex - minIndex) / (numOfCores - 1);
		
		// Arraylist to keep track of threads
		ArrayList<Thread> threads = new ArrayList<Thread>();
		
		for(int i = minIndex; i < maxIndex; i += indexesPerThread) {
			threads.add(new Thread(new PredicateSearchTask(i,
					Math.min(i+indexesPerThread, predicatesList.size() - 1),
					predicate)));
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
		ArrayList<StringScore> newMatchesScores = new ArrayList<StringScore>();
		for(StringScore stringScore : setForPredicates) {
			newMatchesScores.add(stringScore);
		}
		java.util.Collections.sort(newMatchesScores, new StringScoreLengthComparator());
		matchesScores.addAll(newMatchesScores);
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
	public ArrayList<String> findSimilarStringsLiterals(String s, double score){
		
		setForSuggestions.clear();
		
		// ArrayList for match scores
		ArrayList<StringScore> matchesScores = new ArrayList<StringScore>(); 
		
		// ArrayList for the matches
		ArrayList<String> matches = new ArrayList<String>();
		
		// The trimmed string variable
		String trimmedString;
		
		// Check if it's not a literal
		if(!s.startsWith("\""))
			return matches;
		
		// Extract what's between double quotes
		trimmedString = s.substring(s.indexOf("\"")+1,
				s.indexOf("\"", s.indexOf("\"")+1)).toLowerCase();	// Just choose what is between brackets
		
		// Find similar literals withing 5 characters
		int minLength = trimmedString.length() - 2;
		int maxLength = trimmedString.length() + 3;
		
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
			threads.add(new Thread(new SuggestionTask(i, i + indexesPerThread, s, trimmedString, score)));
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
		for(StringScore stringScore : setForSuggestions) {
			matchesScores.add(stringScore);
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
		setForTypeahead.clear();
		
		// The returned JSON array
		JSONArray arrayObj = new JSONArray();
		
		// Search in all predicates
		for(int i = 0; i < predicatesList.size(); ++i){
			if(predicatesList.get(i).toLowerCase().contains(query.toLowerCase())){
				arrayObj.add(predicatesList.get(i));
			}
			ArrayList<String> semanticRelations = semanticRelationsMap.get(query.toLowerCase());
			if(semanticRelations != null) {
				for(String s : semanticRelations) {
					if(predicatesList.get(i).toLowerCase().contains(s.toLowerCase())){
						arrayObj.add(predicatesList.get(i));
					}
				}
			}
		}
		
		// Start the timer for this task
		Timer.start();
		
		// Search in index first
		Collection<Integer> list = in.search(query.toLowerCase());
		HashSet<Integer> output = null;
		if(list.size() > 0) {
			output = (HashSet<Integer>) list;
		}
		else {
			output = new HashSet<Integer>();
		}

		// If found a match, that's a hit
		if(output.size() > 0) {
			++numOfIndexHits;
		}
		
		for(Integer index : output) {
			arrayObj.add(frequentLiteralsList.get(index));
    		--resultsToBeFound;
    	}
		double tempStopTime;
		// If all results were found in suffix tree
		if(resultsToBeFound < 0) {
			Timer.stop();
			suffixTreeTime += Timer.getTimeInSeconds();
			totalTime += Timer.getTimeInSeconds();
			writeStatsToFile();
			return arrayObj;
		}
		
		Timer.stop();
		suffixTreeTime += Timer.getTimeInSeconds();
		tempStopTime = Timer.getTimeInSeconds();
		Timer.start();
		// In length bins
		// Search in bins with a minimum length of the query
		// and a maximum of the query length + 10
		int minLength = query.length()-1;
		int maxLength = query.length() + 9;
		
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
		
		pruningPercentage += 1 - (1.0 * (maxIndex - minIndex) / literalsList.size());
		try{
		System.out.println("Searching for " + query +  " between indeces: " + minIndex + 
				" corresponding to length " + (literalsList.get(minIndex).length()-5) +
				" and " + maxIndex + " corresponding to length " + 
				(literalsList.get(maxIndex).length()-5));
		}
		catch(IndexOutOfBoundsException e) {
			System.out.println("query = " + query);
			System.out.println("minIndex = " + minIndex);
			System.out.println("maxIndex = " + maxIndex);
		}
		
		// Assign threads to search tasks
		int indexesPerThread = (maxIndex - minIndex) / (numOfCores);
		
		// Arraylist to keep track of threads
		ArrayList<Thread> threads = new ArrayList<Thread>();
		
		for(int i = minIndex; i < maxIndex; i += indexesPerThread) {
			threads.add(new Thread(new SearchTask(i, i + indexesPerThread, query)));
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
		
		// Stop timer
		Timer.stop();
		totalTime += tempStopTime + Timer.getTimeInSeconds();
		
		// Sort by length
		ArrayList<String> tempList = new ArrayList<String>();
		for(String string : setForTypeahead) {
			if(!arrayObj.contains(string))
				tempList.add(string);
		}
		java.util.Collections.sort(tempList, new LengthComparator());
		
		// Fill the array
		for(int i = 0; i < tempList.size() && resultsToBeFound > 0; ++i, --resultsToBeFound) {
			arrayObj.add(tempList.get(i));
		}
		
		
		
		// Update the stats file with number of tasks and hits
		writeStatsToFile();
		
		return arrayObj;
	}
}
