package knox.sudoku;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.MenuItem;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.IOException;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.Timer;
import javax.swing.TransferHandler;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.plaf.basic.BasicInternalFrameTitlePane.SystemMenuBar;



/**
 * 
 * This is the GUI (Graphical User Interface) for Sudoku.
 * 
 * It extends JFrame, which means that it is a subclass of JFrame.
 * The JFrame is the main class, and owns the JMenuBar, which is 
 * the menu bar at the top of the screen with the File and Help 
 * and other menus.
 * 
 * 
 * One of the most important instance variables is a JCanvas, which is 
 * kind of like the canvas that we will paint all of the grid squared onto.
 * 
 * @author jaimespacco
 *
 */


public class SudokuGUI extends JFrame {

  private Sudoku sudoku;

  private static final long serialVersionUID = 1L;

  // Sudoku boards have 9 rows and 9 columns
  private int numRows = 9;
  private int numCols = 9;

  // the current row and column we are potentially putting values into
  private int currentRow = -1;
  private int currentCol = -1;
  
  // The number of times you can use SuperSwap
  private int swapsRemaining = 2;
  
  // A toggle to enable only legal values to be entered
  private boolean enableLegalValuesOnly = false;
  
  // A toggle to enable highlighting the reasons a value is illegal upon a keypress.
  private boolean enableIllegalHighlighting = true;
  
  JFileChooser sharedFileChooser = new JFileChooser();
  
  // For hints
  //private int hintRow = -1;
  //private int hintCol = -1;

  private boolean isInSuperSwapMode = false; // Note: this is only for making sure the Super Swap
  // button isn't accidentally pressed twice
  
  // figuring out how big to make each button
  // honestly not sure how much detail is needed here with margins
  protected final int MARGIN_SIZE = 5;
  protected final int DOUBLE_MARGIN_SIZE = MARGIN_SIZE*2;
  protected int squareSize = 90;
  private int width = DOUBLE_MARGIN_SIZE + squareSize * numCols;    		
  private int height = DOUBLE_MARGIN_SIZE + squareSize * numRows;  

  private static Font FONT = new Font("Verdana", Font.BOLD, 40);
  private static Color FONT_COLOR = Color.BLACK;
  private static Color BUTTON_COLOR = Color.GRAY;
  private static Color BACKGROUND_COLOR = Color.WHITE;

  // the canvas is a panel that gets drawn on
  private JPanel panel;

  // this is the menu bar at the top that owns all of the buttons
  private JMenuBar menuBar;
  
  Timer discoTimer;

  
  
  // 2D array of buttons; each sudoku square is a button
  private HButton[][] buttons = new HButton[numRows][numCols];
  
  /* Changes all square background colors to random colors. */
  private void disco() {
    for(HButton[] row : buttons) {
      for(HButton button : row) {
        button.setHighlighted(true);
        int r = ThreadLocalRandom.current().nextInt(0, 255+1); 
        int g = ThreadLocalRandom.current().nextInt(0, 255+1); 
        int b = ThreadLocalRandom.current().nextInt(0, 255+1); 
        // How odd that there isn't a direct conversion...
        float[] HSBColor = Color.RGBtoHSB(r, g, b, null);
        button.setHighlightColor(Color.getHSBColor(HSBColor[0], HSBColor[1], HSBColor[2]));
      }
    }
  }
  
  /* Checks if the game is over. If it is, starts a timer to repeatedly call the disco function. */
  private void checkIfGameOver() {
    if(sudoku.gameOver())
      discoTimer.start();
  }
  

  private class MyKeyListener extends KeyAdapter {
    public final int row;
    public final int col;
    public final Sudoku sudoku;

    MyKeyListener(int row, int col, Sudoku sudoku){
      this.sudoku = sudoku;
      this.row = row;
      this.col = col;
    }

