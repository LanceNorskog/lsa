package org.apache.lucene.opennlp.vector;

import org.apache.lucene.opennlp.vector.SVDSentences.Formula;

import junit.framework.TestCase;


public class TestSVD extends TestCase {
  private static final double EPSILON = 0.000001;
  static String[][] DOC_TERMS = {{"one", "two", "three"}, {"two", "four", "two"}, {"two", "one"}};
  static double[][] DOC_MATRIX = {{1, 0, 1}, {1, 2, 1}, {1, 0, 0}, {0, 1, 0}};
  static double[][] DOC_U = {
{0.9570920264890526, -1.000000000000001, 0.2897841486884301},
{2.4264041609334104, 0.3333333333333333, 0.03810166261363504},
{0.5122201079553047, -0.6666666666666671, -0.5414666347632253},
{0.7346560672221787, 0.6666666666666671, -0.12584124303739744}
  };
  
  public void testOne() {
    Matrix vectorMat = getMatrix(DOC_TERMS);
    SVDSentences svd = new SVDSentences();
    Matrix docTermMatrix = svd.createMatrix(vectorMat, null, Formula.tf, null);
    svd.doSVD(docTermMatrix);
    Matrix u = svd.getSingularU(true);
    u.hashCode();
    checkMatrix(u, DOC_U);
  }
  
  private void checkMatrix(Matrix m, double[][] expect) {
    for(int r = 0; r < m.numRows(); r++) {
      for(int c = 0; c < m.numColumns(); c++) {
        assertEquals(expect[r][c], m.m[r][c], EPSILON);
      }
    }
  }
  
  private Matrix getMatrix(String[][] dOC_TERMS2) {
    LSAVectorizer lsav = new LSAVectorizer();
    for(String[] sentence: DOC_TERMS) {
      lsav.startColumn();
      for(String word: sentence) {
        lsav.vectorizeTerm(word);
      }
      lsav.endColumn();
    }
    lsav.truncateVectors();
    Matrix vectorMat = lsav.getMatrix();
    return vectorMat;
  }
}
