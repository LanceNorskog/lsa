package org.apache.lucene.opennlp.vector;
import java.util.Arrays;

/**
 * Given sentences decomposed into term vectors, run SVD.
 *
 */

public class SVDSentences {
  public enum Formula {count, binary, normal, gfidf, tfidf, idf, logEntropy};
  private final SingularValueDecomposition svd;
    
  public SVDSentences(Matrix mat, double[] counts, Formula formula) {
    Matrix termmat = null;
    if (formula == Formula.count) {
      termmat = mat.copy();
    } else if (formula == Formula.binary) {
      termmat = binaryCounts(mat);
    } else if (formula == Formula.normal) {
      termmat = normalCounts(mat);
    } else if (formula == Formula.gfidf) {
      termmat = gfidf(mat, counts);
    } else if (formula == Formula.tfidf) {
      termmat = tfidf(mat);
    } else if (formula == Formula.idf) {
      termmat = idf(mat);
    } else if (formula == Formula.logEntropy) {
      termmat = logEntropy(mat);
    } else {
      throw new IllegalStateException("Unknown calculation formula: " + formula.toString());
    }
     
    svd = new SingularValueDecomposition(termmat);
  }
  
  /**
   * Got this off the back of a Crackerjack box. Am sure it's bogus.
   */
  private Matrix logEntropy(Matrix mat) {
    double[] logsum = new double[mat.numColumns()];
    Arrays.fill(logsum, 1.0);
    for(int c = 0; c < mat.numColumns(); c++) {
      for(int r = 0; r < mat.numRows(); r++) {
        double tf = mat.m[r][c];
        if (tf > 0) {
          logsum[c] += (tf * Math.log(tf)) / mat.numRows();
        }
      }
    }
    Matrix m = new Matrix(new double[mat.numRows()][mat.numColumns()]);
    for (int r = 0; r < mat.numRows(); r++) {
      for(int c = 0; c < mat.numColumns(); c++) {
        if (mat.m[r][c] > 0) {
          m.m[r][c] = logsum[c] * Math.log(mat.m[r][c] + 1);
        }
      }
    }
    return m;
  }

  /**
   * Inverse Document Frequency
   */
  private Matrix idf(Matrix mat) {
    double[] dfs = new double[mat.numColumns()];
    for(int c = 0; c < mat.numColumns(); c++) {
      int df = 0;
      for(int r = 0; r < mat.numRows(); r++) {
        double tf = mat.m[r][c];
        if (tf > 0) {
          df++;
        }
      }
      dfs[c] = df;
    }
    Matrix m = new Matrix(new double[mat.numRows()][mat.numColumns()]);
    for (int r = 0; r < mat.numRows(); r++) {
      for(int c = 0; c < mat.numColumns(); c++) {
        if (mat.m[r][c] > 0) {
          m.m[r][c] = 1 + Math.log(mat.numRows() / dfs[c]);
        }
      }
    }
    return m;

  }

  /**
   * Global Frequency / Document Frequency
   */
  private Matrix gfidf(Matrix mat, double counts[]) {
    double[] dfs = new double[mat.numColumns()];
    for(int c = 0; c < mat.numColumns(); c++) {
      int df = 0;
      for(int r = 0; r < mat.numRows(); r++) {
        double tf = mat.m[r][c];
        if (tf > 0) {
          df++;
        }
      }
      dfs[c] = df;
    }
    Matrix m = new Matrix(new double[mat.numRows()][mat.numColumns()]);
    for (int r = 0; r < mat.numRows(); r++) {
      for(int c = 0; c < mat.numColumns(); c++) {
        if (mat.m[r][c] > 0)
          m.m[r][c] = counts[c] / dfs[c];
      }
    }
    return m;
  }

  /**
   * Term Frequency / Document Frequency
   */
  private Matrix tfidf(Matrix mat) {
    double[] dfs = new double[mat.numColumns()];
    for(int c = 0; c < mat.numColumns(); c++) {
      int df = 0;
      for(int r = 0; r < mat.numRows(); r++) {
        double tf = mat.m[r][c];
        if (tf > 0) {
          df++;
        }
      }
      dfs[c] = df;
    }
    Matrix m = new Matrix(new double[mat.numRows()][mat.numColumns()]);
    for (int r = 0; r < mat.numRows(); r++) {
      for(int c = 0; c < mat.numColumns(); c++) {
        if (mat.m[r][c] > 0)
          m.m[r][c] = mat.m[r][c] / dfs[c];
      }
    }
    return m;
  }

  /**
   * Column cell is the mean of all counts in that column.
   * Except we forgot to divide by df
   */
  private Matrix normalCounts(Matrix mat) {
    double[] norms = new double[mat.numColumns()];
    for(int c = 0; c < mat.numColumns(); c++) {
      int df = 0;
      for(int r = 0; r < mat.numRows(); r++) {
        double tf = mat.m[r][c];
        if (tf > 0) {
          norms[c] += tf * tf;
          df++;
        }
      }
      // should this be /df ?
      norms[c] = Math.sqrt(norms[c]);
    }
    Matrix m = new Matrix(new double[mat.numRows()][mat.numColumns()]);
    for (int r = 0; r < mat.numRows(); r++) {
      for(int c = 0; c < mat.numColumns(); c++) {
        if (mat.m[r][c] > 0)
          m.m[r][c] = norms[c];
      }
    }
    return m;
  }

  /**
   * Cells are binary-ized
   */
  private Matrix binaryCounts(Matrix mat) {
    Matrix m = new Matrix(new double[mat.numRows()][mat.numColumns()]);
    for (int r = 0; r < mat.numRows(); r++) {
      for(int c = 0; c < mat.numColumns(); c++) {
        if (mat.m[r][c] > 0)
          m.m[r][c] = 1;
      }
    }
    return m;
  }

  public Matrix getSingularU(boolean useSingular) {
    Matrix u = svd.getU();
    double[] svalues = svd.getSingularValues();
    
    int shorter = Math.min(u.numRows(), u.numColumns());
    Matrix s = new Matrix(shorter, shorter);
    for(int i = 0; i < shorter; i++) {
      s.set(i, i, Math.sqrt(svalues[i]));
      s.set(i, i, svalues[i]);
    }
    if (useSingular)
      return u.times(s);
    else
      return u;
  }
  
  public Matrix getSingularV(boolean useSingular) {
    Matrix v = svd.getV();
    double[] svalues = svd.getSingularValues();
    
    Matrix s = new Matrix(svalues.length, svalues.length);
    for(int i = 0; i < svalues.length; i++) {
      s.set(i, i, Math.sqrt(svalues[i]));
      s.set(i, i, svalues[i]);
    }
    if (useSingular)
      return v.times(s); 
    else
      return v;
  }
  
  public double[] getTermStrengths() {
    Matrix termRows = getSingularU(true);
    double[] strengths = new double[termRows .numRows()];
    for(int r = 0; r < termRows.numRows(); r++) {
      strengths[r] = Sorter.squaredNorm(termRows, r, 20);
    }
    return strengths;
  }

}