    public void keyTyped(KeyEvent e) {
      unhighlightAll(); // Unhighlight all if you press a key
      char key = e.getKeyChar();
      //System.out.println(key);
      if (Character.isDigit(key)) {
        // use ascii values to convert chars to ints
        int digit = key - '0';
        if (currentRow == row && currentCol == col) {
          Collection<SudokuSquare> s = sudoku.set(row, col, digit);
          if(enableIllegalHighlighting) {
            highlightList(s); // Highlight all squares that make it illegal to put in that position.
          }
        }
        checkIfGameOver();
        update();
      }
    }
  }

  

  
  private class ButtonListener implements ActionListener {
    class SelectionItem extends JMenuItem { // Custom menu item
      class SelectionListener implements ActionListener { // Custom menu item action listener
        public SelectionListener() {} // TODO: Make keyboard-driven selection as well.
        @Override
        public void actionPerformed(ActionEvent e) {
          sudoku.set(row, col, val); // Set the value at its assigned position.
          SudokuGUI.this.update(); // Call method of class many nested classes back (https://stackoverflow.com/a/2808514/16386050)
        }
      } // End of nested class
      
      // For SelectionItem
      private static final long serialVersionUID = 1L;
      int val;
      SelectionItem(int val) {
        super(""+val); 
        this.val = val;
        addActionListener(new SelectionListener());
      }
    } // End of nested class
    
    
    public final int row;
    public final int col;
    public final Sudoku sudoku;
    ButtonListener(int row, int col, Sudoku sudoku){
      this.sudoku = sudoku;
      this.row = row;
      this.col = col;
    }

    /* Pops up a selector of integer values, with an action listener
     * to set the specified square to one of the given values. Sets it to
     * 0 if the selection was cancelled. */
    public void selectFrom(Collection<Integer> values, JComponent src) {
      JPopupMenu menu = new JPopupMenu();
      Point mouseLoc = src.getMousePosition(); // Get the position of the mouse
      if(mouseLoc == null) return;
      menu.setLocation(mouseLoc);
      for(int val : values) { // Add actions for all possible legal values
        SelectionItem item = new SelectionItem(val); // Add custom menu item
        menu.add(item);
      }
      menu.show(src, mouseLoc.x, mouseLoc.y);
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
      unhighlightAll(); // Unhighlight all if you click another button
      //System.out.printf("row %d, col %d, %s\n", row, col, e);
      JButton button = (JButton)e.getSource();

      if (row == currentRow && col == currentCol) {
        currentRow = -1;
        currentCol = -1;
      } else if (sudoku.isBlank(row, col)) {
        // we can try to enter a value in a 
        currentRow = row;
        currentCol = col;

        // TODO: Make this able to be toggled off and on from a menu
        // TODO: figure out some way that users can enter values
        // A simple way to do this is to take keyboard input
        // or you can cycle through possible legal values with each click
        // or pop up a selector with only the legal valuess
        //SelectionMenu sMenu = new SelectionMenu(sudoku);
        if(enableLegalValuesOnly) {
          selectFrom(sudoku.getLegalValues(row, col), button); // Select from possible values.
          // (By its very definition, there will be no need to highlight illegal values)
        }
        //System.out.println(selectFrom(sudoku.getLegalValues(row, col), button));
      } else {
        // TODO: error dialog letting the user know that they cannot enter values
        // where a value has already been placed
      }
      checkIfGameOver();
      update();
    }
  }

  /**
   * Put text into the given JButton
   * 
   * @param row
   * @param col
   * @param text
   */
  private void setText(int row, int col, String text) {
    buttons[row][col].setText(text);
  }
  
  /* Highlight the HButton at the given position */
  private void setHighlighted(int row, int col, boolean highlighted) {
    buttons[row][col].setHighlighted(highlighted);
  }
  
  /* A convienence function to highlight a collection of SudokuSquares */
  private void highlightList(Collection<SudokuSquare> list) {
    for(SudokuSquare square : list) {
      setHighlighted(square.row, square.col, true);
    }
  }
  
  /* A convienence function to unhighlight all SudokuSquares */
  private void unhighlightAll() {
    for(HButton[] row : buttons) {
      for(HButton button : row) {
        button.setHighlighted(false);
      }
    }
  }

  /* Locates an HButton on the board, or null if it doesn't exist. */
  private Point locateButton(HButton button) {
    for(int c = 0; c < 9; ++c) {
      for(int r = 0; r < 9; ++r) {
        if(buttons[r][c] == button) {
          return new Point(c,r);
        }
      }
    }
    return null;
  }
  
