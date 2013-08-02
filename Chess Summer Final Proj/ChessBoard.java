import java.io.*;
import java.util.*;

/* Chess Board
 * 
 * Contains code for Chess Board
 */

class ChessBoard {
	// activePlayer shows who is playing
	boolean activePlayer;
	// constants WHITE and BLACK represent two players
	static boolean WHITE = false;
	static boolean BLACK = true;
	
	// board holds pointers to all the ChessPiece objects
	ChessPiece board[][];
	HashMap<String, Integer> letterMap = new HashMap<String, Integer>();
		
	public static void main(String args[]) throws IOException {
		ChessBoard activeBoard = new ChessBoard();
		activeBoard.display();
		activeBoard.play();
	}
	
	ChessBoard() {
		activePlayer = WHITE;
		char ch;
		String s;
		// HashMap to store column names on chess board
		for (int i = 1; i <=8; i++) {
			ch = (char) ((int) 'a'+i-1);
			s = String.valueOf(ch);
			letterMap.put(s, new Integer(i));
		}
		
		board = new ChessPiece[9][9];
		board[1][letterMap.get("a").intValue()] = new Rook();
		board[1][letterMap.get("b")] = new Knight();
		board[1][letterMap.get("c")] = new Bishop();
		board[1][letterMap.get("d")] = new King();
		board[1][letterMap.get("e")] = new Queen();
		board[1][letterMap.get("f")] = new Bishop();
		board[1][letterMap.get("g")] = new Knight();
		board[1][letterMap.get("h")] = new Rook();
		for (int r = 2; r < 8; r++) {
			for (int c = 1; c<= 8; c++) {
				if (r == 2 || r == 7)
					board[r][c] = new Pawn();
				else
					board[r][c] = new ChessPiece();
			}
		}
		board[8][letterMap.get("a").intValue()] = new Rook();
		board[8][letterMap.get("b")] = new Knight();
		board[8][letterMap.get("c")] = new Bishop();
		board[8][letterMap.get("d")] = new King();
		board[8][letterMap.get("e")] = new Queen();
		board[8][letterMap.get("f")] = new Bishop();
		board[8][letterMap.get("g")] = new Knight();
		board[8][letterMap.get("h")] = new Rook();
	}
	
	void play() throws IOException {
		// TEMP repeat while game is still going on, until one of players enters "QUIT"
		// get move input
		// if move is valid...
		// perform move
		BufferedReader stream = new BufferedReader(new InputStreamReader(System.in));
		String s1 = stream.readLine();
		String s2 = stream.readLine();
		if (makeMove(s1,s2))
			System.out.println("Move successful");
		else
			System.out.println("Attempted bad move");
		display();
		// display board
		// if check or checkmate, say so
	}
	
	void display() {
		for (int r = 8; r >= 1; r--) {
			for (int c = 1; c <= 8; c++) {
				if (board[r][c].isEmpty())
					System.out.print(" - ");
				else
					System.out.print(" "+board[r][c]+" ");
			}
			System.out.println();
		}
		System.out.println();
	}
	
	// hasCheck checks whether current player has a check
	boolean hasCheck() {
		return false;
		// TEMP fill with logic for checking for check
	}
	
	// hasCheck checks whether current player has a checkmate
	boolean hasCheckmate() {
		return false;
		// TEMP fill with logic for checking for checkmate
	}
	
	// makeMove tries to make a move and returns true if move is made or false if not
	boolean makeMove(String f, String t) {
		// parse the strings to get the positions indicated
		int fromR = Integer.parseInt(f.substring(1, f.length()));
		int fromC = letterMap.get(f.toLowerCase().substring(0,1));
		int toR = Integer.parseInt(t.substring(1, t.length()));
		int toC = letterMap.get(t.toLowerCase().substring(0,1));
		// TEMP fill in more logic for moving
		System.out.println("Move from ("+fromR+", "+fromC+") to ("+toR+", "+toC+").");
		if (board[fromR][fromC].validMove(fromR, fromC, toR, toC)) {
			ChessPiece temp = board[toR][toC];
			board[toR][toC] = board[fromR][fromC];
			board[fromR][fromC] = temp;
			
			/* output for testing
 			System.out.println(temp);
			System.out.println(board[fromR][fromC]);
			System.out.println(board[toR][toC]);
			*/
			
			return true;
		} else return false;
	}
	
	class ChessPiece {
		char pieceName;
		
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
		
		/* method: validMove(from, to)
		 * returns whether something is a valid move for this particular piece
		 */
		 boolean validMove (int fr, int fc, int tr, int tc) {
			return false;
		}
	}
	
	class King extends ChessPiece {
		King() {
			pieceName = 'K';
		}
		// king can move exactly one vacant square in any direction
		boolean validMove (int fr, int fc, int tr, int tc) {
			if ((Math.abs(fr - tr) == 1) && (Math.abs(fc - tc) == 0))
				return true;
			else if ((Math.abs(fr - tr) == 0) && (Math.abs(fc - tc) == 1))
				return true;
			else
				return false;
			// TEMP add possibility of castling
		}
	}
	class Queen extends ChessPiece {
		Queen() {
			pieceName = 'Q';
		}
		boolean validMove (int fr, int fc, int tr, int tc) {
			// can go in diagonals
			if (fr - tr == fc - tc)
				return true;
			// can also go in straight lines
			else if ((fr == tr) || (fc == tc))
				return true;
			else
				return false;
		}
	}
	class Knight extends ChessPiece {
		Knight() {
			pieceName = 'N';
		}
		boolean validMove (int fr, int fc, int tr, int tc) {
			if ((Math.abs(fr - tr) == 2) && (Math.abs(fc - tc) == 1))
				return true;
			else if ((Math.abs(fr - tr) == 1) && (Math.abs(fc - tc) == 2))
				return true;
			else
				return false;
		}
	}
	class Rook extends ChessPiece {
		Rook() {
			pieceName = 'R';
		}
		boolean validMove (int fr, int fc, int tr, int tc) {
			if ((fr == tr) || (fc == tc))
				return true;
			else
				return false;
		}
	}
	class Bishop extends ChessPiece {
		Bishop() {
			pieceName = 'B';
		}
		boolean validMove (int fr, int fc, int tr, int tc) {
			if (fr - tr == fc - tc)
				return true;
			else
				return false;
		}
	}
	class Pawn extends ChessPiece {
		boolean hasMoved;
		Pawn() {
			pieceName = 'P';
			hasMoved = false;
		}
		boolean validMove (int fr, int fc, int tr, int tc) {
			if ((tr - fr == 1) && (fc == tc))
				return true;
			else
				return false;
			// TEMP need to account for ability to move two in first move
		}
	}

}

