package robb.ai;

import java.util.ArrayList;

public class Engine {
	
	public static final byte minimumDepth = 2;
	public static final short maxDepth = 25;
	public static final int mateValue = 200000;
	public static final int deltaMargin = 160;
	private static final int contempt = 0;
	private static final int exponentSize = 24;
	private static final int tTableSize = (int)Math.pow(2, exponentSize);
	private static final int nullMoveReduction = 2;	
	public static final long fetchMask = (1L << exponentSize) - 1;
	private static final boolean pvEnabled = true;
	
	public static boolean debug, completedMinimumDepth = false, searching = false;		
	private static int fh = 0, fhf = 0, historyBefore, seldepth;
	private static long nodes = 0L, qNodes = 0L, timeStarted, timeToEnd;
	private static short bestMove;

	public static Board board;
	private static MoveIterator[] iterators = new MoveIterator[maxDepth];
	public static ArrayList<Long> tTable = new ArrayList<Long>(tTableSize);
	private static Thread search, timer; 
	
	public static void startSearch(final TimeControl tc, final long time, final boolean increment){
		bestMove = -1;
		historyBefore = BoardGeneration.history.size();
		timeStarted = System.currentTimeMillis();
		nodes = 0L;
		qNodes = 0L;
		
		fh = 0;
		fhf = 0;
		
//		MoveOrdering.clearHistory();
//		MoveOrdering.clearKillers();
		resetTTable(false);
		for(int i = 0; i < maxDepth; i++) iterators[i] = new MoveIterator();
		
		completedMinimumDepth = (tc != TimeControl.SUDDENDEATH);
		
		if(tc == TimeControl.INFINITE || tc == TimeControl.DEPTH){
			timeToEnd = Long.MAX_VALUE;	
		}else if(tc == TimeControl.TIMETOMOVE){
			timeToEnd = timeStarted + time;
		}else{
			timeToEnd = timeStarted + getTimeToUse(tc, time, increment);
		}
		
		search = new Thread("Search"){
			public void run(){
				search(tc, time);
			}
		};
		timer = new Thread("Timer"){
			public void run(){
				timer(tc, time, increment);
			}
		};
		
		search.start();
    	timer.start();
	}
	
	public static void resetTTable(boolean clear){
		if(tTable.size() == tTableSize && !clear) return;
		tTable.clear();
		for(int i = 0; i < tTableSize; i++) tTable.add(0L);
	}

	private static void timer(final TimeControl tc, final long time, final boolean increment){
		while(true){
			if(tc == TimeControl.INFINITE || tc == TimeControl.DEPTH) continue;
			if(System.currentTimeMillis() > timeToEnd) break;
		}
		stopSearch();
	}

	@SuppressWarnings("deprecation")
	public static void stopSearch(){
		if(!searching) return;
		searching = false;
		if(nodes != 0){
			System.out.println("info string Move Ordering = " + (int)(((double)fhf / (double)fh) * 100D) + "%");
			System.out.println("info string Quiescence Nodes = " + (int)((double)qNodes / (double)nodes * 100D) + "%");
		}
		System.out.println("bestmove " + (bestMove == -1 ? "null" : Utils.shortMoveToNotation(bestMove)));
		
		search.stop();
		timer.stop();

		//Undo moves
		new java.util.Timer().schedule( 
		        new java.util.TimerTask(){
		            @Override
		            public void run(){
		            	while(historyBefore < BoardGeneration.history.size()){
		        			board = Make.undoMove(board);
		        		}
		            }
		        }, 
		        4 
		);
		
	}
	
