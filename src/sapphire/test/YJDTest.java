package sapphire.test;

import java.util.ArrayList;
import java.util.LinkedHashSet;

import sapphire.test.TestClass;

public class YJDTest {
	public static void main(String[] args){
		ArrayList<String> p = new ArrayList<String>();
		
		p.add("?author");
		p.add("?publisher");
		
		ArrayList<LinkedHashSet<String>> allLiteralsToConnect = new ArrayList<LinkedHashSet<String>>();
		
		LinkedHashSet<String> literalsToConnectGroup1 = new LinkedHashSet<>();
        LinkedHashSet<String> literalsToConnectGroup2 = new LinkedHashSet<>();
        LinkedHashSet<String> literalsToConnectGroup3 = new LinkedHashSet<>();
        
        literalsToConnectGroup1.add("\"Michelle Obama\"@en");
        /*
        literalsToConnectGroup1.add("\"Viking probes\"@en");
        literalsToConnectGroup1.add("\"Viking XPRS\"@en");
        literalsToConnectGroup1.add("\"King Cypress\"@en");
        literalsToConnectGroup1.add("\"Firkin Press\"@en");
        literalsToConnectGroup1.add("\"Vine Press\"@en");
        */
        
        literalsToConnectGroup2.add("\"Barack Obama\"@en");
        /*
        literalsToConnectGroup2.add("\"Jack  Kerouac\"@en");
        literalsToConnectGroup2.add("\"Jack kerouac\"@en");
        literalsToConnectGroup2.add("\"Jack keroac\"@en");
        literalsToConnectGroup2.add("\"Jack Keroac\"@en");
        literalsToConnectGroup2.add("\"Jack Keruoac\"@en");
        */
        
        //literalsToConnectGroup3.add("\"Columbia University\"@en");
        
        allLiteralsToConnect.add(literalsToConnectGroup1);
        allLiteralsToConnect.add(literalsToConnectGroup2);
        //allLiteralsToConnect.add(literalsToConnectGroup3);
        
		TestClass2.FindAnswer(p, allLiteralsToConnect);
	}
}
