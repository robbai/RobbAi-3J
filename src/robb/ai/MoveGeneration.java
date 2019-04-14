package robb.ai;

import java.util.ArrayList;

public class MoveGeneration {
	
	private final static short[] knightDelta = new short[] {15, 6, -10, -17, -15, -6, 10, 17};
	private final static short[] kingDelta = new short[] {9, 8, 7, 1, -1, -7, -8, -9};
	public static long[] northMoves = new long[64];
	public static long[] eastMoves = new long[64];
	public static long[] southMoves = new long[64];
	public static long[] westMoves = new long[64];
	public static long[] northEastMoves = new long[64];
	public static long[] southEastMoves = new long[64];
	public static long[] southWestMoves = new long[64];
	public static long[] northWestMoves = new long[64];
	public static long[] knightMoves = new long[64];
	public static long[] kingMoves = new long[64];
	public static long[][] pawnAttackMasks = new long[2][64];
	
	public static ArrayList<Short> getAllMoves(final Board b){
		return getAllMoves(b, true, false);
	}
	
	public static ArrayList<Short> getAllLoudMoves(final Board b){
		return getAllMoves(b, false, true);
	}
	
	private static ArrayList<Short> getAllMoves(final Board b, final boolean allowCastling, final boolean loudOnly){
		ArrayList<Short> moves = new ArrayList<Short>();
		if(b == null){
			System.out.println("info string Error: Board is Null");
			return moves;
		}
		
		final long friendlyPieces = b.whiteToMove ? (b.WP | b.WN | b.WB | b.WR | b.WQ | b.WK) : (b.BP | b.BN | b.BB | b.BR | b.BQ | b.BK);
		final long enemyPieces = b.whiteToMove ? (b.BP | b.BN | b.BB | b.BR | b.BQ | b.BK) : (b.WP | b.WN | b.WB | b.WR | b.WQ | b.WK);
		
		//Castling
		if(allowCastling){
			if(b.whiteToMove){
				if(b.wCastleKing && (6L & (friendlyPieces | enemyPieces)) == 0L && isCastlingLegal(b, true)) moves.add(NewMoveStructure.createMove((byte)3, (byte)1, (byte)12));
				if(b.wCastleQueen && (112L & (friendlyPieces | enemyPieces)) == 0L && isCastlingLegal(b, false)) moves.add(NewMoveStructure.createMove((byte)3, (byte)5, (byte)12));
			}else{
				if(b.bCastleKing && (432345564227567616L & (friendlyPieces | enemyPieces)) == 0L && isCastlingLegal(b, true)) moves.add(NewMoveStructure.createMove((byte)59, (byte)57, (byte)12));
				if(b.bCastleQueen && (8070450532247928832L & (friendlyPieces | enemyPieces)) == 0L && isCastlingLegal(b, false)) moves.add(NewMoveStructure.createMove((byte)59, (byte)61, (byte)12));
			}
		}
		
		//Pawn moves
		long piecesToVisit = b.whiteToMove ? b.WP : b.BP;
		while(piecesToVisit != 0L){
			int i = Long.numberOfTrailingZeros(piecesToVisit);			
			if(b.whiteToMove){
				moves = generateWhitePawnMoves(b, moves, (byte)i, loudOnly, friendlyPieces | enemyPieces, enemyPieces); 
			}else{
				moves = generateBlackPawnMoves(b, moves, (byte)i, loudOnly, friendlyPieces | enemyPieces, enemyPieces);
			}			
			piecesToVisit ^= (1L << i);
		}
		
		//Knight moves
		piecesToVisit = b.whiteToMove ? b.WN : b.BN;
		while(piecesToVisit != 0L){
			int i = Long.numberOfTrailingZeros(piecesToVisit);
			long m = getAllKnightMoves(i, friendlyPieces);
			if(loudOnly) m &= enemyPieces;
			while(m != 0){
				int f = Long.numberOfTrailingZeros(m);
				moves.add(NewMoveStructure.createMove((byte)i, (byte)f, (byte)12));
				m ^= (1L << f);
			}
			piecesToVisit ^= (1L << i);
		}
		
		//Bishop moves
		piecesToVisit = b.whiteToMove ? b.WB : b.BB;
		while(piecesToVisit != 0L){
			int i = Long.numberOfTrailingZeros(piecesToVisit);
			long m = getAllBishopMoves(i, enemyPieces, friendlyPieces);
			if(loudOnly) m &= enemyPieces;
			while(m != 0){
				int f = Long.numberOfTrailingZeros(m);
				moves.add(NewMoveStructure.createMove((byte)i, (byte)f, (byte)12));
				m ^= (1L << f);
			}
			piecesToVisit ^= (1L << i);
		}

		//Rook moves
		piecesToVisit = b.whiteToMove ? b.WR : b.BR;
		while(piecesToVisit != 0L){
			int i = Long.numberOfTrailingZeros(piecesToVisit);
			long m = getAllRookMoves(i, enemyPieces, friendlyPieces);
			if(loudOnly) m &= enemyPieces;
			while(m != 0){
				int f = Long.numberOfTrailingZeros(m);
				moves.add(NewMoveStructure.createMove((byte)i, (byte)f, (byte)12));
				m ^= (1L << f);
			}
			piecesToVisit ^= (1L << i);
		}

		//Queen moves
		piecesToVisit = b.whiteToMove ? b.WQ : b.BQ;
		while(piecesToVisit != 0L){
			int i = Long.numberOfTrailingZeros(piecesToVisit);
			long m = getAllRookMoves(i, enemyPieces, friendlyPieces) | getAllBishopMoves(i, enemyPieces, friendlyPieces);
			if(loudOnly) m &= enemyPieces;
			while(m != 0){
				int f = Long.numberOfTrailingZeros(m);
				moves.add(NewMoveStructure.createMove((byte)i, (byte)f, (byte)12));
				m ^= (1L << f);
			}
			piecesToVisit ^= (1L << i);
		}
		
		//King moves
		piecesToVisit = b.whiteToMove ? b.WK : b.BK;
		while(piecesToVisit != 0L){
			int i = Long.numberOfTrailingZeros(piecesToVisit);
			long m = getAllKingMoves(i, friendlyPieces);
			if(loudOnly) m &= enemyPieces;
			while(m != 0){
				int f = Long.numberOfTrailingZeros(m);
				moves.add(NewMoveStructure.createMove((byte)i, (byte)f, (byte)12));
				m ^= (1L << f);
			}
			piecesToVisit ^= (1L << i);
		}
		
		return moves;
	}
	
