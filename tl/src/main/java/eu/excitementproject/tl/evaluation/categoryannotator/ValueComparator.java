package eu.excitementproject.tl.evaluation.categoryannotator;

import java.util.Comparator;
import java.util.Map;

/**
 * Value comparator for sorting a Map<String, Float> based on the float value (descending order).  
 * 
 * @author Kathrin Eichler
 *
 */
public class ValueComparator implements Comparator<String> {

    Map<String, Float> base;
    public ValueComparator(Map<String, Float> base) {
        this.base = base;
    }
    
    public int compare(String a, String b) {
        if (base.get(a) > base.get(b) && (Math.abs(base.get(a)-base.get(b))>0.01)) {
        	//check if difference between the values is large enough (otherwise, results differ in each run, due to rounding differences!)
            return -1;
        }
        if (base.get(a) < base.get(b) && (Math.abs(base.get(a)-base.get(b))>0.01)) {
        	return 1;
        }
        //if both have (nearly) the same value, decide based on key:
		if (Integer.parseInt(a) > Integer.parseInt(b)) {
			return -1;
		}
		if (Integer.parseInt(a) < Integer.parseInt(b)) {
	        return 1;
		}
		return 0;
    }
}