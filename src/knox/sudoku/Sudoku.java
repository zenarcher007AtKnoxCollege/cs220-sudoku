package knox.sudoku;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.Set;

/**
 * 
 * This is the MODEL class. This class knows all about the
 * underlying state of the Sudoku game. We can VIEW the data
 * stored in this class in a variety of ways, for example,
 * using a simple toString() method, or using a more complex
 * GUI (Graphical User Interface) such as the SudokuGUI 
 * class that is included.
 * 
 * @author jaimespacco
 *
 */

public class Sudoku {
  
	int[][] board = new int[9][9];
	
	public int get(int row, int col) {
	  try {
		  return board[row][col];
	  } catch(ArrayIndexOutOfBoundsException e) {
	    throw new ArrayIndexOutOfBoundsException("(" + col + ", " + row + ") is out of bounds of the board.");
	  }
	}
	
	/* Sets the position on the board to the given value if it is legal. If not, it may throw an exception.
	 * It will also check if that value is in a legal position on the board. If not, it will not set that value,
	 * and return a LinkedList containing all the SudokuSquares that make it illegal */
	public Collection<SudokuSquare> set(int row, int col, int val) {
		// TODO: make sure val is legal
	  if(val < 0 || val > 9) {
	    throw new IllegalArgumentException("\"" + val + "\"" + " is an invalid value on a sudoku board");
	  }
		Collection<SudokuSquare> illegals = getLegalViolations(row, col, val);
		if(illegals.isEmpty()) { // Only if it was legal
		  try {
	      board[row][col] = val;
	    } catch(ArrayIndexOutOfBoundsException e) {
	      throw new ArrayIndexOutOfBoundsException("(" + col + ", " + row + ") is out of bounds of the board.");
	    }
		}
		return illegals;
	}
	
	// Force a value into a position without checking if it is legal. Only call this if you know it is.
	public void force(int row, int col, int val) {
	  try {
      board[row][col] = val;
    } catch(ArrayIndexOutOfBoundsException e) {
      throw new ArrayIndexOutOfBoundsException("(" + col + ", " + row + ") is out of bounds of the board.");
    }
	}
	
	public boolean isLegal(int row, int col, int val) {
	  // Check cross section of rows and columns
	  for(int i = 0; i < 9; ++i) {
	    if(board[row][i] == val || board[i][col] == val) {
	      return false;
	    }
	  }
	  
	  // Check the 3*3 grid
	  int cr = row - row % 3; // Corner row
	  int cc = col - col % 3; // Corner column
    for(int c = cc; c < cc+3; ++c)
      for(int r = cr; r < cr+3; ++r)
        if(board[r][c] == val)
          return false;
	  
		return true;
	}
	
	/* Returns a Collection containing all the SudokuSquares that make putting a particular value
	 * in a particular place illegal. (i.e. it will return the 4 that is adjacent to the 4 you want to put down) */
	public Collection<SudokuSquare> getLegalViolations(int row, int col, int val) {
	  LinkedList<SudokuSquare> list = new LinkedList<SudokuSquare>(); // Values should not repeat
	  int cr = row - row % 3; // Corner row
    int cc = col - col % 3; // Corner column
	  
    // Check all rows and columns outside of the 3x3 grid it is contained in
    for(int i = 0; i < 9; ++i) {
      // Iterate cross section of rows/columns, skipping the current 3x3 grid.
      if(board[row][i] == val && (i < cc || i > cc+2)) {
        list.add(new SudokuSquare(row, i, val));
      } 
      if(board[i][col] == val && (i < cr || i > cr+2)) {
        list.add(new SudokuSquare(i, col, val));
      }
    }
    
    // Check the 3*3 grid
    for(int c = cc; c < cc+3; ++c) {
      for(int r = cr; r < cr+3; ++r) {
        if(board[r][c] == val) {
          list.add(new SudokuSquare(r, c, val));
        }
      }
    }
    return list;
  }
	
	/* Returns a Collection of all the most constrained squares, where each would be equally suitable
	 * for use as a hint. */
	public Collection<SudokuSquare> getMostContrainedSquares() {
	  LinkedList<SudokuSquare> list = new LinkedList<SudokuSquare>();
	  int numLegalValues = Integer.MAX_VALUE;
	  for(int c = 0; c < 9; ++c) {
	    for(int r = 0; r < 9; ++r) {
	      if(board[r][c] == 0) { // It is only viable if it is empty
	        int legalCount = getLegalValues(r, c).size();
	        if(legalCount <= numLegalValues) { // Add another possibility to the list
	          if(legalCount < numLegalValues) // An even more constrained one has been found. 
	            list = new LinkedList<SudokuSquare>(); // Invalidate all lesser-constrained possibilites
	          numLegalValues = legalCount;
	          list.add(new SudokuSquare(r, c, board[r][c])); // Record this square
	        }
	      }
	    }
	  }
	  return list;
	}
	
