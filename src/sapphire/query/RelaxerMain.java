package sapphire.query;

import java.util.Map;
import java.util.LinkedHashMap;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.PriorityQueue;

import java.util.HashSet;
import java.util.Iterator;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;

import java.util.LinkedHashSet;


public class RelaxerMain {
	//Final
	
	ArrayList<ArrayList<String>> literalsToConnect = new ArrayList<ArrayList<String>>();
    ArrayList <String> predicatesToFavour = new ArrayList<String>(); //+1 for these, + 5 otherwise
    HashSet<String> PTFHelper = new HashSet<String>();
    
    LinkedHashMap<Triple,Integer> uniqueVisitors = new LinkedHashMap<Triple, Integer>(); //To see if every seed group has been at this tuple
    HashMap<Triple,Integer> costSoFar = new HashMap<Triple,Integer>(); //Cost so far to reach said triple, to be weighted by input predicate
    ArrayList<HashSet<Triple>> expandedIn = new ArrayList<HashSet<Triple>> (); //To avoid an already expanded triple from the same seed being expanded again
	ArrayList <PriorityQueue<Triple>> queues = new ArrayList <PriorityQueue<Triple>> ();
	TriplePriority np = new TriplePriority(this);
	int numberOfSeeds;
	int positionInQueues;
	int unencounteredFormatError = 0;
	
	LinkedHashSet<Triple> exploredOnly = new LinkedHashSet<Triple> (); //only what's been taken from the head of the PQ and the literal that made them all connected
	ArrayList<HashSet<Integer>> connectionSet = new ArrayList <HashSet<Integer>>(); //This will become a replacement to uniqueVisitors in connectionCheck, elsewhere uniqueVisitors is still neeeded
	HashMap<Triple, HashSet<Integer>> connectionSetHelper = new HashMap<Triple, HashSet<Integer>>();
	int addedInIHS = 0;
	
	HashMap<String, Integer> makeSet = new HashMap<String, Integer>();
	HashMap<Triple, Integer> edgeCost = new HashMap<Triple, Integer>();
	EdgePriority ep = new EdgePriority(this); //gets the predicate from each triples and compares based on their integer value in edgeCost
	PriorityQueue<Triple> edgesInIncreasingWeight = new PriorityQueue<Triple> (11, ep);
	LinkedHashSet<Triple> MST = new LinkedHashSet<Triple>(); //THIS IS THE STEINER TREE
	
	HashMap<String, Integer> degreeOfVertex = new HashMap<String, Integer>();//a vertex is either a subject or object in a triple/tuple
	
	public RelaxerMain(ArrayList<ArrayList<String>> where) {

		for(ArrayList<String> manyTriples : where){ //is there where similar literals come in? Assuming so. If not logic will have to be modified TODO clarify
			ArrayList<String> tmpLiterals = new ArrayList<String>();
			for(String triple : manyTriples){
				String [] splitStr = triple.trim().split("\\s+"); //trim excess white space & split by white space
				tmpLiterals.add(splitStr[2]);
				if(!PTFHelper.contains(splitStr[1])){
					PTFHelper.add(splitStr[1]);
					predicatesToFavour.add(splitStr[1]);
				}
			}
			literalsToConnect.add(tmpLiterals);
		}

		numberOfSeeds = literalsToConnect.length;
		positionInQueues = 0;
		for(int i = 0; i < numberOfSeeds; i++) {
			connectionSet.add(new HashSet<Integer>());
			connectionSet.get(i).add(i); //all different seed groups start in different elements (sets) in the arraylist, and get grouped together as they connect
		}
	}
	
	
	public LinkedHashSet<Triple> runIt() {
		long startTime = System.currentTimeMillis();

		relaxQuery_v2();
		//printResults();
		//printExploredOnly();
		createMST();
		//printMST();

		long endTime = System.currentTimeMillis();
		System.out.println("Execution time is: " + ((endTime - startTime) / 1000) + " seconds");

		return MST;
	}
	
