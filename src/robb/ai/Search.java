package robb.ai;

import java.util.ArrayList;

public class Search extends Thread implements Runnable {
	
	/**
	 * Search output
	 */
	private static final boolean pvEnabled = true;	
	
	/**
	 * Info regarding time given for search
	 */
	private TimeControl tc;
	private long time;
	private boolean increment;
	
	/**
	 * Search parameters
	 */
	private static final int contempt = 0, nullMoveReduction = 2;
	public static final int deltaMargin = 160;
	
	/**
	 * Constants
	 */
	public static final short minimumDepth = 2, maximumDepth = 25;
	public static final int mateValue = 200000;
	
	/**
	 * Search details and results
	 */
	public boolean searching /*isInterrupted() sux*/, completedMinimumDepth /*For sudden-death*/;
	private int fh = 0, fhf = 0, seldepth = 0;
	private long nodes = 0L, qNodes = 0L, timeStarted;
	public short bestMove;

	/**
	 * Objects
	 */
	private Board board;
	private MoveIterator[] iterators = new MoveIterator[maximumDepth];
	private Timer parralelTimer = null;
		
	/**
	 * Set up the search
	 */
	public Search(Board board, TimeControl tc, long time, boolean increment){
		this.board = board.clone();
		
		//Set the time controls
		this.tc = tc;
		this.time = time;
		this.increment = increment;
		
		//Info regarding the search
		this.bestMove = -1;
		this.timeStarted = System.currentTimeMillis();
		this.nodes = 0L;
		this.qNodes = 0L;
		this.completedMinimumDepth = (tc != TimeControl.SUDDENDEATH);
		this.searching = true;

		//Info regarding move-ordering efficiency
		this.fh = 0;
		this.fhf = 0;

		//Move ordering
		MoveOrdering.clearHistory();
		MoveOrdering.clearKillers();
		Engine.resetTTable(false);
		for(int i = 0; i < maximumDepth; i++) this.iterators[i] = new MoveIterator();
	}

	@Override
	public void run(){
		ArrayList<Short> moves = MoveGeneration.getAllLegalMoves(board);
		bestMove = moves.get(0);
		
		int depth = 1;
		while(moves.size() > 1 && depth <= maximumDepth && (tc != TimeControl.DEPTH || depth <= time /*Time representing the depth to search*/)){
			byte bestThisDepth = -1;
			int bestScore = 0;
			seldepth = 0;
			
			for(byte i = 0; i < moves.size(); i++){
				board = Make.makeMove(board, moves.get(i));
				int score = -minimax((short)(depth - 1), (short)1, Integer.MIN_VALUE + 1, Integer.MAX_VALUE, false);
				board = Make.undoMove(board);
				
				if(!this.searching) return;
				
				if(bestThisDepth == -1 || score > bestScore){
					bestThisDepth = i;
					bestScore = score;
				}
				
				//Debug to print all moves
				if(Engine.debug){
					boolean mate = isMateValue(score);
					System.out.println("info currmove " + Utils.shortMoveToNotation(moves.get(i)) + " score " + (mate ? "mate " + (score < 0 ? "-" : "") + Utils.pliesToMoves(1 + Math.abs(Math.abs(score) - mateValue)) : "cp " + score) + " depth " + depth + " seldepth " + seldepth + " nodes " + nodes + " time " + (System.currentTimeMillis() - timeStarted) + " nps " + nps(nodes, System.currentTimeMillis() - timeStarted) + " pv " + getPv(moves.get(i), depth));
				}				
			}
			
			//Set the best move
//			if(bestThisDepth == -1) break;	
			bestMove = moves.get(bestThisDepth);
			
			//PV line
			if(!Engine.debug){
				final boolean mate = isMateValue(bestScore);				
				System.out.println("info currmove " + Utils.shortMoveToNotation(moves.get(bestThisDepth)) + " score " + (mate ? "mate " + (bestScore < 0 ? "-" : "") + Utils.pliesToMoves(1 + Math.abs(Math.abs(bestScore) - mateValue)) : "cp " + bestScore) + " depth " + depth + " seldepth " + seldepth + " nodes " + nodes + " time " + (System.currentTimeMillis() - timeStarted) + " nps " + nps(nodes, System.currentTimeMillis() - timeStarted) + " pv " + getPv(moves.get(bestThisDepth), depth));
				if(mate){ // && (bestScore > 0 || bestScore <= -mateValue + 6)
					break;
				}
			}
			
			completedMinimumDepth = (depth >= minimumDepth);
			depth ++;
		}
		
		//If we end the search early (one move, forced mate, etc.)
		completedMinimumDepth = true;
		this.interrupt(); 
	}
	
	public void interrupt(){
		if(!this.searching) return;
		
		if(nodes != 0){
			System.out.println("info string Move Ordering = " + (int)(((double)fhf / (double)fh) * 100D) + "%");
			System.out.println("info string Quiescence Nodes = " + (int)((double)qNodes / (double)nodes * 100D) + "%");
		}
		
		System.out.println("bestmove " + (bestMove == -1 ? "null" : Utils.shortMoveToNotation(bestMove)));
		
		if(this.parralelTimer != null && !this.parralelTimer.isInterrupted()) this.parralelTimer.interrupt();
		this.searching = false;
	}
	
