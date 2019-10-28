package sapphire.test;

import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.tdb.TDBFactory;

import SteinerTreeExact.SteinerTree;
import sapphire.query.Triple;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class TestClass2 {

	//Test purpose:
	private static int secondQueryCount = 0;
	
	//Costs For Matching and non-Matching Predicates
	private static int matchingCost = 5;
	private static int nonMatchingCost = 20;
	
	//Filter Strings
	private static String[] filterStringsPredicates = {"wikiPageWikiLink", "wikiPageRedirects","wikiPageDisambiguates"};
	private static String[] filterStringSubjects = {"entity", "Category", "wikidata"};
	
	//Weight from Exact Method before reduction test
	private static int weightBeforeRT;
	
    //Avoid Duplicate Query
    private static LinkedHashSet<String> usedQuery = new LinkedHashSet<>();
    //Avoid expanding already expanded nodes
    private static LinkedHashSet<String> visitedNodes = new LinkedHashSet<>();
    //The expanded graph
    private static LinkedHashSet<Triple> expandedGraph = new LinkedHashSet<>();
    //all the connected terminals
    private static LinkedHashSet<String> connectedLiterals = new LinkedHashSet<>();
    //Triples that have same objects
    private static ArrayList<Triple> duplicatedItems = new ArrayList<>();
    //The starting literals
    private static ArrayList<LinkedHashSet<String>> literalsToConnect = new ArrayList<LinkedHashSet<String>>();
    //Matching predicates
    private static LinkedHashSet<String> predicatesToFavour = new LinkedHashSet<>();
    //all the connected triples, used to find an approximate tree
    private static ArrayList<ArrayList<Triple>> connectedTriples = new ArrayList<>();

    private static LinkedHashSet<Triple> MST = new LinkedHashSet<Triple>();

    //Following Attributes are used to build the cost model
    private static int numberOfQueriesProcessed = 0;
    private static long timeSpentOnExpansion = 0;
    
    
    private static long queryCreateTimeTotal = 0;
    private static long queryExpandMethodTotal = 0;
    private static long resultsHasNextTime = 0;
    
    private static int queryCount = 0;
    
    //Record the number of levels of expansion.
    private static int levelCount = 1;
    //Total weights of connected edges.
    private static ArrayList<HashMap<String, Integer>> result = new ArrayList<>();

    public static HashMap<String, Object> FindAnswer(ArrayList<String> allPredicates, ArrayList<LinkedHashSet<String>> allLiterals) {

        System.out.println("FindAnswer method start running.");
        //String directory = "/home/daix8340/DBpedia_TDB" ;
        String directory = "C:\\Program Files (x86)\\Datasets\\DBpedia_TDB";
        //String directory = "../bin/Datasets/DBpedia_TDB";
        //String directory = "/users/lacture/projects/DBpedia_TDB";
        Dataset dataset = TDBFactory.createDataset(directory) ;
        dataset.begin(ReadWrite.READ);
        System.out.println("datasets reading finished.");

        //the outermost triples in the graph
        ArrayList<ArrayList<Triple>> queryTriples = new ArrayList<>();

        literalsToConnect.addAll(allLiterals);
        for(LinkedHashSet<String> lhs: allLiterals){
            visitedNodes.addAll(lhs);
        }

        //Add predicates to predicatesToFavour
        for(String s: allPredicates){
            predicatesToFavour.add(s);
        }

        //Adding the starting literals to queryTriples and make the fromseed attribute of every Triple to be the first literal string.
        ArrayList<Triple> first = new ArrayList<>();
        for(LinkedHashSet<String> lhs: literalsToConnect){
            String SeedNameCurrent = "";
            Boolean f = true;
            for(String s: lhs){
                if(f){
                    SeedNameCurrent = s;
                    f = false;
                }
                Triple t = new Triple("", "", s, SeedNameCurrent);
                first.add(t);
            }
        }
        queryTriples.add(first);

        int nodeCount = 0;
        int numberOfQueriesToExpand = 0;

        int approximateWeight = 0;
        long startTime = System.currentTimeMillis();

        //Control the loop
        boolean connected = false;

        //nodeCount = 6000, just for test TODO: replace nodeCount by a cost model
        while(!connected && nodeCount < 6000){
            numberOfQueriesToExpand = 0;
            
            long queryExpandMethodTotal1 = System.currentTimeMillis();
            
            //countNeighbors(queryTriples.get(0),dataset);
            //System.out.println("*************************************************");
            queryTriples.set(0, expand(queryTriples.get(0), dataset));
            //System.out.println("*************************************************");
            long queryExpandMethodTotal2 = System.currentTimeMillis();
            queryExpandMethodTotal += (queryExpandMethodTotal2 - queryExpandMethodTotal1);
            
            nodeCount += queryTriples.get(0).size();
            numberOfQueriesToExpand = queryTriples.get(0).size();

            levelCount += 1;
            
            
            //Check if all literals(seeds) are connected
            connected = checkConnection(duplicatedItems, literalsToConnect, connectedTriples);

            //TODO: remove 6000 by a cost model
            if(nodeCount >= 6000){
                System.out.println("Expansion Stop since node count > 6000, node count = " + nodeCount);
            }
            

        }

        //Print Connected Triples
        for(ArrayList<Triple> tpal: connectedTriples){
            System.out.println("--- Connected Triples: ---");
            for(Triple tp: tpal){
                System.out.println(tp);
            }
        }

        //approximateWeight = getApproximateTree2(connectedTriples, literalsToConnect);

        //Reduction Test: degree one
        System.out.println("------------Remove nodes with degree 1---------------");
        LinkedHashSet<Triple> mstGraph = new LinkedHashSet<>();
        
        mstGraph.addAll(expandedGraph);
        int mstGraphLength;
        do {
            mstGraphLength = mstGraph.size();
            mstGraph = reductionTestDegreeOne(mstGraph);
        } while (mstGraphLength != mstGraph.size());
        System.out.println("Results graph size: " + mstGraph.size());
        
        //Get the weight(weightBeforeRT) to do reduction test
        getTreeExact(mstGraph, connectedLiterals);
        
        //Keep expanding and do a reduction test
        boolean stopExpansion = false;
        
        while(!stopExpansion){

        	//Calculate (the shortest distance) + (2nd Shortest distance) + 2*(matching weight) >=? approximate weight
        	//if true, then stop expansion.
            String[] terminalsNames = new String[2];
            int smallestWeight = 999;
            int secondSmallestWeight = 999;

            //a loop to go through the outermost triples. Find the shortest and 2nd shortest distance
            for(Triple queryObject : queryTriples.get(0)) {
                HashMap<String, Integer> hm = queryObject.getClosestAndSecondClosestTerminals2();
                for(Map.Entry<String, Integer> me: hm.entrySet()){

                    if(me.getValue() < smallestWeight){
                        if(me.getKey().equals(terminalsNames[0])){
                            smallestWeight = me.getValue();
                        }else{
                            terminalsNames[1] = terminalsNames[0];
                            secondSmallestWeight = smallestWeight;

                            smallestWeight = me.getValue();
                            terminalsNames[0] = me.getKey();
                        }
                    }else{
                        if(me.getValue() < secondSmallestWeight && !me.getKey().equals(terminalsNames[0])){
                            secondSmallestWeight = me.getValue();
                            terminalsNames[1] = me.getKey();
                        }
                    }
                }
            }

            System.out.println("-----------------------------------");
            System.out.println("Reduction Test: Possible shortest and second shortest distance to two terminals");
            System.out.println(terminalsNames[0] + " " + smallestWeight + " and " + terminalsNames[1] + " " + secondSmallestWeight);
            System.out.println("-----------------------------------");

            //TODO: if literalsToConnect size < 2? need an assertion later.
            if(smallestWeight + secondSmallestWeight + 2 * matchingCost + 20 * (literalsToConnect.size() - 2) >= weightBeforeRT){
                System.out.println("--- No extra Expansion needed! The graph contains the MST---");
                stopExpansion = true;
            }else{
                System.out.println("--- Another Level of expansion needed! ----");
                stopExpansion = true;
            }
            System.out.println("left part = " + (smallestWeight + secondSmallestWeight + 2* matchingCost + 20 * (literalsToConnect.size() - 2)));
            System.out.println("Right Part = " + weightBeforeRT);
            System.out.println("-----------------------------------");
            System.out.println("number of queries to expand: " + numberOfQueriesToExpand);
        }

        //Next line of code forces another level of expansion
        //queryTriples.set(0, expand(queryTriples.get(0), dataset));

        long endTime = System.currentTimeMillis();
        
        System.out.println("-----------------------------------");


        System.out.println("-----------------------------------");
        System.out.println("Exact Method Results: ");
        //TODO: if the previous reduction is passed, then we do not need to run the next exact method
        String path = getTreeExact(mstGraph, connectedLiterals);
        //String path = getTreeExact(expandedGraph, connectedLiterals);
        System.out.println(path);
        System.out.println("-----------------------------------");


        System.out.println((levelCount - 1) + " levels of expansion done! Nodes to expand: " + queryTriples.get(0).size());
        System.out.println("Execution time is: " + (endTime - startTime) + " ms");
        System.out.println("Expanded Graph Size: " + expandedGraph.size());
        
        System.out.println("Total amount of query Expand time: " + queryExpandMethodTotal + " ms");
        System.out.println("Total amount of resultsHasNext time: " + resultsHasNextTime + " ms");
        System.out.println("Time spent on each query: " + queryExpandMethodTotal/numberOfQueriesProcessed +" ms");

        
        for(Triple tp: MST){
            System.out.println(tp);
        }

        HashMap<String, Object> pathAndTriples = new HashMap<>();
        //String path = "XXXXXXXX";
        pathAndTriples.put("path", path);
        pathAndTriples.put("triples", connectedTriples);
        return pathAndTriples;
    }

    //check connection by loop through duplicatedItems
    public static Boolean checkConnection(ArrayList<Triple> di, ArrayList<LinkedHashSet<String>> literalsToConnect, ArrayList<ArrayList<Triple>> ct){

        HashMap<LinkedHashSet<String>, Boolean> literalsSets = new HashMap<>();
        Boolean allConnected = true;

        for(LinkedHashSet<String> lhs: literalsToConnect){
            LinkedHashSet<String> ltc = new LinkedHashSet<>(lhs);
            literalsSets.put(ltc, false);
        }

        for (int i = 0; i < di.size(); i++) {
            for (int j = i + 1; j < di.size(); j++) {
                if (di.get(i).object.equals(di.get(j).object) && !di.get(i).fromSeed.equals(di.get(j).fromSeed)) {

                    String fromSeed_i = di.get(i).fromSeed;
                    String fromSeed_j = di.get(j).fromSeed;
                    boolean theSameLiteralsArray = false;
                    for(Map.Entry<LinkedHashSet<String>, Boolean> entry: literalsSets.entrySet()){
                        if (entry.getKey().contains(fromSeed_i) && entry.getKey().contains(fromSeed_j)) {
                            theSameLiteralsArray = true;
                            break;
                        }
                        if(entry.getKey().contains(fromSeed_i) || entry.getKey().contains(fromSeed_j)){
                            literalsSets.replace(entry.getKey(), true);
                        }
                    }
                    if (theSameLiteralsArray) {
                        continue;
                    }
                    //adding connected literals to connectedLiterals
                    connectedLiterals.add(fromSeed_i);
                    connectedLiterals.add(fromSeed_j);
 
                    //add to connectedTriples
                    ArrayList<Triple> tmp = new ArrayList<>();
                    tmp.add(di.get(i));
                    tmp.add(di.get(j));
                    ct.add(tmp);
                }
            }
        }
        for(Map.Entry<LinkedHashSet<String>, Boolean> entry: literalsSets.entrySet()){
            if(!entry.getValue()){
                allConnected = false;
                break;
            }
        }

        return allConnected;
    }
    
    
    public static void countNeighbors(ArrayList<Triple> queryTriples, Dataset dataset) {
    	Model model = dataset.getDefaultModel();
    	String solutionNumber = "";
    	
    	 for(Triple queryObject : queryTriples) {
            
             //String queryString = makeQueryString(literalsToConnect, queryObject.object);
             String countQueryString = countQueryString(literalsToConnect, queryObject.object);
             
             //adding the queryString to usedQuery, if it's already in, then skip the query
             System.out.println("------Count current query: " + countQueryString + " -------");

             Query query2 = QueryFactory.create(countQueryString);
             
             QueryExecution qexec2 = QueryExecutionFactory.create(query2, model);
             
             try{                 
                 
                 ResultSet results2 = qexec2.execSelect(); 
                 
                 numberOfQueriesProcessed++;

                 long resultsHasNextTime1 = System.currentTimeMillis();
                 long currentQueryHasNextTotal = 0;
                 while(results2.hasNext()) {
                 	long resultsHasNextTime2 = System.currentTimeMillis();
                 	currentQueryHasNextTotal += (resultsHasNextTime2 - resultsHasNextTime1);
                 	
                     QuerySolution solution = results2.nextSolution();
                     solutionNumber = solution.get("?c").toString();
                     
                 }
                 resultsHasNextTime += currentQueryHasNextTotal;
                 System.out.println("       --Time Spent On This Query: " + currentQueryHasNextTotal + " ms -- Results Size : " + solutionNumber);
             }catch(Exception e){
                 e.printStackTrace();
             }

         }
    	 
    }
    public static ArrayList<Triple> expand(ArrayList<Triple> queryTriples, Dataset dataset) {
        Model model = dataset.getDefaultModel();
        LinkedHashSet<String> tmp = new LinkedHashSet<>();
        ArrayList<Triple> matchedTriples = new ArrayList<>();
        ArrayList<Triple> otherTriples = new ArrayList<>();
        
        
        for(Triple queryObject : queryTriples) {
            
            String queryString = makeQueryString(literalsToConnect, queryObject.object);      
            
            //adding the queryString to usedQuery, if it's already in, then skip the query
            System.out.println("------current query: " + queryString + " -------");
            if(usedQuery.contains(queryString)){
                continue;
            }else{
                usedQuery.add(queryString);
            }
            Query query = QueryFactory.create(queryString);

            QueryExecution qexec = QueryExecutionFactory.create(query, model);
            
            try{                
                ResultSet results = qexec.execSelect();   
                
                numberOfQueriesProcessed++;

                long resultsHasNextTime1 = System.currentTimeMillis();
                long currentQueryHasNextTotal = 0;
                int resultsSize = 0;
                while(results.hasNext()) {
                	long resultsHasNextTime2 = System.currentTimeMillis();
                	currentQueryHasNextTotal += (resultsHasNextTime2 - resultsHasNextTime1);
                	resultsSize++;
                	
                    QuerySolution solution = results.nextSolution();
                    String solutionSubject = "";
                    String solutionPredicate = "";
                    String solutionObject = "";
                    
                    //process incoming edges of the entity
                    if(solution.get("?s") != null){
                    	solutionSubject = solution.get("?s").toString();
                    	solutionPredicate = solution.get("?p").toString();

                        int cost = nonMatchingCost;
                        for(String predicate : predicatesToFavour) {
                            if(solutionPredicate.toLowerCase().contains(predicate.toLowerCase()) || predicate.toLowerCase().contains(solutionPredicate.toLowerCase())) {
                                cost = matchingCost;
                                //System.out.println(solutionPredicate);
                                break;
                            }
                        }
                        
                        Triple newTriple = new Triple(queryObject, solutionPredicate, solutionSubject, cost);
                                              
                        if(!expandedGraph.contains(newTriple)){
                            expandedGraph.add(newTriple);
                        }
                        
                        //adding connected nodes in to duplicatedItems
                        //During the expansion, only the outermost triples can be connected, thus we check tmp for duplicated triples.
                        if(tmp.contains(solutionSubject)){
                        	//find triples that have same object from expandedGraph and add to duplicatedItems.
                            addDuplicatedItems(duplicatedItems, expandedGraph, newTriple);
                        }else if (!visitedNodes.contains(solutionSubject)){
                            tmp.add(solutionSubject);
                            visitedNodes.add(solutionSubject);
                            if (newTriple.cost == matchingCost) {
                                matchedTriples.add(newTriple);
                            } else {
                                otherTriples.add(newTriple);
                            }
                        }	                                      
                    }else{
                    	secondQueryCount++;
                    	//process outgoing edges of the entity
                    	solutionPredicate = solution.get("?j").toString();
                    	solutionObject = solution.get("?k").toString();
                    	//System.out.println(solutionPredicate + "   " + solutionObject);
                    	int cost = nonMatchingCost;
                        for(String predicate : predicatesToFavour) {
                            if(solutionPredicate.toLowerCase().contains(predicate.toLowerCase()) || predicate.toLowerCase().contains(solutionPredicate.toLowerCase())) {
                                cost = matchingCost;
                                break;
                            }
                        }
                        
                        Triple newTriple = new Triple(solutionObject, solutionPredicate, queryObject, cost);
                                              
                        if(!expandedGraph.contains(newTriple)){
                            expandedGraph.add(newTriple);
                        }
                        
                        //adding connected nodes in to duplicatedItems
                        //During the expansion, only the outermost triples can be connected, thus we check tmp for duplicated triples.
                        if(tmp.contains(solutionSubject)){
                        	//find triples that have same object from expandedGraph and add to duplicatedItems.
                            addDuplicatedItems(duplicatedItems, expandedGraph, newTriple);
                        }else if (!visitedNodes.contains(solutionSubject)){
                            tmp.add(solutionSubject);
                            visitedNodes.add(solutionSubject);
                            if (newTriple.cost == matchingCost) {
                                matchedTriples.add(newTriple);
                            } else {
                                otherTriples.add(newTriple);
                            }
                        }
                    }     
                    resultsHasNextTime1 = System.currentTimeMillis();
                }
                System.out.println("Outgoing nodes Count: " + secondQueryCount);
                resultsHasNextTime += currentQueryHasNextTotal;
               System.out.println("       --Time Spent On This Query: " + currentQueryHasNextTotal + " ms -- Results Size : " + resultsSize);
            }catch(Exception e){
                e.printStackTrace();
            }

        }

        //add matchedTriples first, then otherTriples
        ArrayList<Triple> newTripleToExpand = new ArrayList<Triple>();
        newTripleToExpand.addAll(matchedTriples);
        newTripleToExpand.addAll(otherTriples);

        return newTripleToExpand;
    }


    public static void printSmallestAndSecondSmallestTerminals(Triple tp){
        HashMap<String, Integer> terminals = tp.getClosestAndSecondClosestTerminals();
        for(Map.Entry<String, Integer> me: terminals.entrySet()){
            System.out.println("To terminal: " + me.getKey() + " Distance: " + me.getValue());
        }
    }

    //Using the exact method to find the MST
    public static String getTreeExact(LinkedHashSet<Triple> graph, LinkedHashSet<String> connectedLiterals) {

        HashMap<String, Integer> vertexToIndex = new HashMap<>();
        ArrayList<ArrayList<Integer>> edges = new ArrayList<>();

        for(Triple triple : graph) {
            ArrayList<Integer> tmp = new ArrayList<>();

            if(!vertexToIndex.containsKey(triple.subject)){
                tmp.add(vertexToIndex.size()+1);
                vertexToIndex.put(triple.subject, vertexToIndex.size()+1);
            }else{
                tmp.add(vertexToIndex.get(triple.subject));
            }

            if(!vertexToIndex.containsKey(triple.object)){
                tmp.add(vertexToIndex.size()+1);
                vertexToIndex.put(triple.object, vertexToIndex.size()+1);
            }else{
                tmp.add(vertexToIndex.get(triple.object));
            }

            tmp.add(triple.cost);
            //Just for test (1000)
            boolean f = true;
            for(Integer i: tmp){
                if(i > 1000){
                    f = false;
                }
            }
            if(f){edges.add(tmp);}

        }


        int nn = vertexToIndex.size();
        int ne = edges.size();
        int nt = connectedLiterals.size();
        ArrayList<Integer> terminals = new ArrayList<>();

        System.out.println("Terminals are: ");
        for (String item : connectedLiterals) {
            terminals.add(vertexToIndex.get(item));
            System.out.println(item);
        }
        System.out.println("------------------");

        /*
        System.out.println("------ Details: -------");
        for(ArrayList<Integer> ali: edges){
        	for(Integer i: ali){
        		System.out.print(i +" --- ");
        	}
        	System.out.println();
        }
        */

        //Test purpose
        if(nn > 1000){
            nn = 1000;
        }

        SteinerTree tree = new SteinerTree(nn, ne, nt, edges, terminals);
        ArrayList<ArrayList<Integer>> results = tree.getMinPaths();
        weightBeforeRT = tree.getMinWeight();
        
        System.out.println("Paths: ");
        StringBuilder path = new StringBuilder();
        for(ArrayList<Integer> ali: results){
            for(Integer i: ali){
                for(Map.Entry<String, Integer> me: vertexToIndex.entrySet()){
                    if(me.getValue() == i){
                        path.append(i).append(" (").append(me.getKey()).append(") ---");
                    }
                }

            }
            path.append("\r\n");

        }
        return path.toString();
    }

    public static LinkedHashSet<Triple> reductionTestDegreeOne(LinkedHashSet<Triple> eg){
        LinkedHashSet<Triple> resultGraph = new LinkedHashSet<Triple>();
        LinkedHashSet<Triple> degreeOneNodes = new LinkedHashSet<Triple>();
        resultGraph.addAll(eg);

        boolean duplicatedObject;
        boolean isLeafNode;

        for(Triple tp1: eg){
            duplicatedObject = false;
            isLeafNode = true;
            for(Triple tp2: eg){
                if(tp2.previousTriple == tp1){
                	isLeafNode = false;
                    continue;
                }
                if(tp1.object.equals(tp2.object) && !tp1.subject.equals(tp2.subject)){
                    duplicatedObject = true;
                    break;
                }
            }

            if(!duplicatedObject && isLeafNode){
                degreeOneNodes.add(tp1);
            }
        }

        System.out.println("D1N size: " + degreeOneNodes.size());

        for(Triple tp: degreeOneNodes){
            resultGraph.remove(tp);
        }

        return resultGraph;
    }

    //Approximate Weight method.
    public static int getApproximateTree2(ArrayList<ArrayList<Triple>> ct, ArrayList<LinkedHashSet<String>> ltc){
        HashMap<String, Integer> tmp = new HashMap<>();
        getNextConnectedTriple(0, 0, tmp, ct);

        HashMap<String, Integer> ltcIndex = new HashMap<>();
        for (int i = 0; i < ltc.size(); i++) {
            for (String s : ltc.get(i)) {
                ltcIndex.put(s, i);
            }
        }
        int approximateWeights = Integer.MAX_VALUE;
        for (HashMap<String, Integer> map : result) {
            TreeSet<Integer> index = new TreeSet<>();
            for (Map.Entry<String, Integer> entry : map.entrySet()) {
                index.add(ltcIndex.get(entry.getKey()));
            }
            if (index.size() != ltc.size()) {
                continue;
            }
            for (Map.Entry<String, Integer> entry : map.entrySet()) {
                System.out.print(entry.getKey() + "  ");
            }
            int cost = map.values().stream().mapToInt(Integer::intValue).sum();
            System.out.println(";  weight: " + cost);
            if (approximateWeights >  cost) {
                approximateWeights = cost;
            }
        }
        return approximateWeights - 20 * (ltc.size() - 2);
    }

    public static void getNextConnectedTriple(int index, int count, HashMap<String, Integer> map, ArrayList<ArrayList<Triple>> ct) {
        if (map.size() == literalsToConnect.size() && count == literalsToConnect.size() - 1) {
            result.add(map);
            return;
        }
        if (index >= ct.size()) {
            return;
        }

        int flag = 0;
        while (flag < 2) {
            HashMap<String, Integer> tmp = new HashMap<>(map);
            if (flag == 0) {
                ArrayList<Triple> list = ct.get(index);
                Set<String> key = tmp.keySet();
                Triple t = list.get(0);
                if (key.contains(t.fromSeed)) {
                    int cost = tmp.get(t.fromSeed) + t.getAccumulatedCost();
                    tmp.replace(t.fromSeed, cost);
                } else {
                    tmp.put(t.fromSeed, t.getAccumulatedCost());
                }
                t = list.get(1);
                if (key.contains(t.fromSeed)) {
                    int cost = tmp.get(t.fromSeed) + t.getAccumulatedCost();
                    tmp.replace(t.fromSeed, cost);
                } else {
                    tmp.put(t.fromSeed, t.getAccumulatedCost());
                }
                index++;
                getNextConnectedTriple(index, count + 1, tmp, ct);
            } else {
                getNextConnectedTriple(index, count, tmp, ct);
            }
            flag++;
        }
    }

    public static void addDuplicatedItems(ArrayList<Triple> dp, LinkedHashSet<Triple> eg, Triple tp){

    	//expandedGraph may contain several triples with same object
        LinkedHashSet<Triple> tripleWithSameObject = new LinkedHashSet<>();
        tripleWithSameObject.add(tp);

        for (Triple triple : eg) {
            if (triple.object.equals(tp.object)) {
                tripleWithSameObject.add(triple);
            }
        }

        //Update the distance to fromseed
        for(Triple triple1: tripleWithSameObject){
            for(Triple triple2: tripleWithSameObject){
                triple1.calculateSeedsAndWeights(triple2);
            }
        }


        //Adding triples into duplicatedItems
        for(Triple triple: tripleWithSameObject){
            if(!dp.contains(triple)){
                dp.add(triple);
            }
        }
    }

    public static String makeQueryString(ArrayList<LinkedHashSet<String>> literals, String obj){
        Boolean found = false;
        //TODO: Optimize?
        for(LinkedHashSet<String> lhs: literals){
            if(lhs.contains(obj)){
                found = true;
                break;
            }
        }

        if(found){
            String result = "SELECT distinct ?s ?p WHERE { " +
                    "	 {?s ?p " + obj + " . " +
                    "} FILTER (";
            
            for(String s: filterStringsPredicates){
            	String fsp = "!regex(str(?p), '" + s + "' , 'i') && ";
            	result = result + fsp;
            }
            
            result = result.substring(0, result.lastIndexOf("&&"));
           
            return result + ")}";
        }else{
            String result = "SELECT distinct ?s ?p ?j ?k WHERE { ";
            
            String incomingQuery = "{?s ?p <" + obj + "> . FILTER (";
            
            String outgoingQuery =  "{<" + obj + "> ?j ?k. FILTER (";
            
            for(String s: filterStringsPredicates){
            	String fsp = "!regex(str(?p), '" + s + "' , 'i') && ";
            	incomingQuery += fsp;
            	String fsj = "!regex(str(?j), '" + s + "' , 'i') && ";
            	outgoingQuery += fsj;
            }
            
            for(String s: filterStringSubjects){
            	String fss = "!regex(str(?s), '" + s + "' , 'i') && ";
            	incomingQuery += fss;
            }
            
            outgoingQuery = outgoingQuery.substring(0, outgoingQuery.lastIndexOf("&&")) + ")} ";
            incomingQuery = incomingQuery.substring(0, incomingQuery.lastIndexOf("&&")) + ")} ";
           
            result = result + incomingQuery + " UNION " + outgoingQuery + "}";
            
            System.out.println(result);
            return result;
        }
    }
    
    public static String countQueryString(ArrayList<LinkedHashSet<String>> literals, String obj){
        Boolean found = false;
      //TODO: Optimize?
        for(LinkedHashSet<String> lhs: literals){
            if(lhs.contains(obj)){
                found = true;
                break;
            }
        }

        if(found){
            return "SELECT distinct (Count(*) as ?c) WHERE { " +
                    "	 ?s ?p " + obj + " . " +
                    "}";
        }else{
            return "SELECT distinct (Count(*) as ?c) WHERE { " +
                    "	 ?s ?p <" + obj + "> . " +
                    "}";
        }
    }


}
