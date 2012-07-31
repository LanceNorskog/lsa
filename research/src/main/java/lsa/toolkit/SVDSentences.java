package lsa.toolkit;


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
  public enum Formula {tf, binary, augNorm, log, normal, length, gfidf, tfidf, idf, entropy, inverse};
  private SingularValueDecomposition svd;
  
  public Matrix createMatrix(Matrix mat, double[] gfs, Formula local, Formula global) {
    Matrix localMat = null;
    Matrix globalMat = null;
    double[] globalFactors = null;
    
    if (local == Formula.tf || local == null) {
      ;
    } else if (local == Formula.binary) {
      localMat = binary(mat);
    } else if (global == Formula.log) {
      localMat = log(mat);    
    } else if (local == Formula.augNorm) {
      localMat = augNorm(mat);
    } else {
      throw new IllegalStateException("Unknown local calculation formula: " + local.toString());
    }
    if (global == null) {
      ;
    } else if (global == Formula.gfidf) {
      globalFactors = gfidf(mat, gfs);
    } else if (global == Formula.idf) {
      globalFactors = idf(mat);
    } else if (global == Formula.entropy) {
      globalFactors = entropy(mat, gfs);
    } else if (global == Formula.normal) {
      globalMat = normalTerm(mat);
    } else if (global == Formula.length) {
      globalMat = normalDoc(mat);   
    } else if (global == Formula.inverse) {
        globalFactors = probInverse(mat);
    } else {
      throw new IllegalStateException("Unknown global calculation formula: " + global.toString());
    }
    if (localMat != null ) {
      mat = null;
      if (globalMat != null) {
        return globalMat.timesCell(localMat);
      } else if (globalFactors != null) {
        return localMat.timesColumn(globalFactors);
      } else {
        return localMat;
      }
    } else {
      if (globalMat != null) {
        return globalMat;
      } else if (globalFactors != null) {
        return mat.copy().timesColumn(globalFactors);
      } else {
        return mat.copy();
      }
    }
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
  private double[] entropy(Matrix mat, double gfs[]) {
    double[] globalFactors = new double[mat.numRows()];
    double logDocs = Math.log(mat.numRows());
    for(int term = 0; term < mat.numColumns(); term++) {
      double sum = 0;
      for (int doc = 0; doc < mat.numRows(); doc++) {
        if (mat.m[doc][term] > 0) {
          double p = mat.m[doc][term] / gfs[term];
          sum += (p *  Math.log(p)) / logDocs;
        }
      }
      globalFactors[term] = (1 + sum);
    }
    return globalFactors;
  }
  
  /**
   * Inverse Document Frequency
   */
  private double[] idf(Matrix mat) {
    double[] dfs = getDocFrequencies(mat);
    double[] globalFactors = new double[mat.numRows()];
    for(int term = 0; term < mat.numColumns(); term++) {
      globalFactors[term] = Math.log(mat.numRows() / (1 + dfs[term]));
    }
    return globalFactors;
  }
  
  /**
   * Global Frequency / Document Frequency
   * 
   * Suggest using with binary local formula
   */
  private double[] gfidf(Matrix mat, double gfs[]) {
    double[] dfs = getDocFrequencies(mat);
    double[] globalFactors = new double[mat.numRows()];
    for(int term = 0; term < mat.numColumns(); term++) {
      globalFactors[term] = gfs[term] / dfs[term];
    }
    return globalFactors;
  }
  
  /**
   * Normal (L2) of document vector.
   * Prevent dominance by longer documents.
   */
  private Matrix normalDoc(Matrix mat) {
    Matrix globalMat = new Matrix(new double[mat.numRows()][mat.numColumns()]);
    for(int doc = 0; doc < mat.numRows(); doc++) {
      double docNorm = 0;
      for(int term = 0; term < mat.numColumns(); term++) {
        double tf = mat.m[doc][term];
        if (tf > 0) {
          docNorm += tf * tf;
        }
      }
      docNorm = Math.sqrt(docNorm);
      for(int term = 0; term < mat.numColumns(); term++) {
        if (mat.m[doc][term] > 0) {
          globalMat.m[doc][term] = docNorm;
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
  
  
  /**
   * Probabilistic Inverse
   */
  private double[] probInverse(Matrix mat) {
    double[] dfs = getDocFrequencies(mat);
    double[] globalFactors = new double[mat.numRows()];
    for(int term = 0; term < mat.numColumns(); term++) {
      globalFactors[term] = Math.log((mat.numRows() - dfs[term]) / dfs[term]);
    }
    return globalFactors;
  }

  private double[] getDocFrequencies(Matrix mat) {
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
    return dfs;
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