	private static void search(final TimeControl tc, final long time){
		if(searching) return;
		searching = true;
		final ArrayList<Short> moves = MoveGeneration.getAllLegalMoves(board);
		bestMove = moves.get(0);
		
		int depth = 1;
		while(moves.size() > 1 && depth <= maxDepth && (tc != TimeControl.DEPTH || depth <= time)){		
			byte bestThisDepth = -1;
			int bestScore = 0;
			seldepth = 0;
			
			for(byte i = 0; i < moves.size(); i++){
				board = Make.makeMove(board, moves.get(i));
				int score = -minimax((short)(depth - 1), (short)1, Integer.MIN_VALUE + 1, Integer.MAX_VALUE, false);
				board = Make.undoMove(board);
				if(bestThisDepth == -1 || score > bestScore){
					bestThisDepth = i;
					bestScore = score;
				}
				
				//Debug to print all moves
				if(debug){
					final boolean mate = isMateValue(score);
					System.out.println("info currmove " + Utils.shortMoveToNotation(moves.get(i)) + " score " + (mate ? "mate " + (score < 0 ? "-" : "") + Utils.pliesToMoves(1 + Math.abs(Math.abs(score) - mateValue)) : "cp " + score) + " depth " + depth + " seldepth " + seldepth + " nodes " + nodes + " time " + (System.currentTimeMillis() - timeStarted) + " nps " + nps(nodes, System.currentTimeMillis() - timeStarted) + " pv " + getPv(moves.get(i), depth));
				}
				
			}
			if(bestThisDepth == -1) break;	
			bestMove = moves.get(bestThisDepth);
			
			//PV line
			if(!debug){
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
		stopSearch(); 
	}
	
	private static boolean isMateValue(int score){
		return Math.abs(Math.abs(score) - mateValue) < 200;
	}

	private static int minimax(short depth, short ply, int alpha, int beta, boolean canNull){		
		seldepth = Math.max(seldepth, ply);
		nodes ++;
		
		if(ply != 1 && Check.isInCheck(Engine.board, !Engine.board.whiteToMove)) return mateValue; //Illegal position		
		if(Evaluation.isInsufficientMaterial(board)) return contempt; //Insufficiency
		if(board.halfMoveClock >= 100) return contempt; //50 move rules
		
		int pieceCount = Utils.pieceCount(board);
		
		//Transposition table retrieval
		long hash = BoardGeneration.threeFold.get(BoardGeneration.threeFold.size() - 1);
		int index = (int)(hash & fetchMask);
		long node = tTable.get(index);
		
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
				if(ply < maxDepth) MoveOrdering.killers[ply][MoveOrdering.getKillerToOverwrite(ply, move)] = move;
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

	private static void recordTransposition(int index, short bestMove, short depth, int bestValue, Flag flag, long hash){
		tTable.set(index, NodeStructure.createNode(depth, bestMove, bestValue, flag, hash));
	}

	private static void recordTransposition(int index, short bestMove, short depth, int bestValue, int alphaOriginal, int beta, long hash){
		Flag flag = (bestValue <= alphaOriginal ? Flag.UPPERBOUND : (bestValue >= beta ? Flag.LOWERBOUND : Flag.EXACT));
		recordTransposition(index, bestMove, depth, bestValue, flag, hash);
	}

	public static int quiescence(int alpha, int beta, short ply, boolean checkLegality, boolean deltaPrune){
		nodes ++;
		qNodes ++;

		if(checkLegality && Check.isInCheck(Engine.board, !Engine.board.whiteToMove)) return mateValue;
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
		final byte from = NewMoveStructure.getFrom(move);
		if(b.whiteToMove){
			if(from > 47 && from < 56) return 0 != (b.WP & (1L << from));
		}else{
			if(from > 7 && from < 16) return 0 != (b.BP & (1L << from));
		}
		return false;
	}
	
	private static String getPv(short startMove, int depth){
		String pv = Utils.shortMoveToNotation(startMove) + " ";
		if(pvEnabled){
			Make.makeMove(board, startMove);
			byte movesToUndo = 1;
			while(true){
				long hash = BoardGeneration.threeFold.size() != 0 ? BoardGeneration.threeFold.get(BoardGeneration.threeFold.size() - 1) : Zobrist.getHash(board);
				int index = (int)(hash & fetchMask);
				long node = tTable.get(index);
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
	
	private static long getTimeToUse(TimeControl tc, long time, boolean increment){
		final int e = ((board.whiteToMove ? 1 : -1) * Evaluation.evaluate(board));
		final int q = quiescence(Integer.MIN_VALUE + 1, Integer.MAX_VALUE, (short)1, true, true);		
		long timeToUse = (long)((double)time / Math.min(Math.max(increment ? 20 : 30, Math.abs(q - e) / 6) + (10 / Math.max(1, time / 60000)), 150));
		System.out.println("info string Spending: " + ((float)timeToUse / 1000F) + " seconds... (" + Math.abs(e - q) + ")");
		return timeToUse;
	}
	
	private static int nps(long nodes, long ms){
		return (int)(nodes / Math.max(0.001F, (float)ms / 1000F));
	}

}

