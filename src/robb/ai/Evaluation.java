package robb.ai;

public class Evaluation {
	
	public static final String[] names = new String[] {"White Pawn", "White Knight", "White Bishop", "White Rook", "White Queen", "White King", "Black Pawn", "Black Knight", "Black Bishop", "Black Rook", "Black Queen", "Black King", "Nothing"};
	public static final int[] score = new int[] {120, 320, 340, 560, 1040, Search.mateValue, -120, -320, -340, -560, -1040, -Search.mateValue, 0};
	public static final int[] minimalistScoreAbs = new int[] {1, 3, 3, 5, 9, 20, 1, 3, 3, 5, 9, 20, -1};
	
	private static final long[] rings = new long[] {35604928818740736L, 66229406269440L, 103481868288L};
	private static final int[] ringBonus = new int[] {4, 9, 6};
	private static final float[] ringImportance = new float[] {0F, 1.5F, 1.0F, 0.1F, -0.05F, 0F, 0F, -1.5F, -1.0F, -0.1F, 0.05F, 0F};
	private static final int[] endgameTaperVal = new int[] {0, 1, 1, 2, 4, 0};
	private static final float endgameTaperSum = (float)(8 * endgameTaperVal[0] + 4 * endgameTaperVal[1] + 4 * endgameTaperVal[2] + 4 * endgameTaperVal[3] + 2 * endgameTaperVal[4] + 2 * endgameTaperVal[5]);
	private static final float materialism = 1.5F, kingPawnSafety = 40F, passedPawnBonus = 70F, fianchettoBonus = 21F;
	private static final long kingSide = (Utils.filesLogical[5] | Utils.filesLogical[6] | Utils.filesLogical[7]), queenSide = (Utils.filesLogical[1] | Utils.filesLogical[0] | Utils.filesLogical[2]);
	private static final long darkSquares = 6172840429334713770L, lightSquares = (~darkSquares);
	private static final long[] whitePassedPawns = new long[] {0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 847736400445440L, 1978051601039360L, 3956103202078720L, 7912206404157440L, 15824412808314880L, 31648825616629760L, 63297651233259520L, 54255129628508160L, 847736400248832L, 1978051600580608L, 3956103201161216L, 7912206402322432L, 15824412804644864L, 31648825609289728L, 63297651218579456L, 54255129615925248L, 847736349917184L, 1978051483140096L, 3956102966280192L, 7912205932560384L, 15824411865120768L, 31648823730241536L, 63297647460483072L, 54255126394699776L, 847723465015296L, 1978021418369024L, 3956042836738048L, 7912085673476096L, 15824171346952192L, 31648342693904384L, 63296685387808768L, 54254301760978944L, 844424930131968L, 1970324836974592L, 3940649673949184L, 7881299347898368L, 15762598695796736L, 31525197391593472L, 63050394783186944L, 54043195528445952L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L};
	private static final long[] blackPassedPawns = new long[] {0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 768L, 1792L, 3584L, 7168L, 14336L, 28672L, 57344L, 49152L, 197376L, 460544L, 921088L, 1842176L, 3684352L, 7368704L, 14737408L, 12632064L, 50529024L, 117901056L, 235802112L, 471604224L, 943208448L, 1886416896L, 3772833792L, 3233857536L, 12935430912L, 30182672128L, 60365344256L, 120730688512L, 241461377024L, 482922754048L, 965845508096L, 827867578368L, 3311470314240L, 7726764066560L, 15453528133120L, 30907056266240L, 61814112532480L, 123628225064960L, 247256450129920L, 211934100111360L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L};	
	private static final long rim = (Utils.filesIndex[0] | Utils.filesIndex[7]);
	private static final long[][] fianchetto = {{1L << 9, 1L << 14}, {1L << 49, 1L << 54}};
	private static final int doubledPenalty = 38, isolatedPenalty = 21, semiOpenFileBonus = 30, openFileBonus = 60, blockerPenalty = 46;
	
//	public static int evaluate(Board b){
//		int total = 0;
//		long[] pieces = b.getPieceArray();
//		for(byte i = 0; i < 12; i++){
//			total += (Long.bitCount(pieces[i]) * score[i]);
//		}
//		return total;
//	}
	
//	public static int evaluate(Board b){
//		int total = 0;
//		long[] pieces = b.getPieceArray();
//		for(byte i = 0; i < 12; i++){
//			total += Long.bitCount(pieces[i]) * score[i];
//			
//			if(i == 0){
//				long pawn = pieces[i];
//				while(pawn != 0){	
//					int index = Long.numberOfTrailingZeros(pawn);
//					total += pawnTable[index];
//					pawn ^= (1L << index);
//				}
//			}else if(i == 6){
//				long pawn = pieces[i];
//				while(pawn != 0){
//					int index = Long.numberOfTrailingZeros(pawn);
//					total -= pawnTable[63 - index];
//					pawn ^= (1L << index);
//				}
//			}else{
//				//Ring
//				for(byte ring = 0; ring < 3; ring++){
//					total += (int)(Long.bitCount(pieces[i] & rings[ring]) * ringImportance[i] * ringBonus[ring]);
//				}
//			}
//		}
//		return total;
//	}
	
//	private static final int[] orthogonalImportance = new int[] {10, 9, 7, 3, 5, 0};
//	private static final boolean orthogonalPawns = false; 
//	public static int evaluate(Board b){
//		int total = 0;
//		
//		int wk = Long.numberOfTrailingZeros(b.WK);
//		int bk = Long.numberOfTrailingZeros(b.BK);
//		
//		int wkRank = (wk / 8), wkFile = (wk % 8);
//		int bkRank = (bk / 8), bkFile = (bk % 8);
//		
//		long[] pieces = b.getPieceArray();
//		for(byte piece = 0; piece < 12; piece++){
//			if(piece == 5 || piece == 11) continue;
//			
//			long piecesToVisit = pieces[piece];
//			boolean white = (piece < 6);
//			
//			while(piecesToVisit != 0L){
//				int i = Long.numberOfTrailingZeros(piecesToVisit);
//				
//				total += score[piece];
//				
//				if(i == 0 && !orthogonalPawns){
//					total += pawnTable[i];
//				}else if(i == 6 && !orthogonalPawns){
//					total -= pawnTable[i];
//				}else{
//					//Calculate distance to the enemy king
//					if(white){
//						int orthogonal = 16 - (Math.abs((i / 8) - bkRank) + Math.abs((i % 8) - bkFile));
//						total += (orthogonal * orthogonal * orthogonalImportance[piece % 6]) / 15;
//					}else{
//						int orthogonal = 16 - (Math.abs((i / 8) - wkRank) + Math.abs((i % 8) - wkFile));
//						total -= (orthogonal * orthogonal * orthogonalImportance[piece % 6]) / 15;
//					}
//				}
//				
//				piecesToVisit ^= (1L << i);
//			}
//		}
//		
//		return total;
//	}
	
