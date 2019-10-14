package robb.ai;

public class BoardGeneration {

	public static Board importFEN(String fen){
		Board b = new Board(0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, false, false, false, false, true, (byte)-1, (byte)0);
		long[] pieces = b.getPieceArray();
		String[] splits = fen.split(" ");
		final String placement = splits[0].replace("/", ""); 
		byte index = -1;
		for(int p = 0; p < 64; p++){
			index ++;
			char c = placement.charAt(index);
			if(c > '0' && c < '9'){
				p += (Byte.parseByte("" + c) - 1);
			}else{
				byte piece = Utils.pieceToByte(c);
				if(p < 64){
					pieces[piece] |= (1L << (63 - p));
				}
			}
		}
		b.updateWithPieceArray(pieces);
		b.whiteToMove = splits[1].equals("w");
		if(!splits[2].equals("-")){
			b.wCastleQueen = splits[2].contains("Q");
			b.wCastleKing = splits[2].contains("K");
			b.bCastleQueen = splits[2].contains("q");
			b.bCastleKing = splits[2].contains("k");
		}
		b.enPassant = (splits[3].equals("-") ? 64 : Utils.notationToByte(splits[3]));
		b.halfMoveClock = (splits.length > 4 ? Byte.parseByte(splits[4]) : 0);
		return b;
	}
	
//	public static Board makeMove(Board b, final int move, final String src){
//		return makeMove(b, move, false, src);
//	}
//
//	public static Board makeMove(Board b, final int move, final boolean canClearThreefold, final String src){		
//		long newHash = threeFold.size() != 0 ? threeFold.get(threeFold.size() - 1) : Zobrist.getHash(b);
//		final byte from = NewMoveStructure.getFrom(move);
//		final byte to = NewMoveStructure.getTo(move);
//		long[] pieces = b.getPieceArray();
//		final byte promote = NewMoveStructure.getPromote(move);
//		final boolean cap = NewMoveStructure.wasCapture(move);
//		
//		System.out.println(indent + "Make: " + Utils.shortMoveToNotation(move) + " (" + from + ", " + to + ")" + " [" + src + "]");
//		indent += "   ";		
////		if(cap) Utils.printBoard(b, false);
//		
//		final byte p = NewMoveStructure.getPieceMoved(move);	
//		pieces[p] ^= ((1L << from) | (1L << to)); //Hash piece
//		newHash ^= (Zobrist.table[p][from] | Zobrist.table[p][to]);
//		
//		byte enemy = 12;
//		if(cap){
//			for(byte i = (byte)(b.whiteToMove ? 6 : 0); i < (byte)(b.whiteToMove ? 12 : 6); i++){
//				if(((1L << to) & pieces[i]) != 0L){
//					enemy = i;
//					pieces[i] ^= (1L << to); //Hash out piece
//					newHash ^= Zobrist.table[i][to];
//					if(canClearThreefold) threeFold.clear();
//					break;
//				}
//			}
//		}
//		final boolean wasEnPassant = (enemy == 12) && cap && b.enPassant != -1;
//		history.add(HistoryStructure.createMove(from, to, promote, ((b.bCastleQueen ? 1 : 0) << 3) + ((b.bCastleKing ? 1 : 0) << 2) + ((b.wCastleQueen ? 1 : 0) << 1) + (b.wCastleKing ? 1 : 0), enemy, wasEnPassant, b.enPassant, 0));
//		if(enemy != 12){
//			b.halfMoveClock = -1;
//			if(b.enPassant != -1) newHash ^= Zobrist.enPassant[b.enPassant];
//			b.enPassant = -1;
//		}else{ //Update En Passant square or do an En Passant capture
//			if(wasEnPassant){
//				if(p == 0){
//					b.halfMoveClock = -1;
//					pieces[6] ^= (1L << (b.enPassant - 8)); //Hash out black pawn
//					newHash ^= Zobrist.table[6][(b.enPassant - 8)];
//				}else if(p == 6){
//					b.halfMoveClock = -1;
//					pieces[0] ^= (1L << (b.enPassant + 8)); //Hash out white pawn
//					newHash ^= Zobrist.table[0][(b.enPassant + 8)];
//				}
//			}
//			if(b.enPassant != -1) newHash ^= Zobrist.enPassant[b.enPassant];
//			b.enPassant = -1;
//			if(!wasEnPassant){
//				if(p == 0){ //Update square
//					if(((1L << from) & Utils.ranks[1]) != 0L && ((1L << to) & Utils.ranks[3]) != 0L){
//						b.enPassant = (byte)(from + 8);
//						newHash ^= Zobrist.enPassant[b.enPassant];
//					} 
//				}else if(p == 6){
//					if(((1L << from) & Utils.ranks[6]) != 0L && ((1L << to) & Utils.ranks[4]) != 0L){
//						b.enPassant = (byte)(from - 8);
//						newHash ^= Zobrist.enPassant[b.enPassant];
//					} 
//				}
//			}
//		}
//		if(p == 0 || p == 6){
//			b.halfMoveClock = -1; //Pawn push
//			if(canClearThreefold) threeFold.clear();
//		}
//		if(from == 0) b.wCastleKing = false;
//		if(from == 7) b.wCastleQueen = false;
//		if(from == 56) b.bCastleKing = false;
//		if(from == 63) b.bCastleQueen = false;
//		if(p == 5){ //King move
//			if(b.wCastleKing || b.wCastleQueen){
//				if(b.wCastleKing) newHash ^= Zobrist.castling[0];
//				if(b.wCastleQueen) newHash ^= Zobrist.castling[1];
//			}
//			b.wCastleKing = false;
//			b.wCastleQueen = false;
//			if(to == 1){ //Kingside
//				pieces[3] ^= (1L << 0);
//				newHash ^= Zobrist.table[3][0];
//				pieces[3] ^= (1L << 2);
//				newHash ^= Zobrist.table[3][2];
//			}else if(to == 5){ //Queenside
//				pieces[3] ^= (1L << 7);
//				newHash ^= Zobrist.table[3][7];
//				pieces[3] ^= (1L << 4);
//				newHash ^= Zobrist.table[3][4];
//			}
//		}else if(p == 11){
//			if(b.bCastleKing || b.bCastleQueen){
//				if(b.bCastleKing) newHash ^= Zobrist.castling[2];
//				if(b.bCastleQueen) newHash ^= Zobrist.castling[3];
//			}
//			b.bCastleKing = false;
//			b.bCastleQueen = false;
//			if(to == 57){ //Kingside
//				pieces[9] ^= (1L << 56);
//				newHash ^= Zobrist.table[9][56];
//				pieces[9] ^= (1L << 58);
//				newHash ^= Zobrist.table[9][58];
//			}else if(to == 61){ //Queenside
//				pieces[9] ^= (1L << 63);
//				newHash ^= Zobrist.table[9][63];
//				pieces[9] ^= (1L << 60);
//				newHash ^= Zobrist.table[9][60];
//			}
//		}
//		if(from == 0){
//			if(b.wCastleKing){
//				newHash ^= Zobrist.castling[0];
//			}
//			b.wCastleKing = false; //Rook moves
//		}
//		if(from == 7){
//			if(b.wCastleQueen){
//				newHash ^= Zobrist.castling[1];
//			}
//			b.wCastleQueen = false;
//		}
//		if(from == 56){
//			if(b.wCastleKing){
//				newHash ^= Zobrist.castling[2];
//			}
//			b.bCastleKing = false;			
//		}
//		if(from == 63){
//			if(b.bCastleQueen){
//				newHash ^= Zobrist.castling[3];
//			}
//			b.bCastleQueen = false;			
//		}
//		if(promote != 0 && promote != 12){
//			pieces[p] ^= (1L << to); //Hash out pawn
//			newHash ^= Zobrist.table[p][to];
//			pieces[promote] ^= (1L << to); //Hash in new piece
//			newHash ^= Zobrist.table[promote][to];
//		}
//		b.whiteToMove = !b.whiteToMove;
//		newHash ^= Zobrist.whiteToMove;
//		b.updateWithPieceArray(pieces);
//		threeFold.add(newHash);
//		b.halfMoveClock ++;
////		if(cap){
////			System.out.println(indent + "Capturing " + Evaluation.names[enemy] + " with " + Evaluation.names[p]);
////			Utils.printBoard(b, false);
////		}
//		return b;
//	}
//	
//	public static Board undoMove(Board b){			
//		final long u = history.get(history.size() - 1);
//		final short move = (short)(u & 65535); //First 16 bits
//		
//		if(HistoryStructure.wasNullMove(u)){ //Null-move
//			b.enPassant = HistoryStructure.getLastEnPassantSquare(u);
//			b.halfMoveClock = HistoryStructure.getHalfMove(u);
//			b.whiteToMove = !b.whiteToMove;
//			threeFold.remove(threeFold.size() - 1);
//			history.remove(history.size() - 1);
//			
////			indent = indent.substring(0, indent.length() - 3);
////			System.out.println(indent + "Undo: Null");
//			
//			return b;
//		}
//		
//		indent = indent.substring(0, indent.length() - 3);
//		System.out.println(indent + "Undo: " + Utils.shortMoveToNotation(move));
//		
//		long[] pieces = b.getPieceArray();
//		final byte p = Utils.getPieceAt(b, HistoryStructure.getTo(u));
//		pieces[p] ^= (1L << HistoryStructure.getTo(u)); //Hash out piece
//		pieces[p] ^= (1L << HistoryStructure.getFrom(u)); //Hash in piece
//		if(HistoryStructure.getPieceCaptured(u) != 12){
//			pieces[HistoryStructure.getPieceCaptured(u)] ^= (1L << HistoryStructure.getTo(u)); //Hash in captured piece
//		}
//		if(p == 5){ //King move
//			if(move == 67){ //Kingside
//				pieces[3] ^= (1L << 2); //Hash out rook
//				pieces[3] ^= (1L << 0); //Hash in rook
//			}else if(move == 323){ //Queenside
//				pieces[3] ^= (1L << 4); //Hash out rook
//				pieces[3] ^= (1L << 7); //Hash in rook
//			}
//		}else if(p == 11){
//			if(move == 3707){ //Kingside
//				pieces[9] ^= (1L << 58); //Hash out rook
//				pieces[9] ^= (1L << 56); //Hash in rook
//			}else if(move == 3963){ //Queenside
//				pieces[9] ^= (1L << 60); //Hash out rook
//				pieces[9] ^= (1L << 63); //Hash in rook
//			}
//		}
//		if(HistoryStructure.wasEnPassant(u)){ //En Passant
//			if(p == 0){ //White pawn move
//				pieces[6] ^= (1L << (HistoryStructure.getTo(u) - 8)); //Hash in black pawn
//			}else if(p == 6){ //Black pawn move
//				pieces[0] ^= (1L << (HistoryStructure.getTo(u) + 8)); //Hash in black pawn
//			}
//		}else if(HistoryStructure.getPromote(u) != 12){
//			pieces[p] ^= (1L << HistoryStructure.getFrom(u)); //Hash out piece
//			if(((1L << HistoryStructure.getTo(u)) & Utils.ranks[7]) != 0L){ 
//				pieces[0] ^= (1L << HistoryStructure.getFrom(u)); //Hash in white pawn
//			}else{ 
//				pieces[6] ^= (1L << HistoryStructure.getFrom(u)); //Hash in black pawn
//			}
//		}
//		b.updateWithPieceArray(pieces);
//		b.whiteToMove = !b.whiteToMove;
//		
//		final byte castles = HistoryStructure.getCastles(u);
//		b.wCastleKing = ((castles & 1) != 0);
//		b.wCastleQueen = ((castles & 2) != 0);
//		b.bCastleKing = ((castles & 4) != 0);
//		b.bCastleQueen = ((castles & 8) != 0);
//		
//		b.enPassant = HistoryStructure.getLastEnPassantSquare(u);
//		b.halfMoveClock = HistoryStructure.getHalfMove(u);
//		threeFold.remove(threeFold.size() - 1);
//		history.remove(history.size() - 1);
//		return b;
//	}
//	
//	public static Board makeNullMove(Board b){
//		history.add(HistoryStructure.createNullMove(b.enPassant, 0));	
//		b.enPassant = -1;
//		b.halfMoveClock = 0;
//		long newHash = (threeFold.size() != 0 ? threeFold.get(threeFold.size() - 1) : Zobrist.getHash(b));
//		b.whiteToMove = !b.whiteToMove;
//		newHash ^= Zobrist.whiteToMove;
//		threeFold.add(newHash);
//		return b;
//	}

}
