package robb.ai;

public class NodeStructure {
	
	// 32 bits
	private static final long scoreMask = 4294967295L;
	
	// 8 bits
	private static final int depthShift = 32;
	private static final long depthMask = 255;
	
	// 16 bits
	private static final int moveShift = 40;
	private static final long moveMask = 65535;
	
	// 2 bits
	private static final int flagShift = 56;
	private static final long flagMask = 3;
	
	// 6 bits
	private static final int checkShift = 58;
	private static final long checkMask = 63;
	
	public static int getScore(long node){
		return (int)(node & scoreMask);
	}
	
	public static int getDepth(long node){
		return (int)((node >>> depthShift) & depthMask);
	}
	
	public static int getMove(Board b, long node){
		int move = (int)((node >>> moveShift) & moveMask);
		return NewMoveStructure.createMove(b, NewMoveStructure.getFrom(move), NewMoveStructure.getTo(move), NewMoveStructure.getPromote(move));
	}
	
	/**
	 * Applies to both nodes and hashes!
	 */
	public static byte getCheckBits(long node){
		return (byte)((node >>> checkShift) & checkMask);
	}
	
	public static Flag getFlag(long node){
		byte flag = (byte)((node >>> flagShift) & flagMask);
		switch(flag){
			case 0:
				return Flag.EXACT;
			case 1:
				return Flag.LOWERBOUND;
			case 2:
				return Flag.UPPERBOUND;
		}
//		System.out.println("Bad Flag (" + flag + "): " + asString(node, false));
		return null;
	}
	
	public static long createNode(int depth, int move, int score, Flag flag, long hash){
//		System.out.println(Utils.shortMoveToNotation(bestMove) + ": " + bestMove + ", " + Long.toBinaryString((long)(bestMove & bestMoveMask)));
//		System.out.println("Creating Node: Depth: " + depth + ", Best Move: " + bestMove + ", Score: " + score + ", Flag: " + flag);
		
		long n = (score & scoreMask) + ((long)depth << depthShift) + ((long)(move & moveMask) << moveShift) + ((flag == Flag.EXACT ? 0L : (flag == Flag.LOWERBOUND ? 1L : 2L)) << flagShift) + ((hash & checkMask) << checkShift);		
		return n;
	}
	
//	public static String asString(long node, boolean flag){
//		return ("Depth: " + Long.toBinaryString(getDepth(node)) + " (" + getDepth(node) + ")" + ", Best Move: " + Integer.toBinaryString(getMove(node)) + " (" + getMove(node) + "L, " + Utils.moveToNotation(getMove(node)) + "), Score: " + Integer.toBinaryString(getScore(node)) + " (" + getScore(node) + ")" + (flag ? ", Flag: " + getFlag(node) : "")) +  ", Node: (" + node + "L)";
//	}

}
