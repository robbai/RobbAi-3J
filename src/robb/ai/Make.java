package robb.ai;

public class Make {

	private static String indent = "";
	public static boolean debug = false;
	
	private static final long[][] castleRook = new long[][] {{4L, 1L, 16L, 128L}, {288230376151711744L, 72057594037927936L, 1152921504606846976L, Long.MIN_VALUE}};

	public static Board makeMove(Board b, int move){
		long newHash = (b.threeFold.size() != 0 ? b.threeFold.get(b.threeFold.size() - 1) : Zobrist.getHash(b));

		int lastEnPassantSquare = b.enPassant;
		int lastHalfMoveClock = b.halfMoveClock;

		if(b.enPassant != 64) newHash ^= Zobrist.enPassant[b.enPassant];
		b.enPassant = 64;
		b.halfMoveClock ++;

		int from = NewMoveStructure.getFrom(move);
		int to = NewMoveStructure.getTo(move);
		int promote = NewMoveStructure.getPromote(move);
		int piece = NewMoveStructure.getPiece(move);
		int capture = NewMoveStructure.getCapture(move);
		
		long fromMask = (1L << from);
		long toMask = (1L << to);
		long moveMask = (fromMask | toMask);

		// 		if(debug){
		// 			System.out.println(indent + "Make: " + Utils.shortMoveToNotation(move) + " (" + from + ", " + to + ")");
		// 			indent += "   ";
		// 		}

		// Hash the piece.
		if(b.whiteToMove){
			if(piece == 0){
				b.WP ^= moveMask;
			}else if(piece == 1){
				b.WN ^= moveMask;
			}else if(piece == 2){
				b.WB ^= moveMask;
			}else if(piece == 3){
				b.WR ^= moveMask;
			}else if(piece == 4){
				b.WQ ^= moveMask;
			}else if(piece == 5){
				b.WK ^= moveMask;
			}
		}else{
			if(piece == 6){
				b.BP ^= moveMask;
			}else if(piece == 7){
				b.BN ^= moveMask;
			}else if(piece == 8){
				b.BB ^= moveMask;
			}else if(piece == 9){
				b.BR ^= moveMask;
			}else if(piece == 10){
				b.BQ ^= moveMask;
			}else if(piece == 11){
				b.BK ^= moveMask;
			}
		}
		newHash ^= Zobrist.table[piece][from];
		newHash ^= Zobrist.table[piece][to];

		// Hash the capture.
		if(capture != 12){
			if(b.whiteToMove){
				if(capture == 6){
					b.BP ^= toMask;
				}else if(capture == 7){
					b.BN ^= toMask;
				}else if(capture == 8){
					b.BB ^= toMask;
				}else if(capture == 9){
					b.BR ^= toMask;
				}else if(capture == 10){
					b.BQ ^= toMask;
				}else if(capture == 11){
					b.BK ^= toMask;
				}
			}else{
				if(capture == 0){
					b.WP ^= toMask;
				}else if(capture == 1){
					b.WN ^= toMask;
				}else if(capture == 2){
					b.WB ^= toMask;
				}else if(capture == 3){
					b.WR ^= toMask;
				}else if(capture == 4){
					b.WQ ^= toMask;
				}else if(capture == 5){
					b.WK ^= toMask;
				}
			}
			newHash ^= Zobrist.table[capture][to];
		}

		boolean enPassant = (lastEnPassantSquare != 64 && capture == 12 && (piece == 0 || piece == 6) && (Math.abs(from - to) % 8) != 0);

		b.history.add(HistoryStructure.createMove(from, to, promote, ((b.bCastleQueen ? 1 : 0) << 3) + ((b.bCastleKing ? 1 : 0) << 2) + ((b.wCastleQueen ? 1 : 0) << 1) + (b.wCastleKing ? 1 : 0), capture, enPassant, lastEnPassantSquare, lastHalfMoveClock));

		if(promote == 12){ 
			// Not a promotion.
			if(piece == 0 || piece == 6){ 
				// Pawn move.
				b.halfMoveClock = 0;
				if(enPassant){
					if(b.whiteToMove){
						b.BP ^= (1L << (lastEnPassantSquare - 8)); // Hash out black pawn.
						newHash ^= Zobrist.table[6][lastEnPassantSquare - 8];
					}else{
						b.WP ^= (1L << (lastEnPassantSquare + 8)); // Hash out white pawn.
						newHash ^= Zobrist.table[0][lastEnPassantSquare + 8];
					}
				}else{
					if(Math.abs(from - to) == 16){ // Double move.
						b.enPassant = (to + (b.whiteToMove ? -8 : 8));
						newHash ^= Zobrist.enPassant[b.enPassant];
					}
				}
			}else if((piece == 5 && from == 3 && (to == 1 || to == 5)) || (piece == 11 && from == 59 && (to == 57 || to == 61))){
				// Castle.
				if(piece == 5){
					if(b.wCastleKing) newHash ^= Zobrist.castling[0];
					if(b.wCastleQueen) newHash ^= Zobrist.castling[1];						
					if(to == 1 && b.wCastleKing){ // Kingside.
						b.WR ^= (1L << 0);
						newHash ^= Zobrist.table[3][0];
						b.WR |= (1L << 2);
						newHash ^= Zobrist.table[3][2];
					}else if(to == 5 && b.wCastleQueen){ // Queenside.
						b.WR ^= (1L << 7);
						newHash ^= Zobrist.table[3][7];
						b.WR |= (1L << 4);
						newHash ^= Zobrist.table[3][4];
					}
					b.wCastleKing = false;
					b.wCastleQueen = false;
				}else{
					if(b.bCastleKing) newHash ^= Zobrist.castling[2];
					if(b.bCastleQueen) newHash ^= Zobrist.castling[3];
					if(to == 57 && b.bCastleKing){ // Kingside.
						b.BR ^= (1L << 56);
						newHash ^= Zobrist.table[9][56];
						b.BR |= (1L << 58);
						newHash ^= Zobrist.table[9][58];
					}else if(to == 61 && b.bCastleQueen){ // Queenside.
						b.BR ^= (1L << 63);
						newHash ^= Zobrist.table[9][63];
						b.BR |= (1L << 60);
						newHash ^= Zobrist.table[9][60];
					}
					b.bCastleKing = false;
					b.bCastleQueen = false;
				}
			}
		}else{ 
			// Promotion.
			b.halfMoveClock = 0;

			// Hash out pawn.
			if(b.whiteToMove){
				b.WP ^= (1L << to);
			}else{
				b.BP ^= (1L << to);
			}
			newHash ^= Zobrist.table[piece][to];

			// Hash in new piece.
			if(b.whiteToMove){
				if(promote == 1){
					b.WN ^= toMask;
				}else if(promote == 2){
					b.WB ^= toMask;
				}else if(promote == 3){
					b.WR ^= toMask;
				}else if(promote == 4){
					b.WQ ^= toMask;
				}
			}else{
				if(promote == 7){
					b.BN ^= toMask;
				}else if(promote == 8){
					b.BB ^= toMask;
				}else if(promote == 9){
					b.BR ^= toMask;
				}else if(promote == 10){
					b.BQ ^= toMask;
				}
			}
			newHash ^= Zobrist.table[promote][to];
		}

		// King squares removing castling availability.
		if(from == 3 || to == 3){
			if(b.wCastleKing){
				newHash ^= Zobrist.castling[0];
				b.wCastleKing = false;
			}
			if(b.wCastleQueen){
				newHash ^= Zobrist.castling[1];
				b.wCastleQueen = false;
			}
		}
		if(from == 59 || to == 59){
			if(b.bCastleKing){
				newHash ^= Zobrist.castling[2];
				b.bCastleKing = false;
			}
			if(b.bCastleQueen){
				newHash ^= Zobrist.castling[3];
				b.bCastleQueen = false;
			}
		}

		// Rook squares removing castling availability.
		if((from == 0 || to == 0) && b.wCastleKing){
			newHash ^= Zobrist.castling[0];
			b.wCastleKing = false;
		}
		if((from == 7 || to == 7) && b.wCastleQueen){
			newHash ^= Zobrist.castling[1];
			b.wCastleQueen = false;
		}
		if((from == 56 || to == 56) && b.wCastleKing){
			newHash ^= Zobrist.castling[2];
			b.bCastleKing = false;
		}
		if((from == 63 || to == 63) && b.bCastleQueen){
			newHash ^= Zobrist.castling[3];
			b.bCastleQueen = false;
		}

		b.whiteToMove = !b.whiteToMove;
		newHash ^= Zobrist.whiteToMove;

		b.threeFold.add(newHash);

		// 		if(debug) Utils.printBoard(b, false);

		return b;
	}

