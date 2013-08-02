import java.lang.*;
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
		
	public static void main(String args[]) {
		ChessBoard activeBoard = new ChessBoard();
		activeBoard.display();
		// activeBoard.play();
	}
	
	
	ChessBoard() {
		activePlayer = WHITE;
		char ch;
		String s;
		// Hashmap to store column names on chess board
		for (int i = 1; i <=8; i++) {
			ch = (char) ((int) 'a'+i-1);
			s = String.valueOf(ch);
			letterMap.put(s, new Integer(i));
		}
		
		board = new ChessPiece[9][9];
		board[1][letterMap.get("a").intValue()] = new ChessPiece('R'); // rook
		board[1][letterMap.get("b")] = new ChessPiece('K'); // knight
		board[1][letterMap.get("c")] = new ChessPiece('B'); // bishop
		board[1][letterMap.get("d")] = new ChessPiece('K'); // king
		board[1][letterMap.get("e")] = new ChessPiece('Q'); // queen
		board[1][letterMap.get("f")] = new ChessPiece('B'); // bishop
		board[1][letterMap.get("g")] = new ChessPiece('K'); // knight
		board[1][letterMap.get("h")] = new ChessPiece('R'); // rook
		for (int r = 2; r < 8; r++) {
			for (int c = 1; c<= 8; c++) {
				if (r == 2 || r == 7)
					board[r][c] = new ChessPiece('P');
				else
					board[r][c] = new ChessPiece();
			}
		}
		board[8][letterMap.get("a")] = new ChessPiece('R'); // rook
		board[8][letterMap.get("b")] = new ChessPiece('K'); // knight
		board[8][letterMap.get("c")] = new ChessPiece('B'); // bishop
		board[8][letterMap.get("d")] = new ChessPiece('K'); // king
		board[8][letterMap.get("e")] = new ChessPiece('Q'); // queen
		board[8][letterMap.get("f")] = new ChessPiece('B'); // bishop
		board[8][letterMap.get("g")] = new ChessPiece('K'); // knight
		board[8][letterMap.get("h")] = new ChessPiece('R'); // rook	}
	}
	
	void play() {
		// TEMP repeat while game is still going on, until one of players enters "QUIT"
		// get move input
		// if move is valid...
		// perform move
		makeMove("A1","A5");
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
		int fromR = letterMap.get(f.toLowerCase().charAt(0));
		int fromC = Integer.parseInt(f.substring(1, f.length()));
		int toR = letterMap.get(t.toLowerCase().charAt(0));
		int toC = Integer.parseInt(f.substring(1, t.length()));
		// TEMP fill in logic for moving
		if (board[fromR][fromC].validMove(fromR, fromC, toR, toC)) {
			board[toR][toC] = board[fromR][fromC];
			board[fromR][fromC] = null;
			return true;
		} else return false;
	}
	
	class ChessPiece {
		
		boolean empty;
		char pieceName;
		
		// constructor
		ChessPiece (char n) {
			pieceName = n;
			empty = false;
			// TEMP - will override in each subclass
		}
		
		ChessPiece () {
			empty = true;
			// TEMP - will override in each subclass
		}
		
		// toString for printing board
		public String toString() {
			if(empty)
				return "";
			else
				return Character.toString(pieceName);
		}
		
		boolean isEmpty() {
			return empty;
		}
		/* method: validMove(from, to)
		 * returns whether something is a valid move for this particular piece
		 */
		 boolean validMove (int fr, int fc, int tr, int tc) {
			return true;
			// TEMP fill with logic for whether something is a valid move
		}
	}
}

