package ayhay.utils;

import java.util.Comparator;

import ayhay.dataStructures.StringScore;

public class StringScoreComparator implements Comparator<StringScore>{

	@Override
	public int compare(StringScore x, StringScore y) {
		if (x == null)
            return y == null ? 0 : -1;
        else if (y == null)
            return +1;
        else {
        	double xScore = x.getScore();
        	double yScore = y.getScore();
        	if (xScore == yScore)
                return y.getS().compareTo(x.getS()); //break ties?
            else
                return yScore < xScore ? -1 : +1;
        }
	}

}