	private long getTimeToUse(TimeControl tc, long time, boolean increment){
		int evalation = ((board.whiteToMove ? 1 : -1) * Evaluation.evaluate(board));
		int quiescence = quiescence(Integer.MIN_VALUE + 1, Integer.MAX_VALUE, (short)1, true, true);
		
		long timeToUse = (long)((double)time / Math.min(Math.max(increment ? 20 : 30, Math.abs(quiescence - evalation) / 6) + (10 / Math.max(1, time / 60000)), 150));
		System.out.println("info string Spending: " + ((float)timeToUse / 1000F) + " seconds... (" + Math.abs(evalation - quiescence) + ")");
		return timeToUse;
	}
	
	private boolean isMateValue(int score){
		return Math.abs(Math.abs(score) - mateValue) < 200;
	}

	private int minimax(short depth, short ply, int alpha, int beta, boolean canNull){		
		seldepth = Math.max(seldepth, ply);
		nodes ++;
		
		if(ply != 1 && Check.isInCheck(board, !board.whiteToMove)) return mateValue; //Illegal position		
		if(Evaluation.isInsufficientMaterial(board)) return contempt; //Insufficiency
		if(board.halfMoveClock >= 100) return contempt; //50 move rules
		
		int pieceCount = Utils.pieceCount(board);
		
		//Transposition table retrieval
		long hash = BoardGeneration.threeFold.get(BoardGeneration.threeFold.size() - 1);
		int index = (int)(hash & Engine.fetchMask);
		long node = Engine.tTable.get(index);
		
		//Threefold repetition (twofold)
		for(int i = (BoardGeneration.threeFold.size() - 2); i >= 0; i--){
			long h = BoardGeneration.threeFold.get(i);
			if(h == hash) return contempt;
		}
		
		//End of the main search, descend into quiescence
		if(depth <= 0){
			int q = quiescence(alpha, beta, (short)(ply + 1), false, pieceCount > 5);
			if(node == 0) recordTransposition(index, (short)-1, (short)0, q, Flag.EXACT, hash);
			return q;
		}
		
		//Check the transposition
		int hashMove = -1;
		if(node != 0 && NodeStructure.getCheckBits(node) == NodeStructure.getCheckBits(hash)){
			Flag nodeFlag = NodeStructure.getFlag(node);
			if(nodeFlag == Flag.EXACT) hashMove = NodeStructure.getBestMove(node);
			if(NodeStructure.getDepth(node) >= depth){
				int nodeValue = NodeStructure.getScore(node);
				switch(nodeFlag){
					case EXACT:
						return nodeValue;
					case LOWERBOUND:
						alpha = Math.max(alpha, nodeValue);
						break;
					case UPPERBOUND:
						beta = Math.min(beta, nodeValue);
						break;
				}
				if(alpha >= beta) return nodeValue;
			}
		}
		
		boolean check = Check.isInCheck(board, board.whiteToMove);		
		
		//Null-move pruning
		if(canNull && !check && depth > (nullMoveReduction + 1) && ply > 1 && !Evaluation.onlyKingAndPawns(board) && pieceCount > 3){ 
			board = Make.makeNullMove(board);
			int score = -minimax((short)(depth - nullMoveReduction - 1), (short)(ply + 1), -beta, -beta + 1, false);
			board = Make.undoMove(board);
			if(score >= beta) return beta;
		}
		
		iterators[ply].generateMoves(board, hashMove, ply);
		
		int alphaOriginal = alpha;
		
		short bestMove = -1;
		
		boolean legal = false;
		
		byte i = 0;
		while(true){
			short move = iterators[ply].getNextMove();
			if(move == -1) break;
			
			short R = (short)0; //Reduction value
			
//			byte enemy = Utils.getToBeCapturedPiece(board, move); 			
			byte friendly = Utils.getPieceAt(board, NewMoveStructure.getFrom(move));
			byte to = NewMoveStructure.getTo(move);
			
			if(check && depth == 1) R = -1;
			if(isPromotion(board, move)) R = -1;
			if(R < -1) R = -1;
			
			board = Make.makeMove(board, move);
			
			int score;
			if(i == 0 || (node == 0 && iterators[ply].highestObservedScore < MoveOrdering.goodCaptureScore)){
				score = -minimax((short)(depth - (1 + R)), (short)(ply + 1), -beta, -alpha, true);
			}else{
				//Null-window search
				score = -minimax((short)(depth - (1 + R)), (short)(ply + 1), -alpha - 1, -alpha, true);
				
				//Failed high
				if(alpha < score && score < beta){
					score = -minimax((short)(depth - (1 + R)), (short)(ply + 1), -beta, -score, true);
				}
			}
			
			board = Make.undoMove(board);
			
			if(score == -mateValue){
				i ++;
				continue;
			}else{
				legal = true;
			}
				
			//Update best move, improve alpha, history heuristic
			if(score > alpha){
				bestMove = move;
				alpha = score;
				MoveOrdering.history[friendly][to] += (depth * depth);
			}
			
			//Alpha-beta prune, killers
			if(alpha >= beta){
				if(ply < maximumDepth) MoveOrdering.killers[ply][MoveOrdering.getKillerToOverwrite(ply, move)] = move;
				fh ++;
				if(i == 0) fhf ++;
				break;
			}
			
			i ++;
		}
		
		//No legal moves
		if(!legal){ 
			//Checkmate or stalemate
			return check ? -mateValue + ply : contempt; 
		}
		
		//Transposition table storing
		if((node == 0 || depth >= NodeStructure.getDepth(node)) && !isMateValue(alpha)){
			recordTransposition(index, bestMove, depth, alpha, alphaOriginal, beta, hash);
		}
		
		return alpha;
	}

