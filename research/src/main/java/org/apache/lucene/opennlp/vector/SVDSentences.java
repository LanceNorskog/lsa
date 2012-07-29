package org.apache.lucene.opennlp.vector;

/**
 * Given sentences decomposed into term vectors, run SVD.
 * Matrix regularization methods from:
 * http://en.wikipedia.org/wiki/Latent_semantic_indexing
 * http://www.amazon.com/Understanding-Search-Engines-Mathematical-Environments/dp/0898715814
 *  Section 3.2.1 Term Weighting
 *  page 34
 *  
 *  This file has no imports.
 */

public class SVDSentences {
  public enum Formula {tf, binary, augNorm, log, normal, length, gfidf, tfidf, idf, entropy};
  private SingularValueDecomposition svd;
  
  public Matrix createMatrix(Matrix mat, double[] gfs, Formula local, Formula global) {
    Matrix localMat = null;
    Matrix globalMat = null;
    
    if (local == Formula.tf || local == null) {
      localMat = mat.copy();
    } else if (local == Formula.binary) {
      localMat = binary(mat);
    } else if (local == Formula.normal) {
      localMat = normalTerm(mat);
    } else if (local == Formula.length) {
      localMat = normalDoc(mat);
    } else if (local == Formula.augNorm) {
      localMat = augNorm(mat);
    } else {
      throw new IllegalStateException("Unknown local calculation formula: " + local.toString());
    }
    if (global == null) {
      ;
    } else if (global == Formula.gfidf) {
      globalMat = gfidf(mat, gfs);
    } else if (global == Formula.tfidf) {
      globalMat = tfidf(mat);
    } else if (global == Formula.idf) {
      globalMat = idf(mat);
    } else if (global == Formula.log) {
      globalMat = log(mat);    
    } else if (global == Formula.entropy) {
      globalMat = entropy(mat, gfs);
    } else {
      throw new IllegalStateException("Unknown global calculation formula: " + global.toString());
    }
    mat = null;
    Matrix docTermMatrix = global == null ? localMat : localMat.timesCell(globalMat);
    return docTermMatrix;
  }
  
  public void doSVD(Matrix docTermMatrix) {
    svd = new SingularValueDecomposition(docTermMatrix);
  }
  
  /* Local transforms */
  /**
   * Cells are binary-ized
   */
  private Matrix binary(Matrix mat) {
    Matrix localMat = new Matrix(new double[mat.numRows()][mat.numColumns()]);
    for (int doc = 0; doc < mat.numRows(); doc++) {
      for(int term = 0; term < mat.numColumns(); term++) {
        if (mat.m[doc][term] > 0)
          localMat.m[doc][term] = 1;
      }
    }
    return localMat;
  }
  
  /**
   * log of count
   */
  private Matrix log(Matrix mat) {
    Matrix localMat = new Matrix(new double[mat.numRows()][mat.numColumns()]);
    for (int doc = 0; doc < mat.numRows(); doc++) {
      for(int term = 0; term < mat.numColumns(); term++) {
        if (mat.m[doc][term] > 0)
          localMat.m[doc][term] = Math.log(mat.m[doc][term] + 1);
      }
    }
    return localMat;
  }
  
  /**
   * function of maximum count in a row
   */
  private Matrix augNorm(Matrix mat) {
    Matrix localMat = new Matrix(new double[mat.numRows()][mat.numColumns()]);
    for(int doc = 0; doc < mat.numRows(); doc++) {
      double maxTf = 0;
      for(int term = 0; term < mat.numColumns(); term++) {
        double tf = mat.m[doc][term];
        maxTf = Math.max(maxTf, tf);
      }
      for(int term = 0; term < mat.numColumns(); term++) {
        double tf = mat.m[doc][term];
        if (tf > 0)
          localMat.m[doc][term] = (tf / maxTf + 1) / 2;
      }
    }
    return localMat;
  }
  
