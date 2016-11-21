package com.caffeine.logic;

import com.caffeine.Chess;
import com.caffeine.view.Core;
import com.caffeine.view.ViewUtils;

import java.util.*;

public class Game {
    public Piece[] pieces = new Piece[32];
    public ArrayList<String> moveHistory = new ArrayList<String>();
    public boolean gameStarted = false;
    public int gameResult = 0; // 0 = ongoing, 1 = white won, 2 = black won, 3 = draw

	private int mode = 0; // 0 = easy, 1 = medium, 2 = hard
	private static final int[] timeoutsForModes = {5, 100, 200};

	private static final String startFEN = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
    
	protected HashMap<String,String> pgnTags;

	protected boolean whiteActive;
	protected boolean userWhite;
	protected String captByBlack;
	protected String captByWhite;

	protected String lastFEN = null;
	protected String currFEN = startFEN;

	protected String enPassantLoc = "-";
	protected Piece enPassantPiece = null;

	// 	Unicode chess pieces
	protected static final String king = "\u265A";
	protected static final String queen = "\u265B";
	protected static final String rook = "\u265C";
	protected static final String bishop = "\u265D";
	protected static final String knight = "\u265E";
	protected static final String pawn = "\u265F";

	public Game() {
		whiteActive = true;
		userWhite = true;
		captByBlack = "";
		captByWhite = "";
		initializesPieces();
		initializePGN();
	}

	/**
	 * 	Populates the pieces array with the standard 32 chess pieces
	 */
	public void initializesPieces() {
		setPiecesFromFEN(startFEN);
	}

	/**
	 * Initialize PGN tags
	 */
	public void initializePGN() {
		pgnTags = new HashMap<String,String>();
		pgnTags.put("Event", "CS1530");
		pgnTags.put("Site", "Pittsburgh, PA, USA");
		pgnTags.put("Date", "Fall 2016");
		pgnTags.put("Round", "420");
		pgnTags.put("FEN", currFEN);
	}

	/**
	 * Starts the game.
	 */
	public void startGame() {
		gameStarted = true;
	}

	/**
	 * 	Sets the user as either white or black
	 * 	
	 *  @param side The color the user will play as
	 */
	public void setSide(String side) {
		if (side.equals("white")) {
			userWhite = true;
			pgnTags.put("White", "User");
			pgnTags.put("Black", "CPU");
		}
		else {
			userWhite = false;
			pgnTags.put("White", "CPU");
			pgnTags.put("Black", "User");
		}
	}

	/**
	 * Sets the mode based on the string passed in.
	 * Default is easy if this method is not called.
	 *
	 * @param mode  The mode to determine difficulty of the CPU opponent.
	 */
	public void setMode(int mode) {
		this.mode = mode;
	}

	/**
	 * 	Getter for whether the user is playing as white or black
	 * 	
	 *  @return true if the user is playing as white, false if black
	 */
	public boolean userWhite() {
		return userWhite;
	}

	/** 
	 * Add the piece passed in to one of the captured strings.
	 * 
	 * @param taken  The piece taken.
	 */
	public void takePiece(Piece taken) {
		taken.moveTo(-1,-1); // indicates piece has been taken
		if (taken.isWhite())
			captureWhitePiece(taken.getType());
		else
			captureBlackPiece(taken.getType());
	}

	/**
	 * 	Adds a piece to the list of black pieces taken
	 * 	
	 *	@param piece The newly taken black piece
	 */
	public void captureBlackPiece(String piece) {
		captByWhite = captByWhite.concat(" " + piece);
		Core.takenPanel.setCaptByWhite(captByWhite);
	}

	/**
	 * 	Adds a piece to the list of white pieces taken
	 * 
	 * 	@param piece The newly taken white piece
	 */
	public void captureWhitePiece(String piece) {
		captByBlack = captByBlack.concat(" " + piece);
		Core.takenPanel.setCaptByBlack(captByBlack);
	}

	/**
	 * 	Getter for the current list of pieces taken by black
	 * @return list of captured white pieces as a String
	 */
	public String getCaptByBlack() {
		return captByBlack;
	}

	/**
	 * 	Getter for the current list of pieces taken by white
	 * @return list of captured black pieces as a String
	 */
	public String getCaptByWhite() {
		return captByWhite;
	}

