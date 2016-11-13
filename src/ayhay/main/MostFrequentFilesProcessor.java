package ayhay.main;

import java.util.ArrayList;

import ayhay.utils.FileManager;

public class MostFrequentFilesProcessor {

	public static void main(String[] args) {
		
		// most frequent literals 
		String contents = "";
		ArrayList<String> fileLines = FileManager.readFileLineByLine(
				"/home/ahmed/work/workspace/Data/Metadata/MostFrequent/most_frequent_literals_person.html");
		for(String s : fileLines) {
			if(s.contains("@en")) {
				String candidate = s.substring(s.indexOf("\""),
						s.lastIndexOf("@en")+3);
				boolean ascii = true;
				for(int i = 0; i < candidate.length(); ++i) {
					if(candidate.charAt(i) < 0 
							|| candidate.charAt(i) > 127) {
						ascii = false;
						break;
					}
				}
				if(ascii) {
					contents += candidate + "\n";
				}
				
			}
		}
		
		FileManager.writeToFile("FrequentLiterals.dat", contents);
		
	}

}
