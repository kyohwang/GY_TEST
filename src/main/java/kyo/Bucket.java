package kyo;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Bucket {
	
	private static final int K = 8;

	/**
	 * ��������ID�����Ľڵ�ɭ��
	 */
	private HashMap<Integer,List<Node>> tree = new HashMap<Integer, List<Node>>();

	private byte[] id;
	
	
	public Bucket(byte[] id){
		this.id = id;
		init();
	}
	
	/**
	 * ��ʼ��ɭ��
	 */
	private void init(){
		for(int i = 1; i <= id.length*8; i++){
			CopyOnWriteArrayList<Node> list = new CopyOnWriteArrayList<Node>();
			tree.put(i, list);
		}
	}
	
	/**
	 * @param node
	 * ���һ�����϶���k���ڵ㣬�������
	 */
	public void addNode(Node node){
		int prefix = this.getPrefix(node.getId());
		List<Node> list = tree.get(prefix);
		if(list.size() < K){
			for(Node n : list){
			//�����ظ����
				if(n.equals(node)){
					return;
				}
			}
			list.add(node);
		}
	}
	
	public void removeNode(Node node){
		int prefix = this.getPrefix(node.getId());
		List<Node> list = tree.get(prefix);
		Node tmp = null; 
		for(Node n : list){
			if(n.equals(node)){
				tmp = n;
			}
		}
		
		if(tmp != null){
			list.remove(tmp);
		}
	}
	
	/**
	 * @param nid
	 * @return �������������нڵ�
	 */
	public List<Node> getNodes(byte[] nid){
		int prefix = this.getPrefix(nid);
		return tree.get(prefix);
	}
	
	public Node getNode(byte[] nid){
		int prefix = this.getPrefix(nid);
		List<Node> list = tree.get(prefix);
		for(Node n : list){
			if(n.match(nid)){
				return n;
			}
		}
		return null;
	}
	
	/**
	 * ��ȡ���������
	 * @param nid
	 * @return
	 */
	public int getPrefix(byte[] nid){
		
		for(int i = 0; i < id.length*8; i++){
			if(id[i] != nid[i]){
				int tmp = id[i] ^ nid[i];
				int max = 0;
				while(tmp>2){
					tmp /= 2;
					max++;
				}
				max++;
				
				return i * 8 + (8 - max);
			}
		}
		return 160;
	}

	public byte[] getId() {
		return id;
	}

	public void setId(byte[] id) {
		this.id = id;
	}
	
}
