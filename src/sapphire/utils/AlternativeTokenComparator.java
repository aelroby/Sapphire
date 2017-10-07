package sapphire.utils;

import java.util.Comparator;

import sapphire.dataStructures.AlternativeToken;

public class AlternativeTokenComparator implements Comparator<AlternativeToken>{

	@Override
	public int compare(AlternativeToken x, AlternativeToken y) {
		if (x == null)
            return y == null ? 0 : -1;
        else if (y == null)
            return +1;
        else {
        	int numX = x.getNumOfRows();
        	int numY = y.getNumOfRows();
        	if(numX == numY)
        		return 0;
        	else
        		return numY < numX ? -1 : +1;
        }
	}

}
