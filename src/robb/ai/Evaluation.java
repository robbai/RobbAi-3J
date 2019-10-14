package robb.ai;

public class Evaluation {
	
	public static final String[] names = new String[] {"White Pawn", "White Knight", "White Bishop", "White Rook", "White Queen", "White King", "Black Pawn", "Black Knight", "Black Bishop", "Black Rook", "Black Queen", "Black King", "Nothing"};
	public static final int[] score = new int[] {100, 320, 330, 560, 1040, Search.mateValue, -100, -320, -330, -560, -1040, -Search.mateValue, 0};
	public static final int[] minimalistScoreAbs = new int[] {1, 3, 3, 5, 9, 20, 1, 3, 3, 5, 9, 20, -1};
	
	private static final long[] rings = new long[] {35604928818740736L, 66229406269440L, 103481868288L};
	private static final int[] ringBonus = new int[] {2, 5, 4};
	private static final float[] ringImportance = new float[] {0F, 1.7F, 1.2F, 0.1F, 0.01F, 0F, 0F, -1.7F, -1.2F, -0.1F, -0.01F, 0F};
	private static final int[] endgameTaperVal = new int[] {0, 1, 1, 2, 4, 0}, orthogonalImportance = new int[] {1, 5, 5, 2, 2, 0};
	private static final float endgameTaperSum = (float)(8 * endgameTaperVal[0] + 4 * endgameTaperVal[1] + 4 * endgameTaperVal[2] + 4 * endgameTaperVal[3] + 2 * endgameTaperVal[4] + 2 * endgameTaperVal[5]);
	private static final float materialism = 1.6F, kingPawnSafety = 55F, passedPawnBonus = 70F, fianchettoBonus = 12F;
	private static final long kingSide = (Utils.filesLogical[5] | Utils.filesLogical[6] | Utils.filesLogical[7]), queenSide = (Utils.filesLogical[1] | Utils.filesLogical[0] | Utils.filesLogical[2]);
	private static final long darkSquares = 6172840429334713770L, lightSquares = (~darkSquares);
	private static final long[][] fianchetto = {{1L << 9, 1L << 14}, {1L << 49, 1L << 54}};
	private static final long[] whitePassedPawns = new long[] {0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 847736400445440L, 1978051601039360L, 3956103202078720L, 7912206404157440L, 15824412808314880L, 31648825616629760L, 63297651233259520L, 54255129628508160L, 847736400248832L, 1978051600580608L, 3956103201161216L, 7912206402322432L, 15824412804644864L, 31648825609289728L, 63297651218579456L, 54255129615925248L, 847736349917184L, 1978051483140096L, 3956102966280192L, 7912205932560384L, 15824411865120768L, 31648823730241536L, 63297647460483072L, 54255126394699776L, 847723465015296L, 1978021418369024L, 3956042836738048L, 7912085673476096L, 15824171346952192L, 31648342693904384L, 63296685387808768L, 54254301760978944L, 844424930131968L, 1970324836974592L, 3940649673949184L, 7881299347898368L, 15762598695796736L, 31525197391593472L, 63050394783186944L, 54043195528445952L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L}, blackPassedPawns = new long[] {0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 768L, 1792L, 3584L, 7168L, 14336L, 28672L, 57344L, 49152L, 197376L, 460544L, 921088L, 1842176L, 3684352L, 7368704L, 14737408L, 12632064L, 50529024L, 117901056L, 235802112L, 471604224L, 943208448L, 1886416896L, 3772833792L, 3233857536L, 12935430912L, 30182672128L, 60365344256L, 120730688512L, 241461377024L, 482922754048L, 965845508096L, 827867578368L, 3311470314240L, 7726764066560L, 15453528133120L, 30907056266240L, 61814112532480L, 123628225064960L, 247256450129920L, 211934100111360L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L};	
	private static final long rim = (Utils.filesIndex[0] | Utils.filesIndex[7]);	
	private static final int doubledPenalty = 15, isolatedPenalty = 9, semiOpenFileBonus = 30, openFileBonus = 60, blockerPenalty = 56;
	
//	public static int evaluate(Board b){
//		int total = 0;
//		long[] pieces = b.getPieceArray();
//		for(byte i = 0; i < 12; i++){
//			total += (Long.bitCount(pieces[i]) * score[i]);
//		}
//		return total;
//	}
	