	public void relaxQuery_v2() throws IOException {
		int iterationCounter = 0;
		for(ArrayList<String> subArray : literalsToConnect) {
			PriorityQueue<Triple> pq = new PriorityQueue<Triple>(11, np);
			HashSet<Triple> hs = new HashSet<Triple>();
			int addedCounter = 0;
			for(String literal : subArray) { //Initialize the starting triples aka triples to expand from
				String queryString =
						"SELECT ?s ?p WHERE { " +
						"	 ?s ?p " + literal + " . " +
						"}";
				Query query = QueryFactory.create(queryString);
				QueryExecution qexec = QueryExecutionFactory.sparqlService("http://dbpedia.org/sparql", query);
				
				try {
					ResultSet results = qexec.execSelect();
					while(results.hasNext()) {
						QuerySolution solution = results.nextSolution();
						Triple triple = new Triple(solution.get("?s").toString(), solution.get("?p").toString(), literal);//HAD to take out qoutes here
						System.out.println("Initializing with these tuples: " + triple.toString());
						if(!uniqueVisitors.containsKey(triple)) {
							uniqueVisitors.put(triple, 1);
							costSoFar.put(triple, 0);
							HashSet<Integer> temp = new HashSet<Integer>();
							temp.add(iterationCounter);
							connectionSetHelper.put(triple, temp);
						}else {
							uniqueVisitors.put(triple, uniqueVisitors.get(triple) + 1);
							connectionSetHelper.get(triple).add(iterationCounter);
						}
						pq.add(triple);
						hs.add(triple);
						addedCounter++;
					}
					
				}catch(Exception e) {
					e.printStackTrace();
				}
			}//end inner for
			if(addedCounter == 0) {
				System.err.println("!!!ERROR, literal(and like literals) of: " + subArray[0] + " not found in dataset!!!, RECCOMEND MISSION ABORT\n");
				//break; Should we hard break out of program here?
			}else {
				queues.add(pq);
				expandedIn.add(hs);
				iterationCounter++;
			}
		}//end nested fors, which encompasses my initialization stages
		System.out.println("Checking queues");
		for(PriorityQueue<Triple> pq : queues) {
			System.out.println(pq.toString());
		}
		System.out.println("Connection set helper / connection set validation");
		System.out.println(connectionSetHelper.toString());
		System.out.println(connectionSet.toString());
		System.out.println("-----End---init-----");
		boolean connected = connectionCheck();
		if(connected) {
			System.out.println("Graph is connected(before while)");
			for(Map.Entry<Triple, Integer> entry : uniqueVisitors.entrySet()) {
				System.out.println(entry.getKey().toString());
			}
			return;
		}
		System.out.println("Queues.size() is " + queues.size());
		int expansionNullCounter = 0;
		int testCounter = 0;
		while(!connected && testCounter < 500) { //begin expansion while ----------------------------
			if(positionInQueues == queues.size()) positionInQueues = 0;
			PriorityQueue<Triple> pq = queues.get(positionInQueues);
			Triple toExpand = pq.poll();
			if(toExpand == null) {
				expansionNullCounter++;
				if(expansionNullCounter == queues.size()) {
					System.out.println("Nowhere to expand anywhere!");
					break;
				}else {
					System.out.println("This PQ is empty, trying another");
					positionInQueues++;
					continue;
				}
			}
			exploredOnly.add(toExpand); //TODO MARK
			addToDOV(toExpand);
			expansionNullCounter = 0;
			String toFixPrefixSubj = toExpand.subject;
			String toFixPrefixObj = toExpand.object;
			char quoteChar = '"';
			if(toFixPrefixSubj.contains("http") && toFixPrefixSubj.charAt(0) != quoteChar) { //considers data in the form of: "O Canada"^^<http://www.w3.org/1999/02/22-rdf-syntax-ns#langString>  as well
				toFixPrefixSubj = "<" + toFixPrefixSubj + ">";
			}
			if(toFixPrefixObj.contains("http") && toFixPrefixObj.charAt(0) != quoteChar) {
				toFixPrefixObj = "<" + toFixPrefixObj + ">";
			}
			System.out.println("BAH " + toFixPrefixSubj + "||" + toFixPrefixObj);
			
			
			String queryStringSubjAsObjSearch =
					"SELECT ?s ?p WHERE { " +
					"	 ?s ?p " + toFixPrefixSubj + " . " +
					"}";
			String queryStringSubjAsSubjSearch = 
					"SELECT ?p ?o WHERE { " +
					toFixPrefixSubj + " ?p ?o . " +
					"}";
			String queryStringObjAsSubjSearch =
					"SELECT ?p ?o WHERE { " +
					toFixPrefixObj + " ?p ?o . " +
					"}";
			String queryStringObjAsObjSearch =
					"SELECT ?s ?p WHERE { " +
					"	 ?s ?p " + toFixPrefixObj + " . " +
					"}";
			
			String dummyQuery = //prefixes + 
					"SELECT ?s ?p ?o WHERE { ?s ?p ?o . }";
			
			Query SubjAsObjSearch = QueryFactory.create(dummyQuery);
			Query SubjAsSubjSearch = QueryFactory.create(dummyQuery);
			Query ObjAsSubjSearch = QueryFactory.create(dummyQuery);
			Query ObjAsObjSearch = QueryFactory.create(dummyQuery);
			QueryExecution SubjAsObjSearchExec = QueryExecutionFactory.create(SubjAsObjSearch); //Placeholder searches...that's their only purpose
			QueryExecution SubjAsSubjSearchExec = QueryExecutionFactory.create(SubjAsObjSearch);
			QueryExecution ObjAsSubjSearchExec = QueryExecutionFactory.create(SubjAsObjSearch);
			QueryExecution ObjAsObjSearchExec = QueryExecutionFactory.create(SubjAsObjSearch);
			try {
				SubjAsObjSearch = QueryFactory.create(queryStringSubjAsObjSearch);
				SubjAsObjSearchExec = QueryExecutionFactory.sparqlService("http://dbpedia.org/sparql", SubjAsObjSearch);
			}catch(Exception e) {
				System.out.println("Unaccounter format in " + "SAO" + e.getMessage());
				e.printStackTrace();
				unencounteredFormatError++;
			}
			try {
				SubjAsSubjSearch = QueryFactory.create(queryStringSubjAsSubjSearch);
				SubjAsSubjSearchExec = QueryExecutionFactory.sparqlService("http://dbpedia.org/sparql", SubjAsSubjSearch);
			}catch(Exception e) {
				System.out.println("Unaccounter format in " + "SAS" + e.getMessage());
				e.printStackTrace();
				unencounteredFormatError++;
			}
			try {
				ObjAsSubjSearch = QueryFactory.create(queryStringObjAsSubjSearch);
				ObjAsSubjSearchExec = QueryExecutionFactory.sparqlService("http://dbpedia.org/sparql", ObjAsSubjSearch);
			}catch(Exception e) {
				System.out.println("Unaccounter format in " + "OAS" + e.getMessage());
				e.printStackTrace();
				unencounteredFormatError++;
			}
			try {
				ObjAsObjSearch = QueryFactory.create(queryStringObjAsObjSearch);
				ObjAsObjSearchExec = QueryExecutionFactory.sparqlService("http://dbpedia.org/sparql", ObjAsObjSearch);
			}catch(Exception e) {
				System.out.println("Unaccounter format in " + "OAO" + e.getMessage()); 
				e.printStackTrace();
				unencounteredFormatError++;
			}

			try {
				ResultSet SubjAsObjResults = SubjAsObjSearchExec.execSelect();
				while(SubjAsObjResults.hasNext()) {
					QuerySolution solution = SubjAsObjResults.nextSolution();
		            Triple triple = new Triple(solution.get("?s").toString(), solution.get("?p").toString(), toExpand.subject);
		            connected = expander(pq, triple, positionInQueues, toExpand);
		            if(connected) return;
				}
			}catch(Exception e) {
				//Comes from unecountered format errors
			}
				
			try {
				ResultSet SubjAsSubjResults = SubjAsSubjSearchExec.execSelect();
				while(SubjAsSubjResults.hasNext()) {
					QuerySolution solution = SubjAsSubjResults.nextSolution();
		            Triple triple = new Triple(toExpand.subject, solution.get("?p").toString(), solution.get("?o").toString());
		            connected = expander(pq, triple, positionInQueues, toExpand);
		            if(connected) return;
				}
			}catch(Exception e) {
				//Comes from unecountered format errors
			}
				
			try {
				ResultSet ObjAsSubjResults = ObjAsSubjSearchExec.execSelect();
				while(ObjAsSubjResults.hasNext()) {
					QuerySolution solution = ObjAsSubjResults.nextSolution();
					Triple triple = new Triple(toExpand.object, solution.get("?p").toString(), solution.get("?o").toString());
					connected = expander(pq, triple, positionInQueues, toExpand);
					if(connected) return;
				}
			}catch(Exception e) {
				//Comes from unecountered format errors
			}
				
			try {
				ResultSet ObjAsObjResults = ObjAsObjSearchExec.execSelect();
				while(ObjAsObjResults.hasNext()) {
					QuerySolution solution = ObjAsObjResults.nextSolution();
					Triple triple = new Triple(solution.get("?s").toString(), solution.get("?p").toString(), toExpand.object);
					connected = expander(pq, triple, positionInQueues, toExpand);
					//if(connected) return; no need here
				}
			}catch(Exception e) {
				//Comes from unecountered format errors
			}
			
			testCounter++;
			positionInQueues++;
		}//end while
	}
	
