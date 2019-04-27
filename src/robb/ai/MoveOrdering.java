package robb.ai;

import java.util.ArrayList;

public class MoveOrdering {
	
	public static final int hashMoveScore = 50000000, kingCaptureScore = 90000000, killerMoveScore = 300000, goodCaptureScore = 800000;
	
	private static final short killersAtPly = 5;
	
	public static int[][] history = new int[12][64];
	public static int[][] killers = new int[Search.maximumDepth][killersAtPly];
	
	public static ArrayList<Short> sort(final Board b, final short hashMove, final short ply, final ArrayList<Short> moves){
		return insertionSort(moves, getMoveScores(b, moves, hashMove, ply));
	}
	
	public static ArrayList<Integer> getMoveScores(final Board b, final ArrayList<Short> moves, final int hashMove, final short ply){
		ArrayList<Integer> scores = new ArrayList<Integer>();
		for(short m : moves){
			scores.add(getMoveScore(b, m, hashMove, ply));
		}		
		return scores;
	}

	public static void clearHistory(){
		for(byte i = 0; i < 12; i++){
			for(byte f = 0; f < 64; f++) history[i][f] = 0;		
		}
	}
	
	public static void clearKillers(){
		for(byte i = 0; i < Search.maximumDepth; i++){
			for(byte f = 0; f < killersAtPly; f++) killers[i][f] = -1;
		}
	}
	
	public static byte getKillerToOverwrite(short pliesDeep, int move){
		int[] killer = killers[pliesDeep];
		for(byte i = 0; i < killersAtPly; i++){
			if(killer[i] == move) return i; //Already exists
			if(killer[i] == -1) return i; //Empty slot
		}
		
		//Pick random slot
//		return (byte)r.nextInt(killersAtPly);
		return (byte)(System.nanoTime() % killersAtPly);
	}
	
	public static ArrayList<Short> loudOrdering(Board board, ArrayList<Short> moves){
		if(moves.size() == 0) return moves;
		
		ArrayList<Integer> scores = new ArrayList<Integer>();
		for(int i = 0; i < moves.size(); i++) scores.add(0); //Populate
		for(int i = (moves.size() - 1); i >= 0; i--){
			short move = moves.get(i);
			int see = SEE.seeCapture(board, move);
			if(see >= 0){ //-Engine.deltaMargin
				scores.set(i, see);
			}else{
				moves.remove(i);
				scores.remove(i);
			}
		}
		
		return insertionSort(moves, scores);
	}
	
	// https://www.geeksforgeeks.org/insertion-sort/
	public static ArrayList<Short> insertionSort(ArrayList<Short> moves, ArrayList<Integer> scores){
		int n = moves.size(); 
		for(int i = 1; i < n; i++){ 
			short key = moves.get(i); 
			int keyValue = scores.get(i); 
			
			int j = (i - 1); 

			while(j >= 0 && scores.get(j) < keyValue){ 
				moves.set(j + 1, moves.get(j));
				scores.set(j + 1, scores.get(j));
				j -= 1; 
			} 
			
			moves.set(j + 1, key);
			scores.set(j + 1, keyValue);
		} 
		return moves;
	}
	
	private static boolean isKiller(final int m, final short ply){
		if(ply < 0) return false;
		for(int k : killers[ply]){
			if(k == -1) break;
			if(k == m) return true;
		}
		return false;
	}
	
	public static boolean isImportantMove(int move, int hashMove, short ply){
		return move == hashMove || isKiller(move, ply);
	}
	
	public static Integer getMoveScore(final Board board, short move, int hashMove, short ply){
		if(move == hashMove){
			//Hash move
			return hashMoveScore; 
		}else{
			final byte enemy = Utils.getToBeCapturedPiece(board, move); 			
			if(enemy == 5 || enemy == 11){
				//King capture
				return kingCaptureScore; 
			}else{
				final byte friendly = Utils.getPieceAt(board, NewMoveStructure.getFrom(move));
				if(enemy != 12){
//					//MVV LVA
//					int score = 10000000 * (1 + (enemy % 6)); 
//					score -= 100000 * (1 + (friendly % 6));
//					return score; 
					
					//SEE
					int see = SEE.seeCapture(board, move);
					if(see >= 0){
						return see + goodCaptureScore;
					}else{
						return see; //Bad capture
					}
				}else if(isKiller(move, ply)){
					//Killer move
					return killerMoveScore; 
				}else{
					//History heuristic	
					final byte to = NewMoveStructure.getTo(move);
					int historyScore = history[friendly][to];
//					if(historyScore < 0) System.out.println(historyScore);
					return Math.min(historyScore, killerMoveScore - 1); 				
				}
			}
		}
	}

}
