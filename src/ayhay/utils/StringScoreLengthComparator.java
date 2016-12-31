package ayhay.utils;

import java.util.Comparator;

import ayhay.dataStructures.StringScore;

public class StringScoreLengthComparator implements Comparator<StringScore>{
	
	@Override
	public int compare(StringScore x, StringScore y) {
		if (x == null)
            return y == null ? 0 : -1;
        else if (y == null)
            return +1;
        else {
        	double xIndex = x.getIndex();
        	double yIndex = y.getIndex();
        	if (xIndex == yIndex)
                return y.getS().compareTo(x.getS()); //break ties?
            else
                return yIndex > xIndex ? -1 : +1;
        }
	}

}
