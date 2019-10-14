package robb.ai;

public class Make {
	
	private static String indent = "";
	public static boolean debug = false;
	
	public static Board makeMove(Board b, int move){
//		if(b.threeFold == null) b.threeFold = new ArrayList<Long>();
		long newHash = (b.threeFold.size() != 0 ? b.threeFold.get(b.threeFold.size() - 1) : Zobrist.getHash(b));
		
		byte lastEnPassantSquare = b.enPassant;
		byte lastHalfMoveClock = b.halfMoveClock;
		
		if(b.enPassant != 64) newHash ^= Zobrist.enPassant[b.enPassant];
		b.enPassant = 64;
		b.halfMoveClock ++;
		
		byte from = NewMoveStructure.getFrom(move);
		byte to = NewMoveStructure.getTo(move);
		
// 		if(debug){
// 			System.out.println(indent + "Make: " + Utils.shortMoveToNotation(move) + " (" + from + ", " + to + ")");
// 			indent += "   ";
// 		}
		
		byte piece = Utils.getPieceAt(b, NewMoveStructure.getFrom(move));
		long[] pieces = b.getPieceArray();
		
		pieces[piece] ^= ((1L << from) | (1L << to)); // Hash the piece
		newHash ^= Zobrist.table[piece][from];
		newHash ^= Zobrist.table[piece][to];
		
		int pieceCaptured = 12;
		for(int enemy = (b.whiteToMove ? 6 : 0); enemy < (b.whiteToMove ? 12 : 6); enemy++){
			if(0 != (pieces[enemy] & (1L << to))){
				pieces[enemy] ^= (1L << to); // Hash out the captured piece
				newHash ^= Zobrist.table[enemy][to];
				pieceCaptured = enemy; // Set the captured piece
//				if(canClearThreefold) b.threeFold.clear();
				b.halfMoveClock = 0;
				break;
			}
		}
		
		byte promote = NewMoveStructure.getPromote(move);	
		boolean ep = (lastEnPassantSquare != 64 && pieceCaptured == 12 && (piece == 0 || piece == 6) && (Math.abs(from - to) % 8) != 0);
		
		b.history.add(HistoryStructure.createMove(from, to, promote, ((b.bCastleQueen ? 1 : 0) << 3) + ((b.bCastleKing ? 1 : 0) << 2) + ((b.wCastleQueen ? 1 : 0) << 1) + (b.wCastleKing ? 1 : 0), pieceCaptured, ep, lastEnPassantSquare, lastHalfMoveClock));

		if(promote == 12){ 
			// Not promote
			if(piece == 0 || piece == 6){ 
				// Pawn move
				b.halfMoveClock = 0;
//				if(canClearThreefold) b.threeFold.clear();
				
				if(ep){
					if(b.whiteToMove){
						pieces[6] ^= (1L << (lastEnPassantSquare - 8)); // Hash out black pawn
						newHash ^= Zobrist.table[6][lastEnPassantSquare - 8];
					}else{
						pieces[0] ^= (1L << (lastEnPassantSquare + 8)); // Hash out white pawn
						newHash ^= Zobrist.table[0][lastEnPassantSquare + 8];
					}
				}else{
					if(Math.abs(from - to) == 16){ // Double move
						b.enPassant = (byte)(to + (b.whiteToMove ? -8 : 8));
						newHash ^= Zobrist.enPassant[b.enPassant];
					}
				}
			}else if((piece == 5 && from == 3 && (to == 1 || to == 5)) || (piece == 11 && from == 59 && (to == 57 || to == 61))){
				// Castle
				if(piece == 5){
					if(b.wCastleKing) newHash ^= Zobrist.castling[0];
					if(b.wCastleQueen) newHash ^= Zobrist.castling[1];						
					if(to == 1 && b.wCastleKing){ // Kingside
						pieces[3] ^= (1L << 0);
						newHash ^= Zobrist.table[3][0];
						pieces[3] |= (1L << 2);
						newHash ^= Zobrist.table[3][2];
					}else if(to == 5 && b.wCastleQueen){ // Queenside
						pieces[3] ^= (1L << 7);
						newHash ^= Zobrist.table[3][7];
						pieces[3] |= (1L << 4);
						newHash ^= Zobrist.table[3][4];
					}
					b.wCastleKing = false;
					b.wCastleQueen = false;
				}else{
					if(b.bCastleKing) newHash ^= Zobrist.castling[2];
					if(b.bCastleQueen) newHash ^= Zobrist.castling[3];
					if(to == 57 && b.bCastleKing){ // Kingside
						pieces[9] ^= (1L << 56);
						newHash ^= Zobrist.table[9][56];
						pieces[9] |= (1L << 58);
						newHash ^= Zobrist.table[9][58];
					}else if(to == 61 && b.bCastleQueen){ // Queenside
						pieces[9] ^= (1L << 63);
						newHash ^= Zobrist.table[9][63];
						pieces[9] |= (1L << 60);
						newHash ^= Zobrist.table[9][60];
					}
					b.bCastleKing = false;
					b.bCastleQueen = false;
				}
			}
		}else{ 
			// Promotion
			b.halfMoveClock = 0;
//			if(canClearThreefold) b.threeFold.clear();
			
			pieces[piece] ^= (1L << to); // Hash out pawn
			newHash ^= Zobrist.table[piece][to];
			pieces[promote] ^= (1L << to); // Hash in new piece
			newHash ^= Zobrist.table[promote][to];
		}
		
		// King squares removing castling availability
		if(from == 3 || to == 3){
			if(b.wCastleKing) newHash ^= Zobrist.castling[0];
			b.wCastleKing = false;
			if(b.wCastleQueen) newHash ^= Zobrist.castling[1];
			b.wCastleQueen = false;
		}
		if(from == 59 || to == 59){
			if(b.bCastleKing) newHash ^= Zobrist.castling[2];
			b.bCastleKing = false;
			if(b.bCastleQueen) newHash ^= Zobrist.castling[3];
			b.bCastleQueen = false;
		}

		// Rook squares removing castling availability
		if(from == 0 || to == 0){
			if(b.wCastleKing) newHash ^= Zobrist.castling[0];
			b.wCastleKing = false; 
		}
		if(from == 7 || to == 7){
			if(b.wCastleQueen) newHash ^= Zobrist.castling[1];
			b.wCastleQueen = false;
		}
		if(from == 56 || to == 56){
			if(b.wCastleKing) newHash ^= Zobrist.castling[2];
			b.bCastleKing = false;			
		}
		if(from == 63 || to == 63){
			if(b.bCastleQueen) newHash ^= Zobrist.castling[3];
			b.bCastleQueen = false;			
		}
		
		b.whiteToMove = !b.whiteToMove;
		newHash ^= Zobrist.whiteToMove;
				
		b.updateWithPieceArray(pieces);
		b.threeFold.add(newHash);
		
// 		if(debug) Utils.printBoard(b, false);
		
		return b;
	}
	
