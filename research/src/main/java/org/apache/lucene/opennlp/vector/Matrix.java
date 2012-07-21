package org.apache.lucene.opennlp.vector;

import java.util.List;

class Matrix {
  public final double[][] m;
  
  Matrix(double[][] m) {
    this.m = m;
  }

  public Matrix(int rows, int columns) {
    m = new double[rows][columns];
  }

  public Matrix copy() {
    double[][] copy = new double[numRows()][numColumns()];
    for(int r = 0; r < numRows(); r++) {
      for(int c = 0; c < numColumns(); c++) {
        copy[r][c] = m[r][c];
      }
    }
    return new Matrix(copy);
  }

  public void set(int r, int c, double value) {
    m[r][c] = value;
  }

  public Matrix times(Matrix other) {
    int columns = m[0].length;
    if (columns != other.m.length) {
      throw new IllegalArgumentException();
    }
    int rows = numRows();
    int otherColumns = other.numColumns();
    double[][] result = new double[rows][otherColumns];
    for (int row = 0; row < rows; row++) {
      for (int col = 0; col < otherColumns; col++) {
        double sum = 0.0;
        for (int k = 0; k < columns; k++) {
          sum += m[row][k] * other.m[k][col];
        }
        result[row][col] = sum;
      }
    }
    return new Matrix(result);
  }

  public int numRows() {
    return m.length;
  }
  
  public int numColumns() {
    return m[0].length;
  }
  
  public String tosString() {
    return "{" + numRows() + "," + numColumns() + "}";
  }
  
  public static Matrix getMatrix(List<double[]> termvecs) {
    Matrix mat = new Matrix(new double[termvecs.size()][termvecs.get(0).length]);
    for(int i = 0; i < termvecs.size(); i++) {
      mat.m[i] = termvecs.get(i);
    }
    return mat;
  }

}

class Vector {
  final double[] v;
  
  Vector(double[] v) {
    this.v = v;
  }
}
