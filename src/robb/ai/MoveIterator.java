package robb.ai;

public class MoveIterator {
	
	public static final int maxMoves = 255;
	
	private int[] moves;
	private int[] scores;
	private int moveCount = 0;

	private int index;
	
//	public int highestObservedScore = Integer.MIN_VALUE;
	
	public MoveIterator(){
		super();
		this.moves = new int[maxMoves];
		this.scores = new int[maxMoves];
		this.index = 0;
	}
	
	public boolean generateMoves(Board b, int hashMove, int ply, boolean loud){
		// Reset.
		this.moveCount = 0;
		this.index = 0;
		for(int i = 0; i < maxMoves; i++){
			if(this.moves[i] == 0) break;
			this.moves[i] = 0;
			this.scores[i] = 0;
		}
		
		if(loud){
			MoveGeneration.getAllLoudMoves(b, this.moves);
			MoveOrdering.getLoudMoveScores(b, this.moves, this.scores);
//			MoveOrdering.getMoveScores(b, this.moves, hashMove, ply, this.scores);
//			MoveOrdering.insertionSort(moves, scores, true);
		}else{
			MoveGeneration.getAllMoves(b, this.moves);
			MoveOrdering.getMoveScores(b, this.moves, hashMove, ply, this.scores);
//			MoveOrdering.insertionSort(moves, scores, false);
		}
		
		this.moveCount = getMoveCount(this.moves);
		
		return this.moveCount != 0;
	}
	
	public int getNextMove(){
		if(this.index >= this.moveCount) return 0;
		
//		this.index ++; 
//		return this.moves[this.index - 1];
		
		int best = -1;
		for(int i = this.index; i < this.moveCount; i++){
			if(best == -1 || this.scores[i] > this.scores[best]){
				best = i;
			}
		}
		
		// Swap.
		int bestMove = this.moves[best];
		int bestScore = this.scores[best];
		this.moves[best] = this.moves[this.index];
		this.moves[this.index] = bestMove;
		this.scores[best] = this.scores[this.index];
		this.scores[this.index] = bestScore;
		
		this.index ++;
		
		return bestMove;
	}
	
	public int getLastScore(){
		return this.scores[this.index - 1];
	}

	public static int getMoveCount(int[] moves2){
		for(int i = 0; i < maxMoves; i++){
			if(moves2[i] == 0) return i;
		}
		return 0;
	}

}
