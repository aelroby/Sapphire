package ayhay.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class FileManager {
	
	
	public static ArrayList<String> readFileLineByLine(String fileName){
		ArrayList<String> lines = new ArrayList<String>();
		String line;
		try {
			FileReader fileReader = new FileReader(fileName);
			BufferedReader bufferedReader = new BufferedReader(fileReader);

	            while((line = bufferedReader.readLine()) != null) {
	                lines.add(line);
	            }   
	            bufferedReader.close();  
	            fileReader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return lines;
	}
	
	public static void writeToFile(String filePath, String contents) {
		BufferedWriter writer;
		try {
			writer = new BufferedWriter( new FileWriter(filePath));
			writer.write(contents);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
}

