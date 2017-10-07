package sapphire.main;

import java.util.HashSet;
import java.util.Set;

import sapphire.utils.FileManager;

public class EvaluationAssignment {

	static int groupNum = 3;
	
	static int[] groupQuestionNum = {9, 8, 8};
	
	static int[] questionsNum = {4,3,3};
	
	static String[][] questions = {
			{
				"Country in which the Ganges starts", 			// 5-train
				"John F. Kennedy's vice president",  			// 4-train
				"Time zone of Salt Lake City",  				// 4-train
				"Children of Margaret Thatcher", 				// 4-train
				"Currency of the Czech Republic",				// 4-train
				"Designer of the Brooklyn Bridge",				// 4-train
				"Wife of U.S. president Abraham Lincoln",		// 3-train
				"Creator of Wikipedia",							// 3-train
				"Depth of lake Placid"							// 4-test
			},
			{
				"Instruments played by Cat Stevens",										// 5-train
				"Parents of the wife of Juan Carlos I",										// 5-train
				"U.S. state in which Fort Knox is located",									// 5-train
				"Person who is called Frank The Tank",										// 5-train
				"Birthdays of all actors of the television show Charmed",					// 5-train
				"Country in which the Limerick Lake is located",							// 5-train
				"Person to which Robert F. Kennedy's daughter is married",					// 5-train
				"Films directed by Steven Spielberg with a budget of at least $80 million"	// 5-train
			},
			{
				"Chess players who died in the same place they were born in",					// 5-train
				"Books by William Goldman with more than 300 pages",							// 5-train
				"Books by Jack Kerouac which were published by Viking Press",					// 5-train
				"Number of people living in the capital of Australia",							// 5-train
				"Films starring Clint Eastwood direct by himself",								// 5-train
				"Presidents born in 1945",														// 5-train
				"Find each company that works in both the aerospace and medicine industries",	// 5-train
				"Number of inhabitants of the most populous city in Canada"						// 5-train
			}
	};
	
	public static void main(String[] args) {
		Set<Integer> chosenSet = new HashSet<Integer>();
		
		String contents = "";
		System.out.println("Assigning questions to participant...");
		for(int i = 0; i < groupNum; ++i) {
			chosenSet.clear();
			for(int j = 0; j < questionsNum[i]; ++j) {
				double randomNum = Math.random();
				int chosenNum = 1 + (int)(randomNum * groupQuestionNum[i]);
				while(chosenSet.contains(chosenNum)) {
					randomNum = Math.random();
					chosenNum = 1 + (int)(randomNum * groupQuestionNum[i]);
				}
				chosenSet.add(chosenNum);
				contents += Integer.toString(i+1) + "." + Integer.toString(chosenNum) + ") "
				+ questions[i][chosenNum-1] + "\n";
			}
			contents += "=====================\n";
		}
		System.out.println(contents);
		FileManager.writeToFile("ParticipantSession.txt", contents);
		System.out.println("Done. Find the assignments in ParticipantSession.txt");
	}

}
