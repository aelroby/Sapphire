package sapphire.utils;

import java.util.Comparator;

import sapphire.dataStructures.LiteralStat;

public class ExtendedLengthComparator implements Comparator<LiteralStat>{

	@Override
	public int compare(LiteralStat x, LiteralStat y) {
		if (x == null)
            return y == null ? 0 : -1;
        else if (y == null)
            return +1;
        else{
        	int lenx = x.getLiteral().length();
            int leny = y.getLiteral().length();
            if (lenx == leny){
            	return 0;
            }
            else
                return lenx < leny ? -1 : +1;
        }
	}

}
