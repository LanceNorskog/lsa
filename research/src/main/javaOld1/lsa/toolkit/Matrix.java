package lsa.toolkit;

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

  /* standard matrix multiplication */
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
  
  /* multiple individual cells */
  public Matrix timesCell(Matrix other) {
    int rows = m.length;
    if (rows != other.m.length) {
      throw new IllegalArgumentException();
    }
    int columns = m[0].length;
    if (columns != other.m[0].length) {
      throw new IllegalArgumentException();
    }
    double[][] result = new double[rows][columns];
    for (int r = 0; r < rows; r++) {
      for (int c = 0; c < columns; c++) {
          result[r][c] = m[r][c] * other.m[r][c];
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

  public Matrix transpose() {
    double[][] values = new double[numRows()][numColumns()];
    for(int r = 0; r < numRows(); r++) {
      for(int c = 0; c < numColumns(); c++) {
        values[c][r] = m[r][c];
      }
    }
    return new Matrix(values);
  }

  public Matrix timesColumn(double[] columnVector) {
    double[][] values = new double[numRows()][numColumns()];
    for(int r = 0; r < numRows(); r++) {
      for(int c = 0; c < numColumns(); c++) {
        values[c][r] = m[r][c] * columnVector[c];
      }
    }
    return new Matrix(values);
  }
}

class Vector {
  final double[] v;
  
  Vector(double[] v) {
    this.v = v;
  }
}
