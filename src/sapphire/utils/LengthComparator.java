package sapphire.utils;

import java.util.Comparator;

public class LengthComparator implements Comparator<String>{

	@Override
	public int compare(String x, String y) {
		if (x == null)
            return y==null ? 0 : -1;
        else if (y == null)
            return +1;
        else {
            int lenx = x.length();
            int leny = y.length();
            if (lenx == leny)
                return x.compareTo(y); //break ties?
            else
                return lenx < leny ? -1 : +1;
        }
	}

}
