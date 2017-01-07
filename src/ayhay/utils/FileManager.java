package ayhay.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

public class FileManager {
	
	
	public static ArrayList<String> readFileLineByLine(String filePath){
		ArrayList<String> lines = new ArrayList<String>();
		String line;
		try {
			FileReader fileReader = new FileReader(filePath);
			BufferedReader bufferedReader = new BufferedReader(fileReader);

	            while((line = bufferedReader.readLine()) != null) {
	                lines.add(line);
	            }   
	            bufferedReader.close();  
	            fileReader.close();
		} catch (FileNotFoundException e) {
			System.out.println("File " + filePath + " not found");
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("Error reading file " + filePath);
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
			System.out.println("Error writing to file " + filePath);
			e.printStackTrace();
		}
		
	}
	
	public static void appendToFileWithNewLine(String filePath, String contents) {
		try(FileWriter fw = new FileWriter(filePath, true);
		    BufferedWriter bw = new BufferedWriter(fw);
		    PrintWriter out = new PrintWriter(bw))
		{
		    out.println(contents);
		} catch (IOException e) {
			System.out.println("Error appending to file " + filePath);
			e.printStackTrace();
		}
	}
	
	public static void appendToFileNoNewLine(String filePath, String contents) {
		try(FileWriter fw = new FileWriter(filePath, true);
		    BufferedWriter bw = new BufferedWriter(fw);
		    PrintWriter out = new PrintWriter(bw))
		{
		    out.print(contents);
		} catch (IOException e) {
			System.out.println("Error appending to file " + filePath);
			e.printStackTrace();
		}
	}
	
}


