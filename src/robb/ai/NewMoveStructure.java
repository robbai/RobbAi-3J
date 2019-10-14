package robb.ai;

public class NewMoveStructure {

	// 6 Bits From, 6 Bits To, 4 Bits Promote, 4 Bits Piece Moved, 1 Bit "Was Capture", 1 Bit "Was Castle"

	private static final long fromMask = 63;

	public static final int toShift = 6;
	private static final long toMask = 63;

	public static final int promoteShift = 12;
	private static final long promoteMask = 15;
	
	public static int getFrom(int move){
		return (int)(move & fromMask);
	}
	
	public static int getTo(int move){
		return (int)((move >>> toShift) & toMask);
	}
	
	public static int getPromote(int move){
		return (int)((move >>> promoteShift) & promoteMask);
	}
	
//	public static int getPieceMoved(int move){
//		return (int)((move >>> pieceMovedShift) & pieceMovedMask);
//	}
//	
//	public static boolean wasCapture(int move){
//		return 0 != ((move >>> wasCaptureShift) & wasCaptureMask);
//	}
//	
//	public static boolean wasCastle(int move){
//		return 0 != ((move >>> wasCastleShift) & wasCastleMask);
//	}
	
	public static short createMove(int from, int to, int promote){
		return (short)(from + ((short)to << toShift) + ((short)promote << promoteShift));
	}

}
