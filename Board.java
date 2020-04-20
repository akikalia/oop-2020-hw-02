// Board.java

/**
 CS108 Tetris Board.
 Represents a Tetris board -- essentially a 2-d grid
 of booleans. Supports tetris pieces and row clearing.
 Has an "undo" feature that allows clients to add and remove pieces efficiently.
 Does not do any drawing or have any idea of pixels. Instead,
 just represents the abstract 2-d board.
*/
public class Board	{
	// Some ivars are stubbed out for you:
	private int width;
	private int height;
	private int maxHeight;
	private int backupWidth;
	private int backupHeight;
	private int backupMaxHeight;
	private boolean[][] grid;
	private boolean[][] backupGrid;
	private boolean DEBUG = true;
	boolean committed;

	private int[] heights;
	private int[] widths;
	private int[] backupHeights;
	private int[] backupWidths;

	// Here a few trivial methods are provided:
	
	/**
	 Creates an empty board of the given width and height
	 measured in blocks.
	*/
	public Board(int width, int height) {
		this.width = width;
		this.height = height;
		grid = new boolean[width][height];
		committed = true;

		maxHeight = 0;

		heights = new int[width];
		widths = new int[height];
	}
	
	
	/**
	 Returns the width of the board in blocks.
	*/
	public int getWidth() {
		return width;
	}
	
	
	/**
	 Returns the height of the board in blocks.
	*/
	public int getHeight() {
		return height;
	}
	
	
	/**
	 Returns the max column height present in the board.
	 For an empty board this is 0.
	*/
	public int getMaxHeight() {	 
		return maxHeight; // YOUR CODE HERE
	}
	
	
	/**
	 Checks the board for internal consistency -- used
	 for debugging.
	*/
	public void sanityCheck() {
		if (DEBUG) {
			//System.out.println(toString());
			int []heightsCurr = new int[width];
			int heightMax = 0;
			int widthCurr;
			for (int y = height -1 ; y >= 0; y--) {
				widthCurr = 0;
				for (int x = width - 1; x >= 0; x--) {
					if (grid[x][y]) {
						if (y+1 > heightsCurr[x])
							heightsCurr[x] = y+1;
						widthCurr++;
					}
				}
				if (widthCurr != widths[y])
					throw new RuntimeException("widths Mismatch");
			}
			for (int i = 0; i< width; i++){
				if (heightsCurr[i] > heightMax)
					heightMax = heightsCurr[i];
				if (heightsCurr[i] != heights[i])
					throw new RuntimeException("hights Mismatch");
			}
			if (maxHeight != heightMax)
				throw new RuntimeException("maxHeight Mismatch");
		}
	}

	/**
	 Given a piece and an x, returns the y
	 value where the piece would come to rest
	 if it were dropped straight down at that x.
	 
	 <p>
	 Implementation: use the skirt and the col heights
	 to compute this fast -- O(skirt length).
	*/
	public int dropHeight(Piece piece, int x) {
		int skirt[] = piece.getSkirt();
		int maxVal = Integer.MIN_VALUE;
		int maxInd = -1;
		for (int i = 0; i < skirt.length && x+i < width; i++){
			int val =  heights[x+i] - skirt[i];
			if (val > maxVal) {
				maxVal = val;
				maxInd = i;
			}
		}
		return heights[maxInd]; // YOUR CODE HERE
	}
	
	
	/**
	 Returns the height of the given column --
	 i.e. the y value of the highest block + 1.
	 The height is 0 if the column contains no blocks.
	*/
	public int getColumnHeight(int x) {
		return heights[x]; // YOUR CODE HERE
	}
	
	
	/**
	 Returns the number of filled blocks in
	 the given row.
	*/
	public int getRowWidth(int y) {
		 return widths[y]; // YOUR CODE HERE
	}
	
	
	/**
	 Returns true if the given block is filled in the board.
	 Blocks outside of the valid width/height area
	 always return true.
	*/
	public boolean getGrid(int x, int y) {
		if (x < width && x >= 0
				&& y < height && y >= 0)
			return grid[x][y];
		return true;
	}
	
	
	public static final int PLACE_OK = 0;
	public static final int PLACE_ROW_FILLED = 1;
	public static final int PLACE_OUT_BOUNDS = 2;
	public static final int PLACE_BAD = 3;


	void backup() {
		backupGrid = new boolean[width][height];
		backupHeights = new int[width];
		backupWidths = new int[height];


		for (int i = 0; i < width; i++)
			System.arraycopy(grid[i], 0, backupGrid[i], 0, height);
		System.arraycopy(heights, 0, backupHeights, 0, width);
		System.arraycopy(widths, 0, backupWidths, 0, height);
		backupHeight = height;
		backupWidth = width;
		backupMaxHeight = maxHeight;
	}