	/**
	 * 	Adds a move to the move history panel
	 * 
	 * 	@param currMove  The newly made move
	 */
	public void addToMoveHistory(String currMove) {
		moveHistory.add(currMove);
		Core.historyPanel.updateMoveHistory(moveHistory);
	}

	/**
	 * 	Move a piece from one set of coordinates to another
	 *  @param  oldRank The current horizontal coordinate
	 *  @param  oldFile The current vertical coordinate
	 *  @param  newRank The new horizontal coordinate to move to
	 *  @param  newFile The new vertical coordinate to move to
	 *  @return true if move is successful, false otherwise
	 */
	public boolean move(int oldRank, int oldFile, int newRank, int newFile) {
		String oldLoc = (char)(oldFile+97) + "" + (oldRank+1);
		String newLoc = (char)(newFile+97) + "" + (newRank+1);

		if (Chess.engine.move(oldLoc+newLoc)) {
			boolean pieceTaken = doMove(oldRank, oldFile, newRank, newFile);
			lastFEN = currFEN;
			currFEN = Chess.engine.getFEN();
			pgnTags.put("FEN", currFEN);

			boolean kingside = false;
			boolean queenside = false;
			if (!currFEN.split(" ")[2].equals(lastFEN.split(" ")[2])) {
				if (oldLoc.equals("e1") || oldLoc.equals("e8")) {
					if (newLoc.equals("g1") || newLoc.equals("g8")) {
						kingside = true;
					}
					if (newLoc.equals("c1") || newLoc.equals("c8")) {
						queenside = true;
					}
				}				
			}

			if (kingside) {
				addToMoveHistory("O-O");
			} else if (queenside) {
				addToMoveHistory("O-O-O");
			} else {
				if (pieceTaken) {
					addToMoveHistory(oldLoc + "x" + newLoc);
				} else {
					addToMoveHistory(oldLoc+newLoc);
				}
			}

			return true;
		}
		return false;
	}

	/**
	 * 	Move a piece as decided by the engine and return the move made.
	 *
	 *  @return The move made if successful. null otherwise.
	 */
	public String cpuMove() {
		int timeout = timeoutsForModes[mode];
		String move = Chess.engine.cpuMove(timeout);
		if (move.equals("(none)")) return null;
		char[] moveData = move.toCharArray();
		int oldRank = (int) moveData[1] - '1';
		int oldFile = (int) moveData[0] - 'a';
		int newRank = (int) moveData[3] - '1';
		int newFile = (int) moveData[2] - 'a';
		boolean pieceTaken = doMove(oldRank, oldFile, newRank, newFile);
		lastFEN = currFEN;
		currFEN = Chess.engine.getFEN();
		pgnTags.put("FEN", currFEN);

		if (pieceTaken) {
			String moveString = moveData[0] + "" + moveData[1] + "x" + moveData[2] + "" + moveData[3];
			addToMoveHistory(moveString);
		} else {
			addToMoveHistory(move);
		}
		return move;
	}

	/**
	 * Do the move functionality: taking pieces, en passant checking, setting pieces array
	 *  @param  oldRank The current horizontal coordinate
	 *  @param  oldFile The current vertical coordinate
	 *  @param  newRank The new horizontal coordinate to move to
	 *  @param  newFile The new vertical coordinate to move to
	 *  @return true if move takes a piece, false otherwise
	 */
	public boolean doMove(int oldRank, int oldFile, int newRank, int newFile) {
		boolean pieceTaken = false;

		Piece taken = getPieceMatching(newRank,newFile);
		Piece moving = getPieceMatching(oldRank, oldFile);
		
		if (taken != null) {
			takePiece(taken);
			pieceTaken = true;
		}

		String fen = Chess.engine.getFEN();

		// check for en passant and taking of en passant
		if (!enPassantLoc.equals("-")) {
			if (moving.getType().equals(pawn)) {
				int enPassantRank = (int) enPassantLoc.charAt(1) - '1';
				int enPassantFile = (int) enPassantLoc.charAt(0) - 'a';
				if (newRank == enPassantRank && newFile == enPassantFile) {
					pieceTaken = true;
					if (moving.isWhite()) {
						takePiece(getPieceMatching(newRank-1,newFile));
					} else {
						takePiece(getPieceMatching(newRank+1,newFile));
					}
				}
			}
		}
		enPassantLoc = fen.split(" ")[3];

		setPiecesFromFEN(fen);

		return pieceTaken;
	}