	public void printResults() throws IOException {
		System.out.println("Graph is connected(inside while)");
		for(Map.Entry<Triple, Integer> entry : uniqueVisitors.entrySet()) {
			System.out.println(entry.getKey().toString());
		}
		System.out.println(uniqueVisitors.size() +  " is # of triples in graph");
	}
	
	public void printExploredOnly () throws IOException {
		System.out.println("---Explored only---");
		Iterator<Triple> it = exploredOnly.iterator();
		while(it.hasNext()) {
			Triple triple = it.next();
			System.out.println(triple.toString());
		}
		System.out.println("Explored only size: " + exploredOnly.size());
		System.out.println("Unecountered format errors " + unencounteredFormatError);
		System.out.println("Added in IHS: " + addedInIHS);
	}
	
	public boolean expander(PriorityQueue<Triple> pq, Triple triple, int positionInQueues, Triple parent) {
		//System.out.println("Attempting to add..." + triple.toString());
		if(!expandedIn.get(positionInQueues).contains(triple)) {
			//System.out.println("^ successfully added");
			expandedIn.get(positionInQueues).add(triple);
        	if(!uniqueVisitors.containsKey(triple)) {
        		uniqueVisitors.put(triple, 1);
        		HashSet<Integer> temp = new HashSet<Integer>();
				temp.add(positionInQueues);
				connectionSetHelper.put(triple, temp);
        	}else {
        		uniqueVisitors.put(triple, uniqueVisitors.get(triple) + 1);
        		connectionSetHelper.get(triple).add(positionInQueues);
        	}
        	int additionCost = costToAdd(triple);
        	if(!costSoFar.containsKey(triple) || costSoFar.get(triple) > costSoFar.get(parent) + additionCost) { //Change this and below plus values for predicate weighting after
        		costSoFar.put(triple, costSoFar.get(parent) + additionCost);
        	}
        	
        	pq.add(triple); //need to but in cost so far BEFORE its added to PQ for comparator logic in TriplePriority to work
        	iterateHashSet(triple);
        }
		return connectionCheck();
	}
	
