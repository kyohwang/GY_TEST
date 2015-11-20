package kyo;

import java.util.List;

import org.apache.log4j.Logger;

public class NodeChecker implements Runnable {
	private static Logger  log = Logger.getLogger(NodeChecker.class);
	
	private Bucket bucket;
	
	public NodeChecker(Bucket bucket){
		this.bucket = bucket;
	}
	
	@Override
	public void run() {
		
		while(true){
			try{
				if(this.bucket.getNodeSize() == 0){
					NodeServer.startFromFiles();
				}
				Thread.sleep(1*60*1000);
				log.info("Node size : " + this.bucket.getNodeSize());
				 for(List<Node> list : bucket.getTree().values()){
					 for(Node node : list){
						 if(node.isOverdue()){
							 bucket.removeNode(node);
						 }
					 }
				 }
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}

}
