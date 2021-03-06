import java.io.*;
import java.net.InetAddress;
import java.net.*;
import java.util.*;

/* Chess Board
 * 
 * Contains code for Chess Board
 */

class ChessBoard {
	// activeColor shows who is playing
	private int activeColor;
	// constants WHITE and BLACK represent two players
	static final int WHITE = 1;
	static final int BLACK = -1;
	
	static int port = 8190;
	
	// board holds pointers to all the ChessPiece objects
	private ChessPiece board[][];
	private int activePlayers;
	
	// coordinates of two kings
	private ChessCoord whiteKing;
	private ChessCoord blackKing;
	
	private static Socket whitePlayer;
	private static Socket blackPlayer;
	
	// move statuses for Pawn
	static final int NEVER_MOVED = 0;
	static final int TWO_SPACES = 2;
	static final int ONE_SPACE = 1;
			
	public static void main(String args[]) throws IOException {
		if (args.length > 0)
			port = Integer.parseInt(args[0]);
		new ChessBoard(port);
		// get move input
	}
	
	ChessBoard(int port) throws IOException {		
		// initial printing on Server side only using System.out
		System.out.println("Simulate a chess game between two players via a computer server");
		activeColor = WHITE;
		activePlayers = 0;
		// set up board and pieces
		board = new ChessPiece[9][9];
		board[1][1] = new Rook(WHITE);
		board[1][2] = new Knight(WHITE);
		board[1][3] = new Bishop(WHITE);
		board[1][4] = new Queen(WHITE);
		whiteKing = new ChessCoord(1,5);
		board[1][5] = new King(WHITE);
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
		board[8][4] = new Queen(BLACK);
		blackKing = new ChessCoord(8,5);
		board[8][5] = new King(BLACK);
		board[8][6] = new Bishop(BLACK);
		board[8][7] = new Knight(BLACK);
		board[8][8] = new Rook(BLACK);		

		// play chess!
		// set up the server socket
		try {
			ServerSocket listener = new ServerSocket(port);
			
			// wait for White
			whitePlayer = listener.accept();
			
			// wait for Black
			blackPlayer = listener.accept();
			new PlayerHandler(whitePlayer, WHITE).start();
			new PlayerHandler(blackPlayer, BLACK).start();
			// close the game
			listener.close();
		}
		catch (IOException e) {
			System.out.println("Port "+port+" may be busy. Try another");
		}
		
	}
	
	class PlayerHandler extends Thread {
		private String playerName;
		private Socket toPlayer;
		private int playerColor;

		
		
		PlayerHandler(Socket s, int i) {
			// remember the client socket number and client ID number
			toPlayer = s;
			playerColor = i;
		}
		
		public void run() {
			try {
				// create i-o streams through the socket we were given when the thread was instantiated and welcome the new client
				BufferedReader in = new BufferedReader(new InputStreamReader (toPlayer.getInputStream()));
				PrintWriter out = new PrintWriter(toPlayer.getOutputStream(), true);
				out.println("\nWhat is your name?");
				playerName = in.readLine().trim();
				
				/*
				// TEMP for testing purposes, input from file
				if (playerColor == 1)
					in = new BufferedReader(new FileReader("player1.txt"));
				else
					in = new BufferedReader(new FileReader("player2.txt"));
				*/
				
				// if both players aren't here, then can't start yet!
				waitForTwoPlayers();
				out.println("\n*** Welcome to Chess, "+playerName+" ***\n");

				
				out.println("To castle, simply move your king to the appropriate position, and the rook will move accordingly. Type \"QUIT\" at any time to quit the game.");
				out.flush();
				showBoardTo(toPlayer);
				String move;
				while (true) {
					if (activeColor == playerColor) {
						out.print(activeColor == WHITE ? "White" : "Black");
						out.print(", your turn. What is your move? Please enter in format \"a1 a2\".\n");
						out.flush();
						try {
							move = in.readLine().toLowerCase();
							// TEMP for testing & verifying moves
							System.out.println(move);
		
							if (move.contains("quit")) {
								showMsgToBoth(playerName + " has quit the game");
								break;
							}
							
							// attempt to perform move
							if (makeMove(move, in, out)) {
								showBoardToBoth();
								int status = hasWinningCondition();
								if (status == 2) {
									showMsgToBoth(playerName + " has won!");
									showMsgTo((activeColor == WHITE ? whitePlayer : blackPlayer), "CONGRATULATIONS!!");
									break;
								} else if (status == 1)
									showMsgToBoth("CHECK.");
								activeColor = -1 * activeColor;
								nextTurn();
							}
							else {
								out.println("Invalid move, please try again.");
							}
						}
						catch (EOFException e) {}
					} else {
						out.println("Waiting for "+ (activeColor == WHITE ? "White" : "Black")+"'s move.");
						waitForTurn();
					}
				}
				endGame();
			} catch (Exception e) {
				System.out.println("Error: "+e);
			}
		}
	}
	