  /* A special JButton that contains data for making it highlightable, among some other cool stuff... */
  private class HButton extends JButton implements MouseListener, MouseMotionListener, DropTargetListener {
    private static final long serialVersionUID = 1L;
    private boolean isHighlighted;
    public Color highlightColor;
    private int wiggleSpeed;
    private float wiggleVariation;
    private Timer wiggleTimer;
    private float wiggleIndex;
    private Point dragStart;
    Collection<SudokuSquare> legalValuesForSwap;
    HButton() {
      super();
      isHighlighted = false;
      dragStart = new Point(0,0);
      legalValuesForSwap = null;
      highlightColor = Color.RED;
      wiggleSpeed = ThreadLocalRandom.current().nextInt(8,14); // Some will rotate faster / slower
      wiggleVariation = (float) (ThreadLocalRandom.current().nextFloat()/2+4.5); // The speed at which it will appear to start/stop wiggling slightly
      wiggleIndex = 0;
      wiggleTimer = new Timer(50, new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
          repaint();
        }
      });
      DropTarget dropTarget = new DropTarget(this, DnDConstants.ACTION_COPY_OR_MOVE,
          this, true, null);
      addMouseListener(this);
      addMouseMotionListener(this);
    }
    
    // To share data with a tile you are swapping with...
    public Collection<SudokuSquare> readLegalValuesOnDrag() {return legalValuesForSwap;}
    public void clearLegalValuesOnDrag() {legalValuesForSwap = null;}
    
    public void setHighlighted(boolean highlighted) {
      isHighlighted = highlighted;
    }

    public void setHighlightColor(Color c) {
      highlightColor = c;
      setBackground(c);
    }

    public void enableWiggleMode() {
      wiggleIndex = 0.1f;
      wiggleTimer.start();
    }

    public void disableWiggleMode() {
      wiggleTimer.stop();
      wiggleIndex = 0;
      repaint();
    }

    @Override
    public void paint(Graphics origG) {
      Graphics2D g = (Graphics2D) origG;
      if(wiggleIndex == 0) { // Restore original position
        g.rotate(0);
      } else { // Wiggle
        g.rotate( (Math.sin(wiggleIndex)*Math.sin(wiggleIndex/1.23)*(1+Math.sin(wiggleIndex/(wiggleVariation))/6))/20 );
        wiggleIndex = wiggleIndex + (float) wiggleSpeed / 15;
      }
      super.paint(origG);
    }
    
    @Override public void mousePressed(MouseEvent e) {
      if(wiggleIndex != 0) { // Only if in wiggle mode
      Point p = locateButton(this); // Where am I?
      if(p == null)
        return;
      currentRow = p.y; // Set cursor position
      currentCol = p.x;
      dragStart = getLocation();
      
      // Temporary remove this value to calculate legal positions.
      int tempValue = sudoku.get(currentRow, currentCol);
      sudoku.force(currentRow, currentCol, 0);
      // Highlight all legal positions
      legalValuesForSwap = sudoku.getAllLegalPositions(tempValue);
      for(SudokuSquare square : legalValuesForSwap) {
        HButton button = buttons[square.row][square.col];
        button.setHighlightColor(Color.GREEN);
        button.setHighlighted(true);
      }
      // Put this value back
      sudoku.force(currentRow, currentCol, tempValue);
      
      setHighlightColor(Color.YELLOW);
      setHighlighted(true);
      }
    }
    @Override public void mouseMoved(MouseEvent e) {}
    @Override public void mouseClicked(MouseEvent e) {}
    @Override public void mouseDragged(MouseEvent e) {
      if(wiggleIndex == 0) return;
      // Start drag
      setTransferHandler(new TransferHandler("icon"));
      this.getTransferHandler().exportAsDrag(this, e, TransferHandler.COPY);
    }
    @Override public void mouseReleased(MouseEvent e) {
      // Known bug: sometimes it doesn't send a mouseRelease event - like it literally doesn't
      // call this function even though I release the mouse...
      setHighlighted(false);
      setHighlightColor(Color.RED);
      SudokuGUI.this.update();
    }
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}

    /* Drag and drop */
    @Override public void dragEnter(DropTargetDragEvent dtde) {}
    @Override public void dragOver(DropTargetDragEvent dtde) {}
    @Override public void dropActionChanged(DropTargetDragEvent dtde) {}
    @Override public void dragExit(DropTargetEvent dte) {}
    @Override public void drop(DropTargetDropEvent dtde) {
      // Cheats a bit... I tried hard looking into drop events, but I just don't have time to learn how to
      // implement it properly using Swing right now.
      if(currentRow < 0 || currentCol < 0) return;
      Point p = locateButton(this); // Not particularly efficient, but...
      int thisRow = p.y;
      int thisCol = p.x;
      HButton dragged = buttons[currentRow][currentCol];
      // Make sure that this is one of the values that the dragger intended to swap with...
      if(! doesCollectionContain(dragged.readLegalValuesOnDrag(), new SudokuSquare(thisRow, thisCol, sudoku.get(thisRow, thisCol)))) {
        dtde.rejectDrop();
        return;
      }
      dragged.clearLegalValuesOnDrag();
      
      // Swap
      if(!(thisRow == currentRow && thisCol == currentCol)) {
        dragged.setHighlightColor(Color.RED);
        unhighlightAll();
        
        // Swap positions on board:
        int temp = sudoku.get(currentRow, currentCol);
        sudoku.force(currentRow, currentCol, sudoku.get(thisRow, thisCol));
        sudoku.force(thisRow, thisCol, temp);
        
        // Turn off wiggle mode.
        for(int c = 0; c < 9; ++c) {
          for(int r = 0; r < 9; ++r) {
            buttons[r][c].disableWiggleMode();
          }
        }
        isInSuperSwapMode = false; // Note: this is only used to prevent the Super Swap button
        // from being pressed twice, wasting both swaps...
        
        /* Change Background Color Back */
        for(HButton[] row : buttons) {
          for(HButton button : row) {
            button.setHighlightColor(Color.RED);
          }
        }
        SudokuGUI.this.update();
        checkIfGameOver();
      } else {
        dtde.rejectDrop();
      }
    }
    
    // A dumb collection.contains() alternative...
    public boolean doesCollectionContain(Collection<SudokuSquare> squares, SudokuSquare square) {
      orf: // Wow, appearently the HashSet.contains() method is really, really stupid so I have to do this
        // in the stupidest way possible...
      if(squares != null) {
        for(SudokuSquare sq : squares) {
          if(sq.equals(square))
            break orf;
        }
        return false;
      } else {
        return false;
      }
      return true;
    }
  }
  
  
  /**
   * This is a private helper method that updates the GUI/view
   * to match any changes to the model
   */
  private void update() {
    for (int row=0; row<numRows; row++) {
      for (int col=0; col<numCols; col++) {
        // Handle the highlighting of buttons
        
        if (row == currentRow && col == currentCol && sudoku.isBlank(row, col)) {
          // draw this grid square special!
          // this is the grid square we are trying to enter value into
          buttons[row][col].setForeground(Color.RED);
          // I can't figure out how to change the background color of a grid square, ugh
          // Maybe I should have used JLabel instead of JButton?
          buttons[row][col].setBackground(Color.CYAN);
          setText(row, col, "_");
        } else if(buttons[row][col].isHighlighted) {
          buttons[row][col].setHighlightColor(buttons[row][col].highlightColor);
        } else {
          buttons[row][col].setBackground(BACKGROUND_COLOR);
          
          buttons[row][col].setForeground(FONT_COLOR);
          int val = sudoku.get(row, col);
          if (val == 0) {
            setText(row, col, "");
          } else {
            setText(row, col, val+"");
          }
        }
      }
    }
    repaint();
  }


  private void createMenuBar() {
    menuBar = new JMenuBar();

    //
    // File menu
    //
    JMenu file = new JMenu("File");
    menuBar.add(file);

    addToMenu(file, "New Game", new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        sudoku.load("easy1.txt");
        discoTimer.stop(); // Power down the disco machine
        unhighlightAll();
        for(HButton[] row : buttons) {
          for(HButton button : row) {
            button.setHighlightColor(Color.RED);
          }
        }
        swapsRemaining = 2;
        repaint();
        update();
      }
    });

    addToMenu(file, "Save", new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) { // Save to a file
        sharedFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Plain text", "txt");
        sharedFileChooser.setFileFilter(filter);
        if(sharedFileChooser.showSaveDialog(file) == JFileChooser.APPROVE_OPTION) {
          String path = sharedFileChooser.getSelectedFile().getAbsolutePath();
          if(path.lastIndexOf(".txt") == -1) { // Correct as .txt file.
            path = path + ".txt";
          }
          Util.writeToFile(path, sudoku.toSaveString());
        }
        
        repaint();
      }
    });

    addToMenu(file, "Load", new ActionListener() { // Load from a file
      @Override
      public void actionPerformed(ActionEvent e) {
        sharedFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Plain text", "txt");
        sharedFileChooser.setFileFilter(filter);
        if(sharedFileChooser.showOpenDialog(file) == JFileChooser.APPROVE_OPTION) {
          String path = sharedFileChooser.getSelectedFile().getAbsolutePath();
          sudoku.load(path);
        }
        repaint();
        update();
      }
    });

    
    JMenu view = new JMenu("View");
    menuBar.add(view);
    /* Toggle for a menu of only legal values upon click */
    JMenuItem legalsOnly = new JCheckBoxMenuItem("Toggle Legal Values Only");
    view.add(legalsOnly);
    legalsOnly.addItemListener(new ItemListener() {
      @Override
      public void itemStateChanged(ItemEvent e) { // Toggle for legal values only
        if(e.getStateChange() == 1)
          enableLegalValuesOnly = true;
        else
          enableLegalValuesOnly = false;
      }
    });
    /* When entering a value that is illegal via the keyboard, this feature will quickly highlight
     *  all other values that serve as reasons why it is illegal. */
    JMenuItem illegalHighlighting = new JCheckBoxMenuItem("Feature #1: Toggle Illegal Highlighting");
    illegalHighlighting.setSelected(true); // Turn it on by default
    view.add(illegalHighlighting);
    illegalHighlighting.addItemListener(new ItemListener() {
      @Override
      public void itemStateChanged(ItemEvent e) { // Toggle for legal values only
        if(e.getStateChange() == 1)
          enableIllegalHighlighting = true;
        else
          enableIllegalHighlighting = false;
      }
    });
    
    //
    // Help menu
    //
    JMenu help = new JMenu("Help");
    menuBar.add(help);
    
    // TODO: You will get two swaps. This will show how many swaps remaining, as well...
    //menuBar.add(new JButton("Activate Swap Mode"));

    addToMenu(help, "Hint", new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        unhighlightAll();
        Collection<SudokuSquare> list = sudoku.getMostContrainedSquares();
        if(list.size() == 0) {
          // Leaving this up to the user will give a more "natural" feel.
          // The user will eventually give up and press the hint button, only to find that
          // there are no more moves left. This is also a rather heavy-weight function
          // to be called often.
          JOptionPane.showMessageDialog(null, "No more moves left!");
          return;
        }
        // Randomly choose from all the equally viable hints...
        // Help from https://stackoverflow.com/a/363692/16386050
        // Note the "+1" is implicitly included because size = max array offset + 1
        int choice = ThreadLocalRandom.current().nextInt(0, list.size()); 
        SudokuSquare sq = (SudokuSquare) list.toArray()[choice];
        buttons[sq.row][sq.col].setHighlighted(true);
        update();
      }
    });

    /* SuperSwap! */
    /* Note that once you activate SuperSwap you cannot deactivate it, and must swap something with
     * something else. This is by design because users could peek at the way it highlights the board, and
     * then simply turn it back off. */
    JButton superSwapButton = new JButton("Activate Super Swap! (" + swapsRemaining + " remaining)");
    superSwapButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        if(swapsRemaining <= 0 || isInSuperSwapMode)
          return;
        else
          isInSuperSwapMode = true; // This is only to make sure this button isn't pressed twice
          --swapsRemaining;
        superSwapButton.setText("Activate Super Swap! (" + swapsRemaining + " remaining)");
        // Start wiggling all non-empty buttons...
        for(int c = 0; c < 9; ++c) {
          for(int r = 0; r < 9; ++r) {
            if(! sudoku.isBlank(r, c)) {
              buttons[r][c].enableWiggleMode();
            }
          }
        }
      }
    });
    menuBar.add(superSwapButton);
    
    this.setJMenuBar(menuBar);
  }


  /**
   * Private helper method to put 
   * 
   * @param menu
   * @param title
   * @param listener
   */
  private void addToMenu(JMenu menu, String title, ActionListener listener) {
    JMenuItem menuItem = new JMenuItem(title);
    menu.add(menuItem);
    menuItem.addActionListener(listener);
  }

  private void createMouseHandler() {
    MouseAdapter a = new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        System.out.printf("%s\n", e.getButton());
      }

    };
    this.addMouseMotionListener(a);
    this.addMouseListener(a);
  }


  private void createKeyboardHandlers() {
    for (int r=0; r<buttons.length; r++) {
      for (int c=0; c<buttons[r].length; c++) {
        buttons[r][c].addKeyListener(new MyKeyListener(r, c, sudoku));
        /*
    			buttons[r][c].addKeyListener(new KeyAdapter() {
    				@Override
    				public void keyTyped(KeyEvent e) {
    					char key = e.getKeyChar();
    					System.out.println(key);
    					if (Character.isDigit(key)) {
    						System.out.println(key);
    						if (currentRow > -1 && currentCol > -1) {
    							guess = Integer.parseInt(key + "");
    						}
    					}

    				}
    			});
         */
      }
    }
    /*
    	this.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent e) {
				char key = e.getKeyChar();
				System.out.println(key);
				if (Character.isDigit(key)) {
					System.out.println(key);
					if (currentRow > -1 && currentCol > -1) {
						guess = Integer.parseInt(key + "");
					}
				}

			}
		});
     */
  }

  public SudokuGUI() {
    sudoku = new Sudoku();
    // load a puzzle from a text file
    // right now we only have 1 puzzle, but we could always add more!
    sudoku.load("easy1.txt");

    setTitle("Sudoku!");

    this.setSize(width, height);

    // the JPanel where everything gets painted
    panel = new JPanel();
    // set up a 9x9 grid layout, since sudoku boards are 9x9
    panel.setLayout(new GridLayout(9, 9));
    // set the preferred size
    // If we don't do this, often the window will be minimized
    // This is a weird quirk of Java GUIs
    panel.setPreferredSize(new Dimension(width, height));

    // This sets up 81 JButtons (9 rows * 9 columns)
    for (int r=0; r<numRows; r++) {
      for (int c=0; c<numCols; c++) {
        HButton b = new HButton();
        b.setPreferredSize(new Dimension(squareSize, squareSize));

        b.setFont(FONT);
        b.setForeground(FONT_COLOR);
        b.setBackground(BUTTON_COLOR);
        buttons[r][c] = b;
        // add the button to the canvas
        // the layout manager (the 9x9 GridLayout from a few lines earlier)
        // will make sure we get a 9x9 grid of these buttons
        panel.add(b);

        // thicker borders in some places
        // sudoku boards use 3x3 sub-grids
        int top = 1;
        int left = 1;
        int right = 1;
        int bottom = 1;
        if (r % 3 == 2) {
          bottom = 5;
        }
        if (c % 3 == 2) {
          right = 5;
        }
        if (r == 0) {
          top = 5;
        }
        if (c == 9) {
          bottom = 5;
        }
        b.setBorder(BorderFactory.createMatteBorder(top, left, bottom, right, Color.black));
        b.setOpaque(true); // To change the background color of a JButton, it had to be opaque. https://stackoverflow.com/a/4173549/16386050
        //
        // button handlers!
        //
        // check the ButtonListener class to see what this does
        //
        b.addActionListener(new ButtonListener(r, c, sudoku));
      }
    }

    this.getContentPane().add(panel, BorderLayout.CENTER);
    this.setPreferredSize(new Dimension(width, height));
    this.setResizable(true);
    this.pack();
    this.setLocation(100,100);
    this.setFocusable(true);

    createMenuBar();
    createKeyboardHandlers();
    createMouseHandler();

    // close the GUI application when people click the X to close the window
    this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

    /* Initialize disco machine */
    discoTimer = new Timer(250, new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        disco();
      }
    });
    
    update();
    repaint();
  }

  public static void main(String[] args) {
    SudokuGUI g = new SudokuGUI();
    g.setVisible(true);
  }

}