	public void iterateHashSet(Triple triple) {

		HashSet<Integer> tmp = connectionSetHelper.get(triple);
		if(tmp.size() < 2) return; //speed optimization
		for(int i = connectionSet.size() - 1; i > -1; i--) { //connecting the sets
			HashSet<Integer> hs = connectionSet.get(i);
			for(int y : tmp) {
				if(hs.contains(y)) {
					tmp.addAll(hs);
					connectionSet.remove(i);
					exploredOnly.add(triple); //TODO Mark
					addToDOV(triple);
					addedInIHS++;
					break;
				}
			}
		}
		connectionSet.add(tmp);
		if(connectionCheck()) {
			exploredOnly.add(triple);//TODO MARK not polled from PQ but still part of the spanning tree!
			addToDOV(triple);
			addedInIHS++;
		}
	}
	
	public void createMST() {
		int counter = 0;
		for(Triple triple : exploredOnly) {//setting up the data structures to build the MST and run kruskals
			if(triple.predicate.toLowerCase().contains("wikipagewikilink")) {
				triple.predicate = predicateReplacer(triple);
			}
			String subject = triple.subject;
			String object = triple.object;
			if(!makeSet.containsKey(subject)) {
				makeSet.put(subject, counter);
				counter++;
			}
			if(!makeSet.containsKey(object)) {
				makeSet.put(object, counter);
				counter++;
			}
			
			int cost = 20;
			if(costSoFar.get(triple) == 0) {
				cost = 0;
			}else {
				for(String predicate : predicatesToFavour) {
					if(triple.predicate.toLowerCase().contains(predicate.toLowerCase()) || predicate.toLowerCase().contains(triple.predicate.toLowerCase())) {
						cost = 5;
						break;
					}
				}
			}
			edgeCost.put(triple, cost);
			edgesInIncreasingWeight.add(triple);
		}
		
		while(!edgesInIncreasingWeight.isEmpty()) { //running the for loop part of kruskals MST algorithm
			Triple triple = edgesInIncreasingWeight.poll();
			String u = triple.subject;
			String v = triple.object;
			//Cannot ignore wikidata, CAN lead to missed connections. && is additional pruning for Steiner tree
			if(makeSet.get(u) != makeSet.get(v) && Math.min(degreeOfVertex.get(u), degreeOfVertex.get(v)) > 1) { 
				MST.add(triple);
				int min = Math.min(makeSet.get(u), makeSet.get(v));
				makeSet.put(u, min);
				makeSet.put(v, min);
			}
		}
		
	}
	