	private static boolean isCastlingLegal(Board b, boolean kingSide){
		long attackedSquares = getAttackedSquares(b, !b.whiteToMove);
		long targetSquares = (b.whiteToMove ? (kingSide ? 14L : 56L) : (kingSide ? 1008806316530991104L : 4035225266123964416L));
		return (attackedSquares & targetSquares) == 0L;
	}

	private static ArrayList<Short> generateWhitePawnMoves(Board b, ArrayList<Short> moves, byte i, final boolean loudOnly, long allPieces, long enemyPieces){
		if((i + 1) % 8 != 0 && (enemyPieces & (1L << (i + 9))) != 0L){ //If not on A file, generate captures
			addPawnMove(moves, true, i, (byte)(i + 9), false, true);
		}
		if((i + 8) % 8 != 0 && (enemyPieces & (1L << (i + 7))) != 0L){ //If not on H file, generate captures
			addPawnMove(moves, true, i, (byte)(i + 7), false, true);
		}
		if(b.enPassant != 64){
			if((i + 1) % 8 != 0 && b.enPassant == (i + 9)){ //No chance of promotion, so no addPawnMove() needed
				moves.add(NewMoveStructure.createMove(i, (byte)(i + 9), (byte)12));
			}else if((i + 8) % 8 != 0 && b.enPassant == (i + 7)){
				moves.add(NewMoveStructure.createMove(i, (byte)(i + 7), (byte)12));
			}
		}
		if((allPieces & (1L << (i + 8))) == 0L){
			addPawnMove(moves, true, i, (byte)(i + 8), loudOnly, false);
			if(!loudOnly && i < 16 && i > 7 && ((allPieces & (1L << (i + 16))) == 0L)){ //No promote
				moves.add(NewMoveStructure.createMove(i, (byte)(i + 16), (byte)12));
			}
		}
		return moves;
	}
	
