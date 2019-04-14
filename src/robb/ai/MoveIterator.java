package robb.ai;

import java.util.ArrayList;

public class MoveIterator {
	
	private ArrayList<Short> moves;
	private ArrayList<Integer> scores;
	public int highestObservedScore = Integer.MIN_VALUE;
	
	public MoveIterator(){
		super();
		moves = new ArrayList<Short>();
		scores = new ArrayList<Integer>();
	}
	
	public boolean generateMoves(Board b, int hashMove, short ply){
		moves = MoveGeneration.getAllMoves(b);
		scores = MoveOrdering.getMoveScores(b, moves, hashMove, ply);
		return moves.size() > 0;
		
//		moves = MoveGeneration.getAllMoves(b);
//		moves = MoveOrdering.insertionSort(moves, MoveOrdering.getMoveScores(b, moves, hashMove, ply));
//		return moves.size() > 0;
	}
	
	public short getNextMove(){
		if(moves.size() == 0) return -1;
		byte bestIndex = -1;
		for(byte i = 0; i < moves.size(); i++){
			if(bestIndex == -1 || scores.get(i) >= scores.get(bestIndex)){
				bestIndex = i;
			}
		}
		short bestMove = moves.get(bestIndex);
		highestObservedScore = Math.max(scores.get(bestIndex), highestObservedScore);
		moves.remove(bestIndex);
		scores.remove(bestIndex);
		return bestMove;
		
//		if(moves.size() == 0) return -1;
//		short m = moves.get(0);
//		moves.remove(0);
//		return m;
	}

}
