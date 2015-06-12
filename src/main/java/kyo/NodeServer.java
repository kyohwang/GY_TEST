package kyo;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class NodeServer {
	
	/**����ID*/
	public static byte[]  id;
	private static Bucket bucket;
	/**
	 * �����Ѿ����ߵ�node�����ԣ��ٲ����ؾ͸ɵ�
	 */
	private static List<Node> pingList ;
	
	/**
	 *  �����б�
	 *  <�������кţ���������>
	 */
	private static ConcurrentHashMap<String,Worker> works;
	
	
	static{
		try {
			id = "niubi098765432345787".getBytes("utf-8");
			bucket = new Bucket(id);
			pingList = new CopyOnWriteArrayList<Node>();
			works = new ConcurrentHashMap<String, Worker>();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * @param node
	 *  ��ӽڵ㵽ɭ��
	 */
	public static void addNode(Node node){
		bucket.addNode(node);
	}
	
	/**
	 * @param taskId
	 * @param node
	 * @return
	 */
	public static boolean checkFinishFindNode(String taskId, Node node){
		Worker work = works.get(taskId);
		if(work == null){
			return true;
		}
		
		if(node.match(work.getTargetId())){
			works.remove(work);
			return true;
		}else{
			work.add(node);
			work.goOn();
			return false;
		}
	}
	
	public static void finishGetPeer(String taskId, List<Peer> peers){
		Worker work = works.get(taskId);
		if(work != null){
			works.remove(work);
		}
	}
	
	public static Worker getWorker(String taskId){
		return works.get(taskId);
	}
	
	/**
	 * @param taskId  
	 *  ǿ�Ƹɵ�ĳ������
	 */
	public static void finishTask(String taskId){
		Worker worker = works.get(taskId);
		if(worker != null){
			works.remove(worker);
		}
	}
	
	public static void main(String[] args){

	
	}

}
