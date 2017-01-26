package ayhay.main;

import java.util.HashSet;
import java.util.Set;

import ayhay.utils.FileManager;

public class EvaluationAssignment {

	static int groupNum = 3;
	
	static int[] groupQuestionNum = {9, 8, 8};
	
	static int[] questionsNum = {4,3,3};
	
	public static void main(String[] args) {
		Set<Integer> chosenSet = new HashSet<Integer>();
		
		String contents = "";
		System.out.println("Assigning questions to participant...");
		for(int i = 0; i < groupNum; ++i) {
			chosenSet.clear();
			for(int j = 0; j < questionsNum[i]; ++j) {
				int chosenNum = 1 + (int)(Math.random() * groupQuestionNum[i]);
				while(chosenSet.contains(chosenNum)) {
					chosenNum = 1 + (int)(Math.random() * groupQuestionNum[i]);
				}
				chosenSet.add(chosenNum);
				contents += Integer.toString(i+1) + "." + Integer.toString(chosenNum) + ") \n";
			}
			contents += "=====================\n";
		}
		System.out.println(contents);
		FileManager.writeToFile("ParticipantSession.txt", contents);
		System.out.println("Done. Find the assignments in ParticipantSession.txt");
	}

}
