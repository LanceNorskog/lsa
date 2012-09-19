package org.apache.solr.summary.lsa;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * One term at a time, build term vectors for documents or sentences
 * Store vector "row" per term, with columns of documents or sentences
 * Columns advance monotonically with startColumn/endColumn
 * Use advances column.
 * 
 * Maintains termVector of doubles and termFreqs as maps.
 * Map.keyset is list of terms.
 * Can return 
 * 
 * TODO: add Random Indexing (random projection, one word at a time).
 * TODO: termVecs and globalFreqs can be lists since we save the term indexes
 */

public class TermVectorizer {
  // per-term array of entries per sentence
  LinkedHashMap<String,Integer> termIndex = new LinkedHashMap<String,Integer>();
  LinkedHashMap<String, double[]> termVecs = new LinkedHashMap<String,double[]>();
  LinkedHashMap<String, AtomicInteger> globalFreqs = new LinkedHashMap<String,AtomicInteger>();
  int columnIndex = -1;
  // expansion buffering - resize termvecs in this size jumps
  static final private int BUMP = 50;
  private int vecSize = BUMP;
  
  public TermVectorizer() {
    init();
  }
  
  void init() {
    
  }
  
  public int startColumn() {
    columnIndex++;
    // make sure room for next sentence
    if (columnIndex == BUMP) {
      vecSize += BUMP;
      for(Entry<String,double[]> term: termVecs.entrySet()) {
        double[] vec = Arrays.copyOf(term.getValue(), vecSize);
        term.setValue(vec);
      }
    }
    return columnIndex;
  }
  
  public int vectorizeTerm(String term, int dim) {
    int index = getOrAddIndex(term);
    postTerm(term, index, dim);
    return index;
  }
  
  private void postTerm(String term, int index, int dim) {
    double[] vec = termVecs.get(term);
    AtomicInteger freq = globalFreqs.get(term);
    if (vec == null) {
      vec = new double[vecSize];
      termVecs.put(term, vec);
      freq = new AtomicInteger(0);
      globalFreqs.put(term, freq);
    }
    vec[dim]++;
    freq.addAndGet(1);
  }
  
  public int getOrAddIndex(String term) {
    int index;
    if (! termIndex.containsKey(term)) {
      index = termIndex.size();
      termIndex.put(term, index);
    } else {
      index = termIndex.get(term);
    }
    return index;
  }
  
  /**
   * Resize all vectors to remove expansion buffer
   */
  public void truncateVectors() {
    if (columnIndex % BUMP != 0) {
      for(Entry<String,double[]> term: termVecs.entrySet()) {
        double[] vec = Arrays.copyOf(term.getValue(), columnIndex + 1);
        term.setValue(vec);
      }
    }
  }
  
  /**
   * Terms are rows
   * Documents are columns
   * Both follow insertion order
   */
  public Matrix getMatrix() {
    double[][] rows = new double[termVecs.size()][];
    int i = 0;
    for(Entry<String,double[]> vec: termVecs.entrySet()) {
      rows[i] = vec.getValue();
      i++;  
    }
    Matrix lsaMatrix = new Matrix(rows);
    return lsaMatrix;
  }
  
  
  public int getNumTerms() {
    return termVecs.size();
  }
  
  public int getNumDocuments() {
    return columnIndex;
  }
  
  // deep copy getters. don't know why these have to be deep copy
  public String[] getTerms() {
    String[] terms = new String[termVecs.size()];
    int count = 0;
    for(String term: termVecs.keySet()) {
      terms[count++] = term;
    }
    return terms;
  } 
  
  /**
   * Term Frequencies, one per document
   * 
   */
  public double[] getTermFreqs(String term) {
    double[] termVec = termVecs.get(term);
    return termVec;
  } 
  
  public double[] getGlobalFreqs() {
    double[] gf = new double[globalFreqs.size()];
    int count = 0;
    for(AtomicInteger freq: globalFreqs.values()) {
      gf[count++] = freq.get();
    }
    return gf;
  }
  
  /**
   * entry order of term or -1 if never added
   */
  public int getTermIndex(String term) {
    Integer index = termIndex.get(term);
    if (index == null)
      return -1;
    else 
      return index;
  }
  
  public int getTermCount() {
    return termVecs.size();
  }
  
}