	synchronized void waitForTwoPlayers() throws IOException {
		activePlayers ++;
		if (activePlayers == 1)
			try {
				wait();
			} catch (InterruptedException e) {}
		else if (activePlayers == 2) {
			notifyAll();
		}
		else
			System.out.println("ERROR: miscounting active players.");
	}
	
	synchronized void waitForTurn() {
		// if you're not currently the active player, wait for it to be
		try {
			wait();
		} catch (InterruptedException e) {}
	}
	
	synchronized void nextTurn() {
		notifyAll();
	}
	
	
	ChessPiece getPiece(ChessCoord a) {
		return board[a.row][a.col];
	}
	
	ChessPiece getPiece(int r, int c) {
		return board[r][c];
	}
	
	synchronized void clearPiece(ChessCoord p) {
		board[p.row][p.col].pieceName = ' ';
		board[p.row][p.col].color = 0;
	}
	
	synchronized void showMsgToBoth(String message) throws IOException {
		// sends the message to both players
		if (whitePlayer != null) showMsgTo(whitePlayer, message);
		if (blackPlayer != null) showMsgTo(blackPlayer, message);
	}
	
	synchronized void showMsgTo(Socket s, String message) throws IOException {
		PrintWriter p = new PrintWriter(s.getOutputStream(), true);
		p.println(message);
		p.flush();
	}
	
	synchronized void showBoardToBoth() throws IOException {
		showBoardTo(whitePlayer);
		showBoardTo(blackPlayer);
	}
	
	synchronized void showBoardTo(Socket s) throws IOException {
		PrintWriter p = new PrintWriter(s.getOutputStream(), true);
		// top row labels
		p.println();
		p.print("  ");
		for (int c = (int)('A'); c <= (int) ('H'); c++)
			p.print("  "+ (char) c +" ");
		p.println();
		// actual board
		for (int r = 8; r >= 1; r--) {
			p.print(r + " ");
			for (int c = 1; c <= 8; c++) {
				if (getPiece(r,c).isEmpty())
					p.print(" -- ");
				else {
					int color = getPiece(r,c).getColor() == BLACK ? 1 : 0;
					p.print(" "+ color + getPiece(r,c)+" ");
				}
			}
			p.print(" " + r);
			p.println();
		}
		// bottom row labels
		p.print("  ");
		for (int c = (int)('A'); c <= (int) ('H'); c++)
			p.print("  "+ (char) c +" ");
		p.println();
		p.println();
		p.flush();
	}
	
	boolean onBoard(int r, int c) {
		if (r < 1 || r > 8 || c < 1 || c > 8)
			return false;
		return true;
	}
	
