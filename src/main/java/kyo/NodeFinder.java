package kyo;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import kyo.net.UdpSender;

import org.apache.log4j.Logger;

public class NodeFinder implements Runnable {
	private static Logger  log = Logger.getLogger(NodeFinder.class);
		
	private Set<Node> askedNodes = new HashSet<Node>();
	
	private long lastClear = System.currentTimeMillis();

	
	@Override
	public void run() {
		
		while(true){
			for(UdpSender udp : NodeServer.updSenders.values()){
				try{				
					Thread.sleep(5*1000);
					if(System.currentTimeMillis() - this.lastClear > 10*60*1000){
						this.lastClear = System.currentTimeMillis();
						this.askedNodes.clear();
					}
					 for(List<Node> list : udp.bucket.getTree().values()){
						 for(Node node : list){
							 if(!this.askedNodes.contains(node)){
								 this.askedNodes.add(node);
								 Utils.findNode(udp.LOCAL_ID, Utils.getNextTaskId(), node,udp.LOCAL_PORT);
							 }
						 }
					 }
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		}
	}

}
