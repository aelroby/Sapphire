package ayhay.utils;


public class Timer {

	private static long start, time;
	
	public static void start() {
		start = System.nanoTime();
	}
	
	public static void stop() {
		time = System.nanoTime() - start;
	}

	public static double getTimeInSeconds() {
		return 1.0 * time / 1000000000;
	}
	
	public static double getTimeInNanoSeconds() {
		return time;
	}
	
	public static double getTimeInMelliseconds() {
		return 1.0 * time / 1000000;
	}

}
