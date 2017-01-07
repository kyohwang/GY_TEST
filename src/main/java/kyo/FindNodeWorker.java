package kyo;

import java.util.List;

public class FindNodeWorker extends Worker {
		
	public FindNodeWorker(String taskId, byte[] targetId, List<Node> list, int ownerPort) {
		super(taskId, targetId, list,ownerPort);
	}

	@Override
	public void goOn() {
		if(this.tryedTimes < this.maxTimes){
			Node node = this.getTop();
			Utils.findNode(this.targetId, taskId, node,this.ownerPort);
		}else{
			//³¬´ÎÊý£¬¸Éµô
			NodeServer.finishTask(taskId);
		}

	}

}