	public static int attacking(Board b){
		int total = 0;
		
		// Target acquired
		int wk = Long.numberOfTrailingZeros(b.WK);
		int bk = Long.numberOfTrailingZeros(b.BK);
		int wkRank = (wk / 8), wkFile = (wk % 8);
		int bkRank = (bk / 8), bkFile = (bk % 8);
		
		boolean includeAttackingPawns = true;
				
		for(int p = 0; p < 12; p++){
			boolean white = (p < 6);
			
			long piecesToVisit = b.P(p);
			
			total += Long.bitCount(piecesToVisit) * score[p];
			
			if(!includeAttackingPawns && (p == 0 || p == 6)) continue; // Skip pawns
			if(p == 5 || p == 12) continue; // Skip kings
			
			// Ring
			total += (Long.bitCount(piecesToVisit & rings[0]) * ringImportance[p] * ringBonus[0]);
			total += (Long.bitCount(piecesToVisit & rings[1]) * ringImportance[p] * ringBonus[1]);
			total += (Long.bitCount(piecesToVisit & rings[2]) * ringImportance[p] * ringBonus[2]);
			
			// Calculate distance to the enemy king
			while(piecesToVisit != 0L){
				int i = Long.numberOfTrailingZeros(piecesToVisit);
				if(white){
					double orthogonal = (Math.pow((i / 8) - bkRank, 2) + Math.pow((i % 8) - bkFile, 2));
					total -= (orthogonal * orthogonalImportance[p % 6]) / 8;
				}else{
					double orthogonal = (Math.pow((i / 8) - wkRank, 2) + Math.pow((i % 8) - wkFile, 2));
					total += (orthogonal * orthogonalImportance[p % 6]) / 8;
				}
				piecesToVisit ^= (1L << i);
			}
		}
		
		return total;
	}
	
 	public static int evaluate(Board b){
// 		int total = 0;
 		int total = attacking(b);
 		total += pawns(b);
 		
// 		long notWhitePieces = ~b.W(), notBlackPieces = ~b.B();
 		for(byte pieceType = 1; pieceType < 12; pieceType += (pieceType == 5 ? 2 : 1)){
 			boolean white = (pieceType < 6);
 			int side = (white ? 1 : -1);
 			
 			int pieceTypeIndependent = (pieceType % 6);
 			
// 			long notOurPieces = (white ? notWhitePieces : notBlackPieces);
 			long targets = 0;
 			if(white){
 				if(pieceTypeIndependent == 1){
 					targets = b.BP | b.BB | b.BR | b.BQ;
 				}else if(pieceTypeIndependent == 2){
 					targets = b.BN | b.BR | b.BQ;
 				}else if(pieceTypeIndependent == 3){
 					targets = b.BP | b.BN | b.BB | b.BR | b.BQ;
 				}else if(pieceTypeIndependent == 4){
 					targets = b.BP | b.BN | b.BB | b.BR | b.BQ;
 				}else if(pieceTypeIndependent == 5){
 					targets = b.WP;
 				}
 			}else{
 				if(pieceTypeIndependent == 1){
 					targets = b.WP | b.WB | b.WR | b.WQ;
 				}else if(pieceTypeIndependent == 2){
 					targets = b.WN | b.WR | b.WQ;
 				}else if(pieceTypeIndependent == 3){
 					targets = b.WP | b.WN | b.WB | b.WR | b.WQ;
 				}else if(pieceTypeIndependent == 4){
 					targets = b.WP | b.WN | b.WB | b.WR | b.WQ;
 				}else if(pieceTypeIndependent == 5){
 					targets = b.BP;
 				}
 			}
 			 			
 			long piecesToVisit = b.P(pieceType);
 			
 			while(piecesToVisit != 0){	
 				int i = Long.numberOfTrailingZeros(piecesToVisit);
 				
 				long moves;
 				if(pieceTypeIndependent == 1){
 					moves = MoveGeneration.knightMoves[i];
 					total += Long.bitCount(moves & targets) * side * 4;
 					total += Long.bitCount(moves) * side;
 				}else{
 					if(pieceTypeIndependent == 2 || pieceTypeIndependent == 4){
 						moves = (MoveGeneration.northEastMoves[i] | MoveGeneration.southEastMoves[i] | MoveGeneration.southWestMoves[i] | MoveGeneration.northWestMoves[i]);
 						total += Long.bitCount(moves & targets) * side * 3;
 						total += Long.bitCount(moves) * side;
 					}
 					if(pieceTypeIndependent == 3 || pieceTypeIndependent == 4){
 						moves = (MoveGeneration.northMoves[i] | MoveGeneration.eastMoves[i] | MoveGeneration.southMoves[i] | MoveGeneration.westMoves[i]);
 						total += Long.bitCount(moves & targets) * side * 3;
 						total += Long.bitCount(moves) * side;
 					}else if(pieceTypeIndependent == 5){
 						moves = MoveGeneration.kingMoves[i];
 						total += Long.bitCount(moves & targets) * side * 5;
 						total += Long.bitCount(moves) * side;
 					}
 				}
 				
 				piecesToVisit ^= (1L << i);
 			}
 			
 		}
 		
 		return total *= (float)(100 - b.halfMoveClock) / 100F;
 	}
	
