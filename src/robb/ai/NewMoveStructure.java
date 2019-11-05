package robb.ai;

public class NewMoveStructure {

	// 6 Bits From, 6 Bits To, 4 Bits Promote, 4 Bits Piece Moved, 4 Bits Piece Captured

	private static final long fromMask = 63;
	public static int getFrom(int move){
		return (int)(move & fromMask);
	}

	private static final int toShift = 6;
	private static final long toMask = 63;
	public static int getTo(int move){
		return (int)((move >>> toShift) & toMask);
	}

	private static final int promoteShift = 12;
	private static final long promoteMask = 15;
	public static int getPromote(int move){
		return (int)((move >>> promoteShift) & promoteMask);
	}
	
	private static final int pieceShift = 16;
	private static final long pieceMask = 15;
	public static int getPiece(int move){
		return (int)((move >>> pieceShift) & pieceMask);
	}
	
	private static final int captureShift = 20;
	private static final long captureMask = 15;
	public static int getCapture(int move){
		return (int)((move >>> captureShift) & captureMask);
	}
	
	public static int createMove(int from, int to, int promote, int piece, int capture){
		return from + (to << toShift) + (promote << promoteShift) + (piece << pieceShift) + (capture << captureShift);
	}
	
	public static int createMove(Board b, int from, int to, int promote){
		return createMove(from, to, promote, Utils.getPieceAt(b, from), Utils.getPieceAt(b, to));
	}

}
