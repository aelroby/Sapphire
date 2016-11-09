package ayhay.autoComplete;

import java.util.ArrayList;
import java.util.PriorityQueue;

import org.json.simple.JSONArray;

import ayhay.dataStructures.LiteralStat;
import ayhay.dataStructures.StringScore;
import ayhay.utils.FileManager;
import ayhay.utils.LiteralStatComparator;
import ayhay.utils.StringScoreComparator;
import ayhay.utils.Timer;
import info.debatty.java.stringsimilarity.JaroWinkler;



public class Warehouse {

	private ArrayList<String> predicatesList;
	private ArrayList<String> literalsList;
	private ArrayList<Integer> indexes;
	private ArrayList<Integer> lengths;
	private PriorityQueue<LiteralStat> mostFrequentQueue;
	private PriorityQueue<LiteralStat> mostFrequentResourcesQueue;
	private JaroWinkler jw; 
	
	public Warehouse() {
		indexes = new ArrayList<Integer>();
		lengths = new ArrayList<Integer>();
		mostFrequentQueue = new PriorityQueue<LiteralStat>(new LiteralStatComparator());
		mostFrequentResourcesQueue = new PriorityQueue<LiteralStat>(new LiteralStatComparator());
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

	
	private void analyzeLiterals() {
		
		int lastLength = 0;
		
		for(int i = 0; i < literalsList.size(); ++i) {
			
			// Update lengths of literals array with indexes
			if(lastLength != literalsList.get(i).length() - 5) {
				indexes.add(i);
				lengths.add(literalsList.get(i).length() - 5); // -5 for ""@en
			}
			lastLength = literalsList.get(i).length() - 5;
			
			// TODO: Update min-heap for most frequent literals
			
			
			// TODO: Update min-heap for most frequent resources associated with literals
			
		}
		
	}
	
	public void initializeWarehouse(String literalsFile, String predicatesFile){
		
		Timer.start();
		System.out.println("Reading literals file...");
		literalsList = FileManager.readFileLineByLine(literalsFile);
		System.out.println("Reading literals file finished!");
		System.out.println("Reading predicates file...");
		predicatesList = FileManager.readFileLineByLine(predicatesFile);
		System.out.println("Reading predicates file finished!");
		jw = new JaroWinkler();
		System.out.println("Analyzing literals...");
		analyzeLiterals();
		Timer.stop();
		System.out.println("Warehouse initialization took: " + Timer.getTime());
		
	}
	
	public void writeLengthHistogram(String fileName) {
		String contents = "Length,Frequency\n";
		int literalLength = 0;
		int frequency = 0;
		for(String literal : literalsList) {
			if(!literal.startsWith("\""))
				continue;
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
		Timer.start();
		JSONArray arrayObj = new JSONArray();
		
		// Search in predicates
		for(int i = 0; i < predicatesList.size(); ++i){
			if(predicatesList.get(i).toLowerCase().contains(query.toLowerCase())){
				arrayObj.add(predicatesList.get(i));
			}
		}
		
		// Search in literals
		
		// Choose bins according to length
		int minLength = query.length();
		int maxLength = query.length() + 10;
		
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
		for(int i = minIndex; i < maxIndex; ++i){
			if(literalsList.get(i).toLowerCase().contains(query.toLowerCase())){
				System.out.println("Found a match in index" + i);
				arrayObj.add(literalsList.get(i));
				if(arrayObj.size() >= 20)
					break;
			}
		}
		Timer.stop();
		System.out.println("Time: " + Timer.getTime());
		return arrayObj;
	}
}
