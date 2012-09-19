package org.apache.solr.summary.lsa;

import org.apache.lucene.util.LuceneTestCase;
import org.junit.Test;

public class TestTermVectorizer extends LuceneTestCase {
  static String[][] DOC_TERMS = {{"one", "two", "three"}, {"two", "four", "two"}, {"two", "one"}};
  static double[][] DOC_MATRIX = {{1, 0, 1}, {1, 2, 1}, {1, 0, 0}, {0, 1, 0}};
  
  @Test
  public void testOne() {
    TermVectorizer lsav = new TermVectorizer();
    
    for(String[] sentence: DOC_TERMS) {
      int dim = lsav.startColumn();
      for(String word: sentence) {
        lsav.vectorizeTerm(word, dim);
      }
    }
    lsav.truncateVectors();
    Matrix vectorMat = lsav.getMatrix();
    assertEquals("Vectorized Matrix rows: ", DOC_MATRIX.length, vectorMat.numRows());
    assertEquals("Vectorized Matrix columns: ", DOC_MATRIX[0].length, vectorMat.numColumns());
    for(int r = 0; r < 4; r++) {
      for(int c = 0; c < 3; c++) {
        assertEquals("vectorMatrix[" + r + "][" + c + "]", DOC_MATRIX[r][c], vectorMat.m[r][c], 0.1);
      }
    }
  }
}