	public static Board undoMove(Board b){	
		long u = b.history.get(b.history.size() - 1);

		if(debug) indent = indent.substring(0, indent.length() - 3);

		// Null-move.
		if(HistoryStructure.wasNullMove(u)){ 
			if(debug) System.out.println(indent + "Undo: Null-move");

			b.enPassant = HistoryStructure.getLastEnPassantSquare(u);
			b.halfMoveClock = HistoryStructure.getHalfMove(u);
			b.whiteToMove = !b.whiteToMove;
			b.threeFold.remove(b.threeFold.size() - 1);
			b.history.remove(b.history.size() - 1);	
			return b;
		}

		int from = HistoryStructure.getFrom(u);
		int to = HistoryStructure.getTo(u);
		int piece = Utils.getPieceAt(b, to); // TODO
		int promote = HistoryStructure.getPromote(u);

		if(debug) System.out.println(indent + "Undo: " + Utils.moveToNotation(from + (to << 6) + (promote << 12)));

//		if(piece == 12){
//			System.out.println(Utils.moveToNotation(from + (to << 6) + (promote << 12)));
//			System.out.println("To " + to + ", From = " + from + ", Piece = " + piece + ", Promote = " + promote);
//			Utils.printBoard(b, true);
//			for(int i = 0; i < b.history.size(); i++){
//				System.out.println(i + ": " + HistoryStructure.toString(b.history.get(i)));
//			}
//		}
		
		long fromMask = (1L << from);
		long toMask = (1L << to);

		// Hash piece.
		long transferMask = (promote == 12 ? (fromMask | toMask) : toMask);
		if(b.whiteToMove){
			if(piece == 6){
				b.BP ^= transferMask;
			}else if(piece == 7){
				b.BN ^= transferMask;
			}else if(piece == 8){
				b.BB ^= transferMask;
			}else if(piece == 9){
				b.BR ^= transferMask;
			}else if(piece == 10){
				b.BQ ^= transferMask;
			}else if(piece == 11){
				b.BK ^= transferMask;
			}
		}else{
			if(piece == 0){
				b.WP ^= transferMask;
			}else if(piece == 1){
				b.WN ^= transferMask;
			}else if(piece == 2){
				b.WB ^= transferMask;
			}else if(piece == 3){
				b.WR ^= transferMask;
			}else if(piece == 4){
				b.WQ ^= transferMask;
			}else if(piece == 5){
				b.WK ^= transferMask;
			}
		}

		byte capture = HistoryStructure.getPieceCaptured(u);
		if(capture != 12){
			// Hash in captured piece.
			if(b.whiteToMove){
				if(capture == 0){
					b.WP ^= toMask;
				}else if(capture == 1){
					b.WN ^= toMask;
				}else if(capture == 2){
					b.WB ^= toMask;
				}else if(capture == 3){
					b.WR ^= toMask;
				}else if(capture == 4){
					b.WQ ^= toMask;
				}else if(capture == 5){
					b.WK ^= toMask;
				}
			}else{
				if(capture == 6){
					b.BP ^= toMask;
				}else if(capture == 7){
					b.BN ^= toMask;
				}else if(capture == 8){
					b.BB ^= toMask;
				}else if(capture == 9){
					b.BR ^= toMask;
				}else if(capture == 10){
					b.BQ ^= toMask;
				}else if(capture == 11){
					b.BK ^= toMask;
				}
			}
		}

		int fromTo = (from + (to << 6));

		if(piece == 5){ // King move.
			if(fromTo == 67){ // Kingside.
				b.WR ^= castleRook[0][0]; // Hash out rook.
				b.WR ^= castleRook[0][1]; // Hash in rook.
			}else if(fromTo == 323){ // Queenside.
				b.WR ^= castleRook[0][2]; // Hash out rook.
				b.WR ^= castleRook[0][3]; // Hash in rook.
			}
		}else if(piece == 11){
			if(fromTo == 3707){ // Kingside.
				b.BR ^= castleRook[1][0]; // Hash out rook.
				b.BR ^= castleRook[1][1]; // Hash in rook.
			}else if(fromTo == 3963){ // Queenside.
				b.BR ^= castleRook[1][2]; // Hash out rook.
				b.BR ^= castleRook[1][3]; // Hash in rook.
			}
		}else if(promote != 12){
//			pieces[piece] ^= fromMask; // Hash out piece.
			if(b.whiteToMove){ 
				b.BP ^= fromMask; // Hash in black pawn.
			}else{ 
				b.WP ^= fromMask; // Hash in white pawn.
			}
		}else if(HistoryStructure.wasEnPassant(u)){ // En Passant.
			if(b.whiteToMove){ // Black pawn move.
				b.WP ^= (1L << (to + 8)); // Hash in black pawn.
			}else{// White pawn move.
				b.BP ^= (1L << (to - 8)); // Hash in black pawn.
			}
		}

		byte castles = HistoryStructure.getCastles(u);
		b.wCastleKing = ((castles & 1) != 0);
		b.wCastleQueen = ((castles & 2) != 0);
		b.bCastleKing = ((castles & 4) != 0);
		b.bCastleQueen = ((castles & 8) != 0);

		b.enPassant = HistoryStructure.getLastEnPassantSquare(u);
		b.halfMoveClock = HistoryStructure.getHalfMove(u);
		b.whiteToMove = !b.whiteToMove;
		b.threeFold.remove(b.threeFold.size() - 1);
		b.history.remove(b.history.size() - 1);
		
		return b;
	}

	public static Board makeNullMove(Board b){
		if(debug){
			System.out.println(indent + "Make: Null-move");
			indent += "   ";
		}	

		b.history.add(HistoryStructure.createNullMove(b.enPassant, b.halfMoveClock));

		// Hash.
		long newHash = (b.threeFold.size() != 0 ? b.threeFold.get(b.threeFold.size() - 1) : Zobrist.getHash(b));
		if(b.enPassant != 64) newHash ^= Zobrist.enPassant[b.enPassant];
		newHash ^= Zobrist.whiteToMove;
		b.threeFold.add(newHash);

		b.enPassant = 64;
		b.halfMoveClock = 0;
		b.whiteToMove = !b.whiteToMove;

		return b;
	}

}
