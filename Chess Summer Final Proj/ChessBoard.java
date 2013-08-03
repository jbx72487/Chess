import java.io.*;
import java.util.*;

/* Chess Board
 * 
 * Contains code for Chess Board
 */

class ChessBoard {
	// activePlayer shows who is playing
	int activePlayer;
	// constants WHITE and BLACK represent two players
	static final int WHITE = 1;
	static final int BLACK = -1;
	
	// board holds pointers to all the ChessPiece objects
	ChessPiece board[][];
	
	// coordinates of two kings
	ChessCoord whiteKing;
	ChessCoord blackKing;
		
	public static void main(String args[]) throws IOException {
		new ChessBoard();
		// get move input
	}
	
	ChessBoard() throws IOException {
		activePlayer = WHITE;
		char ch;
		String s;
		
		// set up board and pieces
		board = new ChessPiece[9][9];
		board[1][1] = new Rook(WHITE);
		board[1][2] = new Knight(WHITE);
		board[1][3] = new Bishop(WHITE);
		board[1][4] = new King(WHITE);
		whiteKing = new ChessCoord(1,4);
		board[1][5] = new Queen(WHITE);
		board[1][6] = new Bishop(WHITE);
		board[1][7] = new Knight(WHITE);
		board[1][8] = new Rook(WHITE);
		for (int r = 2; r < 8; r++) {
			for (int c = 1; c<= 8; c++) {
				if (r == 7)
					board[r][c] = new Pawn(BLACK);
				else if (r == 2)
					board[r][c] = new Pawn(WHITE);
				else
					board[r][c] = new ChessPiece();
			}
		}
		board[8][1] = new Rook(BLACK);
		board[8][2] = new Knight(BLACK);
		board[8][3] = new Bishop(BLACK);
		board[8][4] = new King(BLACK);
		blackKing = new ChessCoord(1,4);
		board[8][5] = new Queen(BLACK);
		board[8][6] = new Bishop(BLACK);
		board[8][7] = new Knight(BLACK);
		board[8][8] = new Rook(BLACK);		
		// play chess!
		play();
	}
	
	
	ChessPiece getPiece(ChessCoord a) {
		return board[a.row][a.col];
	}
	
	ChessPiece getPiece(int r, int c) {
		return board[r][c];
	}
	
	void play() throws IOException {
		System.out.println("Welcome to Chess. Type \"QUIT\" at any time to quit the game.");
		display();
		BufferedReader stream = new BufferedReader(new InputStreamReader(System.in));
		String move;
		while (true) {
			move = getMove(stream);
			if (move.contains("quit")) break;
			
			// attempt to perform move
			if (makeMove(move)) {
				System.out.println("Move successful.");
				if (hasCheck())
					System.out.println("CHECK.");
				display();
				activePlayer = -1 * activePlayer;
			}
			else {
				System.out.println("Invalid move, please try again.");
				display();
			}
			// if check or checkmate, say so
		}
		System.out.println("Thanks for playing!");
	}
	
	String getMove(BufferedReader stream) throws IOException {
		System.out.print(activePlayer == WHITE ? "White" : "Black");
		System.out.print(", your turn. What is your move? Please enter in format \"a1 a2\"\n");
		return stream.readLine().toLowerCase();
	}
	
	void display() {
		// top row labels
		System.out.print("  ");
		for (int c = (int)('A'); c <= (int) ('H'); c++)
			System.out.print("  "+ (char) c +" ");
		System.out.println();
		// actual board
		for (int r = 8; r >= 1; r--) {
			System.out.print(r + " ");
			for (int c = 1; c <= 8; c++) {
				if (getPiece(r,c).isEmpty())
					System.out.print(" -- ");
				else {
					int color = getPiece(r,c).getColor() == BLACK ? 1 : 0;
					System.out.print(" "+ color + getPiece(r,c)+" ");
				}
			}
			System.out.print(" " + r);
			System.out.println();
		}
		// bottom row labels
		System.out.print("  ");
		for (int c = (int)('A'); c <= (int) ('H'); c++)
			System.out.print("  "+ (char) c +" ");
		System.out.println();
	}
	
	boolean lookFor (char n, int r, int c) {
		// checks if there is a ChessPiece that matches n at coordinate (r, c)
		if (r < 1 || r > 8 || c < 1 || c > 8)
			return false;
		else {
			return (getPiece(r, c).getName() == n);
		}
	}
	// hasCheck checks whether current player has a check
	boolean hasCheck() {
		// look for pawn 1 space diagonally in front
		// TEMP not sure if this is best way to check if everything is on board. instantiating a new object each time seems wasteful, but making an "onBoard(int r, int c)" seems redundant since there's already a ChessCoord method for similar purpose? 
		ChessCoord otherKing = activePlayer == WHITE ? blackKing : whiteKing;
		// look for rook horiz or vert in any direction
		
		// look for bishop diag in any direction
		// look for queen horiz, vert, or diag in any direction
		// look for king 1 space in any direction
		// look for knight an "L" away
		
		
		
		return false;
		// TEMP fill with logic for checking for check
	}
	
