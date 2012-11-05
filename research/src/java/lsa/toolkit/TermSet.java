package lsa.toolkit;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Manage sets of base and alternate terms.
 * Base maybe be stem, or a synonym.
 * A word with two meanings should acquire all 
 * synonyms for both meanings.
 * 
 * The first base term for a set of terms always wins.
 * Base set never has unreferenced terms.
 */

public class TermSet {
  Map<String,String> alternates = new HashMap<String,String>();
  Set<String> baseSet = new HashSet<String>();
  
  /**
   * Make sure term is registered.
   * If an alternate, it stays an alternate
   * 
   * return base term
   */
  String addTerm(String term) {
    if (alternates.containsKey(term))
      return alternates.get(term);
    if (!baseSet.contains(term))
      baseSet.add(term);
    return term;
  }
  
  /**
   * Point alternate at base
   * If base is already an alternate, it stays an alternate,
   * and the new alternate points to THAT one's base
   * 
   * return base term
   */
  String addTerm(String base, String alternate) {
    if (alternates.containsKey(base)) {
      String other = alternates.get(base);
      alternates.put(alternate, other);
      return other;
    } else {
      baseSet.add(base);
      alternates.put(alternate, base);
      return base;
    }
  }
  
  /**
   * Find possible mapping for this term
   */
  String getBase(String term) {
    if (alternates.containsKey(term))
      return alternates.get(term);
    else 
      return term;
  }
}