	boolean lookFor (int r, int c, char n, int p) {
		// checks if there is a ChessPiece that matches n at coordinate (r, c)r!onBoard(r, c))
		if (!onBoard(r, c))	
			return false;
		else if (n == ' ')
			// if checking for a blank piece, color doesn't matter
			return (getPiece(r, c).getName() == n);
		else
			return (getPiece(r, c).getName() == n && getPiece(r,c).getColor() == p);
	}
	
	
	int hasWinningCondition() {
		// returns 2 if checkmate, 1 if check, and 0 if none
		ChessCoord otherKing = activeColor == WHITE ? blackKing : whiteKing;
		// capturableByAny returns LinkedList of potential captors
		LinkedList captors = otherKing.getCaptorList(activeColor);
		// No check/checkmate - if this list is empty, return 0;
		if (captors.isEmpty())
			return 0;
		// Check 1: Avoidability - if the otherKing has a blank space around him that a NOT capturableByAny, then return 1 because is avoidable;
		if (lookFor(otherKing.row+1,otherKing.col-1,' ',0) && (new ChessCoord(otherKing.row+1,otherKing.col-1)).capturableByAny(activeColor) == false) return 1;
		if (lookFor(otherKing.row+1,otherKing.col,' ',0) && (new ChessCoord(otherKing.row+1,otherKing.col)).capturableByAny(activeColor) == false) return 1;
		if (lookFor(otherKing.row+1,otherKing.col+1,' ',0) && (new ChessCoord(otherKing.row+1,otherKing.col+1)).capturableByAny(activeColor) == false) return 1;
		if (lookFor(otherKing.row,otherKing.col-1,' ',0) && (new ChessCoord(otherKing.row,otherKing.col-1)).capturableByAny(activeColor) == false) return 1;
		if (lookFor(otherKing.row,otherKing.col+1,' ',0) && (new ChessCoord(otherKing.row,otherKing.col+1)).capturableByAny(activeColor) == false) return 1;
		if (lookFor(otherKing.row-1,otherKing.col-1,' ',0) && (new ChessCoord(otherKing.row-1,otherKing.col-1)).capturableByAny(activeColor) == false) return 1;
		if (lookFor(otherKing.row-1,otherKing.col,' ',0) && (new ChessCoord(otherKing.row-1,otherKing.col)).capturableByAny(activeColor) == false) return 1;
		if (lookFor(otherKing.row-1,otherKing.col+1,' ',0) && (new ChessCoord(otherKing.row-1,otherKing.col+1)).capturableByAny(activeColor) == false) return 1;
		// otherwise loop through capturers because is unavoidable
		PieceCoord cap, capCaptor;
		for (ListIterator list = captors.listIterator(); list.hasNext();) {
			// Check 2: Removability - if the capturer's space is capturable by a piece of the opposite color that isn't the king that we're trying to get, then move on to next capturer because is removable
			cap = (PieceCoord) list.next();
			
			LinkedList capCaptors = (new ChessCoord(cap.row, cap.col)).getCaptorList(activeColor * -1);
			for (ListIterator list2 = capCaptors.listIterator(); list2.hasNext(); ) {
				capCaptor = (PieceCoord) list2.next();
				if (capCaptor.pieceName != 'K') continue;
			}
			
			// Check 3: Blockability - check if the path between otherKing and the captor can be blocked
			// if the captor is a 'N' or 'P' return 2 because of an unremovable, unblockable captor (since knight & pawn can't be blocked)
			if (getPiece(cap.row, cap.col).getName() == 'N' || getPiece(cap.row, cap.col).getName() == 'P') return 2;
			// use a nested for loop to check path between otherKing and capturer
			int incR = (int) Math.signum(cap.row - otherKing.row);
			int incC = (int) Math.signum(cap.col - otherKing.col);
			int r = otherKing.row + incR;
			int c = otherKing.col + incC;
			while (r != cap.row & c != cap.col) {
				// if space is reachableByAnyButKing of the opposite color, then move on to next capturer because is blockable
				if ((new ChessCoord(r, c)).reachableByAnyButKing(activeColor * -1))
					break; 
				r += incR;
				c += incC;
			}
			 // if get to end and nothing was reachableByAnyButKing, return 2 because of an unremovable, unblockable captor;
			return 2;
		}
		// if get to end of capturers and haven't returned anything, return 1 because there is a captor but it was removable;
		return 1;
	}
	
	
	
