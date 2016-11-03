package ayhay.dataStructures;

import java.util.ArrayList;

public class SPARQLQuery {
	private ArrayList<String> select;
	private ArrayList<ArrayList<String>> where;
	private ArrayList<String> modifiers;
	
	public SPARQLQuery(String query) {
		select = new ArrayList<String>();
		where = new ArrayList<ArrayList<String>>();
		modifiers = new ArrayList<String>();
	}
}