	/**
	 * 	Gets the piece at a given position
	 *  @param  rank 	The given x coordinate
	 *  @param  file 	The given y coordinate
	 *  @return a Piece if a Piece exists at the given position, null if not
	 */
	public Piece getPieceMatching(int rank, int file) {
		for (Piece p : pieces) {
			if (p != null && p.getRank() == rank) {
				if (p.getFile() == file) {
					return p;
				}
			}
		}
		return null;
	}

	/**
	 * Sets pieces array based on data from FEN string passed in
	 *
	 * @param fen  The FEN string that determines piece placement
	 */
	public void setPiecesFromFEN(String fen) {

		// new array for pieces
		Piece[] pieces = new Piece[32];
		// Get board as String[8] where [0] is row 8 and [7] is row 1.
		String[] board = fen.split(" ", 2)[0].split("/");
		// The current line examined in the for loop, and its length
        String currentLine;
        // The current character in the currentLine String AND its num val.
        char currentChar;
        int charVal;
        // The progress through the current board row
        int rowCursor;
        // Overall progress through pieces
        int pieceInd = 0;
        for (int i = 0; i < 8; i++) {
        	currentLine = board[i];
        	rowCursor = 0;
        	for (int j = 0; j < currentLine.length(); j++) {
        		currentChar = currentLine.charAt(j);
        		charVal = (int) currentChar - '0';
        		if (charVal > 9) { // piece character
        			if (rowCursor == 0) {
        				pieces[pieceInd] = new Piece(typeToUnicode(currentChar),
        										typeToSide(currentChar),7-i,j);
        			} else {
        				pieces[pieceInd] = new Piece(typeToUnicode(currentChar),
        										typeToSide(currentChar),7-i,rowCursor);
        			}
        			pieceInd++;
        			rowCursor++;
        		} else { // number of empty squares
        			rowCursor += charVal;
        		}
        	}
        }
        for (int i = pieceInd; i < 32; i++) {
        	// taken pieces no longer in array, these are placeholders
        	pieces[pieceInd] = new Piece("null","null",-1,-1);
        }

        this.pieces = pieces;
	}

	/** 
	 * Converts from type KQRBNPkqrbnp to equivalent Unicode characters
	 *
	 * @param type  the type to be converted
	 */
	public String typeToUnicode(char type) {

		switch(type) {
			case 'K':
			case 'k': return king;
			case 'Q':
			case 'q': return queen;
			case 'R':
			case 'r': return rook;
			case 'B':
			case 'b': return bishop;
			case 'N':
			case 'n': return knight;
			case 'P':
			case 'p': return pawn;
			default: return "null";
		}
	}

	/** 
	 * Converts from type KQRBNPkqrbnp to equivalent side black/white
	 *
	 * @param type  the type to be converted
	 */
	public String typeToSide(char type) {
		if (((int) type) < 90) return "white";
		else return "black";
	}

	/**
	 * For game loading.
	 *
	 * @param fen  The fen string to load.
	 */
	public void loadFEN(String fen) {
		setPiecesFromFEN(fen);
		ViewUtils.refreshBoard();
		enPassantLoc = fen.split(" ")[3];
		Chess.engine.setFEN(fen);
		// TO DO: set taken from fen
		Character[] pieces = {'K','Q','R','B','N','P','k','q','r','b','n','p'};
		ArrayList<Character> possTaken = new ArrayList<Character>(Arrays.asList(pieces));
		for (int i = 0; i < 7; i++) {
			possTaken.add(Character.valueOf('p'));
			possTaken.add(Character.valueOf('P'));
		}
		char[] fenArray = fen.split(" ")[0].toCharArray();
		for (int i = 0; i < fenArray.length; i++) {
			if (fenArray[i] > '9') {
				possTaken.remove(Character.valueOf(fenArray[i]));
			}
		}
		for (Character c : possTaken) {
			Piece p = new Piece(typeToUnicode(c), typeToSide(c),-1,-1);
			takePiece(p);
		}

	}
}