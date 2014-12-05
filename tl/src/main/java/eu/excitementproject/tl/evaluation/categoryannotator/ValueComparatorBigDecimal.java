package eu.excitementproject.tl.evaluation.categoryannotator;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.Map;

/**
 * Value comparator for sorting a Map<String, BigDecimal> based on the big decimal value (descending order).  
 * 
 * @author Kathrin Eichler
 *
 */
public class ValueComparatorBigDecimal implements Comparator<String> {

    Map<String, BigDecimal> base;
    public ValueComparatorBigDecimal(Map<String, BigDecimal> base) {
        this.base = base;
    }
    
    public int compare(String a, String b) {
    	if (base.get(a).equals(base.get(b))) {
            //if both have the same value, decide based on key:
    		return a.compareTo(b);
    	} 
    	if (base.get(a).min(base.get(b)) == base.get(a)) return 1;
    	else return -1;		
    }
}