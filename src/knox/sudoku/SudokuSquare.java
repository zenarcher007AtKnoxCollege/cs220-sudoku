package knox.sudoku;

/* This class is purely for passing info between objects when needed. 
 * It describes the location and value of a square on the board. */
public class SudokuSquare {
  public int value;
  public int row;
  public int col;
  public SudokuSquare(int row, int col, int value) {
    this.row = row;
    this.col = col;
    this.value = value;
  }
  
  public SudokuSquare() {}
  
  @Override
  public String toString() {
    return "[r=" + row + ", c=" + col + ", v=" + value + "]";
  }
  
  @Override
  public boolean equals(Object o) {
    if(!(o instanceof SudokuSquare)) return false;
    SudokuSquare s = (SudokuSquare) o;
    return this.row == s.row && this.col == s.col && this.value == s.value;
  }
  
}