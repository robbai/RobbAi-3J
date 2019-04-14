package robb.ai;

public class Board {
	
	public long WP = 0L;
	public long WN = 0L;
	public long WB = 0L;
	public long WR = 0L;	
	public long WQ = 0L;	
	public long WK = 0L;
	public long BP = 0L;
	public long BN = 0L;
	public long BB = 0L;
	public long BR = 0L;
	public long BQ = 0L;
	public long BK = 0L;
	
	public boolean wCastleQueen;
	public boolean wCastleKing;
	public boolean bCastleQueen;
	public boolean bCastleKing;
	public boolean whiteToMove;
	
	public byte enPassant;
	public byte halfMoveClock;
		
	public Board(long wP, long wN, long wB, long wR, long wQ, long wK, long bP, long bN, long bB, long bR, long bQ, long bK, boolean wCastleQueen, boolean wCastleKing, boolean bCastleQueen, boolean bCastleKing, boolean whiteToMove, byte enPassant, byte halfMoveClock){
		super();
		WP = wP;
		WN = wN;
		WB = wB;
		WR = wR;
		WQ = wQ;
		WK = wK;
		BP = bP;
		BN = bN;
		BB = bB;
		BR = bR;
		BQ = bQ;
		BK = bK;
		this.wCastleQueen = wCastleQueen;
		this.wCastleKing = wCastleKing;
		this.bCastleQueen = bCastleQueen;
		this.bCastleKing = bCastleKing;
		this.whiteToMove = whiteToMove;
		this.enPassant = enPassant;
		this.halfMoveClock = halfMoveClock;
	}
	
	public long[] getPieceArray(){
		return new long[] {WP, WN, WB, WR, WQ, WK, BP, BN, BB, BR, BQ, BK};
	}
	
	public void updateWithPieceArray(final long[] array){
		WP = array[0];
		WN = array[1];
		WB = array[2];
		WR = array[3];
		WQ = array[4];
		WK = array[5];
		BP = array[6];
		BN = array[7];
		BB = array[8];
		BR = array[9];
		BQ = array[10];
		BK = array[11];
	}

}
