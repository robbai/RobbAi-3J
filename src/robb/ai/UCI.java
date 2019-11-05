package robb.ai;

import java.util.Scanner;

public class UCI {
	
	private static final String engineName = "RobbAi 3J", engineAuthor = "Robbie";
	
    public static void main(String[] args){
    	System.out.println(Long.toBinaryString(Long.MIN_VALUE));
    	
    	System.out.println(engineName + " by " + engineAuthor);
    	System.out.println("info string Test Message: 6");
    	Utils.printBoardCoords();
    	
    	Zobrist.initZobrist();
    	MoveGeneration.initMovesBoards();
    	Engine.resetTTable(true);
    	inputPosition("position startpos");
    	
		@SuppressWarnings("resource")
		Scanner input = new Scanner(System.in);
        while(true){
            String str = input.nextLine();
            if(str.equals("uci")){
                inputUCI();
            }else if(str.startsWith("setoption")){
                inputSetOption(str);
            }else if("isready".equals(str)){
                System.out.println("readyok");
            }else if("ucinewgame".equals(str)){
                inputUCINewGame();
            }else if("print".equals(str)){
            	Utils.printBoard(Engine.board, true);
//            }else if(str.startsWith("eval")){
//            	if(Engine.board != null){
//            		int evaluation = (str.contains("static") ? Evaluation.evaluate(Engine.board) : Engine.quiescence(Integer.MIN_VALUE + 1, Integer.MAX_VALUE, (short)0, true, true));
//            		System.out.println("info string Evaluation = " + evaluation + (str.contains("static") ? "cp (static)" : "cp"));
//            	}else{
//            		System.out.println("info string Error: No Board to Evaluate");
//            	}
            }else if(str.startsWith("position")){
                inputPosition(str);
            }else if(str.startsWith("go ")){
            	inputGo(str);
            }else if(str.equals("stop")){
//            	System.out.println("Stop");
            	Engine.stopSearch();
            }else if(str.startsWith("moves")){
            	boolean captures = (str.contains("loud") || str.contains("captures"));
            	boolean legal = str.contains("legal");
            	int[] moves = new int[MoveIterator.maxMoves];
            	moves = (captures ? MoveGeneration.getAllLoudMoves(Engine.board, moves) : (legal ? MoveGeneration.getAllLegalMoves(Engine.board) : MoveGeneration.getAllMoves(Engine.board, moves)));
            	for(short i = 0; i < MoveIterator.maxMoves; i++){
            		int move = moves[i];
            		if(move == -1 || move == 0) break;
            		System.out.println((i + 1) + ": " + Utils.moveToNotation(move) + (captures ? " (" + SEE.seeCapture(Engine.board, move) + ")" : ""));
            	}
            }else if(str.equals("order")){
            	int[] moves = MoveGeneration.getAllLegalMoves(Engine.board);
            	
            	long hash = (Engine.board.threeFold.size() != 0 ? Engine.board.threeFold.get(Engine.board.threeFold.size() - 1) : Zobrist.getHash(Engine.board));
        		long node = Engine.tTable.get((int)(hash & Engine.fetchMask));
            	int[] scores = new int[MoveIterator.maxMoves];
            	MoveOrdering.getMoveScores(Engine.board, moves, (node == 0 ? -1 : NodeStructure.getBestMove(node)), (short)0, scores);
            	
            	moves = MoveOrdering.insertionSort(moves, scores);
            	for(short i = 0; i < MoveIterator.maxMoves; i++){
            		int move = moves[i];
            		if(move == 0) break;
            		System.out.println((i + 1) + ": " + Utils.moveToNotation(move) + " = " + scores[i]);
            	}
            }else if(str.startsWith("perft ")){
            	if(Engine.board != null){
            		if(str.contains("divide")){
            			int depth = Integer.parseInt(str.substring(str.indexOf(" ") + 1));
            			int[] moves = MoveGeneration.getAllLegalMoves(Engine.board);
                		for(int move : moves){
                			if(move == -1 || move == 0) break;
                			Engine.board = Make.makeMove(Engine.board, move);
                			int result = perft(depth);
                			System.out.println(Utils.moveToNotation(move) + ": " + result);
                			Engine.board = Make.undoMove(Engine.board);
                		}
            		}else{
	            		long timeStarted = System.currentTimeMillis();
	                	int depth = Integer.parseInt(str.substring(str.indexOf(" ") + 1));
	                	int result = perft(depth);
	                	System.out.print("info string Perft " + depth + ": ");
	                	System.out.printf("%,d\n", result);
	                	System.out.print("info string Time in Milliseconds: ");
	                	System.out.printf("%,d\n", (System.currentTimeMillis() - timeStarted));
            		}
            	}else{
            		System.out.println("Error: No Board to Test");
            	}
            }else if(str.equals("quit")){
            	System.out.println("info string Ending " + engineName + "...");
            	System.exit(0);
			}else if(str.equals("undo")){
            	if(Engine.board.history.size() == 0){
            		System.out.println("info string Error: No Moves to Undo");
            	}else{
            		Make.undoMove(Engine.board);
            	}
			}else if(str.equals("tt")){
				for(long l : Engine.tTable){
					if(l != 0L) System.out.println(NodeStructure.asString(l, true));
				}
			}
        }
    }
	
