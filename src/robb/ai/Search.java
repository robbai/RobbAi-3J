package robb.ai;

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
	private static final int contempt = 0, nullMoveReduction = 3;
	public static final int deltaMargin = 150;

	/**
	 * Constants
	 */
	public static final short minimumDepth = 2, maximumDepth = 100;
	public static final int mateValue = 200000;

	/**
	 * Search details and results
	 */
	public boolean searching /*isInterrupted() sux*/, completedMinimumDepth /*For sudden-death*/;
	private int fh = 0, fhf = 0, seldepth = 0;
	private long nodes = 0L, qNodes = 0L, timeStarted;
	public int bestMove;

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

		// Set the time controls
		this.tc = tc;
		this.time = time;
		this.increment = increment;

		// Info regarding the search
		this.bestMove = -1;
		this.timeStarted = System.currentTimeMillis();
		this.nodes = 0L;
		this.qNodes = 0L;
		this.completedMinimumDepth = (tc != TimeControl.SUDDENDEATH);
		this.searching = true;

		// Info regarding move-ordering efficiency.
		this.fh = 0;
		this.fhf = 0;

		// Move ordering.
		for(int i = 0; i < maximumDepth; i++) this.iterators[i] = new MoveIterator();

		Engine.reset();
	}

	@Override
	public void run(){
		int[] moves = MoveGeneration.getAllLegalMoves(board);

		// Initial ordering.
		int[] initialScores = new int[moves.length];
		long hash = (board.threeFold.size() != 0 ? board.threeFold.get(board.threeFold.size() - 1) : Zobrist.getHash(board));
		int index = (int)(hash & Engine.fetchMask);
		long node = Engine.tTable.get(index);
		if(node != 0 && NodeStructure.getCheckBits(node) == NodeStructure.getCheckBits(hash)){
			short hashMove = NodeStructure.getBestMove(node);
			initialScores = MoveOrdering.getMoveScores(board, moves, hashMove, -1, initialScores);
		}else{
			initialScores = MoveOrdering.getMoveScores(board, moves, 0, -1, initialScores);
		}
		MoveOrdering.insertionSort(moves, initialScores);

		bestMove = moves[0];

		int depth = 1;
		while(moves[1] != 0 && depth <= maximumDepth && (tc != TimeControl.DEPTH || depth <= time /* Time representing the depth to search */)){
			int bestThisDepth = -1;
			int bestScore = (Integer.MIN_VALUE + 1);

			int[] scores = new int[moves.length];

			seldepth = 0;

			//			System.out.println(alpha + ", " + beta);

			for(int i = 0; i < MoveIterator.maxMoves; i++){
				int move = moves[i];
				if(move == 0) break;

				board = Make.makeMove(board, move);
				int score = -minimax(depth - 1, 1, Integer.MIN_VALUE + 1, -bestScore, false);
				scores[i] = score;
				board = Make.undoMove(board);

				if(!this.searching) return;

				if(bestThisDepth == -1 || score > bestScore){
					bestThisDepth = i;
					bestScore = score;
				}		
			}

			boolean mate = isMateValue(bestScore);				
			if(bestThisDepth != -1){
				System.out.println("info currmove " + Utils.shortMoveToNotation(moves[bestThisDepth]) + " score " + (mate ? "mate " + (bestScore < 0 ? "-" : "") + Utils.pliesToMoves(1 + Math.abs(Math.abs(bestScore) - mateValue)) : "cp " + bestScore) + " depth " + depth + " seldepth " + seldepth + " nodes " + nodes + " time " + (System.currentTimeMillis() - timeStarted) + " nps " + nps(nodes, System.currentTimeMillis() - timeStarted) + " pv " + getPv(moves[bestThisDepth], depth));
			}

			// Set the best move.
			bestMove = moves[bestThisDepth];

			// If mate is found, terminate the search early and return the best move.
			if(mate) break;

			MoveOrdering.insertionSort(moves, scores);

			completedMinimumDepth = (depth >= minimumDepth);
			depth ++;
		}

		// If we end the search early (one move, forced mate, etc.)
		completedMinimumDepth = true;
		this.interrupt(); 
	}

	/**
	 * Ends the search.
	 */
	public void interrupt(){
		if(!this.searching) return;
		this.searching = false;

		if(nodes != 0){
			System.out.println("info string Move Ordering = " + (int)(((double)fhf / (double)fh) * 100D) + "%");
			System.out.println("info string Quiescence Nodes = " + (int)((double)qNodes / (double)nodes * 100D) + "%");
		}

		System.out.println("bestmove " + (bestMove == 0 ? "null" : Utils.shortMoveToNotation(bestMove)));

		//		Engine.reset();

		if(this.parralelTimer != null && !this.parralelTimer.isInterrupted()) this.parralelTimer.interrupt();		
	}

	/**
	 * Calculates how much time (in milliseconds) should be spent thinking for a move
	 */
	private long getTimeToUse(TimeControl tc, long time, boolean increment){
		int evaluation = ((board.whiteToMove ? 1 : -1) * Evaluation.evaluate(board));
		int quiescence = quiescence(Integer.MIN_VALUE + 1, Integer.MAX_VALUE, (short)1, true, true);

		long timeToUse = (long)((double)time / Math.min(Math.max(increment ? 20 : 30, Math.abs(quiescence - evaluation) / 6) + (10 / Math.max(1, time / 60000)), 150));

		System.out.println("info string Spending: " + ((float)timeToUse / 1000) + " seconds... (" + Math.abs(evaluation - quiescence) + ")");

		return timeToUse;
	}

	private boolean isMateValue(int score){
		return Math.abs(Math.abs(score) - mateValue) < 200;
	}

	private int minimax(int depth, int ply, int alpha, int beta, boolean canNull){		
		seldepth = Math.max(seldepth, ply);
		nodes ++;

		if(ply != 1 && Check.isInCheck(board, !board.whiteToMove)) return mateValue; // Illegal position.		
		if(Evaluation.isInsufficientMaterial(board)) return contempt; // Insufficient material.
		if(board.halfMoveClock >= 100) return contempt; // 50 move rule.

		int pieceCount = Utils.pieceCount(board);

		// Transposition table retrieval.
		long hash = board.threeFold.get(board.threeFold.size() - 1);
		int index = (int)(hash & Engine.fetchMask);
		long node = Engine.tTable.get(index);

		// Threefold repetition (twofold).
		//		System.out.println(board.halfMoveClock);
		for(int i = Math.max(0, board.threeFold.size() - 1 - board.halfMoveClock); i < (board.threeFold.size() - 1); i++){
			long h = board.threeFold.get(i);
			if(h == hash) return contempt;
		}

		// Check the transposition.
		int hashMove = 0;
		if(node != 0 && NodeStructure.getCheckBits(node) == NodeStructure.getCheckBits(hash)){
			byte nodeDepth = NodeStructure.getDepth(node);
			if(nodeDepth >= depth - 1){
				hashMove = NodeStructure.getBestMove(node);
				if(nodeDepth >= depth){
					Flag nodeFlag = NodeStructure.getFlag(node);
					int nodeScore = NodeStructure.getScore(node);
					switch(nodeFlag){
						case EXACT:
							return nodeScore;
						case LOWERBOUND:
							alpha = Math.max(alpha, nodeScore);
							break;
						case UPPERBOUND:
							beta = Math.min(beta, nodeScore);
							break;
					}
					if(alpha >= beta) return nodeScore;
				}
			}
		}

		/* End of the main search,
		 * descend into quiescence.
		 */
		if(depth <= 0){
			int q = quiescence(alpha, beta, ply + 1, false, pieceCount > 5);
			return q;
		}

		boolean check = Check.isInCheck(board, board.whiteToMove);		

		//		// Null-move pruning.
		if(canNull && !check && depth != 1 && ply > 2 && pieceCount > 3){ 
			board = Make.makeNullMove(board);
			int score = -minimax(depth - nullMoveReduction - 1, ply + 1, -beta, -beta + 1, false);
			board = Make.undoMove(board);
			if(score >= beta) return beta;
		}

		int alphaOriginal = alpha;

		boolean legal = false;

		MoveIterator moves = iterators[ply];
		moves.generateMoves(board, hashMove, ply, false);
		int i = 0;

		int bestScore = (Integer.MIN_VALUE + 1);
		int bestMove = 0;

		while(true){
			int move = moves.getNextMove();
			if(move == 0) break;

			board = Make.makeMove(board, move);

			int R = (depth > 2 || !check ? 1 : 0);
			//			int R = 1;

			int score;
			if(i == 0){
				// 			if(i == 0 || (node == 0 && iterators[ply].highestObservedScore < MoveOrdering.goodCaptureScore)){
				//			if(i == 0 || node == 0){
				//			if(alpha == alphaOriginal){
				score = -minimax(depth - R, ply + 1, -beta, -alpha, true);
			}else{
				// Null-window search.
				score = -minimax(depth - R, ply + 1, -alpha - 1, -alpha, true);

				// Failed high.
				if(alpha < score && score < beta){
					score = -minimax(depth - R, ply + 1, -beta, -score, true);
				}
			}

			board = Make.undoMove(board);

			if(score == -mateValue){
				i ++;
				continue;
			}else{
				if(isMateValue(score)) score -= Math.signum(score);
				legal = true;
			}

			// Update best move
			if(score > bestScore){
				bestScore = score;
				bestMove = move;

				// Improve alpha, history heuristic.
				if(score > alpha){
					alpha = score;

					byte enemy = Utils.getToBeCapturedPiece(board, move);
					byte friendly = Utils.getPieceAt(board, NewMoveStructure.getFrom(move));
					byte to = NewMoveStructure.getTo(move);

					if(enemy == 12) MoveOrdering.history[friendly][to] += depth;	

					// Alpha-beta prune, killers.
					if(score >= beta){
						if(enemy == 12 && ply < maximumDepth){
							MoveOrdering.saveKiller(ply, move);
						}

						fh ++;
						if(i == 0) fhf ++;

						break;
					}
				}
			}

			i ++;
		}

		// No legal moves.
		if(!legal){ 
			// Checkmate or stalemate.
			return check ? 1 - mateValue : contempt; 
		}

		// Transposition table storing
		if(bestMove != 0){
			if(node == 0 || depth >= NodeStructure.getDepth(node)){
				recordTransposition(index, bestMove, depth, bestScore, alphaOriginal, beta, hash);
			}
		}

		return bestScore;
	}

	private void recordTransposition(int index, int move, int depth, int score, Flag flag, long hash){
		Engine.tTable.set(index, NodeStructure.createNode(depth, move, score, flag, hash));
	}

	private void recordTransposition(int index, int move, int depth, int score, int alphaOriginal, int beta, long hash){
		Flag flag = (score <= alphaOriginal ? Flag.UPPERBOUND : (score >= beta ? Flag.LOWERBOUND : Flag.EXACT));
		recordTransposition(index, move, depth, score, flag, hash);
	}

	/**
	 * Quiescence search
	 */
	public int quiescence(int alpha, int beta, int ply, boolean checkLegality, boolean deltaPrune){
		nodes ++;
		qNodes ++;

		if(checkLegality && Check.isInCheck(board, !board.whiteToMove)) return mateValue;

		if(ply + 1 >= maximumDepth){
			return ((board.whiteToMove ? 1 : -1) * Evaluation.evaluate(board));
		}

		// Insufficient material or 50 move rule.
		if(Evaluation.isInsufficientMaterial(board) || board.halfMoveClock >= 100){
			return contempt; 
		}

		int bestScore = (Integer.MIN_VALUE + 1);
		//		int alphaOriginal = alpha;
		boolean check = Check.isInCheck(board, board.whiteToMove);

		if(!check){
			bestScore = ((board.whiteToMove ? 1 : -1) * Evaluation.evaluate(board));

			// Delta prune.
			if(deltaPrune && bestScore + deltaMargin + Evaluation.score[4] <= alpha) return bestScore;

			if(bestScore > alpha){
				alpha = bestScore;
				if(bestScore >= beta) return bestScore;
			}
		}

		MoveIterator moves = iterators[ply];
		moves.generateMoves(board, 0, ply, !check);
		boolean legal = !check;
		int i = 0;

		while(true){
			int move = moves.getNextMove();
			if(move == 0) break;

			// Pruning.
			if(!check && i != 0){
				byte enemy = Utils.getToBeCapturedPiece(board, move);

				if(enemy != 12){
					// Delta prune.
					if(deltaPrune && bestScore + deltaMargin + Math.abs(Evaluation.score[enemy]) <= alpha) continue;

					// SEE prune.
					//					int see = SEE.seeCapture(board, move);
					int see = moves.getLastScore();
					if(see < 0) continue;
				}
			}

			board = Make.makeMove(board, move);
			int score = -quiescence(-beta, -alpha, ply + 1, true, deltaPrune);
			if(score != -mateValue){
				if(isMateValue(score)) score -= Math.signum(score);
				legal = true;
			}
			board = Make.undoMove(board);

			if(score > bestScore){
				bestScore = score;
				if(score > alpha){
					alpha = score;

					if(score >= beta){
						fh ++;
						if(i == 0) fhf ++;
						break;
					}
				}
			}

			i++;
		}

		if(!legal) return 1 - mateValue;

		return bestScore;
	}

	//	private static boolean isPromotion(Board b, int move){
	// 		byte from = NewMoveStructure.getFrom(move);
	// 		if(b.whiteToMove){
	// 			if(from > 47 && from < 56) return 0 != (b.WP & (1L << from));
	// 		}else{
	// 			if(from > 7 && from < 16) return 0 != (b.BP & (1L << from));
	// 		}
	// 		return false;
	// 	}

	private String getPv(int move, int depth){
		String pv = Utils.shortMoveToNotation(move) + " ";
		if(pvEnabled){
			board = Make.makeMove(board, move);
			byte movesToUndo = 1;
			while(true){
				long hash = (board.threeFold.size() != 0 ? board.threeFold.get(board.threeFold.size() - 1) : Zobrist.getHash(board));
				int index = (int)(hash & Engine.fetchMask);
				long node = Engine.tTable.get(index);

				if(node == 0) break;

				short hashMove = NodeStructure.getBestMove(node);
				if(hashMove == 0){
					break;
				}else{
					//					if(Utils.getPieceAt(board, NewMoveStructure.getFrom(hashMove)) == 12) break;

					int[] legalMoves = MoveGeneration.getAllLegalMoves(board);
					boolean found = false;
					for(int legalMove : legalMoves){
						if(legalMove == 0) break;
						if(legalMove == hashMove){
							found = true;
							break;
						}
					}

					if(found){
						pv += Utils.shortMoveToNotation(hashMove) + " ";
						board = Make.makeMove(board, hashMove);
					}else{
						break;
					}
				}

				movesToUndo ++;
				if(movesToUndo >= depth) break;
			}
			for(int i = 0; i < movesToUndo; i++) board = Make.undoMove(board);
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
