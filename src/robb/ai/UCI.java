package robb.ai;

import java.util.ArrayList;
import java.util.Scanner;

public class UCI {
	
	private static final String engineName = "RobbAi 3J", engineAuthor = "Robbie";
	public static Variant variant;
	
    public static void main(String[] args){
    	System.out.println(engineName + " by " + engineAuthor);
    	System.out.println("info string Test Message: 6");
    	Utils.printBoardCoords();
    	
    	Zobrist.initZobrist();
    	MoveGeneration.initMovesBoards();
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
            }else if(str.startsWith("eval")){
            	if(Engine.board != null){
            		final int e = (str.contains("static") ? Evaluation.evaluate(Engine.board) : Engine.quiescence(Integer.MIN_VALUE + 1, Integer.MAX_VALUE, (short)0, true, true));
            		System.out.println("info string Evaluation = " + e + (str.contains("static") ? "cp (static)" : "cp"));
            	}else{
            		System.out.println("info string Error: No Board to Evaluate");
            	}
            }else if(str.startsWith("position")){
                inputPosition(str);
            }else if(str.startsWith("go ")){
            	inputGo(str);
            }else if(str.equals("stop")){
            	Engine.stopSearch();
            }else if(str.startsWith("moves")){
            	boolean captures = str.contains("loud");
            	boolean legal = str.contains("legal");
            	ArrayList<Short> moves = (captures ? MoveGeneration.getAllLoudMoves(Engine.board) : (legal ? MoveGeneration.getAllLegalMoves(Engine.board) : MoveGeneration.getAllMoves(Engine.board)));
            	for(int i = 0; i < moves.size(); i++) System.out.println((i + 1) + ": " + Utils.shortMoveToNotation(moves.get(i)) + (captures ? " (" + SEE.seeCapture(Engine.board, moves.get(i)) + ")" : ""));
            }else if(str.equals("order")){
            	ArrayList<Short> moves = MoveGeneration.getAllLegalMoves(Engine.board);
            	
            	long hash = BoardGeneration.threeFold.size() != 0 ? BoardGeneration.threeFold.get(BoardGeneration.threeFold.size() - 1) : Zobrist.getHash(Engine.board);
        		long node = Engine.tTable.get((int)(hash & Engine.fetchMask));
            	ArrayList<Integer> scores = MoveOrdering.getMoveScores(Engine.board, moves, (node == 0 ? -1 : NodeStructure.getBestMove(node)), (short)0);
            	
            	moves = MoveOrdering.insertionSort(moves, scores);
            	for(int i = 0; i < moves.size(); i++) System.out.println((i + 1) + ": " + Utils.shortMoveToNotation(moves.get(i)) + " = " + scores.get(i));
            }else if(str.equals("debug")){
                Engine.debug = !Engine.debug;
//                Make.debug = Engine.debug;
                System.out.println("info string Debug Mode is now " + (Engine.debug ? "enabled" : "disabled"));
//              System.out.println("info string Threefold Size = " + BoardGeneration.threeFold.size());
            }else if(str.startsWith("perft ")){
            	if(Engine.board != null){
            		final long timeStarted = System.currentTimeMillis();
                	int depth = Integer.parseInt(str.substring(str.indexOf(" ") + 1));
                	int result = perft(depth);
                	System.out.print("info string Perft " + depth + ": ");
                	System.out.printf("%,d\n", result);
                	System.out.print("info string Time in Milliseconds: ");
                	System.out.printf("%,d\n", (System.currentTimeMillis() - timeStarted));
//                	System.out.println();
            	}else{
            		System.out.println("Error: No Board to Test");
            	}
            }else if(str.equals("quit")){
            	System.out.println("info string Ending " + engineName + "...");
            	System.exit(0);
			}else if(str.equals("undo")){
            	if(BoardGeneration.history.size() == 0){
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
		final ArrayList<Short> moves = MoveGeneration.getAllLegalMoves(Engine.board);
		if(depth <= 1) return moves.size();
		
		int nodes = 0;
		for(short m : moves){
			Make.makeMove(Engine.board, m);
			nodes += perft(depth - 1);
			Make.undoMove(Engine.board);
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
    	if(input.contains("uci_variant ")){
    		String v = input.substring(input.indexOf("uci_variant ") + 12).trim();
    		if(v.contains(" ")) v = v.substring(0, v.indexOf(" "));
    		
    		if(v.equalsIgnoreCase("koth") || v.equalsIgnoreCase("kingofthehill")){
    			variant = Variant.KOTH;
    			System.out.println("info string Variant: King of the Hill");
    		}else{
        		System.out.println("info string Unknown Variant: '" + v.toUpperCase() + "'");
    		}
    	}else{
    		System.out.println("info string Options Unsupported");
    	}
    }
    
    private static void inputUCINewGame(){
    	Engine.resetTTable(true);
    	MoveOrdering.clearHistory();
		MoveOrdering.clearKillers();
    	Engine.board = new Board(0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, true, true, true, true, true, (byte)64, (byte)0);
    	BoardGeneration.threeFold.clear();
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
            	Engine.board = Make.makeMove(Engine.board, Utils.notationMoveToShort(Engine.board, m), true);            	
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
