package kyo;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

public class NodeFinder implements Runnable {
	private static Logger  log = Logger.getLogger(NodeFinder.class);
	
	private Bucket bucket;
	
	private Set<Node> askedNodes = new HashSet<Node>();
	
	private long lastClear = System.currentTimeMillis();
	
	public NodeFinder(Bucket bucket){
		this.bucket = bucket;
	}
	
	@Override
	public void run() {
		
		while(true){
			try{
				Thread.sleep(5*1000);
				if(System.currentTimeMillis() - this.lastClear > 1*20*1000){
					this.lastClear = System.currentTimeMillis();
					this.askedNodes.clear();
				}
				 for(List<Node> list : bucket.getTree().values()){
					 for(Node node : list){
						 if(!this.askedNodes.contains(node)){
							 this.askedNodes.add(node);
							 Utils.findNode(NodeServer.getLOCAL_ID(), Utils.getNextTaskId(), node);
						 }
					 }
				 }
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}

}
