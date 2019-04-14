package robb.ai;

public class NodeStructure {
	
	//32 bits
	private static final long scoreMask = 4294967295L;
	
	//8 bits
	private static final int depthShift = 32;
	private static final long depthMask = 255;
	
	//16 bits
	private static final int bestMoveShift = 40;
	private static final long bestMoveMask = 65535;
	
	//2 bits
	private static final int flagShift = 56;
	private static final long flagMask = 3;
	
	//6 bits
	private static final int checkShift = 58;
	private static final long checkMask = 63;
	
	public static int getScore(final long node){
		return (int)(node & scoreMask);
	}
	
	public static byte getDepth(final long node){
		return (byte)((node >>> depthShift) & depthMask);
	}
	
	public static short getBestMove(final long node){
		return (short)((node >>> bestMoveShift) & bestMoveMask);
	}
	
	/**
	 * Applies to both nodes and hashes!
	 */
	public static byte getCheckBits(final long node){
		return (byte)((node >>> checkShift) & checkMask);
	}
	
	public static Flag getFlag(final long node){
		byte flag = (byte)((node >>> flagShift) & flagMask);
		switch(flag){
			case 0:
				return Flag.EXACT;
			case 1:
				return Flag.LOWERBOUND;
			case 2:
				return Flag.UPPERBOUND;
		}
		System.out.println("Bad Flag (" + flag + "): " + asString(node, false));
		return null;
	}
	
	public static long createNode(int depth, short bestMove, int score, Flag flag, long hash){
//		System.out.println(Utils.shortMoveToNotation(bestMove) + ": " + bestMove + ", " + Long.toBinaryString((long)(bestMove & bestMoveMask)));
//		System.out.println("Creating Node: Depth: " + depth + ", Best Move: " + bestMove + ", Score: " + score + ", Flag: " + flag);
		
		long n = (score & scoreMask) + ((long)depth << depthShift) + ((long)(bestMove & bestMoveMask) << bestMoveShift) + ((flag == Flag.EXACT ? 0L : (flag == Flag.LOWERBOUND ? 1L : 2L)) << flagShift) + (hash & (checkMask << checkShift));		
		return n;
	}
	
	public static String asString(long node, boolean flag){
		return ("Depth: " + Long.toBinaryString(getDepth(node)) + " (" + getDepth(node) + ")" + ", Best Move: " + Integer.toBinaryString(getBestMove(node)) + " (" + getBestMove(node) + "L, " + Utils.shortMoveToNotation(getBestMove(node)) + "), Score: " + Integer.toBinaryString(getScore(node)) + " (" + getScore(node) + ")" + (flag ? ", Flag: " + getFlag(node) : "")) +  ", Node: (" + node + "L)";
	}

}
