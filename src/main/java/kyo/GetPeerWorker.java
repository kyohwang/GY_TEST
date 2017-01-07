package kyo;

import java.util.List;

public class GetPeerWorker extends Worker {

	public GetPeerWorker(String taskId, byte[] targetId, List<Node> list,int ownerPort) {
		super(taskId, targetId, list, ownerPort);
	}

	@Override
	public void goOn() {
		if(this.tryedTimes < this.maxTimes){
			Node node = this.getTop();
			if(node != null){
				Utils.get_peer(this.targetId, taskId, node,this.ownerPort);
			}
		}else{
			//���������ɵ�
			NodeServer.finishTask(taskId);
		}

	}

}