 	private static int pawns(Board b){
 		int total = 0;
		boolean white = true;
		while(true){
			long p = (white ? b.WP : b.BP);
			
			long lane = (p << 8);
			total -= (white ? 1 : -1) * Long.bitCount(lane & p) * doubledPenalty;
			lane = (lane << 8);
			total -= (white ? 1 : -1) * Long.bitCount(lane & p) * doubledPenalty / 2;
			
			lane = ((p << 8) | (p << 16) | (p << 24) | (p << 32) | (p << 40) | (p << 48));
			long side = (lane |= p) & ~rim;
			side = (side << 1) | (side >> 1);
			total -= (white ? 1 : -1) * Long.bitCount(side & ~lane)/* * isolatedPenalty / 8*/;
			
			while(p != 0){	
 				int i = Long.numberOfTrailingZeros(p);
 				
 				if(white){
 					total += pawnTable[i];
 				}else{
 					total -= pawnTable[63 - i];
 				}
 				
 				p ^= (1L << i);
			}
			
			if(!white) break;
			white = false;
		}
		
		return total;
	}

//	public static int evaluate(Board b){
//		int total = 0;
//		
//		int endgame = (int)endgameTaperSum;
//		int whiteMaterial = 0, blackMaterial = 0;
//		
//		// Target acquired
//		int wk = Long.numberOfTrailingZeros(b.WK);
//		int bk = Long.numberOfTrailingZeros(b.BK);
//		int wkRank = (wk / 8), wkFile = (wk % 8);
//		int bkRank = (bk / 8), bkFile = (bk % 8);
//		
//		// Pawn placement
//		long pawn = b.WP;
//		while(pawn != 0L){	
//			int index = Long.numberOfTrailingZeros(pawn);
//			total += score[0] * materialism;
//			whiteMaterial += minimalistScoreAbs[0];
// 			boolean affectKing = doesPawnAffectKing(b.WK, index);
// 			if(affectKing){
//				total += pawnTable[index] * 2;
// 			}else{
// 				int fileDiff = Math.abs((index % 8) - bkFile), rank = (index / 8);
// 				total += (7 - Math.min(4, fileDiff)) * rank;
// 			}
//			endgame -= endgameTaperVal[0];
//			pawn ^= (1L << index);
//		}
//		pawn = b.BP;
//		while(pawn != 0L){
//			int index = Long.numberOfTrailingZeros(pawn);
//			total += score[6] * materialism;
//			blackMaterial += minimalistScoreAbs[6];
// 			boolean affectKing = doesPawnAffectKing(b.BK, index);
// 			if(affectKing){
//				total -= pawnTable[63 - index] * 2;
// 			}else{
// 				int fileDiff = Math.abs((index % 8) - bkFile), rank = 7 - (index / 8);
// 				total -= (7 - Math.min(4, fileDiff)) * rank;
// 			}
//			endgame -= endgameTaperVal[0];
//			pawn ^= (1L << index);
//		}
//		
//		// Material
//		long[] pieces = b.getPieceArray();
//		for(int p = 1; p < 11; p++){
//			if(p == 5 || p == 6) continue; // Skip black pawns & white king
//			int c = Long.bitCount(pieces[p]);
//		
//			total += (c * score[p] * materialism);
//			endgame -= c * endgameTaperVal[p % 6];
//			
//			if(p % 6 != 5){
//				if(p < 6){
//					whiteMaterial += minimalistScoreAbs[p] * c;
//				}else{
//					blackMaterial += minimalistScoreAbs[p] * c;
//				}
//			}
//			
//			// Calculate distance to the enemy king
//			long piecesToVisit = pieces[p];
//			boolean white = (p < 6);
//			while(piecesToVisit != 0L){
//				int i = Long.numberOfTrailingZeros(piecesToVisit);
//				if(white){
//					int orthogonal = 14 - (Math.abs((i / 8) - bkRank) + Math.abs((i % 8) - bkFile));
//					total += (orthogonal * orthogonalImportance[p % 6]);
//				}else{
//					int orthogonal = 14 - (Math.abs((i / 8) - wkRank) + Math.abs((i % 8) - wkFile));
//					total -= (orthogonal * orthogonalImportance[p % 6]);
//				}
//				piecesToVisit ^= (1L << i);
//			}
//		}
//		
//		// The lower this is, the closer is is to the endgame
//		float endgameF = 1 - ((float)endgame / endgameTaperSum);
//		
//		// Bishop penalty if pawns are on the same squares
//		int wbCount = Long.bitCount(b.WB), bbCount = Long.bitCount(b.BB);
//		if(wbCount == 1){ 
//			boolean light = (lightSquares & b.WB) != 0L;
//			total -= 6 * Long.bitCount(b.WP & (light ? lightSquares : darkSquares));
//		}else if(bbCount == 1){ 
//			boolean light = (lightSquares & b.BB) != 0L;
//			total += 6 * Long.bitCount(b.BP & (light ? lightSquares : darkSquares));
//		}
//		
//		// Bishop pair
//		if(wbCount > 1) total += (45 - (int)(15 * endgameF));
//		if(bbCount > 1) total -= (45 - (int)(15 * endgameF));
//		
//		// Fianchetto
//		if(0L != (b.WB & fianchetto[0][0]) && 0L != (b.WP & (fianchetto[0][0] << 8))) total += fianchettoBonus * endgameF;
//		if(0L != (b.WB & fianchetto[0][1]) && 0L != (b.WP & (fianchetto[0][1] << 8))) total += fianchettoBonus * endgameF;
//		if(0L != (b.BB & fianchetto[1][0]) && 0L != (b.BP & (fianchetto[1][0] >> 8))) total -= fianchettoBonus * endgameF;
//		if(0L != (b.BB & fianchetto[1][1]) && 0L != (b.BP & (fianchetto[1][1] >> 8))) total -= fianchettoBonus * endgameF;
//		
//		// Undeveloped pieces
//		double whiteUndeveloped = (36F * Math.pow(0.3F * (float)Long.bitCount((b.WN | b.WB) & Utils.ranks[0]), 2));
//		total -= (int)whiteUndeveloped; 
//		double blackUndeveloped = (36F * Math.pow(0.3F * (float)Long.bitCount((b.BN | b.BB) & Utils.ranks[7]), 2));
//		total += (int)blackUndeveloped;
//		
//		// Dim knights
//		total -= Long.bitCount(b.WN & rim) * (30 + 20 * endgameF);
//		total += Long.bitCount(b.BN & rim) * (30 + 20 * endgameF);
//		
//		// Blockers in front of their own pawns
//		total -= Long.bitCount((b.WP << 8) & (b.WB | b.WR | b.WQ)) * blockerPenalty;
//		total += Long.bitCount((b.BP >>> 8) & (b.BB | b.BR | b.BQ)) * blockerPenalty;
//		
//		// Pigs (rooks on ranks with opposing pawns)
//		for(int i = 1; i < 6; i++){ 
//			if((b.WR & Utils.ranks[i]) != 0) total += 18 * Math.min(5, Long.bitCount(b.BP & Utils.ranks[i]));
//			if((b.BR & Utils.ranks[i]) != 0) total -= 18 * Math.min(5, Long.bitCount(b.WP & Utils.ranks[i]));
//		}
//
//		// Doubled and isolated pawns pawns
//		for(int i = 0; i < 8; i++){
//			long wp = Long.bitCount(b.WP & Utils.filesLogical[i]);
//			if(wp > 0){
//				if(wp > 1) total -= doubledPenalty * (wp - 1);
//				long isoMask = ((i == 0 ? 0 : Utils.filesLogical[i - 1]) | (i == 7 ? 0 : Utils.filesLogical[i + 1]));
//				if(0L == (isoMask & b.WP)) total -= isolatedPenalty;
//			}
//
//			long bp = Long.bitCount(b.BP & Utils.filesLogical[i]);
//			if(bp > 0){
//				if(bp > 1) total += doubledPenalty * (bp - 1);
//				long isoMask = ((i == 0 ? 0 : Utils.filesLogical[i - 1]) | (i == 7 ? 0 : Utils.filesLogical[i + 1]));
//				if(0L == (isoMask & b.BP)) total += isolatedPenalty;
//			}
//		}
//						
//		// Material imbalance (encourage trading when ahead)
//		if(whiteMaterial > blackMaterial){
//			int change = (int)(Math.pow(1F - endgameF, 2) * 1400F * (1F / (float)(blackMaterial + 1)));
//			total += change;
//		}else if(blackMaterial > whiteMaterial){
//			int change = (int)(Math.pow(1F - endgameF, 2) * 1400F * (1F / (float)(whiteMaterial + 1))); 
//			total -= change;
//		}
//		
//		// Xray on king
//		if(wk != 64){
//			long bMask = (MoveGeneration.northEastMoves[wk] | MoveGeneration.northWestMoves[wk] | MoveGeneration.southEastMoves[wk] | MoveGeneration.southWestMoves[wk]);
//			total += Long.bitCount(b.WB & bMask) * 25;
//			long rMask = (MoveGeneration.northMoves[wk] | MoveGeneration.eastMoves[wk] | MoveGeneration.southMoves[wk] | MoveGeneration.westMoves[wk]);
//			total += Long.bitCount(b.WR & rMask) * 22;
//			long qMask = (bMask | rMask);
//			total += Long.bitCount(b.WQ & qMask) * 20;
//		}
//		if(bk != 64){
//			long bMask = (MoveGeneration.northEastMoves[bk] | MoveGeneration.northWestMoves[bk] | MoveGeneration.southEastMoves[bk] | MoveGeneration.southWestMoves[bk]);
//			total -= Long.bitCount(b.BB & bMask) * 25;
//			long rMask = (MoveGeneration.northMoves[bk] | MoveGeneration.eastMoves[bk] | MoveGeneration.southMoves[bk] | MoveGeneration.westMoves[bk]);
//			total -= Long.bitCount(b.BR & rMask) * 22;
//			long qMask = (bMask | rMask);
//			total -= Long.bitCount(b.BQ & qMask) * 20;
//		}
//		
//		// Rook files
//		for(int i = 0; i < 8; i++){ 
//			if((b.WR & Utils.filesLogical[i]) != 0){
//				if((b.WP & Utils.filesLogical[i]) == 0){
//					total += semiOpenFileBonus; // Semi-open
//					if((b.BP & Utils.filesLogical[i]) == 0) total += (openFileBonus - semiOpenFileBonus); // Open
//				}
//			}
//			if((b.BR & Utils.filesLogical[i]) != 0){
//				if((b.BP & Utils.filesLogical[i]) == 0){
//					total -= semiOpenFileBonus; // Semi-open
//					if((b.WP & Utils.filesLogical[i]) == 0) total -= (openFileBonus - semiOpenFileBonus); // Open
//				}
//			}
//		}
//		
//		// Passed pawns
//		double passedPawnEndgame = Math.pow(1 - endgameF, 2);
//		int passedFiles = 0;
//		pawn = b.WP;
//		while(pawn != 0L){
//			int i = (63 - Long.numberOfLeadingZeros(pawn));
//			int f = (i % 8);
//			if(0L == (passedFiles & (1 << f))){
//				long passedMask = whitePassedPawns[i];
//				if(0L == (passedMask & b.BP)){
//					passedFiles |= (1 << f);
//					int rank = (int)((i / 8) + 1);
//					total += (int)(Math.pow((float)rank / 3, 2) * passedPawnBonus * passedPawnEndgame);
//				
//					passedMask &= Utils.filesIndex[f];
//					if(0L != (passedMask & (b.WR | b.WQ))){
//						total -= passedPawnBonus * 0.4;
//					}else if(0L != (Utils.filesIndex[f] & b.WR)){
//						total += passedPawnBonus * 0.2;
//					}
//				}
//			}
//			pawn ^= (1L << i);		
//		}
//		passedFiles = 0;
//		pawn = b.BP;
//		while(pawn != 0L){
//			int i = Long.numberOfTrailingZeros(pawn);
//			int f = (i % 8);
//			if(0L == (passedFiles & (1 << f))){
//				long passedMask = blackPassedPawns[i];
//				if(0L == (passedMask & b.WP)){
//					passedFiles |= (1 << f);
//					int rank = (int)(8 - (i / 8));
//					total -= (int)(Math.pow((float)rank / 3, 2) * passedPawnBonus * passedPawnEndgame);
//					
//					passedMask &= Utils.filesIndex[f];
//					if(0L != (passedMask & (b.BR | b.BQ))){
//						total += passedPawnBonus * 0.4;
//					}else if(0L != (Utils.filesIndex[f] & b.BR)){
//						total -= passedPawnBonus * 0.2;
//					}
//				}
//			}
//			pawn ^= (1L << i);		
//		}
//		
//		// King centering
//		float kingCentreEndgame = (float)Math.pow((1 - endgameF) * 2 - 1, 3);
//		for(int r = 0; r < 3; r++){
//			if((b.WK & rings[r]) != 0L){
//				int whiteKingBonus = (int)(62D * (kingCentreEndgame));
//				total += whiteKingBonus;
//			}
//			if((b.BK & rings[r]) != 0L){
//				int blackKingBonus = (int)(62D * (kingCentreEndgame));
//				total -= blackKingBonus;
//			}
//		}
//		
//		// King's pawn protection
//		float kingPawns = (float)(1.1 * Math.pow(endgameF - 0.5, 2) + 0.2);
//		if((b.WK & kingSide) != 0L){ // White king with pawns
//			int pawns = Long.bitCount(b.WP & kingSide);
//			if(pawns > 1){
//				if(isRookTrappedByKing(true, b.WR, b.WK, true)){
//					int trappedRook = (int)(145F * kingPawns);
//					total -= trappedRook;
//				}
//			}
//			int whiteKingProtection = (int)(kingPawns * kingPawnSafety * pawns);
//			total += whiteKingProtection;
//			if((b.WP & Utils.filesLogical[6]) == 0L) total -= 55 * endgameF; // Lacking white g-pawn
//		}else if((b.WK & queenSide) != 0L){
//			int pawns = Long.bitCount(b.WP & queenSide);
//			if(pawns > 1){
//				if(isRookTrappedByKing(true, b.WR, b.WK, false)){
//					int trappedRook = (int)(145F * kingPawns);
//					total -= trappedRook;
//				}
//			}
//			int whiteKingProtection = (int)(kingPawns * kingPawnSafety * pawns);
//			total += whiteKingProtection;
//			if((b.WP & Utils.filesLogical[1]) == 0L) total -= 55 * endgameF; // Lacking white b-pawn
//		}else{
//			total -= (b.wCastleKing || b.wCastleQueen ? 75 : 100) * endgameF; // Uncastled white king
//		}
//		if((b.BK & kingSide) != 0L){ // Black king with pawns
//			int pawns = Long.bitCount(b.BP & kingSide);
//			if(pawns > 1){
//				if(isRookTrappedByKing(false, b.BR, b.BK, true)){
//					int trappedRook = (int)(145F * kingPawns);
//					total += trappedRook;
//				}
//			}
//			int blackKingProtection = (int)(kingPawns * kingPawnSafety * pawns);
//			total -= blackKingProtection;
//			if((b.BP & Utils.filesLogical[6]) == 0L) total += 55 * endgameF; // Lacking black g-pawn
//		}else if((b.BK & queenSide) != 0L){
//			int pawns = Long.bitCount(b.BP & queenSide);
//			if(pawns > 1){
//				if(isRookTrappedByKing(false, b.BR, b.BK, false)){
//					int trappedRook = (int)(145F * kingPawns);
//					total += trappedRook;
//				}
//			}
//			int blackKingProtection = (int)(kingPawns * kingPawnSafety * pawns);
//			total -= blackKingProtection;
//			if((b.BP & Utils.filesLogical[1]) == 0L) total += 55 * endgameF; // Lacking black b-pawn
//		}else{
//			total += (b.bCastleKing || b.bCastleQueen ? 75 : 100) * endgameF; // Uncastled black king
//		}
//		
//		total /= materialism;
//		
//		// Consider insufficiency as a limited "advantage"
//		if(total == 0){
//			return 0;
//		}else if(total > 0){
//			return isInsufficientMaterial(b, true) ? 0 : total;
//		}else{
//			return isInsufficientMaterial(b, false) ? 0 : total;
//		}
//	}
	
