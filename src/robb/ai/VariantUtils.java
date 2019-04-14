package robb.ai;

public class VariantUtils {
	
	private static long kothWinMask = ((Utils.filesLogical[3] | Utils.filesLogical[4]) & (Utils.ranks[3] | Utils.ranks[4]));
	
	public static boolean isKothWinForOpposingSide(Board b){
		return 0L != (kothWinMask & (b.whiteToMove ? b.BK : b.WK));
	}

}