	// hasCheck checks whether current player has a checkmate
	boolean hasCheckmate() {
		return false;
		// TEMP fill with logic for checking for checkmate
	}
	
	// makeMove tries to make a move and returns true if move is made or false if not
	boolean makeMove(String m) throws IOException {
		// parse the strings to get the positions indicated
		// m is already lowercase by the time it gets here
		int fromR, fromC, toR, toC;
		try {
			fromR = Integer.parseInt(m.substring(1, 2));
			fromC = ((int) m.charAt(0) - (int) 'a') + 1;
			toR = Integer.parseInt(m.substring(4, 5));
			toC = ((int) m.charAt(3) - (int) 'a') + 1;
		}
		catch (Exception e) {return false;}
		
		ChessCoord from = new ChessCoord(fromR, fromC);
		ChessCoord to = new ChessCoord(toR, toC);
		
		
		// TEMP this output is for testing only
		System.out.println("Move from ("+fromR+", "+fromC+") to ("+toR+", "+toC+").");
		if (from.isValid() && to.isValid() && // both "from" and "to" spaces must be on the gameboard
				getPiece(from).getColor() == activePlayer &&// can only move your own piece
				!getPiece(from).isEmpty() && // "from" space must be occupied
				!(from.isEqual(to)) && // "from" and "to" can't be the same space
				getPiece(from).validMove(from, to)) { // must be valid move based on that gamepiece
			// if "to" space is empty, can just switch the "from" and "to"
			if (getPiece(to).isEmpty()) {
				ChessPiece temp = getPiece(to);
				board[toR][toC] = getPiece(from);
				board[fromR][fromC] = temp;
				// if pawn that hasn't moved before, mark it as moved
				if (getPiece(to).getName() == 'P') {
					Pawn p = (Pawn) getPiece(to);
					if (!p.hasMoved)
						p.move();
				}
				if (getPiece(to).getName() == 'K') {
					if (activePlayer == WHITE) {
						whiteKing.change(toR, toC);
					} else {
						blackKing.change(toR, toC);
					}
				}
			} else { // if the "to" isn't empty
				// if it's on your own side, can't make this move
				if (activePlayer == getPiece(toR,toC).getColor()) return false; // "to" space must be on other side
				System.out.println(getPiece(toR,toC)+" captured.");
				getPiece(to).remove();
				ChessPiece temp = getPiece(to);
				board[toR][toC] = getPiece(from);
				board[fromR][fromC] = temp;
			}
			
			// Promotion
			// If the pawn reaches a square on the back rank of the opponent, it promotes to the player's choice of a queen, rook, bishop, or knight
			if (getPiece(toR,toC).getName() == 'P' &&
					((getPiece(toR,toC).getColor() == WHITE && toR == 8) || (getPiece(toR,toC).getColor() == BLACK && toR == 1))) {
				System.out.println("Congratulations! Your pawn has reached the opposite end of the board. Would you like to replace it with a Queen, Rook, Bishop, or Knight?");
				BufferedReader stream = new BufferedReader(new InputStreamReader(System.in));
				String s;
				char choice;
				while (true) {
					System.out.print("Please enter your choice: Q, R, B, or N: ");
					s = stream.readLine();
					choice = s.toUpperCase().charAt(0);
					if (choice == 'Q' || choice == 'R' || choice == 'B' || choice == 'N')
						break;
				}
				switch (choice) {
				case 'Q': board[toR][toC] = new Queen(activePlayer); break;
				case 'R': board[toR][toC] = new Rook(activePlayer); break;
				case 'B': board[toR][toC] = new Bishop(activePlayer); break;
				case 'N': board[toR][toC] = new Knight(activePlayer); break;
				}
			}
			
			return true;
		} else return false;
	}
	
	class ChessCoord {
		int row;
		int col;
		
		ChessCoord(int r, int c) {
			row = r;
			col = c;
		}
		
		boolean isValid() {
			// checks to make sure a point is on board
			return (row >= 1 && row <= 8 &&
					col >= 1 && col <= 8);
		}
		
		boolean isEqual(ChessCoord a) {
			return (row == a.row && col == a.col);
		}
		
		void change(int r, int c) {
			row = r;
			col = c;
		}
	}
	
	class ChessPiece {
		char pieceName;
		int color;
		
		ChessPiece () {
			// if empty, set pieceName to be null
			pieceName = ' ';
		}
		
		// toString for printing board
		public String toString() {
			if(isEmpty())
				return "";
			else
				return Character.toString(pieceName);
		}
		
		boolean isEmpty() {
			return (pieceName == ' ');
		}
		
		 		 
		 boolean validMove (ChessCoord f, ChessCoord t) {
			// returns whether something is a valid move for this particular piece
			return false;
		}
		 
		char getName() {
			return pieceName;
		}
		 
		int getColor() {
			return color;
		}
		
		void remove() {
			pieceName = ' ';
		}
	}
	
