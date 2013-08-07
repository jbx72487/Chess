import java.io.*;
import java.net.InetAddress;
import java.net.*;

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
	
	static int port = 8190;
	
	// board holds pointers to all the ChessPiece objects
	ChessPiece board[][];
	
	// coordinates of two kings
	ChessCoord whiteKing;
	ChessCoord blackKing;
	
	static Socket p1;
	static Socket p2;
		
	public static void main(String args[]) throws IOException {
		if (args.length > 0)
			port = Integer.parseInt(args[0]);
		new ChessBoard(port);
		// get move input
	}
	
	ChessBoard(int port) throws IOException {		
		// initial printing on Server side only using System.out
		System.out.println("Simulate a chess game between two players via a computer server");
		
		activePlayer = WHITE;
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
		blackKing = new ChessCoord(8,4);
		board[8][5] = new Queen(BLACK);
		board[8][6] = new Bishop(BLACK);
		board[8][7] = new Knight(BLACK);
		board[8][8] = new Rook(BLACK);		

		// play chess!
		// set up the server socket
		try {
			ServerSocket listener = new ServerSocket(port);
			
			// wait for White
			p1 = listener.accept();
			System.out.println("Player 1 has joined.");
			new PlayerHandler(p1, WHITE).start();
			// wait for Black
			p2 = listener.accept();
			System.out.println("Player 2 has joined.");
			new PlayerHandler(p2, BLACK).start();
			listener.close();
		}
		catch (IOException e) {
			System.out.println("Port "+port+" may be busy. Try another");
		}
		
	}
	
	class PlayerHandler extends Thread {
		private BufferedReader in;
		private PrintWriter out;
		private Socket toPlayer;
		private int playerID;
		
		
		PlayerHandler(Socket s, int i) {
			// remember the client socket number and client ID number
			toPlayer = s;
			playerID = i;
		}
		
		public void run() {
			try {
				// create i-o streams through the socket we were given when the thread was instantiated and welcome the new client
				
				in = new BufferedReader(new InputStreamReader (toPlayer.getInputStream()));
				out = new PrintWriter(toPlayer.getOutputStream(), true);
				out.println("*** Welcome to Chess ***");
				out.println("Type \"QUIT\" at any time to quit the game.");
				display();
				String move;
				while (true) {
					out.print(activePlayer == WHITE ? "White" : "Black");
					out.print(", your turn. What is your move? Please enter in format \"a1 a2\"\n");
					move = in.readLine().toLowerCase();

					if (move.contains("quit")) break;
					
					// attempt to perform move
					if (makeMove(move, in, out)) {
						if (hasCheck())
							ChessBoard.broadcast("CHECK.");
						display();
						activePlayer = -1 * activePlayer;
					}
					else {
						out.println("Invalid move, please try again.");
						display();
					}
					// if check or checkmate, say so
				}
				out.println("Thanks for playing!");
				toPlayer.close();
			} catch (Exception e) {
				System.out.println("Error: "+e);
			}
		}
	}
	
	synchronized static void broadcast(String message) throws IOException {
		// sends the message to both players
		PrintWriter p;
		p = new PrintWriter(p1.getOutputStream(), true);
		p.println(message);
		p = new PrintWriter(p2.getOutputStream(), true);
		p.println(message);
	}
	
	ChessPiece getPiece(ChessCoord a) {
		return board[a.row][a.col];
	}
	
	ChessPiece getPiece(int r, int c) {
		return board[r][c];
	}
	
	void display() throws IOException {
		// top row labels
		PrintWriter p;
		p = new PrintWriter(p1.getOutputStream(), true);
		for (int i = 0; i < 2; i++) {
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
			p = new PrintWriter(p2.getOutputStream(), true);			
		}
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
		else {
			return (getPiece(r, c).getName() == n && getPiece(r,c).getColor() == p);
		}
	}
	
	// hasCheck checks whether current player has a check
	boolean hasCheck() {

		ChessCoord otherKing = activePlayer == WHITE ? blackKing : whiteKing;
		
		int r = otherKing.row;
		int c = otherKing.col;
		
		// look for pawn 1 space diagonally in front
		// increment (+/- 1) depends on which player is active (which direction player faces)
		int inc = activePlayer*-1;
		if (lookFor(r + inc, c + 1, 'P', activePlayer) ||
				lookFor(r + inc, c - 1, 'P', activePlayer)) {
			return true;
		}
		
		// look for knight an "L" away
		if (lookFor(r + 2, c + 1, 'N', activePlayer) ||
				lookFor(r + 1, c + 2, 'N', activePlayer) ||
				lookFor(r + 2, c - 1, 'N', activePlayer) ||
				lookFor(r + 1, c - 2, 'N', activePlayer) ||
				lookFor(r - 2, c + 1, 'N', activePlayer) ||
				lookFor(r - 1, c + 2, 'N', activePlayer) ||
				lookFor(r - 2, c - 1, 'N', activePlayer) ||
				lookFor(r - 1, c - 2, 'N', activePlayer)) {
			return true;
		}
		
		// look for king 1 space in any direction
		if (lookFor(r + 1, c + 1, 'K', activePlayer) ||
				lookFor(r + 1, c - 1, 'K', activePlayer) ||
				lookFor(r - 1, c + 1, 'K', activePlayer) ||
				lookFor(r - 1, c - 1, 'K', activePlayer) ||
				lookFor(r, c + 1, 'K', activePlayer) ||
				lookFor(r, c - 1, 'K', activePlayer) ||
				lookFor(r + 1, c, 'K', activePlayer) ||
				lookFor(r - 1, c, 'K', activePlayer)) {
			return true;
		}
		
		// look for rook or queen horiz or vert in any direction
		// increase row, col stays same
		r = otherKing.row + 1;
		while(onBoard(r,c)) {
			if ((getPiece(r, c).getName() == 'R' || getPiece(r, c).getName() == 'Q') && getPiece(r,c).getColor() == activePlayer) return true;
			// if it's a piece other than a space, then you've encountered some other piece and that path is safe
			else if (getPiece(r, c).getName() != ' ') break;
			r++;
		}
		// decrease row, col stays same
		r = otherKing.row - 1;
		while(onBoard(r,c)) {
			if ((getPiece(r, c).getName() == 'R' || getPiece(r, c).getName() == 'Q') && getPiece(r,c).getColor() == activePlayer) return true;
			else if (getPiece(r, c).getName() != ' ') break;
			r--;
		}
		// same row, increase column
		r = otherKing.row;
		c = otherKing.col + 1;
		while(onBoard(r,c)) {
			if ((getPiece(r, c).getName() == 'R' || getPiece(r, c).getName() == 'Q') && getPiece(r,c).getColor() == activePlayer) return true;
			else if (getPiece(r, c).getName() != ' ') break;
			c++;
		}
		// same row, decrease column
		c = otherKing.col - 1;
		while(onBoard(r,c)) {
			if ((getPiece(r, c).getName() == 'R' || getPiece(r, c).getName() == 'Q') && getPiece(r,c).getColor() == activePlayer) return true;
			else if (getPiece(r, c).getName() != ' ') break;
			c--;
		}
		
		// look for bishop or queen diag in any direction
		// increase row, increase col
		r = otherKing.row+1;
		c = otherKing.col+1;
		while(onBoard(r,c)) {
			if ((getPiece(r, c).getName() == 'B' || getPiece(r, c).getName() == 'Q') && getPiece(r,c).getColor() == activePlayer) return true;
			else if (getPiece(r, c).getName() != ' ') break;
			r++;
			c++;
		}
		// increase row, increase col
		r = otherKing.row-1;
		c = otherKing.col-1;
		while(onBoard(r,c)) {
			if ((getPiece(r, c).getName() == 'B' || getPiece(r, c).getName() == 'Q') && getPiece(r,c).getColor() == activePlayer) return true;
			else if (getPiece(r, c).getName() != ' ') break;
			r--;
			c--;
		}
		// increase row, increase col
		r = otherKing.row+1;
		c = otherKing.col-1;
		while(onBoard(r,c)) {
			if ((getPiece(r, c).getName() == 'B' || getPiece(r, c).getName() == 'Q') && getPiece(r,c).getColor() == activePlayer) return true;
			else if (getPiece(r, c).getName() != ' ') break;
			r++;
			c--;
		}
		// increase row, increase col
		r = otherKing.row-1;
		c = otherKing.col+1;
		while(onBoard(r,c)) {
			if ((getPiece(r, c).getName() == 'B' || getPiece(r, c).getName() == 'Q') && getPiece(r,c).getColor() == activePlayer) return true;
			else if (getPiece(r, c).getName() != ' ') break;
			r--;
			c++;
		}
		
		return false;
	}
	
	// hasCheck checks whether current player has a checkmate
	boolean hasCheckmate() {
		return false;
		// TEMP fill with logic for checking for checkmate
	}
	
	// makeMove tries to make a move and returns true if move is made or false if not
	boolean makeMove(String m, BufferedReader in, PrintWriter out) throws IOException {
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
				ChessBoard.broadcast(getPiece(toR,toC)+"has been captured.");
				getPiece(to).remove();
				ChessPiece temp = getPiece(to);
				board[toR][toC] = getPiece(from);
				board[fromR][fromC] = temp;
			}
			
			// Promotion
			// If the pawn reaches a square on the back rank of the opponent, it promotes to the player's choice of a queen, rook, bishop, or knight
			if (getPiece(toR,toC).getName() == 'P' &&
					((getPiece(toR,toC).getColor() == WHITE && toR == 8) || (getPiece(toR,toC).getColor() == BLACK && toR == 1))) {
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
		
		boolean isOnBoard() {
			// checks to make sure a point is on board
			return onBoard(row, col);
		}
		
		boolean isEqual(ChessCoord a) {
			return (row == a.row && col == a.col);
		}
		
		void change(int r, int c) {
			row = r;
			col = c;
		}
		
		boolean reachableBy(char pieceName) {
			return false;
		}
		
		boolean capturableBy(char pieceName) {
			return false;
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

