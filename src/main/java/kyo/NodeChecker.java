package kyo;

import java.util.List;

import kyo.net.UdpSender;

import org.apache.log4j.Logger;

public class NodeChecker implements Runnable {
	
	private static Logger  log = Logger.getLogger(NodeChecker.class);
	
	@Override
	public void run() {
		
		while(true){
			try {
				Thread.sleep(1*60*1000);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			for(UdpSender udp : NodeServer.updSenders.values()){
				try{
					if(udp.bucket.getNodeSize() == 0){
						NodeServer.startFromFiles(udp.LOCAL_PORT);
						
					}
					log.info("Node size : " + udp.bucket.getNodeSize());
					 for(List<Node> list : udp.bucket.getTree().values()){
						 for(Node node : list){
							 if(node.isOverdue()){
								 udp.bucket.removeNode(node);
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
