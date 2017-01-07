package ayhay.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class SimpleTimestamp {

	public static String getFormattedTimestamp () {
		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss.SSS");
		Date dt = new Date();
		String s = "[" + sdf.format(dt) + "] ";
		return s;
	}
	
}