	private static ArrayList<Short> generateBlackPawnMoves(Board b, ArrayList<Short> moves, byte i, final boolean loudOnly, long allPieces, long enemyPieces){
		if((i + 1) % 8 != 0 && (enemyPieces & (1L << (i - 7))) != 0L){ //If not on A file, generate captures
			addPawnMove(moves, false, i, (byte)(i - 7), false, true);
		}
		if((i + 8) % 8 != 0 && (enemyPieces & (1L << (i - 9))) != 0L){ //If not on H file, generate captures
			addPawnMove(moves, false, i, (byte)(i - 9), false, true);
		}
		if(b.enPassant != 64){
			if((i + 1) % 8 != 0 && b.enPassant == (i - 7)){ //No chance of promotion, so no addPawnMove() needed
				moves.add(NewMoveStructure.createMove(i, (byte)(i - 7), (byte)12));
			}else if((i + 8) % 8 != 0 && b.enPassant == (i - 9)){
				moves.add(NewMoveStructure.createMove(i, (byte)(i - 9), (byte)12));
			}
		}
		if((allPieces & (1L << (i - 8))) == 0L){
			addPawnMove(moves, false, i, (byte)(i - 8), loudOnly, false);
			if(!loudOnly && i < 56 && i > 47 && ((allPieces & (1L << (i - 16))) == 0L)){ //No promote
				moves.add(NewMoveStructure.createMove(i, (byte)(i - 16), (byte)12));
			}
		}
		return moves;
	}
		
	private static ArrayList<Short> addPawnMove(ArrayList<Short> moves, final boolean white, final byte startSquare, final byte endSquare, final boolean onlyPromote, final boolean capture){
//		boolean promote = (((1L << endSquare) & (white ? Utils.ranks[7] : Utils.ranks[0])) != 0L);
		boolean promote = (white ? (endSquare > 55) : (endSquare < 8));
		if(promote){
			moves.add(NewMoveStructure.createMove(startSquare, endSquare, (byte)(white ? 4 :10))); //Queen 
			moves.add(NewMoveStructure.createMove(startSquare, endSquare, (byte)(white ? 1 : 7))); //Knight
			moves.add(NewMoveStructure.createMove(startSquare, endSquare, (byte)(white ? 3 : 9))); //Rook
			moves.add(NewMoveStructure.createMove(startSquare, endSquare, (byte)(white ? 2 : 8))); //Bishop
		}else if(!onlyPromote){
			moves.add(NewMoveStructure.createMove(startSquare, endSquare, (byte)12));
		}
		return moves;
	}
	
	public static ArrayList<Short> getAllLegalMoves(Board b){
		ArrayList<Short> moves = getAllMoves(b);
		for(int i = (moves.size() - 1); i >= 0; i--){
			b = Make.makeMove(b, moves.get(i));
			if(Check.isInCheck(b, !b.whiteToMove)) moves.remove(i);			
			b = Make.undoMove(b);
		}
		return moves;
	}
	
	public static long getAllRookMoves(int i, long enemyPieces, long friendlyPieces){
		return getAllRookMoves(i, enemyPieces, friendlyPieces, 0L);
	}
	
