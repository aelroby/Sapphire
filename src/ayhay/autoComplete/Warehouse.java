package ayhay.autoComplete;

//import java.io.File;
import java.util.ArrayList;
//import java.util.HashSet;
//import java.util.Iterator;
//import java.util.Set;

import org.json.simple.JSONArray;

import ayhay.FileManagement.FileManager;
import ayhay.dataStructures.StringScore;
import ayhay.utils.StringScoreComparator;
import info.debatty.java.stringsimilarity.JaroWinkler;


//import ayhay.FileManagement.FileManager;
//import ayhay.utils.LengthComparator;

public class Warehouse {

	private ArrayList<String> predicatesList;
	private ArrayList<String> literalsList;
	JaroWinkler jw;
//	private Set<String> labels;
//	private ArrayList<String> labelsList;
//	private ArrayList<String> labelsLines;
	
	
	public void initializeWarehouse(String literalsFile, String predicatesFile){
		
		long startTime = System.currentTimeMillis();
		System.out.println("Reading literals file...");
		FileManager fManagerPredicates = new FileManager();
		literalsList = fManagerPredicates.readFileLineByLine(literalsFile);
		System.out.println("Reading literals file finished!");
		System.out.println("Reading predicates file...");
		predicatesList = fManagerPredicates.readFileLineByLine(predicatesFile);
		System.out.println("Reading predicates file finished!");
		jw = new JaroWinkler();
		long stopTime = System.currentTimeMillis();
		double elapsedTime = stopTime - startTime;
		elapsedTime = elapsedTime / 60000;
		System.out.println("Warehouse initialization took: " + elapsedTime + " minutes");
		
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
		double score;
		String currentLiteral;
		// Search in LiteralsIndex
		for(int i = 0; i < literalsList.size(); ++i){
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
		JSONArray arrayObj = new JSONArray();
		// Search in predicates
		for(int i = 0; i < predicatesList.size(); ++i){
			if(predicatesList.get(i).toLowerCase().contains(query.toLowerCase())){
				arrayObj.add(predicatesList.get(i));
			}
		}
		
		// Search in labels
		for(int i = 0; i < literalsList.size(); ++i){
			if(literalsList.get(i).toLowerCase().contains(query.toLowerCase())){
				arrayObj.add(literalsList.get(i));
				if(arrayObj.size() >= 20)
					break;
			}
		}
		
		return arrayObj;
	}
}