	private void recordTransposition(int index, short bestMove, short depth, int bestValue, Flag flag, long hash){
		Engine.tTable.set(index, NodeStructure.createNode(depth, bestMove, bestValue, flag, hash));
	}

	private void recordTransposition(int index, short bestMove, short depth, int bestValue, int alphaOriginal, int beta, long hash){
		Flag flag = (bestValue <= alphaOriginal ? Flag.UPPERBOUND : (bestValue >= beta ? Flag.LOWERBOUND : Flag.EXACT));
		recordTransposition(index, bestMove, depth, bestValue, flag, hash);
	}

	public int quiescence(int alpha, int beta, short ply, boolean checkLegality, boolean deltaPrune){
		nodes ++;
		qNodes ++;

		if(checkLegality && Check.isInCheck(board, !board.whiteToMove)) return mateValue;
		if(Evaluation.isInsufficientMaterial(board)) return contempt; //Insufficiency
		if(board.halfMoveClock >= 100) return contempt; //50 move rules

		final int eval = ((board.whiteToMove ? 1 : -1) * Evaluation.evaluate(board));
		if(eval >= beta) return beta;
		
		if(deltaPrune && eval + deltaMargin + Evaluation.score[4] <= alpha) return eval; //Delta prune
		
		alpha = Math.max(eval, alpha);

		ArrayList<Short> moves = MoveOrdering.loudOrdering(board, MoveGeneration.getAllLoudMoves(board));

		for(byte i = 0; i < moves.size(); i++){
			short move = moves.get(i);

			byte enemy = Utils.getToBeCapturedPiece(board, move);
			if(enemy != 12){
				if(deltaPrune && eval + deltaMargin + Math.abs(Evaluation.score[enemy]) <= alpha) continue; //Delta prune
			}

			board = Make.makeMove(board, move);
			int value = -quiescence(-beta, -alpha, (short)(ply + 1), false, deltaPrune);
			board = Make.undoMove(board);

			if(value >= beta) return beta;
			alpha = Math.max(value, alpha);
		}

		return alpha;
	}
	
	private static boolean isPromotion(Board b, int move){
		byte from = NewMoveStructure.getFrom(move);
		if(b.whiteToMove){
			if(from > 47 && from < 56) return 0 != (b.WP & (1L << from));
		}else{
			if(from > 7 && from < 16) return 0 != (b.BP & (1L << from));
		}
		return false;
	}
	
	private String getPv(short startMove, int depth){
		String pv = Utils.shortMoveToNotation(startMove) + " ";
		if(pvEnabled){
			Make.makeMove(board, startMove);
			byte movesToUndo = 1;
			while(true){
				long hash = BoardGeneration.threeFold.size() != 0 ? BoardGeneration.threeFold.get(BoardGeneration.threeFold.size() - 1) : Zobrist.getHash(board);
				int index = (int)(hash & Engine.fetchMask);
				long node = Engine.tTable.get(index);
				if(node == 0) break;
				
				short hashMove = NodeStructure.getBestMove(node);
				if(hashMove == -1 || hashMove == 0 || NodeStructure.getFlag(node) != Flag.EXACT){
					break;
				}else{
					if(Utils.getPieceAt(board, NewMoveStructure.getFrom(hashMove)) == 12) break;
					pv += Utils.shortMoveToNotation(hashMove) + " ";
					board = Make.makeMove(board, hashMove);
				}
				
				movesToUndo ++;
				if(movesToUndo >= depth) break;
			}
			for(byte i = 0; i < movesToUndo; i++) board = Make.undoMove(board);
		}
		return pv;
	}
	
	private int nps(long nodes, long ms){
		return (int)(nodes / Math.max(0.001F, (float)ms / 1000F));
	}
	
	public long getTimeToEnd(){
		if(tc == TimeControl.INFINITE || tc == TimeControl.DEPTH){
			return Long.MAX_VALUE;	
		}else if(tc == TimeControl.TIMETOMOVE){
			return timeStarted + time;
		}else{
			return timeStarted + getTimeToUse(tc, time, increment);
		}
	}
	
	public Search withParralelTimer(Timer timer){
		this.parralelTimer = timer;
		return this;
	}	

}
