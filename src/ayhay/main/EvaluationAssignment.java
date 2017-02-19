package ayhay.main;

import java.util.HashSet;
import java.util.Set;

import ayhay.utils.FileManager;

public class EvaluationAssignment {

	static int groupNum = 3;
	
	static int[] groupQuestionNum = {9, 8, 8};
	
	static int[] questionsNum = {4,3,3};
	
	static String[][] questions = {
			{
				"Country in which the Ganges starts",
				"John F. Kennedy's vice president",
				"Time zone of Salt Lake City",
				"Children of Margaret Thatcher",
				"Currency of the Czech Republic",
				"Designer of the Brooklyn Bridge",
				"Wife of U.S. president Abraham Lincoln",
				"Creator of Wikipedia",
				"Depth of lake Placid"
			},
			{
				"Instruments played by Cat Stevens",
				"Parents of the wife of Juan Carlos I",
				"U.S. state in which Fort Knox is located",
				"Person who is called Frank The Tank",
				"Birthdays of all actors of the television show Charmed",
				"Country in which the Limerick Lake is located",
				"Person to which Robert F. Kennedy's daughter is married",
				"Films directed by Steven Spielberg with a budget of at least $80 million"	
			},
			{
				"Chess players who died in the same place they were born in",
				"Books by William Goldman with more than 300 pages",
				"Books by Jack Kerouac which were published by Viking Press",
				"Number of people living in the capital of Australia",
				"Films starring Clint Eastwood direct by himself",
				"Presidents born in 1945",
				"Find each company that works in both the aerospace and medicine industries",
				"Number of inhabitants of the most populous city in Canada"
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
