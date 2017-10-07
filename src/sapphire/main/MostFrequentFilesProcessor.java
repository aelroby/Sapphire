package sapphire.main;

import java.util.ArrayList;

import sapphire.utils.FileManager;

public class MostFrequentFilesProcessor {

	public static void processPredicatesFile() {
		String contents = "";
		ArrayList<String> fileLines = FileManager.readFileLineByLine(
				"/home/ahmed/work/workspace/Data/Metadata/MostFrequent/most_frequent_predicates.csv");
		for(String s : fileLines) {
			String newLine = "<" + s + ">" + System.lineSeparator();
			contents += newLine;
		}
		FileManager.writeToFile("/home/ahmed/work/workspace/Data/Metadata/MostFrequent/FrequentPredicates.dat", contents);
	}
	
	public static void processLiteralsFile() {
		// most frequent literals 
				String contents = "";
				ArrayList<String> fileLines = FileManager.readFileLineByLine(
						"/home/ahmed/work/workspace/Data/Metadata/40000.dat");
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
				
				FileManager.writeToFile("/home/ahmed/work/workspace/Data/Metadata/40000_refined.dat", contents);
	}
	
	public static void main(String[] args) {
		System.out.println("Processing file...");
		processLiteralsFile();
		System.out.println("Finished processing!");
	}

}
