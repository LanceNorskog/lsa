package org.apache.solr.summary.lsa;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Sorter {
  
  /**
   * Sort sentence and term feature vectors by (squared) Euclidean (L2) norm across all features.
   */
  
  public List<Integer> getSentenceList(SVDSentences svds, List<Double> fvec) {
    List<DIPair> dipairs = new ArrayList<DIPair>();
    
    Matrix u = svds.getSingularU(true);
    int rows = u.numRows();
    
    double rowFV[] = new double[rows];
    int norm = u.numColumns();
    for(int r = 0; r < rows; r++) {
      double fv = squaredNorm(u, r, norm);
      rowFV[r] = fv;
    }
    for(int r = 0; r < rows; r++) {
      double fv = squaredNorm(u, r, norm);
      dipairs.add(new DIPair(fv, r, true));
      rowFV[r] = fv;
    }
    Collections.sort(dipairs);
    Arrays.sort(rowFV);
    List<Integer> indexes = new ArrayList<Integer>();
    for (int i = 0; i < rows; i++) {
      DIPair dip = dipairs.get(i);
      indexes.add(dip.i);
      if (fvec != null) {
        fvec.add(dip.d);
      }
    }
    return indexes;
  }
  
  public List<Integer> getTermList(SVDSentences svds, List<Double> fvec) {
    List<DIPair> dipairs = new ArrayList<DIPair>();
    
    Matrix v = svds.getSingularV(true);
    int rows = v.numRows();
    
    double rowFV[] = new double[rows];
    int norm =  v.numColumns(); 
    for(int r = 0; r < rows; r++) {
      double fv = squaredNorm(v, r, norm);
      rowFV[r] = fv;
    }
    for(int r = 0; r < rows; r++) {
      double fv = squaredNorm(v, r, norm);
      dipairs.add(new DIPair(fv, r, true));
      rowFV[r] = fv;
    }
    Collections.sort(dipairs);
    Arrays.sort(rowFV);
    List<Integer> indexes = new ArrayList<Integer>();
    for (int i = 0; i < rows; i++) {
      DIPair dip = dipairs.get(i);
      indexes.add(dip.i);
      if (fvec != null) {
        fvec.add(dip.d);
      }    
    }
    return indexes;
  }
  
  public static double squaredNorm(Matrix mat, int r, int d) {
    double sum = 0;
    for(int i = 0; i < d; i++) {
      sum += mat.m[r][i] * mat.m[r][i];
    }
    ;
    return sum;
  }
  
  
}


class DIPair implements Comparable<DIPair> {
  Double d;
  int i;
  int sign = 1;
  
  public DIPair(double d, int i, boolean reverse) {
    this.d = d;
    this.i = i;
    this.sign = reverse ? -1 : 1;
  }
  
  public int compareTo(DIPair other) {
    int compare = d.compareTo(other.d);
    return compare * sign;
  }
  
  @Override
  public boolean equals(Object obj) {
    return d == ((DIPair) obj).d && i == ((DIPair) obj).i;
  }
  
  @Override
  public int hashCode() {
    return d.hashCode() ^ new Integer(i).hashCode();
  }
}

