package robb.ai;

public class NewMoveStructure {

	//6 Bits From, 6 Bits To, 4 Bits Promote, 4 Bits Piece Moved, 1 Bit "Was Capture", 1 Bit "Was Castle"

	private static final long fromMask = 63;

	public static final int toShift = 6;
	private static final long toMask = 63;

	public static final int promoteShift = 12;
	private static final long promoteMask = 15;
	
//	public static final int pieceMovedShift = 16;
//	private static final long pieceMovedMask = 15;
//	
//	public static final int wasCaptureShift = 20;
//	private static final long wasCaptureMask = 1;
//	
//	public static final int wasCastleShift = 21;
//	private static final long wasCastleMask = 1;
	
//	public static int createMove(int from, int to, int promote, int pieceMoved, boolean wasCapture, boolean wasCastle){
//		return from + (to << toShift) + (promote << promoteShift) + (pieceMoved << pieceMovedShift) + ((wasCapture ? 1 : 0) << wasCaptureShift) + ((wasCastle ? 1 : 0) << wasCastleShift);
//	}
	
	public static byte getFrom(final int move){
		return (byte)(move & fromMask);
	}
	
	public static byte getTo(final int move){
		return (byte)((move >>> toShift) & toMask);
	}
	
	public static byte getPromote(final int move){
		return (byte)((move >>> promoteShift) & promoteMask);
	}
	
//	public static byte getPieceMoved(final int move){
//		return (byte)((move >>> pieceMovedShift) & pieceMovedMask);
//	}
//	
//	public static boolean wasCapture(final int move){
//		return 0 != ((move >>> wasCaptureShift) & wasCaptureMask);
//	}
//	
//	public static boolean wasCastle(final int move){
//		return 0 != ((move >>> wasCastleShift) & wasCastleMask);
//	}
	
	public static short createMove(byte from, byte to, byte promote){
		return (short)(from + ((short)to << toShift) + ((short)promote << promoteShift));
	}

}
