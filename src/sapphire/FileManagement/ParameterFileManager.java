/**
 * This class is concerned with reading and checking the correctness of parameter file
 */
package sapphire.FileManagement;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 * @author Ahmed El-Roby
 *
 */
public class ParameterFileManager {

	private String TDBDirectory, labelsFile, predicatesFile, frequentLiteralsFile, lemonSummaryFile;
	String fileName;
	
	/**
	 * Constructs a ParameterFileManager
	 * @param fileName The parameter file's path
	 * @throws IOException
	 */
	public ParameterFileManager(String fileName) throws IOException {
		System.out.println("Working Directory = " + System.getProperty("user.dir"));
		File f = new File(fileName);
		if(!f.exists()){
			throw new FileNotFoundException("Parameter file " + fileName + "not found");
		}
		this.fileName = fileName;
	}

	/**
	 * @return TDB Directory form parameter file
	 */
	public String getTDBDirectory(){
		return TDBDirectory;
	}
	
	public String getFrequentLiteralsFile() {
		return frequentLiteralsFile;
	}
	
	public String getLabelsFile() {
		return labelsFile;
	}

	public String getPredicatesFile() {
		return predicatesFile;
	}
	
	public String getLemonSummaryFile() {
		return lemonSummaryFile;
	}
	
	public void readParameterFile() throws IOException{
		BufferedReader br = new BufferedReader(new FileReader(fileName));
		
		// First line is TDB directory
		String line = br.readLine();
		String[] parts = line.split("=");
		if(parts[0].compareTo("TDBDirectory") == 0){
			TDBDirectory = parts[1];
			try{
				File f = new File(TDBDirectory);
				if(!f.exists()){
					br.close();
					throw new FileNotFoundException("TDB Directory " + TDBDirectory + " not found");
				}
			}
			catch(FileNotFoundException e){
				System.out.println(e.getMessage());
			}
		}
		else{
			br.close();
			throw new IllegalArgumentException("First line should be formatted \"TDB=<TDBDirectory>\"");
		}
		
		// Second line is labels file
		line = br.readLine();
		parts = line.split("=");
		if(parts[0].compareTo("Labels") == 0){
			labelsFile = parts[1];
			try{
				File f = new File(labelsFile);
				if(!f.exists()){
					br.close();
					throw new FileNotFoundException("Labels directory " + labelsFile + " file not found");
				}
			}
			catch(FileNotFoundException e){
				System.out.println(e.getMessage());
			}
		}
		else{
			br.close();
			throw new IllegalArgumentException("Second line should be formatted \"Labels=<groundTruthFilesDirectory>\"");
		}
		
		// Third line is predicates file
		line = br.readLine();
		parts = line.split("=");
		if(parts[0].compareTo("Predicates") == 0){
			predicatesFile = parts[1];
			try{
				File f = new File(predicatesFile);
				if(!f.exists()){
					br.close();
					throw new FileNotFoundException("predicates file " + predicatesFile + " file not found");
				}
			}
			catch(FileNotFoundException e){
				System.out.println(e.getMessage());
			}
		}
		else{
			br.close();
			throw new IllegalArgumentException("Third line should be formatted \"InputLinks=<InputLinksFilesDirectory>\"");
		}
		
		// Fourth line is most frequent literals
		line = br.readLine();
		parts = line.split("=");
		if(parts[0].compareTo("FrequentLiterals") == 0){
			frequentLiteralsFile = parts[1];
			try{
				File f = new File(frequentLiteralsFile);
				if(!f.exists()){
					br.close();
					throw new FileNotFoundException("most frequent literals file "
					+ frequentLiteralsFile + " not found");
				}
			}
			catch(FileNotFoundException e){
				System.out.println(e.getMessage());
			}
		}
		else{
			br.close();
			throw new IllegalArgumentException("Fourth line should be formatted \"FrequentLiterals=<FrequentLiteralsFilesDirectory>\"");
		}
		
		// Fifth line is the Lemon Summary
		line = br.readLine();
		parts = line.split("=");
		if(parts[0].compareTo("LemonSummary") == 0){
			lemonSummaryFile = parts[1];
			try{
				File f = new File(lemonSummaryFile);
				if(!f.exists()){
					br.close();
					throw new FileNotFoundException("most frequent literals file "
					+ lemonSummaryFile + " not found");
				}
			}
			catch(FileNotFoundException e){
				System.out.println(e.getMessage());
			}
		}
		else{
			br.close();
			throw new IllegalArgumentException("Fourth line should be formatted \"FrequentLiterals=<FrequentLiteralsFilesDirectory>\"");
		}
		br.close();
	}

}
