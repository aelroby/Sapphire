package ayhay.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SemanticRelationMapTest {

	public static void main(String[] args) {
		Map<String, ArrayList<String>> semanticRelationsMap = new HashMap<String, ArrayList<String>>();
		ArrayList<String> list = new ArrayList<String>();
		list.add("spouse");
		semanticRelationsMap.put("wife", list);
		
		list = new ArrayList<String>();
		list.add("vp");
		semanticRelationsMap.put("vice president", list);
		
		for (Map.Entry<String, ArrayList<String>> entry : semanticRelationsMap.entrySet()) { 
			System.out.println("Key: " + entry.getKey());
			ArrayList<String> value = entry.getValue();
			for(String s : value) {
				System.out.println("Value: " + s);
			}
		}
	}

}