    private static int perft(int depth){
    	if(depth == 0) return 1;
    	
		int[] moves = MoveGeneration.getAllLegalMoves(Engine.board);
		
//		if(depth <= 1) return MoveIterator.getMoveCount(moves);
		
		int nodes = 0;
		for(int move : moves){
			if(move == -1 || move == 0) break;
			Engine.board = Make.makeMove(Engine.board, move);
			nodes += perft(depth - 1);
			Engine.board = Make.undoMove(Engine.board);
		}
		
		return nodes;
	}

	private static void inputUCI(){
        System.out.println("id name " + engineName);
        System.out.println("id author " + engineAuthor);
        System.out.println("option name Hash type spin min 0 max 128 default 0");
        System.out.println("option name MultiPV type spin min 1 max 1 default 1");
        System.out.println("option name Clear Hash type button");
        System.out.println("uciok");
    }
    
    private static void inputSetOption(String input){
//    	if(input.contains("uci_variant ")){
//    		String v = input.substring(input.indexOf("uci_variant ") + 12).trim();
//    		if(v.contains(" ")) v = v.substring(0, v.indexOf(" "));
//    		
//    		if(v.equalsIgnoreCase("koth") || v.equalsIgnoreCase("kingofthehill")){
//    			variant = Variant.KOTH;
//    			System.out.println("info string Variant: King of the Hill");
//    		}else{
//        		System.out.println("info string Unknown Variant: '" + v.toUpperCase() + "'");
//    		}
//    	}else{
    		System.out.println("info string Options Unsupported");
//    	}
    }
    
    private static void inputUCINewGame(){
    	Engine.resetTTable(true);
    	MoveOrdering.clearHistory();
		MoveOrdering.clearKillers();
    	Engine.board = new Board(0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, true, true, true, true, true, (byte)64, (byte)0);
    }
    
    private static void inputPosition(String input){
        input = input.substring(9).concat(" ");
        if(input.contains("startpos")){
            input = input.substring(9);
            Engine.board = BoardGeneration.importFEN("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
        }else if(input.contains("fen")){
            input = input.substring(4);
            Engine.board = BoardGeneration.importFEN(input);
        }
        if(input.contains("moves")){
            input = input.substring(input.indexOf("moves") + 6);
            String[] moves = input.split(" ");
            for(String m : moves){
            	Engine.board = Make.makeMove(Engine.board, Utils.notationToMove(Engine.board, m));            	
            }
        }
    }
    
    private static void inputGo(String full){
    	full = full.trim() + " ";
    	final boolean increment = full.contains(Engine.board.whiteToMove ? "winc" : "binc");
    	if(full.contains("infinite")){
    		Engine.startSearch(TimeControl.INFINITE, 0L, increment);
    	}else if(full.contains("movetime ")){
    		full = full.substring(full.indexOf("movetime ") + 9);
    		Engine.startSearch(TimeControl.TIMETOMOVE, Long.parseLong(full.substring(0, full.indexOf(" "))), increment);
    	}else if(full.contains("depth ")){
    		full = full.substring(full.indexOf("depth ") + 6);
    		Engine.startSearch(TimeControl.DEPTH, Long.parseLong(full.substring(0, full.indexOf(" "))), increment);
    	}else{
    		full = full.substring(full.indexOf(Engine.board.whiteToMove ? "wtime " : "btime ") + 6);
    		Engine.startSearch(TimeControl.SUDDENDEATH, Long.parseLong(full.substring(0, full.indexOf(" "))), increment);
    	}
    }

}