	// makeMove tries to make a move and returns true if move is made or false if not
	synchronized boolean makeMove(String m, BufferedReader in, PrintWriter out) throws IOException {
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
		
		
		if (from.isOnBoard() && to.isOnBoard() && // both "from" and "to" spaces must be on the gameboard
				getPiece(from).getColor() == activeColor &&// can only move your own piece
				!getPiece(from).isEmpty() && // "from" space must be occupied
				!(from.isEqual(to)) && // "from" and "to" can't be the same space
				getPiece(from).validMove(from, to)) { // must be valid move based on that gamepiece
			// if "to" space is empty, can just switch the "from" and "to"
			if (getPiece(to).isEmpty()) {
				showMsgToBoth((activeColor == WHITE ? "White" : "Black") +" moves " + getPiece(from).getName()+" from "+m.substring(0,2)+" to "+m.substring(3,5)+".");
				// if the "to" is empty, the "from" is a pawn, and the "to" and "from" columns are different, then it was an en passant
				if (getPiece(from).getName() == 'P' && from.col != to.col) {
					clearPiece(new ChessCoord(to.row - activeColor, to.col));
					showMsgToBoth("P has been captured.");
				}
				swapPieces(from, to);
			} else { // if the "to" isn't empty
				// if it's on your own side, can't make this move
				if (activeColor == getPiece(to).getColor()) return false;
				showMsgToBoth((activeColor == WHITE ? "White" : "Black") +" moves " + getPiece(from).getName() + " from " + m.substring(0,2)+" to "+m.substring(3,5)+".");
				// if "to" space belongs to the other person, then capture it
				showMsgToBoth(getPiece(to)+" has been captured.");
				clearPiece(to);
				swapPieces(from, to);
			}
			
			if (getPiece(to).getName() == 'K') {
				King k = (King) getPiece(to);
				if (activeColor == WHITE) {
					whiteKing.setCoord(toR, toC);
				} else {
					blackKing.setCoord(toR, toC);
				}
				if (!k.hasMoved) {
					// if the move was a castle, then move the appropriate rook
					if (Math.abs(toC - fromC) == 2) {
						int inc = (toC-fromC)/2;
						ChessCoord rookFrom = new ChessCoord(fromR, fromC + inc * (inc == -1 ? 4 : 3));
						ChessCoord rookTo = new ChessCoord(fromR, fromC + inc);
						swapPieces(rookFrom, rookTo);
					}
					k.move();
				}
			}
			if (getPiece(to).getName() == 'R') {
				Rook r = (Rook) getPiece(to);
				if (!r.hasMoved)
					r.move();
			}
			// Promotion
			// If the pawn reaches a square on the back rank of the opponent, it promotes to the player's choice of a queen, rook, bishop, or knight
			if (getPiece(to).getName() == 'P') {
				// if pawn that hasn't moved before, mark it as moved
				Pawn p = (Pawn) getPiece(to);
				if (p.lastMove == NEVER_MOVED || p.lastMove == TWO_SPACES)
					p.setLastMove(Math.abs(to.row - from.row));
				// promotion
				if ((p.getColor() == WHITE && toR == 8) || (p.getColor() == BLACK && toR == 1)) {
					out.println("Congratulations! Your pawn has reached the opposite end of the board. Would you like to replace it with a Queen, Rook, Bishop, or Knight?");
					String s;
					char choice;
					while (true) {
						out.print("Please enter your choice: Q, R, B, or N: ");
						s = in.readLine();
						choice = s.toUpperCase().charAt(0);
						if (choice == 'Q' || choice == 'R' || choice == 'B' || choice == 'N')
							break;
					}
					switch (choice) {
					case 'Q': board[toR][toC] = new Queen(activeColor); break;
					case 'R': board[toR][toC] = new Rook(activeColor); break;
					case 'B': board[toR][toC] = new Bishop(activeColor); break;
					case 'N': board[toR][toC] = new Knight(activeColor); break;
					}
				}
			}
			return true;
		} else return false;
	}
	
	synchronized void swapPieces(ChessCoord from, ChessCoord to) {
		ChessPiece temp = getPiece(to);
		board[to.row][to.col] = getPiece(from);
		board[from.row][from.col] = temp;
	}
	
	void endGame() throws IOException {
		showMsgToBoth("Thanks for playing!");
		whitePlayer.close();
		blackPlayer.close();
	}
	
	public class PieceCoord {
		public int row;
		public int col;
		public char pieceName;
		
		PieceCoord (int r, int c, char n) {
			row = r;
			col = c;
			pieceName = n;
		}
	}
	
	public class ChessCoord {
		int row;
		int col;
		
		ChessCoord(int r, int c) {
			row = r;
			col = c;
		}
		
		boolean isOnBoard() {
			// checks to make sure a point is on board
			return onBoard(row, col);
		}
		
		boolean isEqual(ChessCoord a) {
			return (row == a.row && col == a.col);
		}
		
		void setCoord(int r, int c) {
			row = r;
			col = c;
		}
		