	public String predicateReplacer(Triple triple) { //to get rid of the annoying wikiPageWikiLink
		String toReturn = triple.predicate;
		
		String subject = triple.subject;
		String object = triple.object;
		if(subject.contains("http")) {
			subject = "<" + subject + ">";
		}
		if(object.contains("http")) {
			object = "<" + object + ">";
		}
		String queryString = //prefixes + 
				"SELECT ?p WHERE { " +
				"	 " + subject + " ?p " + object + " . " +
				"}";
		Query query = QueryFactory.create(queryString);
		QueryExecution qexec = QueryExecutionFactory.sparqlService("http://dbpedia.org/sparql", query);
		
		try {
			ResultSet results = qexec.execSelect();
			while(results.hasNext()) {
				QuerySolution solution = results.nextSolution();
				toReturn = solution.get("?p").toString();
				if(!toReturn.contains("wikiPageWikiLink")) break;
			}
		}catch(Exception e) {
			System.out.println("In predicate replacer " + e.getMessage());
			e.printStackTrace();
		}
		

		return toReturn;
	}
	
	public void printMST() {
		System.out.println("Literals to connect:");
		for( ArrayList<String> x : literalsToConnect) {
			System.out.print("{");
			for(String y : x) {
				System.out.print("|" +  y + "|");
			}
			System.out.print("}");
			System.out.println();
		}
		System.out.println("Number of triples explored: " + uniqueVisitors.size());
		System.out.println("---Begin Steiner Tree---");
		for(Triple triple : MST) {
			System.out.println(triple.toString());
		}
		System.out.println("Steiner tree size: " + MST.size());
		//System.out.println(degreeOfVertex.toString());
	}
	
	public void addToDOV(Triple triple) {
		if(!degreeOfVertex.containsKey(triple.subject) && isAStartingLiteral(triple.subject)) {
			degreeOfVertex.put(triple.subject, 10);
		}else if(!degreeOfVertex.containsKey(triple.subject)) {
			degreeOfVertex.put(triple.subject, 1);
		}else {
			int x = (degreeOfVertex.get(triple.subject)) + 1;
			degreeOfVertex.put(triple.subject, x);
		}
		
		if(!degreeOfVertex.containsKey(triple.object) && isAStartingLiteral(triple.object)) {
			degreeOfVertex.put(triple.object, 10);
		}else if(!degreeOfVertex.containsKey(triple.object)) {
			degreeOfVertex.put(triple.object, 1);
		}else {
			int x = (degreeOfVertex.get(triple.object)) + 1;
			degreeOfVertex.put(triple.object, x);
		}
	}
	
	public boolean isAStartingLiteral(String s) {
		for(ArrayList<String> subArr : literalsToConnect) {
			for(String x : subArr) {
				if(s.toLowerCase().equals(x.toLowerCase())) {
					return true;
				}
			}
		}
		return false;
	}
	
	
	public boolean connectionCheck() { //O(1) runtime now, but additional cost in interateHashSet is there...
		boolean connected = false;
		if(connectionSet.size() == 1) {
			return true;
		}
		
		return connected;
	}
	
	public int costToAdd(Triple triple) { //Helps determine priority of a triple to expand based on if its predicates match the one in the initial query
		int additionCost = 5;
		for(String predicate : predicatesToFavour) {
			//System.out.println("Predicate: " + predicate + "||" + triple.predicate);
			if(triple.predicate.toLowerCase().contains(predicate.toLowerCase()) || predicate.toLowerCase().contains(triple.predicate.toLowerCase())) {
				//System.out.println("Reduced cost for " + triple.toString());
				additionCost = 1;
				break;
			}
		}
		return additionCost;
	}

}