	/* Gets all legal positions where a number could possibly go, including on top of existing numbers */
	public Collection<SudokuSquare> getAllLegalPositions(int val) {
	  HashSet<SudokuSquare> set = new HashSet<SudokuSquare>();
	  for(int c = 0; c < 9; ++c) {
      for(int r = 0; r < 9; ++r) {
        if(getLegalValues(r, c).contains(val))
          set.add(new SudokuSquare(r, c, board[r][c])); // Add all squares to the set
      }
	  }
	  /*for(int c = 0; c < 9; ++c) {
      for(int r = 0; r < 9; ++r) {
        set.removeAll(getLegalViolations(r, c, val)); // Remove squares that are illegal
      }
	  }*/
	  return set;
	}
	
	public Collection<Integer> getLegalValues(int row, int col) {
		// Return only the legal values that can be stored at the given row, col
	  // Get all values that don't show up in:
	  // the same row as row
	  // the same column as col
	  // the same 3x3 area where row,col is located.
	  Set<Integer> result = new HashSet<>(Arrays.asList(1,2,3,4,5,6,7,8,9));
	  
	  // Remove values in the same column or row from the list
	  for(int i = 0; i < 9; ++i) {
	    result.remove(board[row][i]);
	    result.remove(board[i][col]);
	  }
	  
	  // Remove values in the same 3x3 grid
	  // Doesn't matter if it checks some twice
	  int cr = row - row % 3;
	  int cc = col - col % 3;
	  // Check the 3*3 grid
    for(int c = cc; c < cc+3; ++c) {
      for(int r = cr; r < cr+3; ++r) {
        result.remove(board[r][c]);
      }
    }
		return result;
	}
	

	
	public void load(String filename) {
		try {
			Scanner scan = new Scanner(new FileInputStream(filename));
			// read the file
			for (int r=0; r<9; r++) {
				for (int c=0; c<9; c++) {
					int val = scan.nextInt();
					board[r][c] = val;
				}
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Return which 3x3 grid this row is contained in.
	 * 
	 * @param row
	 * @return
	 */
	public int get3x3row(int row) {
		return row / 3;
	}
	
	/**
	 * Convert this Sudoku board into a String
	 */
	public String toString() {
		StringBuilder sb = new StringBuilder(9*9*2+1);
		for (int r=0; r<9; r++) {
			for (int c=0; c<9; c++) {
				int val = get(r, c);
				if (val == 0) {
					sb.append("_ ");
				} else {
					sb.append(val + " ");
				}
			}
			sb.append("\n");
		}
		return sb.toString();
	}
	
	/* Returns a string, but with zeroes instead of underscores. */
	public String toSaveString() {
    StringBuilder sb = new StringBuilder(9*9*2+1);
    for (int r=0; r<9; r++) {
      for (int c=0; c<9; c++) {
        int val = get(r, c);
        if (val == 0) {
          sb.append("0 ");
        } else {
          sb.append(val + " ");
        }
      }
      sb.append("\n");
    }
    return sb.toString();
  }
	
	public static void main(String[] args) {
		Sudoku sudoku = new Sudoku();
		sudoku.load("easy1.txt");
		System.out.println(sudoku);
		
		Scanner scan = new Scanner(System.in);
		while (!sudoku.gameOver()) {
			System.out.println("enter value r, c, v :");
			int r = scan.nextInt();
			int c = scan.nextInt();
			int v = scan.nextInt();
			sudoku.set(r, c, v);

			System.out.println(sudoku);
		}
	}

	/* Return true if all spots are filled (there are not any zeroes in the board) */
	public boolean gameOver() {
	  for(int r = 0; r < 9; ++r) {
	    for(int c = 0; c < 9; ++c) {
	      if(isBlank(r, c)) {
	        return false;
	      }
	    }
	  }
		return true;
	}

	public boolean isBlank(int row, int col) {
		return board[row][col] == 0;
	}
	
	//public boolean getMostConstrainedSquare() {
	  
	//}

}