  /* Global transforms */
  /**
   * ???
   */
  private Matrix entropy(Matrix mat, double gfs[]) {
    Matrix globalMat = new Matrix(new double[mat.numRows()][mat.numColumns()]);
    for (int doc = 0; doc < mat.numRows(); doc++) {
      double sum = 0;
      for(int term = 0; term < mat.numColumns(); term++) {
        if (mat.m[doc][term] > 0) {
          double p = mat.m[doc][term] / gfs[term];
          sum += (p *  Math.log(p)) / mat.numRows();
        }
      }
      for(int term = 0; term < mat.numColumns(); term++) {
        if (mat.m[doc][term] > 0) {
          globalMat.m[doc][term] = mat.m[doc][term] * (1 + sum);
        }
      }
    }
    return globalMat;
  }
  
  /**
   * Inverse Document Frequency
   */
  private Matrix idf(Matrix mat) {
    double[] dfs = new double[mat.numColumns()];
    for(int term = 0; term < mat.numColumns(); term++) {
      int df = 0;
      for(int doc = 0; doc < mat.numRows(); doc++) {
        double tf = mat.m[doc][term];
        if (tf > 0) {
          df++;
        }
      }
      dfs[term] = df;
    }
    Matrix globalMat = new Matrix(new double[mat.numRows()][mat.numColumns()]);
    for (int doc = 0; doc < mat.numRows(); doc++) {
      for(int term = 0; term < mat.numColumns(); term++) {
        if (mat.m[doc][term] > 0) {
          globalMat.m[doc][term] = 1 + Math.log(mat.numRows() / dfs[term]);
        }
      }
    }
    return globalMat;
    
  }
  
  /**
   * Global Frequency / Document Frequency
   */
  private Matrix gfidf(Matrix mat, double gfs[]) {
    double[] dfs = new double[mat.numColumns()];
    for(int term = 0; term < mat.numColumns(); term++) {
      int df = 0;
      for(int doc = 0; doc < mat.numRows(); doc++) {
        double tf = mat.m[doc][term];
        if (tf > 0) {
          df++;
        }
      }
      dfs[term] = df;
    }
    Matrix globalMat = new Matrix(new double[mat.numRows()][mat.numColumns()]);
    for (int doc = 0; doc < mat.numRows(); doc++) {
      for(int term = 0; term < mat.numColumns(); term++) {
        double tf = mat.m[doc][term];
        if (tf > 0)
          globalMat.m[doc][term] = gfs[term] / dfs[term];
      }
    }
    return globalMat;
  }
  
  /**
   * Term Frequency / Document Frequency
   */
  private Matrix tfidf(Matrix mat) {
    double[] dfs = new double[mat.numColumns()];
    for(int term = 0; term < mat.numColumns(); term++) {
      int df = 0;
      for(int doc = 0; doc < mat.numRows(); doc++) {
        double tf = mat.m[doc][term];
        if (tf > 0) {
          df++;
        }
      }
      dfs[term] = df;
    }
    Matrix globalMat = new Matrix(new double[mat.numRows()][mat.numColumns()]);
    for (int doc = 0; doc < mat.numRows(); doc++) {
      for(int term = 0; term < mat.numColumns(); term++) {
        if (mat.m[doc][term] > 0)
          globalMat.m[doc][term] = mat.m[doc][term] * Math.log(1 / dfs[term]);
      }
    }
    return globalMat;
  }
  
  /**
   * Normalize (L2) length of document vector.
   * Prevent dominance by longer documents.
   */
  private Matrix normalDoc(Matrix mat) {
    Matrix globalMat = new Matrix(new double[mat.numRows()][mat.numColumns()]);
    for(int doc = 0; doc < mat.numRows(); doc++) {
      double norm = 0;
      for(int term = 0; term < mat.numColumns(); term++) {
        double tf = mat.m[doc][term];
        if (tf > 0) {
          norm += tf * tf;
        }
      }
      norm = Math.sqrt(norm);
      for(int term = 0; term < mat.numColumns(); term++) {
        if (mat.m[doc][term] > 0) {
          globalMat.m[doc][term] = mat.m[doc][term] / norm;
        }
      }
    }
    return globalMat;
  }
  
  /**
   * Normalize (L2) length of term vector.
   * Prevent dominance by popular terms.
   */
  
  private Matrix normalTerm(Matrix mat) {
    Matrix globalMat = normalDoc(mat).transpose();
    return globalMat;
  }
  
  /* U is document matrix, V is term matrix */
  
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
