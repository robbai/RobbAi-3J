package robb.ai;

public class MoveOrdering {
	
	public static final int kingCaptureScore = 90000000, hashMoveScore = 50000000, goodCaptureScore = 10000000, killerMoveScore = 2000000;
	
	private static final int killersAtPly = 3;
	
	public static int[][] history = new int[12][64];
	public static int[][] killers = new int[Search.maximumDepth][killersAtPly];
	
//	public static ArrayList<Short> sort(Board b, short hashMove, short ply, ArrayList<Short> moves){
//		return insertionSort(moves, getMoveScores(b, moves, hashMove, ply, new ArrayList<Integer>()));
//	}
	
	public static int[] getMoveScores(Board b, int[] moves, int hashMove, int ply, int[] scores){
		for(int i = 0; i < MoveIterator.maxMoves; i++){
			int move = moves[i];
			if(move == 0) break;
			scores[i] = getMoveScore(b, move, hashMove, ply);
		}		
		return scores;
	}

	public static void clearHistory(){
//		for(byte piece = 0; piece < 12; piece++){
//			for(byte square = 0; square < 64; square++){
//				history[piece][square] = 0;		
//			}
//		}
		history = new int[12][64];
	}
	
	public static void clearKillers(){
//		for(byte ply = 0; ply < Search.maximumDepth; ply++){
//			for(byte perPly = 0; perPly < killersAtPly; perPly++){
//				killers[ply][perPly] = 0;
//			}
//		}
		killers = new int[Search.maximumDepth][killersAtPly];
	}
	
	public static int[] getLoudMoveScores(Board board, int[] moves, int[] scores){
		for(int i = 0; i < MoveIterator.maxMoves; i++){
			if(moves[i] == 0) break;
			
			int score = SEE.seeCapture(board, moves[i]);
			
//			int score = 0;
//			byte enemy = Utils.getToBeCapturedPiece(board, moves[i]);
//			if(enemy != 12){
//				score = 10 * Evaluation.minimalistScoreAbs[enemy % 6]; 
//				byte friendly = Utils.getPieceAt(board, NewMoveStructure.getFrom(moves[i]));
//				score -= Evaluation.minimalistScoreAbs[friendly % 6]; 
//			}
			
			scores[i] = score;
		}
		
		return scores;
	}
	
	//  https:// www.geeksforgeeks.org/insertion-sort/
	public static int[] insertionSort(int[] moves, int[] scores){
		for(int i = 1; i < MoveIterator.maxMoves; i++){
			int key = moves[i];
			int keyValue = scores[i];
			if(key == 0) break;
			
			int j = (i - 1);

			while(j >= 0 && scores[j] < keyValue){ 
				moves[j + 1] = moves[j];
				scores[j + 1] = scores[j];
				j--;
			}
			
			moves[j + 1] = key;
			scores[j + 1] = keyValue;
		}
		return moves;
	}
	
	public static void saveKiller(int ply, int move){
		for(int i = (killersAtPly - 1); i > 0; i--){
			killers[ply][i] = killers[ply][i - 1];
		}
		killers[ply][0] = move;
	}
	
	private static int getKillerIndex(int move, int ply){
		if(ply != -1){
			for(int i = 0; i < killersAtPly; i++){
				int killer = killers[ply][i];
				if(killer == 0) break;
				if(killer == move) return i;
			}
		}
		return -1;
	}
	
	public static boolean isImportantMove(int move, int hashMove, short ply){
		return move == hashMove || getKillerIndex(move, ply) != -1;
	}
	
	public static int getMoveScore(Board board, int move, int hashMove, int ply){
		if(move == hashMove){
			// Hash move.
			return hashMoveScore; 
		}else{
			int capture = Utils.getToBeCapturedPiece(board, move);
			
			int killerIndex = getKillerIndex(move, ply);
			
			if(capture != 12){
				// King capture.
				if(capture == 5 || capture == 11) return kingCaptureScore; 
					
//				// MVV LVA.
//				int score = goodCaptureScore * (1 + (enemy % 6)); 
//				byte friendly = Utils.getPieceAt(board, NewMoveStructure.getFrom(move));
//				score -= goodCaptureScore / 10 * (1 + (friendly % 6));
//				return score; 

				// SEE.
				int see = SEE.seeCapture(board, move);
				if(see >= 0){
					return see + goodCaptureScore;
				}else{
					return see; // Bad capture
				}
			}else if(killerIndex != -1){
				// Killer move.
				return killerMoveScore - killerIndex;
			}else{
				// History heuristic.
				int piece = NewMoveStructure.getPiece(move);
				int to = NewMoveStructure.getTo(move);
				
				int historyScore = history[piece][to];
				
				return Math.min(historyScore, killerMoveScore - killersAtPly);
//				return historyScore;
			}
		}
	}

}
