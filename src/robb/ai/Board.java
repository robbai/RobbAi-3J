package robb.ai;

import java.util.ArrayList;

public class Board {
	
	public ArrayList<Long> threeFold;
	public ArrayList<Long> history;
	
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
	
	public int enPassant;
	public int halfMoveClock;
		
	public Board(long wP, long wN, long wB, long wR, long wQ, long wK, long bP, long bN, long bB, long bR, long bQ, long bK, boolean wCastleQueen, boolean wCastleKing, boolean bCastleQueen, boolean bCastleKing, boolean whiteToMove, int enPassant, int halfMoveClock, ArrayList<Long> threeFold, ArrayList<Long> history){
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
		this.threeFold = threeFold;
		this.history = history;
	}
	
	public Board(long wP, long wN, long wB, long wR, long wQ, long wK, long bP, long bN, long bB, long bR, long bQ, long bK, boolean wCastleQueen, boolean wCastleKing, boolean bCastleQueen, boolean bCastleKing, boolean whiteToMove, byte enPassant, byte halfMoveClock){
		this(wP, wN, wB, wR, wQ, wK, bP, bN, bB, bR, bQ, bK, wCastleQueen, wCastleKing, bCastleQueen, bCastleKing, whiteToMove, enPassant, halfMoveClock, new ArrayList<Long>(), new ArrayList<Long>());
	}
	
	public long[] getPieceArray(){
		return new long[] {WP, WN, WB, WR, WQ, WK, BP, BN, BB, BR, BQ, BK};
	}
	
	public void updateWithPieceArray(long[] array){
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
	
	public Board clone(){
		ArrayList<Long> threeFoldClone = new ArrayList<Long>();
		for(long l : this.threeFold) threeFoldClone.add(l);
		
		ArrayList<Long> historyClone = new ArrayList<Long>();
		for(long l : this.history) historyClone.add(l);
		
		return new Board(this.WP, this.WN, this.WB, this.WR, this.WQ, this.WK, this.BP, this.BN, this.BB, this.BR, this.BQ, this.BK, this.wCastleQueen, this.wCastleKing, this.bCastleQueen, this.bCastleKing, this.whiteToMove, this.enPassant, this.halfMoveClock, threeFoldClone, historyClone);
	}
	
	public long P(int p){
		if(p == 0){
			return WP;
		}else if(p == 1){
			return WN;
		}else if(p == 2){
			return WB;
		}else if(p == 3){
			return WR;
		}else if(p == 4){
			return WQ;
		}else if(p == 5){
			return WK;
		}else if(p == 6){
			return BP;
		}else if(p == 7){
			return BN;
		}else if(p == 8){
			return BB;
		}else if(p == 9){
			return BR;
		}else if(p == 10){
			return BQ;
		}else if(p == 11){
			return BK;
		}
		return 0;
	}

	public long W(){
		return WP | WN | WB | WR | WQ | WK;
	}
	
	public long B(){
		return BP | BN | BB | BR | BQ | BK;
	}

}
