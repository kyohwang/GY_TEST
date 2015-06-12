package kyo;

import java.util.List;

public class FindNodeWorker extends Worker {

	public FindNodeWorker(String taskId, byte[] targetId, List<Node> list) {
		super(taskId, targetId, list);
	}

	@Override
	public void goOn() {
		if(this.tryedTimes < this.maxTimes){
			Node node = this.getTop();
			Utils.findNode(this.targetId, taskId, node);
		}else{
			//³¬´ÎÊý£¬¸Éµô
			NodeServer.finishTask(taskId);
		}

	}

}
