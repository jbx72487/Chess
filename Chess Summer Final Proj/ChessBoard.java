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
		board[1][letterMap.get("a").intValue()] = new Rook(WHITE);
		board[1][letterMap.get("b")] = new Knight(WHITE);
		board[1][letterMap.get("c")] = new Bishop(WHITE);
		board[1][letterMap.get("d")] = new King(WHITE);
		board[1][letterMap.get("e")] = new Queen(WHITE);
		board[1][letterMap.get("f")] = new Bishop(WHITE);
		board[1][letterMap.get("g")] = new Knight(WHITE);
		board[1][letterMap.get("h")] = new Rook(WHITE);
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
		board[8][letterMap.get("a").intValue()] = new Rook(BLACK);
		board[8][letterMap.get("b")] = new Knight(BLACK);
		board[8][letterMap.get("c")] = new Bishop(BLACK);
		board[8][letterMap.get("d")] = new King(BLACK);
		board[8][letterMap.get("e")] = new Queen(BLACK);
		board[8][letterMap.get("f")] = new Bishop(BLACK);
		board[8][letterMap.get("g")] = new Knight(BLACK);
		board[8][letterMap.get("h")] = new Rook(BLACK);
	}
	
	void play() throws IOException {
		// TEMP repeat while game is still going on, until one of players enters "QUIT"
		// get move input
		BufferedReader stream = new BufferedReader(new InputStreamReader(System.in));
		String s1;
		String s2;
		while (true) {
			System.out.println("From: ");
			s1 = stream.readLine();
			if (s1 == "QUIT") break;
			System.out.println("To: ");
			s2 = stream.readLine();
			if (s2 == "QUIT") break;
			// attempt to perform move
			if (makeMove(s1,s2)) {
				System.out.println("Move successful.");
				// if successful, display new board
				display();
			}
			else {
				System.out.println("Invalid move, please try again.");
				display();
			}
		}
		
		// if check or checkmate, say so
	}
	
	void display() {
		for (int r = 8; r >= 1; r--) {
			for (int c = 1; c <= 8; c++) {
				if (board[r][c].isEmpty())
					System.out.print(" -- ");
				else {
					int color = board[r][c].getColor() == BLACK ? 1 : 0;
					System.out.print(" "+ color + board[r][c]+" ");
				}
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
		int fromR, fromC, toR, toC;
		try {
			fromR = Integer.parseInt(f.substring(1, f.length()));
			fromC = letterMap.get(f.toLowerCase().substring(0,1));
			toR = Integer.parseInt(t.substring(1, t.length()));
			toC = letterMap.get(t.toLowerCase().substring(0,1));
		}
		catch (Exception e) {return false;}
		// TEMP this output is for testing only
		System.out.println("Move from ("+fromR+", "+fromC+") to ("+toR+", "+toC+").");
		if (onBoard(fromR, fromC) && onBoard(toR, toC) && // both "from" and "to" spaces must be on the gameboard
				!board[fromR][fromC].isEmpty() && // "from" space must be occupied
				!(fromR == toR && fromC == toC) && // "from" and "to" can't be the same space
				board[fromR][fromC].validMove(fromR, fromC, toR, toC)) { // must be valid move based on that gamepiece
			// if "to" space is empty, can just switch the "from" and "to"
			if (board[toR][toC].isEmpty()) {
				ChessPiece temp = board[toR][toC];
				board[toR][toC] = board[fromR][fromC];
				board[fromR][fromC] = temp;
			} else if (board[fromR][fromC].getColor() != board[toR][toC].getColor()) {
				// if the "to" isn't empty and it's on the other side, capture it
				System.out.println(board[toR][toC]+" captured.");
				board[toR][toC].remove();
				ChessPiece temp = board[toR][toC];
				board[toR][toC] = board[fromR][fromC];
				board[fromR][fromC] = temp;
			}
			// TEMP still need to test if everything in middle is empty
			
			/* output for testing
 			System.out.println(temp);
			System.out.println(board[fromR][fromC]);
			System.out.println(board[toR][toC]);
			*/
			
			return true;
		} else return false;
	}
	
	// checks to make sure a point is on board
	boolean onBoard(int r, int c) {
		return (r >= 1 && r <= 8 &&
				c >= 1 && c <= 8);
	}
	
	class ChessPiece {
		char pieceName;
		boolean color;
		
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
		 
		boolean getColor() {
			return color;
		}
		
		void remove() {
			pieceName = ' ';
		}
	}
	
	class King extends ChessPiece {
		King(boolean c) {
			pieceName = 'K';
			color = c;
		}
		// king can move exactly one vacant square in any direction
		boolean validMove (int fr, int fc, int tr, int tc) {
			if (((Math.abs(fr - tr) == 1) && (Math.abs(fc - tc) == 0)) || ((Math.abs(fr - tr) == 0) && (Math.abs(fc - tc) == 1)))
				return true;
			else
				return false;
			// TEMP add possibility of castling
		}
	}
	class Queen extends ChessPiece {
		Queen(boolean c) {
			pieceName = 'Q';
			color = c;
		}
		boolean validMove (int fr, int fc, int tr, int tc) {
			// can go in diagonals or straight lines
			if ((fc - tc)/(fr - tr) == 1) {
				// if positive slope
				int c = Math.min(fc, tc) + 1;
				for (int r = Math.min(fr,tr) + 1; r < Math.max(fr, tr); r++) {
					if (!board[r][c].isEmpty()) return false;
					c++;
				}
				return true;
			} else if ((fc - tc)/(fr - tr) == -1) {
				// if negative slope
				int c = Math.min(fc, tc) + 1;
				for (int r = Math.max(fr,tr) - 1; r > Math.min(fr, tr); r--) {
					if (!board[r][c].isEmpty()) return false;
					c++;
				}
				return true;
			} else if (fr == tr) {
				// if going horiz, check for pieces in between
				for (int c = Math.min(fc, tc)+1; c < Math.max(fc, tc); c++)
					if (!board[fr][c].isEmpty()) return false;
				return true;
			} else if (fc == tc) {
				// if going vert, check for pieces in between
				for (int r = Math.min(fr, tr)+1; r < Math.max(fr, tr); r++)
					if (!board[r][fc].isEmpty()) return false;
				return true;
			} else
				return false;
		}
	}
	class Knight extends ChessPiece {
		Knight(boolean c) {
			pieceName = 'N';
			color = c;
		}
		boolean validMove (int fr, int fc, int tr, int tc) {
			if (((Math.abs(fr - tr) == 2) && (Math.abs(fc - tc) == 1)) ||
					((Math.abs(fr - tr) == 1) && (Math.abs(fc - tc) == 2)))
				return true;
			else
				return false;
		}
	}
	class Rook extends ChessPiece {
		Rook(boolean c) {
			pieceName = 'R';
			color = c;
		}
		boolean validMove (int fr, int fc, int tr, int tc) {
			if (fr == tr) {
				// if going horiz, check for pieces in between
				for (int c = Math.min(fc, tc)+1; c < Math.max(fc, tc); c++)
					if (!board[fr][c].isEmpty()) return false;
				return true;
			} else if (fc == tc) {
				// if going vert, check for pieces in between
				for (int r = Math.min(fr, tr)+1; r < Math.max(fr, tr); r++)
					if (!board[r][fc].isEmpty()) return false;
				return true;
			} else
				return false;
		}
	}
	class Bishop extends ChessPiece {
		Bishop(boolean c) {
			pieceName = 'B';
			color = c;
		}
		boolean validMove (int fr, int fc, int tr, int tc) {
			if ((fc - tc)/(fr - tr) == 1) {
				// if positive slope
				int c = Math.min(fc, tc) + 1;
				for (int r = Math.min(fr,tr) + 1; r < Math.max(fr, tr); r++) {
					if (!board[r][c].isEmpty()) return false;
					c++;
				}
				return true;
			} else if ((fc - tc)/(fr - tr) == -1) {
				// if negative slope
				int c = Math.min(fc, tc) + 1;
				for (int r = Math.max(fr,tr) - 1; r > Math.min(fr, tr); r--) {
					if (!board[r][c].isEmpty()) return false;
					c++;
				}
				return true;
			} else
				return false;
		}
	}
	class Pawn extends ChessPiece {
		boolean hasMoved;
		Pawn(boolean c) {
			pieceName = 'P';
			hasMoved = false;
			color = c;
		}
		boolean validMove (int fr, int fc, int tr, int tc) {
			// if moving forward 1 in the same column and the target is empty, return true
			if ((tr - fr == (color == WHITE ? 1 : -1)) && (fc == tc) && board[tr][tc].isEmpty())
				return true;
			// if moving forward 1 and sideways 1 and the target is not empty, return true
			else if ((tr - fr == (color == WHITE ? 1 : -1)) && (Math.abs(tc-fc) == 1) && !board[tr][tc].isEmpty())
				return true;
			else
				return false;
			// TEMP need to account for ability to move two in first move
		}
	}

}