	class King extends ChessPiece {
		King(int c) {
			pieceName = 'K';
			color = c;
		}
		// king can move exactly one vacant square in any direction
		boolean validMove (ChessCoord f, ChessCoord t) {
			if (((Math.abs(f.row - t.row) == 1) && (Math.abs(f.col - t.col) == 0)) || ((Math.abs(f.row - t.row) == 0) && (Math.abs(f.col - t.col) == 1)) ||
					(Math.abs(f.row - t.row) == 1 && Math.abs(f.col - t.col) == 1))
				return true;
			else
				return false;
			// TEMP add possibility of castling
		} 
	}
	class Queen extends ChessPiece {
		Queen(int c) {
			pieceName = 'Q';
			color = c;
		}
		boolean validMove (ChessCoord f, ChessCoord t) {
			// can go in diagonals or straight lines
			if ((t.col - f.col)/(t.row - f.row) == 1) {
				// if positive slope
				int c = Math.min(f.col, t.col) + 1;
				for (int r = Math.min(f.row,t.row) + 1; r < Math.max(f.row, t.row); r++) {
					if (!getPiece(r,c).isEmpty()) return false;
					c++;
				}
				return true;
			} else if ((t.col - f.col)/(t.row - f.row) == -1) {
				// if negative slope
				int c = Math.min(f.col, t.col) + 1;
				for (int r = Math.max(f.row,t.row) - 1; r > Math.min(f.row, t.row); r--) {
					if (!getPiece(r,c).isEmpty()) return false;
					c++;
				}
				return true;
			} else if (t.row == f.row) {
				// if going horiz, check for pieces in between
				for (int c = Math.min(f.col, t.col)+1; c < Math.max(f.col, t.col); c++)
					if (!getPiece(f.row,c).isEmpty()) return false;
				return true;
			} else if (t.col == f.col) {
				// if going vert, check for pieces in between
				for (int r = Math.min(f.row, t.row)+1; r < Math.max(f.row, t.row); r++)
					if (!getPiece(r,f.col).isEmpty()) return false;
				return true;
			} else
				return false;
		}
	}
	class Knight extends ChessPiece {
		Knight(int c) {
			pieceName = 'N';
			color = c;
		}
		boolean validMove (ChessCoord f, ChessCoord t) {
			if (((Math.abs(t.row - f.row) == 2) && (Math.abs(t.col - f.col) == 1)) ||
					((Math.abs(t.row - f.row) == 1) && (Math.abs(t.col - f.col) == 2)))
				return true;
			else
				return false;
		}
	}
	class Rook extends ChessPiece {
		Rook(int c) {
			pieceName = 'R';
			color = c;
		}
		boolean validMove (ChessCoord f, ChessCoord t) {
			if (t.row == t.row) {
				// if going horiz, check for pieces in between
				for (int c = Math.min(f.col, t.col)+1; c < Math.max(f.col, t.col); c++)
					if (!getPiece(f.row,c).isEmpty()) return false;
				return true;
			} else if (t.col == f.col) {
				// if going vert, check for pieces in between
				for (int r = Math.min(f.row, t.row)+1; r < Math.max(f.row, t.row); r++)
					if (!getPiece(r,f.col).isEmpty()) return false;
				return true;
			} else
				return false;
		}
	}
	class Bishop extends ChessPiece {
		Bishop(int c) {
			pieceName = 'B';
			color = c;
		}
		boolean validMove (ChessCoord f, ChessCoord t) {
			if ((f.col - t.col)/(f.row - t.row) == 1) {
				// if positive slope
				int c = Math.min(f.col, t.col) + 1;
				for (int r = Math.min(f.row,t.row) + 1; r < Math.max(f.row, t.row); r++) {
					if (!getPiece(r,c).isEmpty()) return false;
					c++;
				}
				return true;
			} else if ((f.col - t.col)/(f.row - t.row) == -1) {
				// if negative slope
				int c = Math.min(f.col, t.col) + 1;
				for (int r = Math.max(f.row,t.row) - 1; r > Math.min(f.row, t.row); r--) {
					if (!getPiece(r,c).isEmpty()) return false;
					c++;
				}
				return true;
			} else
				return false;
		}
	}
	class Pawn extends ChessPiece {
		boolean hasMoved;
		Pawn(int c) {
			pieceName = 'P';
			hasMoved = false;
			color = c;
		}
		void move() {
			hasMoved = true;
		}
		boolean validMove (ChessCoord f, ChessCoord t) {
			// if moving forward 1 in the same column and the target is empty, return true
			if ((t.row - f.row == color) && (f.col == t.col) && getPiece(t).isEmpty())
				return true;
			// if moving forward 1 and sideways 1 and the target is not empty, return true
			else if ((t.row - f.row == color) && (Math.abs(t.col-f.col) == 1) && !getPiece(t).isEmpty())
				return true;
			// if it's this pawn's fist move and moving forward 2 in the same column and the target is empty, return true
			else if (!hasMoved && (t.row - f.row == color*2) && (f.col == t.col) && getPiece(t).isEmpty())
				return true;
			else
				return false;
			// TEMP need to account for ability to move two in first move
		}
	}

}

