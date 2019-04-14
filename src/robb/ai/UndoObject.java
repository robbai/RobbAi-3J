package robb.ai;

public class UndoObject {
	
	public byte moveFrom, moveTo, enPassantBefore, halfMoveClockBefore, movePromotion, pieceCaptured;
	public int lastIrreversibleMoveBefore;
	public boolean wCastleKing, wCastleQueen, bCastleKing, bCastleQueen, wasEnPassantCapture;
	
	public UndoObject(byte moveFrom, byte moveTo, boolean wCastleKing, boolean wCastleQueen, boolean bCastleKing, boolean bCastleQueen, byte pieceCaptured, byte enPassantBefore, byte halfMoveClockBefore, int lastIrreversibleMoveBefore, byte movePromotion, boolean wasEnPassantCapture){
		super();
		this.moveFrom = moveFrom;
		this.moveTo = moveTo;
		this.enPassantBefore = enPassantBefore;
		this.halfMoveClockBefore = halfMoveClockBefore;
		this.lastIrreversibleMoveBefore = lastIrreversibleMoveBefore;
		this.movePromotion = movePromotion;
		this.wCastleKing = wCastleKing;
		this.wCastleQueen = wCastleQueen;
		this.bCastleKing = bCastleKing;
		this.bCastleQueen = bCastleQueen;
		this.wasEnPassantCapture = wasEnPassantCapture;
		this.pieceCaptured = pieceCaptured;
	}

	@Override
	public String toString() {
		return "UndoObject [moveFrom=" + Utils.notation[moveFrom] + ", moveTo=" + Utils.notation[moveTo] + ", enPassantBefore=" + (enPassantBefore == -1 ? "None" : Utils.notation[enPassantBefore])
				+ ", halfMoveClockBefore=" + halfMoveClockBefore + ", movePromotion=" + (movePromotion == 0 ? "0" : Utils.pieceChars[movePromotion])
				+ ", pieceCaptured=" + (pieceCaptured == 12 ? "None" : Utils.pieceChars[pieceCaptured]) + ", lastIrreversibleMoveBefore=" + lastIrreversibleMoveBefore
				+ ", wCastleKing=" + wCastleKing + ", wCastleQueen=" + wCastleQueen + ", bCastleKing=" + bCastleKing
				+ ", bCastleQueen=" + bCastleQueen + ", wasEnPassantCapture=" + wasEnPassantCapture + "]";
	}

}