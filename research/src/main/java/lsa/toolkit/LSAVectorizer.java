package lsa.toolkit;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;


/**
 * One term at a time, build term vectors for documents or sentences
 * Store vector "row" per term, with columns of documents or sentences
 * Columns advance monotonically with startColumn/endColumn
 * Use advances column.
 */

public class LSAVectorizer {
  // per-term array of entries per sentence
  // use LinkedHashMap so that order stays constant with term addition
  // 
  Map<String, double[]> termvecs = new LinkedHashMap<String,double[]>();
  int sentenceIndex = 0;
  // expansion buffering - resize termvecs in this size jumps
  static final private int BUMP = 50;
  private int vecSize = BUMP;
  
  public LSAVectorizer() {
    init();
  }
  
  void init() {
    
  }
  
  public void startColumn() {
    // make sure room for next sentence
    if (sentenceIndex == BUMP) {
      vecSize += BUMP;
      for(Entry<String,double[]> term: termvecs.entrySet()) {
        double[] vec = Arrays.copyOf(term.getValue(), vecSize);
        term.setValue(vec);
      }
    }
  }
  
  public void endColumn() {
    sentenceIndex++;
  }
  
  public void vectorizeTerm(String term) {
    addTerm(term);
  }
  
  /**
   * Resize all vectors to remove expansion buffer
   */
  public void truncateVectors() {
    if (sentenceIndex % BUMP != 0) {
      for(Entry<String,double[]> term: termvecs.entrySet()) {
        double[] vec = Arrays.copyOf(term.getValue(), sentenceIndex);
        term.setValue(vec);
      }
    }
  }
  
  /**
   * return matrix
   * terms follow insertion order
   */
  public Matrix getMatrix() {
    double[][] rows = new double[termvecs.size()][];
    int i = 0;
    for(Entry<String,double[]> vec: termvecs.entrySet()) {
      rows[i] = vec.getValue();
      i++;  
    }
    Matrix lsaMatrix = new Matrix(rows);
    return lsaMatrix;
  }
  
  private void addTerm(String term) {
    double[] vec = termvecs.get(term);
    if (vec == null) {
      vec = new double[vecSize];
      termvecs.put(term, vec);
    }
    vec[sentenceIndex]++;
  } 
  
}
