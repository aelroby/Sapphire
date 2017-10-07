package sapphire.test;

import java.util.ArrayList;
import java.util.Iterator;

import sapphire.dataStructures.SPARQLQuery;

public class SPARQLQueryTest {

	public static void main(String[] args) {
		String[] queryArray = {"?s", "?s", "?p", "\"Barack Obama\"@en", ""};
		String[] filtersArray = {"-1", "-1", "-1", "-1"};
		SPARQLQuery query = new SPARQLQuery(queryArray, filtersArray);
		System.out.println(query.queryString);
		SPARQLQuery newQuery = query.copyObject();
		ArrayList<ArrayList<String>> where = newQuery.getWhere();
		Iterator<ArrayList<String>> iterator = where.iterator();
		iterator.next().set(2, "\"Ahmed El-Roby\"@en");
		where.get(0).set(2, "\"Ahmed El-Roby\"@en");
		newQuery.setWhere(where);
		System.out.println(query.queryString);
		newQuery.updateQueryString();
		System.out.println(newQuery.queryString);
		
		
	}

}
