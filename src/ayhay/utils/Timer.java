package ayhay.utils;


public class Timer {

	private static long start, time;
	
	public static void start() {
		start = System.nanoTime();
	}
	
	public static void stop() {
		time = System.nanoTime() - start;
	}

	public static double getTime() {
		return 1.0 * time / 1000000000;
	}

}
