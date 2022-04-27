Completed by Justin Douty

Note: I didn't really make a pull request because noone else did...  
Feature #1 is located in the "View" menu in the menu bar, labled with "Feature #1: Toggle Illegal Highlighting"  
I refrained from adding popup dialogs anywhere within the game because I found that they would be too distracting, and I wanted to make the game have a minimalist, lightweight, and paper-like feel. Rather than showing a popup dialog when a user tries to enter a value in an illegal position, I added Feature #1, which highlights any tiles within the same 3x3 grid, row or column that make that illegal. For an extra lightweight feel, this can be toggled on or off in the View menu, although it is on by default. When off, entering an illegal value will simply have no effect, implying that a user should look more carefully and try to enter a legal value. This feature does not apply when using "Legal Values Only" mode because it is impossible to enter an value in an illegal position in this mode.
Feature #2 is a button located within the menu bar that activates Super Swap.  
This feature activates a mode in which you can swap one tile with another tile in a legal position on the board, highlighted in green.  
Once activated, click and drag a wiggling tile over a green tile to swap it with that tile. You can only do this twice per game.  

Original:

TODO for this assignment
--

Disallow entering illegal values
== 
* So, if you try to enter a 7 when there is already a 7 in the same row, column, or 3x3 grid, it should give an error message instead
* You can use a `JOptionPane.showMessageDialog`
* This should use the Sudoku model class to check for this. Probably you should add a new method!

`Help => Hint`
== 
* Choosing this menu option should highlight any squares that can only contain one possible value

Victory!
==
* If you have won it should let you know and end the game!

Only Legal values
==
* Figure out some way to show only the legal values for a square. This can be a special keyboard
	input (like if you click on an empty square and then click 'h' it will show you the legal
	values somehow, either through a `JOptionPane.showMessageDialog` or even something like
	a `ListSelectionModel`)

Load/Save games
==
* Set up the load/save menu options so that they can load and save a game
* To save a game, write the board to a text file. Use a `JFileChooser` to decide what file to write to
* To load a game, use a `JFileChooser` to load a game from a text file
