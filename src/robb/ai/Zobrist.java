package robb.ai;

import java.util.Random;

public class Zobrist {
	
	private static Random random = new Random();
	public static long[][] table = new long[12][64];
	public static long[] castling = new long[4];
	public static long[] enPassant = new long[64]; //TODO only use available squares, instead of all 64
	public static long whiteToMove;
	
	public static void initZobrist(){
		for(byte p = 0; p < 12; p++){
			for(byte i = 0; i < 64; i++){
				table[p][i] = random.nextLong();
			}
		}
		for(byte i = 0; i < 4; i++) castling[i] = random.nextLong();
		for(byte i = 0; i < 64; i++) enPassant[i] = random.nextLong();
		whiteToMove = random.nextLong();
	}
	
	public static long getHash(final Board b){
		long total = 0L;
		byte i = 0;
		long piecesToVisit = b.WP | b.WN | b.WB | b.WR | b.WQ | b.WK | b.BP | b.BN | b.BB | b.BR | b.BQ | b.BK; 
		while(piecesToVisit != 0){
			if((piecesToVisit & 1L) == 1L){
				byte p = Utils.getPieceAt(b, i);
				total ^= table[p][i];
			}
			piecesToVisit = (piecesToVisit >>> 1);
			i++;
		}
		if(b.wCastleKing) total ^= castling[0];
		if(b.wCastleQueen) total ^= castling[1];
		if(b.bCastleKing) total ^= castling[2];
		if(b.bCastleQueen) total ^= castling[3];
		if(b.enPassant != 64) total ^= enPassant[b.enPassant];
		if(b.whiteToMove) total ^= whiteToMove;
		return total;
	}

}
