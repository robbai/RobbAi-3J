package robb.ai;

/**
 * This whole class is for one method lol
 */
public class Check {
	
	public static boolean isInCheck(Board b, boolean white){
		long targetMask = (white ? b.WK : b.BK);
		int targetSquare = Long.numberOfTrailingZeros(targetMask);
		
		if(targetSquare == 64){
//			Utils.printBoard(b, true);
//			for(int i = 0; i < b.history.size(); i++){
//				System.out.println(i + ": " + HistoryStructure.toString(b.history.get(i)));
//			}
			return true;
		}
		
		// Pawn moves.
		long piecesToVisit = (white ? b.BP : b.WP) & MoveGeneration.pawnAttackMasks[white ? 0 : 1][targetSquare];
		if(piecesToVisit != 0) return true;
		
		// Knight moves.
		piecesToVisit = (white ? b.BN : b.WN) & MoveGeneration.knightMoves[targetSquare];
		if(piecesToVisit != 0) return true;
		
		// King moves.
		piecesToVisit = (white ? b.BK : b.WK) & MoveGeneration.kingMoves[targetSquare];
		if(piecesToVisit != 0) return true;
				
		// Handle sliding pieces.
		long friendlyPieces = (white ? (b.WP | b.WN | b.WB | b.WR | b.WQ | b.WK) : (b.BP | b.BN | b.BB | b.BR | b.BQ | b.BK)) & ~targetMask;
		long enemyPieces = white ? (b.BP | b.BN | b.BB | b.BR | b.BQ | b.BK) : (b.WP | b.WN | b.WB | b.WR | b.WQ | b.WK);				
		long bishopMoves = MoveGeneration.getAllBishopMoves(targetSquare, enemyPieces, friendlyPieces);
		long rookMoves = MoveGeneration.getAllRookMoves(targetSquare, enemyPieces, friendlyPieces);
		
		// Queen moves.
		piecesToVisit = (white ? b.BQ : b.WQ) & (bishopMoves | rookMoves);
		if(piecesToVisit != 0) return true;
		
		// Rook moves.
		piecesToVisit = (white ? b.BR : b.WR) & rookMoves;
		if(piecesToVisit != 0) return true;
		
		// Bishop moves.
		piecesToVisit = (white ? b.BB : b.WB) & bishopMoves;
		return piecesToVisit != 0;
	}

}