	public static long getAllRookMoves(int i, long enemyPieces, long friendlyPieces, long emptyMask){
		final long northSquares = northMoves[i];
		long nBlockers = northSquares & (~emptyMask & (enemyPieces | friendlyPieces));
		final long nBlockersFlood = (nBlockers << 8) | (nBlockers << 16) | (nBlockers << 24) | (nBlockers << 32) | (nBlockers << 40) | (nBlockers << 48) | (nBlockers << 56);
		long northMoves = (northSquares & ~nBlockersFlood) & ~(~emptyMask & friendlyPieces);
		
		final long eastSquares = eastMoves[i];
		long eBlockers = eastSquares & (~emptyMask & (enemyPieces | friendlyPieces));
		eBlockers = (eBlockers >>> 1) | (eBlockers >>> 2) | (eBlockers >>> 3) | (eBlockers >>> 4) | (eBlockers >>> 5) | (eBlockers >>> 6) | (eBlockers >>> 7) | (eBlockers >>> 8);
		long eastMoves = (eastSquares & ~eBlockers) & ~(~emptyMask & friendlyPieces);
		
		final long southSquares = southMoves[i];
		long sBlockers = southSquares & (~emptyMask & (enemyPieces | friendlyPieces));
		sBlockers = (sBlockers >>> 8) | (sBlockers >>> 16) | (sBlockers >>> 24) | (sBlockers >>> 32) | (sBlockers >>> 40) | (sBlockers >>> 48) | (sBlockers >>> 56);
		long southMoves = (southSquares & ~sBlockers) & ~(~emptyMask & friendlyPieces);
		
		final long westSquares = westMoves[i];
		long wBlockers = westSquares & (~emptyMask & (enemyPieces | friendlyPieces));
		wBlockers = (wBlockers << 1) | (wBlockers << 2) | (wBlockers << 3) | (wBlockers << 4) | (wBlockers << 5) | (wBlockers << 6) | (wBlockers << 7) | (wBlockers << 8);
		long westMoves = (westSquares & ~wBlockers) & ~(~emptyMask & friendlyPieces);
		
		return northMoves | eastMoves | southMoves | westMoves;
	}
	
	public static long getAllBishopMoves(int i, long enemyPieces, long friendlyPieces){
		return getAllBishopMoves(i, enemyPieces, friendlyPieces, 0L);
	}
	
	public static long getAllBishopMoves(int i, long enemyPieces, long friendlyPieces, long emptyMask){
		final long northEastSquares = northEastMoves[i];
		long neBlockers = northEastSquares & (~emptyMask & (enemyPieces | friendlyPieces));
		final long neBlockersFlood = (neBlockers << 7) | (neBlockers << 14) | (neBlockers << 21) | (neBlockers << 28) | (neBlockers << 35) | (neBlockers << 42) | (neBlockers << 49) | (neBlockers << 56);
		long northEastMoves = (northEastSquares & ~neBlockersFlood) & ~(~emptyMask & friendlyPieces);
		
		final long southEastSquares = southEastMoves[i];
		long seBlockers = southEastSquares & (~emptyMask & (enemyPieces | friendlyPieces));
		final long seBlockersFlood = (seBlockers >>> 9) | (seBlockers >>> 18) | (seBlockers >>> 27) | (seBlockers >>> 36) |  (seBlockers >>> 45) | (seBlockers >>> 54) | (seBlockers >>> 63);
		long southEastMoves = (southEastSquares & ~seBlockersFlood) & ~(~emptyMask & friendlyPieces);
		
		final long southWestSquares = southWestMoves[i];
		long swBlockers = southWestSquares & (~emptyMask & (enemyPieces | friendlyPieces));
		final long swBlockersFlood = (swBlockers >>> 7) | (swBlockers >>> 14) | (swBlockers >>> 21) | (swBlockers >>> 28) | (swBlockers >>> 35) | (swBlockers >>> 42) | (swBlockers >>> 49);       
		long southWestMoves = (southWestSquares & ~swBlockersFlood) & ~(~emptyMask & friendlyPieces);
		
		final long northWestSquares = northWestMoves[i];
		long nwBlockers = northWestSquares & (~emptyMask & (enemyPieces | friendlyPieces));
		final long nwBlockersFlood = (nwBlockers << 9) | (nwBlockers << 18) | (nwBlockers << 27) | (nwBlockers << 36) |  (nwBlockers << 45) | (nwBlockers << 54) | (nwBlockers << 63);
		long northWestMoves = (northWestSquares & ~nwBlockersFlood) & ~(~emptyMask & friendlyPieces);
		
		return northEastMoves | southEastMoves | southWestMoves | northWestMoves;
	}
	
