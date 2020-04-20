import junit.framework.TestCase;


public class BoardTest extends TestCase {
	Board b0, b1, b2, b3;
	Piece pyr1, pyr2, pyr3, pyr4, s, sRotated;
	Piece p1, p2, p3, p4, p5,  m, mRotated;

	// This shows how to build things in setUp() to re-use
	// across tests.
	
	// In this case, setUp() makes shapes,
	// and also a 3X6 board, with pyr placed at the bottom,
	// ready to be used by tests.
	
	protected void setUp() throws Exception {
		b0 = new Board(3, 6);
		
		pyr1 = new Piece(Piece.PYRAMID_STR);
		pyr2 = pyr1.computeNextRotation();
		pyr3 = pyr2.computeNextRotation();
		pyr4 = pyr3.computeNextRotation();
		
		s = new Piece(Piece.S1_STR);
		sRotated = s.computeNextRotation();
		
		b0.place(pyr1, 0, 0);

		b1 = new Board(1, 1);

		b2 = new Board(5, 10);

		b3 = new Board(5, 8);

		p1 = new Piece(Piece.PYRAMID_STR);
		p2 = new Piece(Piece.S1_STR);
		p3 = new Piece(Piece.L1_STR);
		p4 = new Piece(Piece.STICK_STR);
		p5 = new Piece(Piece.SQUARE_STR);
	}
	
	// Check the basic width/height/max after the one placement
	public void testSample1() {
		assertEquals(1, b0.getColumnHeight(0));
		assertEquals(2, b0.getColumnHeight(1));
		assertEquals(2, b0.getMaxHeight());
		assertEquals(3, b0.getRowWidth(0));
		assertEquals(1, b0.getRowWidth(1));
		assertEquals(0, b0.getRowWidth(2));
	}
	
	// Place sRotated into the board, then check some measures
	public void testSample2() {
		b0.commit();
		int result = b0.place(sRotated, 1, 1);
		assertEquals(Board.PLACE_OK, result);
		assertEquals(1, b0.getColumnHeight(0));
		assertEquals(4, b0.getColumnHeight(1));
		assertEquals(3, b0.getColumnHeight(2));
		assertEquals(4, b0.getMaxHeight());
	}

	// Makre  more tests, by putting together longer series of
	// place, clearRows, undo, place ... checking a few col/row/max
	// numbers that the board looks right after the operations.
	public void testConstructor1() {
		assertEquals(0, b1.getColumnHeight(0));
		assertEquals(0, b1.getRowWidth(0));
		assertEquals(1, b1.getHeight());
		assertEquals(1, b1.getWidth());
		assertEquals(0, b1.getMaxHeight());
		assertEquals(false, b1.getGrid(0, 0));
		assertEquals(true, b1.getGrid(1, 1));
	}

	public void testConstructor2() {
		assertEquals(0, b2.getColumnHeight(0));
		assertEquals(0, b2.getColumnHeight(4));
		assertEquals(0, b2.getRowWidth(0));
		assertEquals(0, b2.getRowWidth(9));
		assertEquals(10, b2.getHeight());
		assertEquals(5, b2.getWidth());
		assertEquals(0, b2.getMaxHeight());
		assertEquals(false, b2.getGrid(0, 0));
		assertEquals(false, b2.getGrid(4, 9));
		assertEquals(true, b2.getGrid(-1, -1));
		assertEquals(true, b2.getGrid(5, 10));
	}

	public void testPlacement() {
		int err;
		b3.place(p1, 0, 0);
		b3.commit();
		err = b3.place(p1, 0, 0);
		assertTrue(err == Board.PLACE_BAD);
		b3.undo();
		err = b3.place(p1, -1, 0);
		assertTrue(err == Board.PLACE_OUT_BOUNDS);
		b3.undo();
		err = b3.place(p1, 0, -1);
		assertTrue(err == Board.PLACE_OUT_BOUNDS);
		b3.undo();
		err = b3.place(p1, 8, 4);
		assertTrue(err == Board.PLACE_OUT_BOUNDS);
		b3.undo();
		err = b3.place(p1, 7, 4);
		assertTrue(err == Board.PLACE_OUT_BOUNDS);
		b3.undo();
		assertTrue(b3.getColumnHeight(1) == 2);

	}



	public void testClearRows() {
		int ret;
		b3.place(p1, 0, 0);
		b3.commit();
		ret = b3.place(p5, 3, 0);
		assertTrue(ret == Board.PLACE_ROW_FILLED);
		ret = b3.clearRows();
		assertTrue(ret == 1);
		assertTrue(b3.getMaxHeight() == 1);
		assertTrue(b3.getRowWidth(0) == 3);
		assertTrue(b3.getRowWidth(1) == 0);
		b3.undo();
		assertTrue(b3.getMaxHeight() == 2);
		assertTrue(b3.getRowWidth(1) == 1);
		Piece line = (new Piece(Piece.STICK_STR));
		Piece line_horiz = line.computeNextRotation();

		b3.place(line,4,0);
		b3.commit();
		b3.place(line,4,4);
		b3.commit();
		b3.place(line_horiz,0,2);
		b3.commit();
		b3.place(line_horiz,0,3);
		b3.commit();
		b3.place(line_horiz,0,4);
		b3.commit();
		b3.place(line_horiz,0,5);
		b3.commit();
		b3.place(line_horiz,0,6);
		b3.commit();
		ret = b3.place(line_horiz,0,7);
		b3.commit();
		assertTrue(ret == Board.PLACE_ROW_FILLED);
		ret = b3.clearRows();
		assertTrue(ret == 6);
		ret = b3.clearRows();
		assertTrue(ret == 0);
		assertTrue(b3.getMaxHeight() == 2);
		assertTrue(b3.getRowWidth(0) == 4);
		assertTrue(b3.getRowWidth(1) == 2);
		assertTrue(b3.getRowWidth(2) == 0);
		b3.undo();
		assertTrue(b3.getMaxHeight() == 8);
		assertTrue(b3.getColumnHeight(0) == 8);
		assertTrue(b3.getRowWidth(1) == 2);

	}

	public void testUndoCommit() {
		b3.place(p1, 0 , 0);
		assertTrue(b3.getGrid(0, 0));
		b3.undo();
		assertFalse(b3.getGrid(0,0));
		b3.place(p1, 0, 0);
		b3.commit();
		b3.undo();
		assertTrue(b3.getGrid(0, 0));
		b3.commit();
		assertTrue(b3.getGrid(0, 0));

	}
	public void testDropHeight() {
		int placeHeight;
		placeHeight = b3.dropHeight(p1, 0);
		assertEquals(placeHeight, 0);
		b3.place(p1, 0, placeHeight);
		b3.commit();
		placeHeight = b3.dropHeight(p5, 2);
		assertEquals(placeHeight, 1);
		b3.place(p5, 2, placeHeight);
		b3.commit();
		placeHeight = b3.dropHeight(p2, 0);
		b3.place(p2, 0, placeHeight);
		b3.commit();
		assertEquals(placeHeight, 2);
		placeHeight = b3.dropHeight(p2, 0);
		b3.place(p2, 0, placeHeight);
		b3.commit();
		assertEquals(placeHeight, 4);
		placeHeight = b3.dropHeight(p2, 0);
		b3.place(p2, 0, placeHeight);
		b3.commit();
		assertEquals(placeHeight, 6);
	}
}