	private int check_bounds(Piece piece, int x, int y){
		TPoint [] body = piece.getBody();
		if (x < 0 || y < 0 || x + piece.getWidth() > width || y + piece.getHeight() > height)
			return PLACE_OUT_BOUNDS;

		for (int i = 0; i < body.length; i++) {
			if (getGrid(x + body[i].x, y + body[i].y))
				return PLACE_BAD;
		}
		return PLACE_OK;
	}

	/**
	 Attempts to add the body of a piece to the board.
	 Copies the piece blocks into the board grid.
	 Returns PLACE_OK for a regular placement, or PLACE_ROW_FILLED
	 for a regular placement that causes at least one row to be filled.
	 
	 <p>Error cases:
	 A placement may fail in two ways. First, if part of the piece may falls out
	 of bounds of the board, PLACE_OUT_BOUNDS is returned.
	 Or the placement may collide with existing blocks in the grid
	 in which case PLACE_BAD is returned.
	 In both error cases, the board may be left in an invalid
	 state. The client can use undo(), to recover the valid, pre-place state.
	*/
	public int place(Piece piece, int x, int y) {
		// flag !committed problem
		TPoint []body = piece.getBody();
		if (!committed) throw new RuntimeException("place commit problem");
		backup();
		committed = false;
		int result;
		result = check_bounds(piece, x, y);
		if (result > 0)
			return result;
		for (int i = 0; i < body.length; i++) {
			if (heights[x + body[i].x] < y + body[i].y + 1)
				heights[x + body[i].x] = y + body[i].y + 1;
			widths[y + body[i].y]++;
			grid[x + body[i].x][y + body[i].y] = true;
			if (widths[y + body[i].y] == width)
				result = PLACE_ROW_FILLED;
			if (maxHeight < heights[x + body[i].x])
				maxHeight = heights[x + body[i].x];
		}

		sanityCheck();
		//update widths and heights and return result according to widths being full
		return result;
	}

	private void shift(int y, int lines){
		if (lines == 0)
			return ;
		for (int i = 0;y + lines + i < height;i++){
			for (int n = 0; n < width;n++){
				grid[n][y + i] = grid[n][y + lines + i];
			}
			widths[y + i] = widths[y + lines + i];
		}
		while(lines > 0){
			for (int i = 0; i < width; i++)
				grid[i][height - lines] = false;
			widths[height - lines] = 0;
			lines--;
		}
	}

	/**
	 Deletes rows that are filled all the way across, moving
	 things above down. Returns the number of rows cleared.
	*/

	public int clearRows() {
		System.out.println(toString());
		int rowsCleared = 0;
		if (committed) {
			committed = false;
			backup();
		}

		for (int y = 0; y < height; y++)
		{
			int i;
			for (i = 0; y + i < height && getRowWidth(y + i) == width; i++){
			}
			shift(y, i);
			rowsCleared += i;
		}
		maxHeight = 0;
		for (int i = 0; i < width; i++) {
			heights[i] = 0;
			for (int m = 0;  m < height; m++) {
				if (grid[i][m])
					heights[i] = m + 1;
			}
			if (maxHeight < heights[i])
				maxHeight = heights[i];
		}
		sanityCheck();
		return rowsCleared;
	}

	/**
	 Reverts the board to its state before up to one place
	 and one clearRows();
	 If the conditions for undo() are not met, such as
	 calling undo() twice in a row, then the second undo() does nothing.
	 See the overview docs.
	*/
	public void undo() {
		if (committed)
			return ;
		Object temp;

		temp  = width;
		width = backupWidth;
		backupWidth = (Integer)temp;

		temp = height;
		height = backupHeight;
		backupHeight = (Integer)temp;

		temp = widths;
		widths = backupWidths;
		backupWidths = (int [])temp;

		temp = heights;
		heights = backupHeights;
		backupHeights = (int [])temp;

		temp = maxHeight;
		maxHeight = backupMaxHeight;
		backupMaxHeight = (Integer)temp;

		temp = grid;
		grid = backupGrid;
		backupGrid = (boolean [][])temp;
		commit();
		sanityCheck();
	}
	
	
	/**
	 Puts the board in the committed state.
	*/
	public void commit() {
		committed = true;
	}


	
	/*
	 Renders the board state as a big String, suitable for printing.
	 This is the sort of print-obj-state utility that can help see complex
	 state change over time.
	 (provided debugging utility) 
	 */
	public String toString() {
		StringBuilder buff = new StringBuilder();
		for (int y = height-1; y>=0; y--) {
			buff.append('|');
			for (int x=0; x<width; x++) {
				if (getGrid(x,y)) buff.append('+');
				else buff.append(' ');
			}
			buff.append("|\n");
		}
		for (int x=0; x<width+2; x++) buff.append('-');
		return(buff.toString());
	}
}


