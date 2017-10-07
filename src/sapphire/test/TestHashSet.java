package sapphire.test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;


public class TestHashSet {
	
	public static Set<String> testSet;
	
	public class TestTask implements Runnable {
		public TestTask() {
			
		}
		@Override
		public void run() {
			testSet.add("Ahmed");
		}
		
	}

	public static void main(String[] args) {
		
		TestHashSet test = new TestHashSet();
		
		testSet = Collections.synchronizedSet(new HashSet<String>());
		
		testSet.add("AHMED");

		ArrayList<Thread> threads = new ArrayList<Thread>();
		threads.add(new Thread(test.new TestTask()));
		threads.add(new Thread(test.new TestTask()));
		
		threads.get(0).start();
		threads.get(1).start();
		
		try {
			threads.get(0).join();
			threads.get(1).join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		
		for(String s : testSet) {
			System.out.println(s);
		}
	}

}