	public static int evaluate(Board b){
		int total = 0;
		
		int endgame = (int)endgameTaperSum;
		int whiteMaterial = 0, blackMaterial = 0;
		
		//Pawn placement
		long pawn = b.WP;
		while(pawn != 0L){	
			int index = Long.numberOfTrailingZeros(pawn);
			total += score[0] * materialism;
			whiteMaterial += minimalistScoreAbs[0];
			total += Math.max(doesPawnAffectKing(b.WK, index) ? Integer.MIN_VALUE : 0, pawnTable[index]);
			endgame -= endgameTaperVal[0];
			pawn ^= (1L << index);
		}
		pawn = b.BP;
		while(pawn != 0L){
			int index = Long.numberOfTrailingZeros(pawn);
			total += score[6] * materialism;
			blackMaterial += minimalistScoreAbs[6];
			total -= Math.max(doesPawnAffectKing(b.BK, index) ? Integer.MIN_VALUE : 0, pawnTable[63 - index]);
			endgame -= endgameTaperVal[0];
			pawn ^= (1L << index);
		}
		
		//Material
		long[] pieces = b.getPieceArray();
		for(byte i = 1; i < 12; i++){
			if(i == 6) continue; //Skip black pawns
			byte c = (byte)Long.bitCount(pieces[i]);
		
			total += (c * score[i] * materialism);
			endgame -= c * endgameTaperVal[i % 6];
			
			if(i % 6 != 5){
				if(i < 6){
					whiteMaterial += minimalistScoreAbs[i] * c;
				}else{
					blackMaterial += minimalistScoreAbs[i] * c;
				}
			}
			
			//Bishop penalty if pawns are on the same squares
			if(c == 1){ 
				if(i == 2){ 
					final boolean light = (lightSquares & pieces[i]) != 0L;
					total -= 6 * Long.bitCount(b.WP & (light ? lightSquares : darkSquares));
				}else if(i == 8){
					final boolean light = (lightSquares & pieces[i]) != 0L;
					total += 6 * Long.bitCount(b.BP & (light ? lightSquares : darkSquares));
				}
			}
			
			if(ringImportance[i] != 0F){
				for(byte ring = 0; ring < 3; ring++){
					int change = (int)((Long.bitCount(pieces[i] & rings[ring]) * ringImportance[i] * ringBonus[ring]));
					total += change;
				}
			}
		}
		
		//The lower this is, the closer is is to the endgame
		final float endgameF = 1 - ((float)endgame / endgameTaperSum);
		
		//Bishop pair
		final byte whitePair = (byte)Long.bitCount(b.WB), blackPair = (byte)Long.bitCount(b.BB);
		if(whitePair > 1) total += (60 - (int)(15 * endgameF));
		if(blackPair > 1) total -= (60 - (int)(15 * endgameF));
		
		//Fianchetto
		if(0L != (b.WB & fianchetto[0][0]) && 0L != (b.WP & (fianchetto[0][0] << 8))) total += fianchettoBonus * endgameF;
		if(0L != (b.WB & fianchetto[0][1]) && 0L != (b.WP & (fianchetto[0][1] << 8))) total += fianchettoBonus * endgameF;
		if(0L != (b.BB & fianchetto[1][0]) && 0L != (b.BP & (fianchetto[1][0] >> 8))) total -= fianchettoBonus * endgameF;
		if(0L != (b.BB & fianchetto[1][1]) && 0L != (b.BP & (fianchetto[1][1] >> 8))) total -= fianchettoBonus * endgameF;
		
		//Undeveloped pieces
		double whiteUndeveloped = (42F * Math.pow(0.4F * (float)Long.bitCount((b.WN | b.WB) & Utils.ranks[0]), 2));
		total -= (int)whiteUndeveloped; 
		double blackUndeveloped = (42F * Math.pow(0.4F * (float)Long.bitCount((b.BN | b.BB) & Utils.ranks[7]), 2));
		total += (int)blackUndeveloped;
		
		//Dim knights
		total -= Long.bitCount(b.WN & rim) * (40 + 26 * endgameF);
		total += Long.bitCount(b.BN & rim) * (40 + 26 * endgameF);
		
		//Blockers in front of their own pawns
		total -= Long.bitCount((b.WP << 8) & (b.WB | b.WR | b.WQ)) * blockerPenalty;
		total += Long.bitCount((b.BP >> 8) & (b.BB | b.BR | b.BQ)) * blockerPenalty;
		
		//Pigs (rooks on ranks with opposing pawns)
		for(byte i = 1; i < 6; i++){ 
			if((b.WR & Utils.ranks[i]) != 0) total += 16 * Math.min(5, Long.bitCount(b.BP & Utils.ranks[i]));
			if((b.BR & Utils.ranks[i]) != 0) total -= 16 * Math.min(5, Long.bitCount(b.WP & Utils.ranks[i]));
		}

		//Doubled and isolated pawns pawns
		for(byte i = 0; i < 8; i++){
			long wp = Long.bitCount(b.WP & Utils.filesLogical[i]);
			if(wp > 0){
				if(wp > 1) total -= doubledPenalty * (wp - 1);
				long isoMask = ((i == 0 ? 0 : Utils.filesLogical[i - 1]) | (i == 7 ? 0 : Utils.filesLogical[i + 1]));
				if(0L == (isoMask & b.WP)) total -= isolatedPenalty;
			}

			long bp = Long.bitCount(b.BP & Utils.filesLogical[i]);
			if(bp > 0){
				if(bp > 1) total += doubledPenalty * (bp - 1);
				long isoMask = ((i == 0 ? 0 : Utils.filesLogical[i - 1]) | (i == 7 ? 0 : Utils.filesLogical[i + 1]));
				if(0L == (isoMask & b.BP)) total += isolatedPenalty;
			}
		}
						
		//Material imbalance (encourage trading when ahead)
		if(whiteMaterial > blackMaterial){
			final int change = (int)(Math.pow(1F - endgameF, 2) * 1200F * (1F / (float)(blackMaterial + 1)));
			total += change;
		}else if(blackMaterial > whiteMaterial){
			final int change = (int)(Math.pow(1F - endgameF, 2) * 1200F * (1F / (float)(whiteMaterial + 1))); 
			total -= change;
		}
		
		//Xray on king
		int index = Long.numberOfTrailingZeros(b.BK);
		if(index != 64){
			long bMask = (MoveGeneration.northEastMoves[index] | MoveGeneration.northWestMoves[index] | MoveGeneration.southEastMoves[index] | MoveGeneration.southWestMoves[index]);
			total += Long.bitCount(b.WB & bMask) * 28;				
			long rMask = (MoveGeneration.northMoves[index] | MoveGeneration.eastMoves[index] | MoveGeneration.southMoves[index] | MoveGeneration.westMoves[index]);
			total += Long.bitCount(b.WR & rMask) * 25;				
			long qMask = (bMask | rMask);
			total += Long.bitCount(b.WQ & qMask) * 26;				
		}
		index = Long.numberOfTrailingZeros(b.WK);
		if(index != 64){
			long bMask = (MoveGeneration.northEastMoves[index] | MoveGeneration.northWestMoves[index] | MoveGeneration.southEastMoves[index] | MoveGeneration.southWestMoves[index]);
			total -= Long.bitCount(b.BB & bMask) * 30;				
			long rMask = (MoveGeneration.northMoves[index] | MoveGeneration.eastMoves[index] | MoveGeneration.southMoves[index] | MoveGeneration.westMoves[index]);
			total -= Long.bitCount(b.BR & rMask) * 25;				
			long qMask = (bMask | rMask);
			total -= Long.bitCount(b.BQ & qMask) * 26;			
		}
		
		//Rook files
		for(byte i = 0; i < 8; i++){ 
			if((b.WR & Utils.filesLogical[i]) != 0){
				if((b.WP & Utils.filesLogical[i]) == 0){
					total += semiOpenFileBonus; //Semi-open
					if((b.BP & Utils.filesLogical[i]) == 0) total += (openFileBonus - semiOpenFileBonus); //Open
				}
			}
			if((b.BR & Utils.filesLogical[i]) != 0){
				if((b.BP & Utils.filesLogical[i]) == 0){
					total -= semiOpenFileBonus; //Semi-open
					if((b.WP & Utils.filesLogical[i]) == 0) total -= (openFileBonus - semiOpenFileBonus); //Open
				}
			}
		}
		
		//Passed pawns
		double passedPawnEndgame = Math.pow(1 - endgameF, 2);
		byte passedFiles = 0;
		pawn = b.WP;
		while(pawn != 0L){
			int i = 63 - Long.numberOfLeadingZeros(pawn);
			byte f = (byte)(i % 8);
			if(0L == (passedFiles & (1 << f))){
				long passedMask = whitePassedPawns[i];
				if(0L == (passedMask & b.BP)){
					passedFiles |= (1 << f);
					byte rank = (byte)((i / 8) + 1);
					total += (int)(Math.pow((float)rank / 3, 2) * passedPawnBonus * passedPawnEndgame);
				
					passedMask &= Utils.filesIndex[f];
					if(0L != (passedMask & (b.WR | b.WQ))){
						total -= passedPawnBonus * 0.4;
					}else if(0L != (Utils.filesIndex[f] & b.WR)){
						total += passedPawnBonus * 0.2;
					}
				}
			}
			pawn ^= (1L << i);		
		}
		passedFiles = 0;
		pawn = b.BP;
		while(pawn != 0L){
			int i = Long.numberOfTrailingZeros(pawn);
			byte f = (byte)(i % 8);
			if(0L == (passedFiles & (1 << f))){
				long passedMask = blackPassedPawns[i];
				if(0L == (passedMask & b.WP)){
					passedFiles |= (1 << f);
					byte rank = (byte)(8 - (i / 8));
					total -= (int)(Math.pow((float)rank / 3, 2) * passedPawnBonus * passedPawnEndgame);
					
					passedMask &= Utils.filesIndex[f];
					if(0L != (passedMask & (b.BR | b.BQ))){
						total += passedPawnBonus * 0.4;
					}else if(0L != (Utils.filesIndex[f] & b.BR)){
						total -= passedPawnBonus * 0.2;
					}
				}
			}
			pawn ^= (1L << i);		
		}
		
		//King centering
		float kingCentreEndgame = (float)Math.pow((1 - endgameF) * 2 - 1, 3);
		for(byte r = 0; r < 3; r++){
			if((b.WK & rings[r]) != 0L){
				int whiteKingBonus = (int)(60D * (kingCentreEndgame));
				total += whiteKingBonus;
			}
			if((b.BK & rings[r]) != 0L){
				int blackKingBonus = (int)(60D * (kingCentreEndgame));
				total -= blackKingBonus;
			}
		}
		
		//King's pawn protection
		float kingPawns = (float)(1.0 * Math.pow(endgameF - 0.5, 2) + 0.2);
		if((b.WK & kingSide) != 0L){ //White king with pawns
			byte pawns = (byte)Long.bitCount(b.WP & kingSide);
			if(pawns > 1){
				if(isRookTrappedByKing(true, b.WR, b.WK, true)){
					int trappedRook = (int)(115F * kingPawns);
					total -= trappedRook;
				}
			}
			int whiteKingProtection = (int)(kingPawns * kingPawnSafety * pawns);
			total += whiteKingProtection;
			if((b.WP & Utils.filesLogical[6]) == 0L) total -= 45 * endgameF; //Lacking white g-pawn
		}else if((b.WK & queenSide) != 0L){
			byte pawns = (byte)Long.bitCount(b.WP & queenSide);
			if(pawns > 1){
				if(isRookTrappedByKing(true, b.WR, b.WK, false)){
					int trappedRook = (int)(115F * kingPawns);
					total -= trappedRook;
				}
			}
			int whiteKingProtection = (int)(kingPawns * kingPawnSafety * pawns);
			total += whiteKingProtection;
			if((b.WP & Utils.filesLogical[1]) == 0L) total -= 30 * endgameF; //Lacking white b-pawn
		}else{
			total -= (b.wCastleKing || b.wCastleQueen ? 65 : 100) * endgameF; //Uncastled white king
		}
		if((b.BK & kingSide) != 0L){ //Black king with pawns
			byte pawns = (byte)Long.bitCount(b.BP & kingSide);
			if(pawns > 1){
				if(isRookTrappedByKing(false, b.BR, b.BK, true)){
					int trappedRook = (int)(155F * kingPawns);
					total += trappedRook;
				}
			}
			int blackKingProtection = (int)(kingPawns * kingPawnSafety * pawns);
			total -= blackKingProtection;
			if((b.BP & Utils.filesLogical[6]) == 0L) total += 30 * endgameF; //Lacking black g-pawn
		}else if((b.BK & queenSide) != 0L){
			byte pawns = (byte)Long.bitCount(b.BP & queenSide);
			if(pawns > 1){
				if(isRookTrappedByKing(false, b.BR, b.BK, false)){
					int trappedRook = (int)(155F * kingPawns);
					total += trappedRook;
				}
			}
			int blackKingProtection = (int)(kingPawns * kingPawnSafety * pawns);
			total -= blackKingProtection;
			if((b.BP & Utils.filesLogical[1]) == 0L) total += 45 * endgameF; //Lacking black b-pawn
		}else{
			total += (b.bCastleKing || b.bCastleQueen ? 65 : 100) * endgameF; //Uncastled black king
		}
		
		total /= materialism;
		
		//Consider insufficiency as a limited "advantage"
		if(total == 0){
			return 0;
		}else if(total > 0){
			return isInsufficientMaterial(b, true) ? 0 : total;
		}else{
			return isInsufficientMaterial(b, false) ? 0 : total;
		}
	}
	
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
		byte endgame = 0;
		long[] pieces = b.getPieceArray();
		for(byte i = 0; i < 12; i++){
			byte c = (byte)Long.bitCount(pieces[i]);
			endgame += c * endgameTaperVal[i % 6];
		}
		return ((float)endgame / 24F);
	}
	
	private static boolean doesPawnAffectKing(long king, int index){
		return (0L != (king & kingSide)) == (0L != ((1L << index) & kingSide));
	}
	
	//Reminder that this is upside down
	public static final int[] pawnTable = new int[] {0,  0,  0,  0,  0,  0,  0,  0, 
													 5, 10, 10,-35,-35, 10, 10,  5, 
													 5,  0,-10,-10,-10,-10,  0,  5, 
													 0,  0,  0, 30, 30,  0,  0,  0, 
													 5,  5, 10, 30, 30, 10,  5,  5, 
													10, 10, 30, 40, 40, 30, 10, 10, 
													55, 60, 60, 60, 60, 60, 60, 55, 
													 0,  0,  0,  0,  0,  0,  0, 0};

}
