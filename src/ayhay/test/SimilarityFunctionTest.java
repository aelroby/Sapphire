package ayhay.test;

import info.debatty.java.stringsimilarity.JaroWinkler;
import me.xdrop.fuzzywuzzy.FuzzySearch;

public class SimilarityFunctionTest {

	public static void main(String[] args) {
		
		JaroWinkler jw = new JaroWinkler();
		String s1 = "African Countries";
		String s2 = "Countries in Africa";
		
				
		System.out.println("FuzzyWuzzy Similarity between \"" + s1 + "\" and \"" + s2 + "\": " + 
				1.0 * FuzzySearch.ratio(s1, s2)/100);
				
		System.out.println("Jaro-Winkler Similarity between \"" + s1 + "\" and \"" + s2 + "\": " + 
				jw.similarity(s1, s2));

	}

}
