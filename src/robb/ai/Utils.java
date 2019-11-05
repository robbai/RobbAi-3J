package robb.ai;

public class Utils {
	
	private static final String[] pieces = new String[] {"P", "N", "B", "R", "Q", "K", "p", "n", "b", "r", "q", "k", "."};
	private static final char[] filesChars = new char[] {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h'};
	public static final long[] ranks = new long[] {255L, 65280L, 16711680L, 4278190080L, 1095216660480L, 280375465082880L, 71776119061217280L, -72057594037927936L};
	public static final long[] filesIndex = new long[] {72340172838076673L, 144680345676153346L, 289360691352306692L, 578721382704613384L, 1157442765409226768L, 2314885530818453536L, 4629771061636907072L, -9187201950435737472L};
	public static final long[] filesLogical = new long[] {-9187201950435737472L, 4629771061636907072L, 2314885530818453536L, 1157442765409226768L, 578721382704613384L, 289360691352306692L, 144680345676153346L, 72340172838076673L};
	public static final String[] notation = new String[] {"h1", "g1", "f1", "e1", "d1", "c1", "b1", "a1", "h2", "g2", "f2", "e2", "d2", "c2", "b2", "a2", "h3", "g3", "f3", "e3", "d3", "c3", "b3", "a3", "h4", "g4", "f4", "e4", "d4", "c4", "b4", "a4", "h5", "g5", "f5", "e5", "d5", "c5", "b5", "a5", "h6", "g6", "f6", "e6", "d6", "c6", "b6", "a6", "h7", "g7", "f7", "e7", "d7", "c7", "b7", "a7", "h8", "g8", "f8", "e8", "d8", "c8", "b8", "a8"};
	public static final char[] pieceChars = new char[] {'P', 'N', 'B', 'R', 'Q', 'K', 'p', 'n', 'b', 'r', 'q', 'k', '0'};
	
	public static byte notationToByte(String square){
		char file = square.charAt(0);
		byte fileIndex = 0;
		for(byte i = 0; i < 8; i++){
			if(filesChars[i] == file){
				fileIndex = i;
				break;
			}
		}
		return (byte)((7 - fileIndex) + ((Byte.parseByte("" + square.charAt(1)) - 1) * 8));
	}
	
	public static int notationToMove(Board b, String notation){
		byte from = notationToByte(notation.substring(0, 2));
		byte to = notationToByte(notation.substring(2, 4));
//		byte p = Utils.getPieceAt(b, from);
//		boolean cap = Utils.getPieceAt(b, to) != 12 || ((p == 0 || p == 6) && (Math.abs(from - to) % 8) != 0);
				
		if(notation.length() == 5){
			boolean white = (notation.charAt(3) == '8');
			switch(notation.charAt(4)){
				case 'q':
					return NewMoveStructure.createMove(b, from, to, (white ? 4 : 10));
				case 'n':
					return NewMoveStructure.createMove(b, from, to, (white ? 1 : 7));
				case 'r':
					return NewMoveStructure.createMove(b, from, to, (white ? 3 : 9));
				case 'b':
					return NewMoveStructure.createMove(b, from, to, (white ? 2 : 8));				
			}
		}
		
//		boolean castle = (p == 5 && from == 3 && (to == 1 || to == 5)) || (p == 11 && from == 59 && (to == 57 || to == 61)); 		
		return NewMoveStructure.createMove(b, from, to, 12);
	}
	
	public static byte pieceToByte(char p){
		byte black = 0;
		if(Character.toLowerCase(p) == p) black += 6;		
		switch(Character.toLowerCase(p)){
			case 'p':
				return (byte)(black + 0);
			case 'n':
				return (byte)(black + 1);
			case 'b':
				return (byte)(black + 2);
			case 'r':
				return (byte)(black + 3);
			case 'q':
				return (byte)(black + 4);
			case 'k':
				return (byte)(black + 5);
		}
		return -1;
	}
	
	public static byte getPieceAt(Board b, int square){
		Long mask = (1L << square);
		if((b.WP & mask) != 0) return 0; 
		if((b.WN & mask) != 0) return 1; 
		if((b.WB & mask) != 0) return 2; 
		if((b.WR & mask) != 0) return 3; 
		if((b.WQ & mask) != 0) return 4; 
		if((b.WK & mask) != 0) return 5;
		if((b.BP & mask) != 0) return 6; 
		if((b.BN & mask) != 0) return 7; 
		if((b.BB & mask) != 0) return 8; 
		if((b.BR & mask) != 0) return 9; 
		if((b.BQ & mask) != 0) return 10; 
		if((b.BK & mask) != 0) return 11;
		return 12;
	}
	
	private static boolean isPieceAt(Board b, byte square){
		return ((1L << square) & (b.WP | b.WN | b.WB | b.WR | b.WQ | b.WK | b.BP | b.BN | b.BB | b.BR | b.BQ | b.BK)) != 0L;		
	}
	
	public static void printBoard(Board b, boolean info){
		if(b == null){
			System.err.println("Error: Null Board");
			return;
		}
		for(int y = 7; y > -1; y--){
			String s = "";
			for(int x = 7; x > -1; x--){
				s += pieces[getPieceAt(b, (byte)(y * 8 + x))] + " ";
			}
			System.out.println(s);
		}
		if(info){
			System.out.println("Side To Move: " + (b.whiteToMove ? "White" : "Black"));
			System.out.println("White Castling: King=" + b.wCastleKing + ", Queen=" + b.wCastleQueen);
			System.out.println("Black Castling: King=" + b.bCastleKing + ", Queen=" + b.bCastleQueen);
			System.out.println("En Passant = " + (b.enPassant == 64 ? "None" : notation[b.enPassant]) + ", Half Move Clock = " + b.halfMoveClock);
		}
	}
	
	public static void printBoardCoords(){
		for(int y = 7; y > -1; y--){
			String s = "";
			for(int x = 7; x > -1; x--){
				int v = (y * 8) + x;
				s += ((v + "").length() == 2 ? v + " " : " " + v + " ");
			}
			System.out.println(s);
		}
	}
	
	private static void printBoardCoordsX88(){
		for(int y = 7; y > -1; y--){
			String s = "";
			for(int x = 7; x > -1; x--){
				int v = (y * 16) + x;
				s += v + " " + ((v + "").length() < 3 ? " " : "") + ((v + "").length() < 2 ? " " : "");
			}
			System.out.println(s);
		}
	}
	
	public static String moveToNotation(int move){
		if(move == 0) return "0000";
		
		int from = NewMoveStructure.getFrom(move);
		int to = NewMoveStructure.getTo(move);
		int promote = NewMoveStructure.getPromote(move);
		
		if(promote != 12){
			return notation[from] + notation[to] + pieces[promote].toLowerCase();
		}else{
			return notation[from] + notation[to];
		}
	}
	
	public static short pliesToMoves(int i){
		return (byte)(i % 2 == 0 ? i / 2 : (i + 1) / 2);
	}
	
	private static boolean isCapture(Board b, short move){
		if(isPieceAt(b, (byte)((move >>> 6) & 63))) return true;
		byte start = (byte)(move & 63);
		if(b.enPassant != 64){ // En Passant.
			if(((1L << start) & (b.WP | b.BP)) != 0L) return (Math.abs(start - ((move >>> 6) & 63)) % 8) != 0; // If Pawn move, return diagonal
		}
		return false;
	}
	
	public static int getToBeCapturedPiece(Board b, int move){
		int capture = NewMoveStructure.getCapture(move); 
		if(capture != 12 || b.enPassant == 64) return capture;
		
		// En Passant.
		if((NewMoveStructure.getPiece(move) % 6) == 0){
			int from = NewMoveStructure.getFrom(move); 
			return ((Math.abs(from - ((move >>> 6) & 63)) % 8) != 0 ? (b.whiteToMove ? 6 : 0) : 12); // If Pawn move, is diagonal.
		}

		return 12;
	}
	
	private static void printBitboard(Long m){
		printBitboard("Board", m);
	}
	
	private static void printBitboard(String name, Long m){
		String s = Long.toBinaryString(m);
    	while(s.length() < 64) s = "0" + s;
    	s = s.replace("1", "1 ").replace("0", "0 ");
    	System.out.println(name + ": ");
    	for(int f = 0; f < 8; f++){
    		System.out.println(s.substring(f * 16, f * 16 + 16));
    	}
	}

	public static int pieceCount(Board board){
		return Long.bitCount(board.WN | board.WB | board.WR | board.WQ | board.BN | board.BB | board.BR | board.BQ);
	}

}