		boolean reachableByAnyButKing(int pieceColor) {
			// returns whether any piece of the active player's color can reach this Chess Coordinate
			LinkedList captors = getCaptorList(pieceColor);
			PieceCoord cap;
			// loop through for any non-pawn pieces, in which case return true
			for (ListIterator list = captors.listIterator(); list.hasNext(); ) {
				cap = (PieceCoord) list.next();
				if (cap.pieceName != 'P' && cap.pieceName != 'K') return true;
			}
			// check for pawn
			int inc = pieceColor*-1;
			// if the piece directly in front is a pawn, return true
			if (lookFor(row + inc, col, 'P', pieceColor))
				return true;
			// if the piece two forward is a pawn & it hasn't moved yet, return true
			if (lookFor(row + 2*inc, col, 'P', pieceColor) && !((Pawn) getPiece(row+2*inc, col)).hasMoved)
				return true;
			return false;

		}

		boolean capturableByAny(int pieceColor) {
			// returns whether any piece of pieceColor can capture something at this Chess Coordinate
			return (getCaptorList(pieceColor).size() > 0);
			}
				
		LinkedList getCaptorList(int pieceColor) {
			// returns list of the pieces of the pieceColor that can capture something at this Chess Coordinate
			LinkedList captorList = new LinkedList();
			int r, c;
			// look for pawn 1 space diagonally in front
			// increment (+/- 1) depends on which player is active (which direction player faces)
			int inc = pieceColor*-1;
			if (lookFor(row + inc, col + 1, 'P', pieceColor)) captorList.add(new PieceCoord(row + inc, col + 1, 'P'));
			if (lookFor(row + inc, col - 1, 'P', pieceColor)) captorList.add(new PieceCoord(row + inc, col - 1, 'P'));
		
			// look for knight an "L" away
			if (lookFor(row + 2, col + 1, 'N', pieceColor)) captorList.add(new PieceCoord(row + 2, col + 1, 'N'));
			if (lookFor(row + 1, col + 2, 'N', pieceColor)) captorList.add(new PieceCoord(row + 1, col + 2, 'N'));
			if (lookFor(row + 2, col - 1, 'N', pieceColor)) captorList.add(new PieceCoord(row + 2, col - 1, 'N'));
			if (lookFor(row + 1, col - 2, 'N', pieceColor)) captorList.add(new PieceCoord(row + 1, col - 2, 'N'));
			if (lookFor(row - 2, col + 1, 'N', pieceColor)) captorList.add(new PieceCoord(row - 2, col + 1, 'N'));
			if (lookFor(row - 1, col + 2, 'N', pieceColor)) captorList.add(new PieceCoord(row - 1, col + 2, 'N'));
			if (lookFor(row - 2, col - 1, 'N', pieceColor)) captorList.add(new PieceCoord(row - 2, col - 1, 'N'));
			if (lookFor(row - 1, col - 2, 'N', pieceColor)) captorList.add(new PieceCoord(row - 1, col - 2, 'N'));

			// look for king 1 space in any direction
			if (lookFor(row + 1, col + 1, 'K', pieceColor)) captorList.add(new PieceCoord(row + 1, col + 1, 'K'));
			if (lookFor(row + 1, col - 1, 'K', pieceColor)) captorList.add(new PieceCoord(row + 1, col - 1, 'K'));
			if (lookFor(row - 1, col + 1, 'K', pieceColor)) captorList.add(new PieceCoord(row - 1, col + 1, 'K'));
			if (lookFor(row - 1, col - 1, 'K', pieceColor)) captorList.add(new PieceCoord(row - 1, col - 1, 'K'));
			if (lookFor(row, col + 1, 'K', pieceColor)) captorList.add(new PieceCoord(row, col + 1, 'K'));
			if (lookFor(row, col - 1, 'K', pieceColor)) captorList.add(new PieceCoord(row, col - 1, 'K'));
			if (lookFor(row + 1, col, 'K', pieceColor)) captorList.add(new PieceCoord(row + 1, col, 'K'));
			if (lookFor(row - 1, col, 'K', pieceColor)) captorList.add(new PieceCoord(row - 1, col, 'K'));
			
			// look for rook or queen horiz or vert in any direction
			// increase row, col stays same
			r = row + 1;
			c = col;
			while(onBoard(r,c)) {
				if ((getPiece(r, c).getName() == 'R') && getPiece(r,c).getColor() == pieceColor) captorList.add(new PieceCoord(row - 1, col, 'R'));
				if ((getPiece(r, c).getName() == 'Q') && getPiece(r,c).getColor() == pieceColor) captorList.add(new PieceCoord(row - 1, col, 'Q'));
				// if it's a piece other than a space, then you've encountered some other piece and that path is safe
				else if (getPiece(r, c).getName() != ' ') break;
				r++;
			}
			// decrease row, col stays same
			r = row - 1;
			while(onBoard(r,c)) {
				if ((getPiece(r, c).getName() == 'R') && getPiece(r,c).getColor() == pieceColor) captorList.add(new PieceCoord(row - 1, col, 'R'));
				if ((getPiece(r, c).getName() == 'Q') && getPiece(r,c).getColor() == pieceColor) captorList.add(new PieceCoord(row - 1, col, 'Q'));
				else if (getPiece(r, c).getName() != ' ') break;
				r--;
			}
			// same row, increase column
			r = row;
			c = col + 1;
			while(onBoard(r,c)) {
				if ((getPiece(r, c).getName() == 'R') && getPiece(r,c).getColor() == pieceColor) captorList.add(new PieceCoord(row - 1, col, 'R'));
				if ((getPiece(r, c).getName() == 'Q') && getPiece(r,c).getColor() == pieceColor) captorList.add(new PieceCoord(row - 1, col, 'Q'));
				else if (getPiece(r, c).getName() != ' ') break;
				c++;
			}
			// same row, decrease column
			c = col - 1;
			while(onBoard(r,c)) {
				if ((getPiece(r, c).getName() == 'R') && getPiece(r,c).getColor() == pieceColor) captorList.add(new PieceCoord(row - 1, col, 'R'));
				if ((getPiece(r, c).getName() == 'Q') && getPiece(r,c).getColor() == pieceColor) captorList.add(new PieceCoord(row - 1, col, 'Q'));
				else if (getPiece(r, c).getName() != ' ') break;
				c--;
			}

			// look for bishop or queen diag in any direction
			// increase row, increase col
			r = row+1;
			c = col+1;
			while(onBoard(r,c)) {
				if ((getPiece(r, c).getName() == 'B') && getPiece(r,c).getColor() == pieceColor) captorList.add(new PieceCoord(row - 1, col, 'B'));
				if ((getPiece(r, c).getName() == 'Q') && getPiece(r,c).getColor() == pieceColor) captorList.add(new PieceCoord(row - 1, col, 'Q'));
				else if (getPiece(r, c).getName() != ' ') break;
				r++;
				c++;
			}
			// increase row, increase col
			r = row-1;
			c = col-1;
			while(onBoard(r,c)) {
				if ((getPiece(r, c).getName() == 'B') && getPiece(r,c).getColor() == pieceColor) captorList.add(new PieceCoord(row - 1, col, 'B'));
				if ((getPiece(r, c).getName() == 'Q') && getPiece(r,c).getColor() == pieceColor) captorList.add(new PieceCoord(row - 1, col, 'Q'));
				else if (getPiece(r, c).getName() != ' ') break;
				r--;
				c--;
			}
			// increase row, increase col
			r = row+1;
			c = col-1;
			while(onBoard(r,c)) {
				if ((getPiece(r, c).getName() == 'B') && getPiece(r,c).getColor() == pieceColor) captorList.add(new PieceCoord(row - 1, col, 'B'));
				if ((getPiece(r, c).getName() == 'Q') && getPiece(r,c).getColor() == pieceColor) captorList.add(new PieceCoord(row - 1, col, 'Q'));
				else if (getPiece(r, c).getName() != ' ') break;
				r++;
				c--;
			}
			// increase row, increase col
			r = row-1;
			c = col+1;
			while(onBoard(r,c)) {
				if ((getPiece(r, c).getName() == 'B') && getPiece(r,c).getColor() == pieceColor) captorList.add(new PieceCoord(row - 1, col, 'B'));
				if ((getPiece(r, c).getName() == 'Q') && getPiece(r,c).getColor() == pieceColor) captorList.add(new PieceCoord(row - 1, col, 'Q'));
				else if (getPiece(r, c).getName() != ' ') break;
				r--;
				c++;
			}
			return captorList;
		}
	}
	
