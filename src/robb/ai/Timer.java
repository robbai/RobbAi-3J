package robb.ai;

public class Timer extends Thread implements Runnable {
	
	private Search search;
	
	private TimeControl timeControl;
	private long timeToEnd;
	
	private boolean forceEnd;

	public Timer(Search search, TimeControl timeControl, long timeToEnd){
		this.search = search;
		this.timeControl = timeControl;
		this.timeToEnd = timeToEnd;
		this.forceEnd = false;
	}

	@Override
	public void run(){
		//Wait until the time is up
		while(!this.isInterrupted()){
			if(this.isOver()) break;
		}
		
		//End the search
		search.interrupt();
	}

	private boolean isOver(){
		if(this.forceEnd) return true;
		if(timeControl == TimeControl.INFINITE || timeControl == TimeControl.DEPTH) return false;
		return search.completedMinimumDepth && System.currentTimeMillis() > timeToEnd && search.bestMove != -1;
	}
	
	public void forceEnd(){
		this.forceEnd = true;
	}

}