	public static boolean onlyKingAndPawns(Board b){
		return 0L == (b.WN | b.WB  | b.WR  | b.WQ | b.BN | b.BB  | b.BR  | b.BQ);
	}
	
	public static boolean isInsufficientMaterial(Board b){
		return b.WP == 0L && b.BP == 0L && b.WQ == 0L && b.BQ == 0L && b.WR == 0L && b.BR == 0L && (Long.bitCount(b.WN | b.WB) <= 1) && (Long.bitCount(b.BN | b.BB) <= 1);
	}
	
	public static boolean isInsufficientMaterial(Board b, boolean white){
		if(white){
			return b.WP == 0L && b.WQ == 0L && b.WR == 0L && (Long.bitCount(b.WN | b.WB) <= 1);
		}else{
			return b.BP == 0L && b.BQ == 0L && b.BR == 0L && (Long.bitCount(b.BN | b.BB) <= 1);
		}
	}
		
	private static boolean isRookTrappedByKing(final boolean white, final long R, final long K, final boolean kingSide){
		boolean rook = (0L != ((kingSide ? Utils.filesLogical[6] | Utils.filesLogical[7] : Utils.filesLogical[0] | Utils.filesLogical[1]) & R & (Utils.ranks[white ? 0 : 7] | Utils.ranks[white ? 1 : 6])));
		boolean king = (0L != ((kingSide ? Utils.filesLogical[5] | Utils.filesLogical[6] : Utils.filesLogical[1] | Utils.filesLogical[2]) & K & (Utils.ranks[white ? 0 : 7] | Utils.ranks[white ? 1 : 6])));
		return rook && king;
	}
	
	public static float getEndgameCloseness(Board b){
		int endgame = 0;
		long[] pieces = b.getPieceArray();
		for(int i = 0; i < 12; i++){
			int c = Long.bitCount(pieces[i]);
			endgame += c * endgameTaperVal[i % 6];
		}
		return ((float)endgame / 24F);
	}
	
	private static boolean doesPawnAffectKing(long king, int index){
		return (0L != (king & kingSide)) == (0L != ((1L << index) & kingSide));
	}
	
	// Reminder that this is upside down.
	public static final int[] pawnTable = new int[] {0,  0,  0,  0,  0,  0,  0,  0, 
													 5, 10, 10,-25,-25, 10, 10,  5, 
													 5,  0, -5,-25,-25, -5,  0,  5, 
													 0,  0,  0, 30, 30,  0,  0,  0, 
													 5,  5, 10, 30, 30, 10,  5,  5, 
													10, 10, 30, 40, 40, 30, 10, 10, 
													55, 60, 60, 60, 60, 60, 60, 55, 
													 0,  0,  0,  0,  0,  0,  0, 0};

}
