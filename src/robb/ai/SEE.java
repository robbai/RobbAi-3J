package robb.ai;

public class SEE {
	
	private static int see(Board board, boolean white, byte targetSquare, byte enemy, long emptyMask){
		byte bestCapture = (byte)getBestCapture(board, white, targetSquare, emptyMask);
		int value = 0;
		
		if(bestCapture != -1){
			if(enemy == (white ? 11 : 5)) return Search.mateValue;
			byte piece = Utils.getPieceAt(board, bestCapture);
			value = Math.max(0, Math.abs(Evaluation.score[enemy]) - see(board, !white, targetSquare, piece, emptyMask | (1L << bestCapture)));
	   }
		
	   return value;
	}
	
	public static int seeCapture(Board board, short move){
		byte piece = Utils.getPieceAt(board, NewMoveStructure.getFrom(move));
		if(piece == 12) return 0;
		
		byte end = NewMoveStructure.getTo(move);
		byte enemy = Utils.getPieceAt(board, end);		
		
		if(enemy == 5 || enemy == 11) return Search.mateValue;
		
		int value = Math.abs(Evaluation.score[enemy]) - see(board, !board.whiteToMove, end, piece, (1L << NewMoveStructure.getFrom(move)));
		return value;
	}
	
	/**
	 * This looks for the lowest value capturer by looking at the opponent's moves, and seeing whether they match-up with the corresponding piece,
	 * hence why the use of most methods seem backwards in their "friendly" and "enemy" pieces
	 */
	private static int getBestCapture(Board b, boolean white, byte targetSquare, long emptyMask){
		long notEmpty = ~emptyMask;
		long targetMask = (1L << targetSquare);
		
		//Pawn moves
		long piecesToVisit = (white ? b.WP : b.BP) & notEmpty & MoveGeneration.pawnAttackMasks[white ? 1 : 0][targetSquare];
		if(piecesToVisit != 0) return Long.numberOfTrailingZeros(piecesToVisit);
		
		//Knight moves
		piecesToVisit = (white ? b.WN : b.BN) & notEmpty & MoveGeneration.knightMoves[targetSquare];
		if(piecesToVisit != 0) return Long.numberOfTrailingZeros(piecesToVisit);
		
		//Mask the pieces and moves for handling the sliding piece moves
		long friendlyPieces = (white ? (b.WP | b.WN | b.WB | b.WR | b.WQ | b.WK) : (b.BP | b.BN | b.BB | b.BR | b.BQ | b.BK)) & ~targetMask;
		long enemyPieces = white ? (b.BP | b.BN | b.BB | b.BR | b.BQ | b.BK) : (b.WP | b.WN | b.WB | b.WR | b.WQ | b.WK);
		long bishopMoves = MoveGeneration.getAllBishopMoves(targetSquare, friendlyPieces, enemyPieces, emptyMask);
		long rookMoves = MoveGeneration.getAllRookMoves(targetSquare, friendlyPieces, enemyPieces, emptyMask);
		
		//Bishop moves
		piecesToVisit = (white ? b.WB : b.BB) & notEmpty & bishopMoves;
		if(piecesToVisit != 0) return Long.numberOfTrailingZeros(piecesToVisit);
		
		//Rook moves
		piecesToVisit = (white ? b.WR : b.BR) & notEmpty & rookMoves;
		if(piecesToVisit != 0) return Long.numberOfTrailingZeros(piecesToVisit);
		
		//Queen moves
		piecesToVisit = (white ? b.WQ : b.BQ) & notEmpty & (bishopMoves | rookMoves);
		if(piecesToVisit != 0) return Long.numberOfTrailingZeros(piecesToVisit);
		
		//King moves
		piecesToVisit = (white ? b.WK : b.BK) & notEmpty & MoveGeneration.kingMoves[targetSquare];
		if(piecesToVisit != 0) return Long.numberOfTrailingZeros(piecesToVisit);
		
		return -1;
	}

}
