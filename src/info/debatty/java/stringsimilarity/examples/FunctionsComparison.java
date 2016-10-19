package info.debatty.java.stringsimilarity.examples;

import info.debatty.java.stringsimilarity.Cosine;
import info.debatty.java.stringsimilarity.Jaccard;
import info.debatty.java.stringsimilarity.JaroWinkler;
import info.debatty.java.stringsimilarity.NGram;
import info.debatty.java.stringsimilarity.NormalizedLevenshtein;
import info.debatty.java.stringsimilarity.SorensenDice;

public class FunctionsComparison {

	public static void main(String[] args) {

		double similarityScore;
		String s1 = "name";
		String s2 = "familyname";
		
		System.out.println("Comparison between \"" + s1 + "\" and \"" + s2 + "\":");
		
		// Jaccard Index
		Jaccard j2 = new Jaccard(2);
		System.out.println("Jaccard Score: " + j2.similarity(s1, s2));
		
		// Jaro-Winkler
		JaroWinkler jw = new JaroWinkler();
		System.out.println("Jaro-Winkler Score: " + jw.similarity(s1, s2));
		
		// Cosine
		Cosine cos = new Cosine(3);
		System.out.println("Cosine Score: " + cos.similarity(s1, s2));
		
		// Normalized Levenshtein
        NormalizedLevenshtein l = new NormalizedLevenshtein();
        similarityScore = 1 - l.distance(s1, s2);
        System.out.println("Normalized Levenshtein Score: " + similarityScore);
        
        // NGram
        NGram twogram = new NGram(2);
        System.out.println("NGram Score: " + twogram.distance(s1, s2));
        
        // Sorensen-Dice
        SorensenDice sd = new SorensenDice(2);
        System.out.println("Sorensen-Dice Score: " + sd.similarity(s1, s2));
	}

}
