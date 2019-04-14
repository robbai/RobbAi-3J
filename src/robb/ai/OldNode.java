package robb.ai;

public class OldNode {
	
	byte depth;
	short bestMove;
	int value;
	Flag flag;
	
	public OldNode(byte depth, short bestMove, int value, Flag flag){
		super();
		this.depth = depth;
		this.bestMove = bestMove;
		this.value = value;
		this.flag = flag;
	}

}