	class ChessPiece {
		char pieceName;
		int color;
		
		ChessPiece () {
			// if empty, set pieceName to be null
			pieceName = ' ';
			color = 0;
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
		
	}
	
	class King extends ChessPiece {
		boolean hasMoved;
		
		King(int c) {
			pieceName = 'K';
			hasMoved = false;
			color = c;
		}
		void move() {
			hasMoved = true;
		}
		
		// king can move exactly one vacant square in any direction
		boolean validMove (ChessCoord f, ChessCoord t) {
			if (((Math.abs(f.row - t.row) == 1) && (Math.abs(f.col - t.col) == 0)) || ((Math.abs(f.row - t.row) == 0) && (Math.abs(f.col - t.col) == 1)) ||
					(Math.abs(f.row - t.row) == 1 && Math.abs(f.col - t.col) == 1))
				return true;
			else
				// check for castling
				if (hasMoved == false && // king hasn't moved before
				Math.abs(f.row - t.row) == 0 && Math.abs(f.col - t.col) == 2 && // trying to move exactly two spaces left/right
				getPiece(t.row, t.col).pieceName == ' ') { // trying to move to a blank space 
					int inc = (t.col - f.col)/2;
					if (getPiece(f.row, f.col+inc).pieceName != ' ') return false; // space that rook will go to must be empty
					if (inc == -1) if (getPiece(f.row, f.col+3*inc).pieceName != ' ') return false; // if queenside rook, one more space must be empty
					if (getPiece(f.row, f.col+ (inc == -1 ? 4 : 3)*inc).pieceName != 'R') return false; // if corresponding piece isn't a rook, can't castle
					if (((Rook) getPiece(f.row, f.col+ (inc == -1 ? 4 : 3)*inc)).hasMoved == true) return false; // if corresponding rook has moved, can't castle
					return true;
				}
				return false;
		} 
	}
	class Queen extends ChessPiece {
		Queen(int c) {
			pieceName = 'Q';
			color = c;
		}
		boolean validMove (ChessCoord f, ChessCoord t) {
			// can go in diagonals or straight lines
			if (t.row == f.row) {
				// if going horiz, check for pieces in between
				for (int c = Math.min(f.col, t.col)+1; c < Math.max(f.col, t.col); c++)
					if (!getPiece(f.row,c).isEmpty()) return false;
				return true;
			} else if (t.col == f.col) {
				// if going vert, check for pieces in between
				for (int r = Math.min(f.row, t.row)+1; r < Math.max(f.row, t.row); r++)
					if (!getPiece(r,f.col).isEmpty()) return false;
				return true;
			} else if ((t.col - f.col)/(t.row - f.row) == 1) {
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
		boolean hasMoved;
		Rook(int c) {
			pieceName = 'R';
			hasMoved = false;
			color = c;
		}
		void move() {
			hasMoved = true;
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
		int lastMove;
		
		Pawn (int c) {
			pieceName = 'P';
			lastMove = NEVER_MOVED;
			color = c;
		}
		int getLastMove() {
			return lastMove;
		}
		void setLastMove(int numSpaces) {
			lastMove = numSpaces;
		}
		
		boolean validMove (ChessCoord f, ChessCoord t) {
			// if moving forward 1 in the same column and the target is empty, return true
			if ((t.row - f.row == color) && (f.col == t.col) && getPiece(t).isEmpty())
				return true;
			// if moving forward 1 and sideways 1 and the target is not empty, return true
			else if ((t.row - f.row == color) && (Math.abs(t.col-f.col) == 1) && !getPiece(t).isEmpty())
				return true;
			// if moving forward 1 and sideways 1 and the target IS empty, but the piece behind the target is a pawn whose last move was 2 spaces, return true for en passant
			else if ((t.row - f.row == color) && (Math.abs(t.col-f.col) == 1) && getPiece(t.row - color, t.col).getName() == 'P' && ((Pawn) getPiece(t.row - color, t.col)).getLastMove() == TWO_SPACES)
				return true;
			// if it's this pawn's fist move and moving forward 2 in the same column and the target is empty, return true
			else if (lastMove == NEVER_MOVED && (t.row - f.row == color*2) && (f.col == t.col) && getPiece(t).isEmpty())
				return true;
			else
				return false;
		}
	}

}

