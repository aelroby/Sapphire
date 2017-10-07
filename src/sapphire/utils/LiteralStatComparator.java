package sapphire.utils;

import java.util.Comparator;

import sapphire.dataStructures.LiteralStat;

public class LiteralStatComparator implements Comparator<LiteralStat>{

	@Override
	public int compare(LiteralStat x, LiteralStat y) {
		if (x == null)
            return y == null ? 0 : -1;
        else if (y == null)
            return +1;
        else{
        	int lenx = x.getFrequency();
            int leny = y.getFrequency();
            if (lenx == leny){
            	return x.getLiteral().length() < y.getLiteral().length() ? -1 : +1;
            }
            else
                return lenx < leny ? -1 : +1;
        }
	}

}