	public static Board undoMove(Board b){	
		long u = b.history.get(b.history.size() - 1);
		
		if(debug) indent = indent.substring(0, indent.length() - 3);
		
		if(HistoryStructure.wasNullMove(u)){ // Null-move
			if(debug) System.out.println(indent + "Undo: Null-move");
			
			b.enPassant = HistoryStructure.getLastEnPassantSquare(u);
			b.halfMoveClock = HistoryStructure.getHalfMove(u);
			b.whiteToMove = !b.whiteToMove;
			b.threeFold.remove(b.threeFold.size() - 1);
			b.history.remove(b.history.size() - 1);	
			return b;
		}
		
		long[] pieces = b.getPieceArray();
		
		byte from = HistoryStructure.getFrom(u);
		byte to = HistoryStructure.getTo(u);
		byte p = Utils.getPieceAt(b, to);
		byte promote = HistoryStructure.getPromote(u);
		
		if(debug) System.out.println(indent + "Undo: " + Utils.shortMoveToNotation(from + (to << 6) + (promote << 12)));
		
		if(p == 12){
			System.out.println(Utils.shortMoveToNotation(from + (to << 6) + (promote << 12)));
			System.out.println("To " + to + ", From = " + from + ", Piece = " + p + ", Promote = " + promote);
			Utils.printBoard(b, true);
			for(int i = 0; i < b.history.size(); i++){
				System.out.println(i + ": " + HistoryStructure.toString(b.history.get(i)));
			}
		}
		
		pieces[p] &= ~(1L << to); // Hash piece
		pieces[p] |= (1L << from); // Hash piece
		
		byte captured = HistoryStructure.getPieceCaptured(u); 
		if(captured != 12){
			pieces[captured] |= (1L << to); // Hash in captured piece
		}
		
		int fromTo = (from + (to << 6));
		
		if(p == 5){ // King move
			if(fromTo == 67){ // Kingside
				pieces[3] ^= (1L << 2); // Hash out rook
				pieces[3] ^= (1L << 0); // Hash in rook
			}else if(fromTo == 323){ // Queenside
				pieces[3] ^= (1L << 4); // Hash out rook
				pieces[3] ^= (1L << 7); // Hash in rook
			}
		}else if(p == 11){
			if(fromTo == 3707){ // Kingside
				pieces[9] ^= (1L << 58); // Hash out rook
				pieces[9] ^= (1L << 56); // Hash in rook
			}else if(fromTo == 3963){ // Queenside
				pieces[9] ^= (1L << 60); // Hash out rook
				pieces[9] ^= (1L << 63); // Hash in rook
			}
		}else if(HistoryStructure.wasEnPassant(u)){ // En Passant
			if(p == 0){ // White pawn move
				pieces[6] ^= (1L << (to - 8)); // Hash in black pawn
			}else if(p == 6){ // Black pawn move
				pieces[0] ^= (1L << (to + 8)); // Hash in black pawn
			}
		}else if(promote != 12){
			pieces[p] ^= (1L << from); // Hash out piece
			if(((1L << to) & Utils.ranks[7]) != 0L){ 
				pieces[0] ^= (1L << HistoryStructure.getFrom(u)); // Hash in white pawn
			}else{ 
				pieces[6] ^= (1L << HistoryStructure.getFrom(u)); // Hash in black pawn
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
		b.updateWithPieceArray(pieces);
		return b;
	}
	
	public static Board makeNullMove(Board b){
		if(debug){
			System.out.println(indent + "Make: Null-move");
			indent += "   ";
		}	
		
		b.history.add(HistoryStructure.createNullMove(b.enPassant, b.halfMoveClock));
		
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
