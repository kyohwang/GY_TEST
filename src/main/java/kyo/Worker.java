package kyo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public abstract class Worker {
	
	protected List<Node> list = new ArrayList<Node>();
	protected byte[] targetId;
	protected int tryedTimes;
	protected int maxTimes = 100;
	protected String taskId;
	protected long startTime;
	
	Comparator<Node> comp = new Comparator<Node>(){

		@Override
		public int compare(Node n1, Node n2) {
			
			int[] id1 = xor(n1.getId(), targetId);
			int[] id2 = xor(n2.getId(), targetId);
			
			for(int  i = 0; i < n1.getId().length; i++){
				if(id1[i] < id2[i]){
					return -1;
				}else if(id1[i] == id2[i]){
					
				}else{
					return 1;
				}
				
			}
			
			return 0;
		}
		
	};
	
	public Worker(String taskId, byte[]  targetId, List<Node> list){
		this.targetId = targetId;
		this.list.addAll(list);
		this.taskId = taskId;
		this.startTime = System.currentTimeMillis();
	}
	
	public String getTaskId() {
		return taskId;
	}

	public void setTaskId(String taskId) {
		this.taskId = taskId;
	}

	public synchronized void add(Node node){
		
		list.add(node);
		
		Collections.sort(list,comp);
		if(list.size() > 10){
			list.remove(list.size()-1);
		}
		
		/*System.out.println("*******");
		for(Node n : list){
			System.out.println(HexBin.bytesToString(n.getId()));
		}*/

//		System.out.println("Add. Left:" + list.size());
		
	}
	
	public synchronized Node getTop(){
		if(list.size() == 0){
			return null;
		}
//		Collections.sort(list, comp);
		Node node = list.get(0);
		list.remove(0);
//		System.out.println("Remove. Left:" + list.size());
		
		return node;
	}
	
	public static int[] xor(byte[] id1, byte[] id2){
		int[] id0 = new int[id1.length];
		for(int i = 0; i < id1.length; i++){
			id0[i] = (id1[i] ^ id2[i]);
		}
		return id0;
	}

	public byte[] getTargetId() {
		return targetId;
	}

	public int getTryedTimes() {
		return tryedTimes;
	}

	public int getMaxTimes() {
		return maxTimes;
	}

	public void setTargetId(byte[] targetId) {
		this.targetId = targetId;
	}

	public void setTryedTimes(int tryedTimes) {
		this.tryedTimes = tryedTimes;
	}

	public void setMaxTimes(int maxTimes) {
		this.maxTimes = maxTimes;
	}
	
	public abstract void goOn();

}