	public static long getAllKnightMoves(int i, long friendlyPieces){
		return knightMoves[i] & ~friendlyPieces;
	}
	
	public static long getAllKingMoves(int i, long friendlyPieces){
		return kingMoves[i] & ~friendlyPieces;
	}
	
	public static void initMovesBoards(){
		for(int i = 0; i < 64; i++){
			long l = 0L;
			for(int f = 1; f < 8; f++){
				if((i + f * 8) > 63) break;
				l |= (1L << (i + f * 8));
			}
			northMoves[i] = l;
		}
		for(int i = 0; i < 64; i++){
			long l = 0L;
			for(int f = 1; f < 8; f++){
				if((i + f * -8) < 0) break;
				l |= (1L << (i + f * -8));
			}
			southMoves[i] = l;
		}
		for(int i = 0; i < 64; i++){
			long l = 0L;
			for(int f = 1; f < 8; f++){
				if((8 + i + f * -1) % 8 == 7) break;
				l |= (1L << (i + f * -1));
			}
			eastMoves[i] = l;
		}
		for(int i = 0; i < 64; i++){
			long l = 0L;
			for(int f = 1; f < 8; f++){
				if((8 + i + f * 1) % 8 == 0) break;
				l |= (1L << (i + f * 1));
			}
			westMoves[i] = l;
		}
		for(int i = 0; i < 64; i++){
			long l = 0L;
			for(int f = 1; f < 8; f++){
				if((i + f * 7) > 63 || (8 + i + f * 7) % 8 == 7) break;
				l |= (1L << (i + f * 7));
			}
			northEastMoves[i] = l;
		}
		for(int i = 0; i < 64; i++){
			long l = 0L;
			for(int f = 1; f < 8; f++){
				if((i + f * -9) < 0 || (8 + i + f * -9) % 8 == 7) break;
				l |= (1L << (i + f * -9));
			}
			southEastMoves[i] = l;
		}
		for(int i = 0; i < 64; i++){
			long l = 0L;
			for(int f = 1; f < 8; f++){
				if((i + f * -7) < 0 || (8 + i + f * -7) % 8 == 0) break;
				l |= (1L << (i + f * -7));
			}
			southWestMoves[i] = l;
		}
		for(int i = 0; i < 64; i++){
			long l = 0L;
			for(int f = 1; f < 8; f++){
				if((i + f * 9) > 63 || (8 + i + f * 9) % 8 == 0) break;
				l |= (1L << (i + f * 9));
			}
			northWestMoves[i] = l;
		}
		for(int i = 0; i < 64; i++){ //{15, 6, -10, -17, -15, -6, 10, 17}
			long l = 0L;
			long start = (1L << i); //up2 right1 (0), up1 right2 (1), down1 right2 (2), down2 right1 (3), down2 left1 (4), down1 left2 (5), up1 left2 (6), up2 left1 (7)
			boolean fileA = ((start & Utils.filesLogical[0]) != 0L);
			boolean fileB = ((start & Utils.filesLogical[1]) != 0L);
			boolean fileG = ((start & Utils.filesLogical[6]) != 0L);
			boolean fileH = ((start & Utils.filesLogical[7]) != 0L);
			boolean rank1 = ((start & Utils.ranks[0]) != 0L);
			boolean rank2 = ((start & Utils.ranks[1]) != 0L);
			boolean rank7 = ((start & Utils.ranks[6]) != 0L);
			boolean rank8 = ((start & Utils.ranks[7]) != 0L);
			for(int f = 0; f < 8; f++){
				if(fileA && f > 3) continue;
				if(fileB && (f == 5 || f == 6)) continue;
				if(fileH && f < 4) continue;
				if(fileG && (f == 1 || f == 2)) continue;
				if(rank1 && f > 1 && f < 6) continue;
				if(rank2 && (f == 3 || f == 4)) continue;
				if(rank8 && (f < 2 || f > 5)) continue;
				if(rank7 && (f == 0 || f == 7)) continue;
				l |= (1L << (i + knightDelta[f]));
			}
			knightMoves[i] = l;
		}
		for(int i = 0; i < 64; i++){
			long l = 0L; //{9, 8, 7, -1, -9, -8, -7, 1}
			long start = (1L << i);
			boolean fileA = ((start & Utils.filesLogical[0]) != 0L);
			boolean fileH = ((start & Utils.filesLogical[7]) != 0L);
			boolean rank1 = ((start & Utils.ranks[0]) != 0L); 
			boolean rank8 = ((start & Utils.ranks[7]) != 0L); 
			for(int f = 0; f < 8; f++){
				if(rank1 && f > 4) continue;
				if(rank8 && f < 3) continue;
				if(fileA && (f == 0 || f == 3 || f == 5)) continue;
				if(fileH && (f == 2 || f == 4 || f == 7)) continue;
				l |= (1L << (i + kingDelta[f]));
			}
			kingMoves[i] = l;
		}
		for(byte c = 0; c < 2; c++){
			for(int i = (c == 0 ? 0 : 8); i < (c == 0 ? 56 : 64); i++){
				long l = 0L; 
				if((i + 1) % 8 != 0) l |= (1L << (i + (c == 0 ? 9 : -7)));
				if((i + 8) % 8 != 0) l |= (1L << (i + (c == 0 ? 7 : -9)));
				pawnAttackMasks[c][i] = l;
			}
		}
	}
	
