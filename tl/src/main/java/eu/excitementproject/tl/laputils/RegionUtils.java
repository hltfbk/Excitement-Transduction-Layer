package eu.excitementproject.tl.laputils;

import java.util.Comparator;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import eu.excitementproject.tl.laputils.CASUtils.Region;

/**
 * A collection of static methods for manipulating Region-s
 * 
 * @author Vivi Nastase
 *
 */
public class RegionUtils {

	/**
	 * Compresses (previously generated) spans by aggregating adjacent ones
	 * We know it contains more than one Region
	 * 
	 * @param spans
	 * @return a compressed version of the given spans
	 */
	public static Set<Region> compressRegions(Set<Region> spanset) {
		
		SortedSet<Region> compressedSpans = new TreeSet<Region>(new Comparator<Region>() {
			public int compare(Region a, Region b) {
				if (a.getBegin() < b.getBegin()) { return -1; }
				if (a.getBegin() > b.getBegin()) { return 1; }
				return 0;
			}
		});
		
		compressedSpans.addAll(spanset);
		Region[] spans = compressedSpans.toArray(new Region[compressedSpans.size()]);
		
		compressedSpans.removeAll(spanset);
		
		if (spans != null && spans.length > 0) {  
		
			int begin = spans[0].getBegin();	
			int end = spans[0].getEnd();
		
			for(int i=1; i < spans.length; i++) {
						
				if ( (0 <= (spans[i].getBegin() - end)) && ((spans[i].getBegin() - end) <= 3)) {
					end = spans[i].getEnd();
				} else {
					compressedSpans.add(new Region(begin, end));
					begin = spans[i].getBegin();
					end = spans[i].getEnd();
				}			
			}
		
			compressedSpans.add(new Region(begin, end));
		}
		
		return compressedSpans;
	}
	
}
