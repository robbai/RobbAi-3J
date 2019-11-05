package robb.ai;

public class HistoryStructure {
	
	//6 Bits From, 6 Bits To, 4 Bits Promote, 4 Bits Castling, 4 Bits Captured Piece, 1 Bit "En Passant", 7 Bits Previous "En Passant" Square, 7 Bits Half Move Clock, 1 Bit Null Move 	
	//0 0000000 0000000 0 0000 0000 0000 000000 000000
	//N HALF-MC EP-SQRE E CAPP CAST PROM TO-SQR FROM-S
	
	private static final long fromMask = 63;
	
	public static final int toShift = 6;
	private static final long toMask = 63;

	public static final int promoteShift = 12;
	private static final long promoteMask = 15;
	
	private static final int castleShift = 16;
	private static final long castleMask = 15;
	
	public static final int pieceCapturedShift = 20;
	private static final long pieceCapturedMask = 15;
	
	private static final int enPassantShift = 24;
	
	private static final int lastEnPassantSquareShift = 25;
	private static final long lastEnPassantSquareMask = 127;
	
	private static final int halfMoveShift = 32;
	private static final long halfMoveMask = 127;
	
	private static final int nullMoveShift = 39;
		
	public static byte getFrom(long move){
		return (byte)(move & fromMask);
	}
	
	public static byte getTo(long move){
		return (byte)((move >>> toShift) & toMask);
	}
	
	public static byte getPromote(long move){
		return (byte)((move >>> promoteShift) & promoteMask);
	}
	
	public static byte getCastles(long move){
		return (byte)((move >>> castleShift) & castleMask);
	}
	
	public static byte getPieceCaptured(long move){
		return (byte)((move >>> pieceCapturedShift) & pieceCapturedMask);
	}
	
	public static boolean wasEnPassant(long move){
		return 1 == ((move >>> enPassantShift) & 1L);
	}
	
	public static byte getLastEnPassantSquare(long move){
		return (byte)((move >>> lastEnPassantSquareShift) & lastEnPassantSquareMask);
	}
	
	public static byte getHalfMove(long move){
		return (byte)((move >>> halfMoveShift) & halfMoveMask);
	}
	
	public static boolean wasNullMove(long move){
		return 1L == ((move >>> nullMoveShift) & 1L);
	}
	
	public static long createMove(int from, int to, int promote, int castle, int pieceCaptured, boolean enPassant, int lastEnPassantSquare, int halfMoveClock){
		return ((long)from + (long)(to << toShift) + (long)(promote << promoteShift) + (long)(castle << castleShift) + (long)(pieceCaptured << pieceCapturedShift) + (long)((enPassant ? 1 : 0) << enPassantShift) + (long)((long)lastEnPassantSquare << lastEnPassantSquareShift) + (long)((long)halfMoveClock << halfMoveShift));
	}
	
	public static long createNullMove(int lastEnPassantSquare, int halfMoveClock){
		return ((long)((long)lastEnPassantSquare << lastEnPassantSquareShift) + (long)((long)halfMoveClock << halfMoveShift) + (long)(1L << nullMoveShift));
	}
	
	public static String toString(long l){
		return "Move = " + Utils.moveToNotation(getFrom(l) + (getTo(l) << toShift) + (getPromote(l) << promoteShift)) + " (From = " + getFrom(l) + ", To = " + getTo(l) + ", Promote = " + getPromote(l) + "), Castles = " + Long.toBinaryString(getCastles(l)) + ", Piece Captured = " + getPieceCaptured(l) + ", Was En Passant = " + wasEnPassant(l) + ", Last En Passant Square = " + getLastEnPassantSquare(l) + ", Half Move = " + getHalfMove(l) + ", Was Null" + " = " + wasNullMove(l); 
	}

}