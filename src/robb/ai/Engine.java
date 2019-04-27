package robb.ai;

import java.util.ArrayList;

public class Engine {
	
	/**
	 * Transposition table
	 */
	public static final int exponentSize = 24;
	public static final int tTableSize = (int)Math.pow(2, exponentSize);		
	public static final long fetchMask = (1L << exponentSize) - 1;
	public static ArrayList<Long> tTable = new ArrayList<Long>(tTableSize);
	
	public static Board board;
	
	public static Search search;
	public static Timer timer;
	
	public static boolean debug = false; 
	
	public static void startSearch(TimeControl timeControl, long time, boolean increment){
		search = new Search(board, timeControl, time, increment);
		timer = new Timer(search, timeControl, search.getTimeToEnd());
		
		search.withParralelTimer(timer);
		
		timer.start();
		search.start();
	}
	
	public static void resetTTable(boolean clear){
		if(tTable.size() == tTableSize && !clear) return;
		tTable.clear();
		for(int i = 0; i < tTableSize; i++) tTable.add(0L);
	}

	public static void stopSearch(){
		timer.forceEnd();
	}

}