	public static long getAttackedSquares(final Board b, final boolean white){
		if(b == null) return 0L;
		final long friendlyPieces = white ? (b.WP | b.WN | b.WB | b.WR | b.WQ | b.WK) : (b.BP | b.BN | b.BB | b.BR | b.BQ | b.BK);
		final long enemyPieces = white ? (b.BP | b.BN | b.BB | b.BR | b.BQ | b.BK) : (b.WP | b.WN | b.WB | b.WR | b.WQ | b.WK);
		long total = 0L;
		int i = 0;
		long piecesToVisit = white ? b.WK : b.BK;
		while(piecesToVisit != 0L){
			if((piecesToVisit & 1L) == 1L){
				total |= getAllKingMoves(i, friendlyPieces);
			}
			i ++;
			piecesToVisit = piecesToVisit >>> 1;
		}
		i = 0;
		piecesToVisit = white ? b.WN : b.BN;
		while(piecesToVisit != 0L){
			if((piecesToVisit & 1L) == 1L){
				total |= getAllKnightMoves(i, friendlyPieces);
			}
			i ++;
			piecesToVisit = piecesToVisit >>> 1;
		}
		i = 0;
		piecesToVisit = white ? b.WR : b.BR;
		while(piecesToVisit != 0L){
			if((piecesToVisit & 1L) == 1L){
				total |= getAllRookMoves(i, enemyPieces, friendlyPieces);				
			}
			i ++;
			piecesToVisit = piecesToVisit >>> 1;
		}
		i = 0;
		piecesToVisit = white ? b.WB : b.BB;
		while(piecesToVisit != 0L){
			if((piecesToVisit & 1L) == 1L){
				total |= getAllBishopMoves(i, enemyPieces, friendlyPieces);				
			}
			i ++;
			piecesToVisit = piecesToVisit >>> 1;
		}
		i = 0;
		piecesToVisit = white ? b.WQ : b.BQ;
		while(piecesToVisit != 0L){
			if((piecesToVisit & 1L) == 1L){
				total |= (getAllRookMoves(i, enemyPieces, friendlyPieces) | getAllBishopMoves(i, enemyPieces, friendlyPieces));
			}
			i ++;
			piecesToVisit = piecesToVisit >>> 1;
		}
		i = 0;
		piecesToVisit = white ? b.WP : b.BP;
		while(piecesToVisit != 0L){
			if((piecesToVisit & 1L) == 1L){
				total |= pawnAttackMasks[white ? 0 : 1][i];
			}
			i ++;
			piecesToVisit = piecesToVisit >>> 1;
		}
		return total;
	}

}
