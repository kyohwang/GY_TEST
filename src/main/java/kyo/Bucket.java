package kyo;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class Bucket {
	
	private static final int K = 8;

	/**
	 * 按照自身ID构建的节点森林
	 */
	private ConcurrentHashMap<Integer,List<Node>> tree = new ConcurrentHashMap<Integer, List<Node>>();

	private byte[] id;
	
	
	public Bucket(byte[] id){
		this.id = id;
		init();
	}
	
	/**
	 * 初始化森林
	 */
	private void init(){
		for(int i = 1; i <= id.length*8; i++){
			CopyOnWriteArrayList<Node> list = new CopyOnWriteArrayList<Node>();
			tree.put(i, list);
		}
	}
	
	/**
	 * @param node
	 * 如果一个树上多余k个节点，则不再添加
	 */
	public void addNode(Node node){
		int prefix = this.getPrefix(node.getId());
		List<Node> list = tree.get(prefix);
		if(list.size() < K){
			for(Node n : list){
			//避免重复添加
				if(n.equals(node)){
					n.setTriedTimes(0);
					n.setLastActive(System.currentTimeMillis());
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
	 * @return 所在子树的所有节点
	 */
	public List<Node> getNodes(byte[] nid){
		int prefix = this.getPrefix(nid);
		return tree.get(prefix);
	}
	
	/**
	 * @param nid
	 * @return 所在子树的所有节点
	 */
	public List<Node> getNodes(int count, byte[] nid){
		int prefix = this.getPrefix(nid);
		List<Node> tmp =  tree.get(prefix);
		List<Node> rst = new ArrayList<Node>();
		if(tmp != null){
			for(int i = 0;i < tmp.size() && i < count; i++){
				rst.add(tmp.get(i));
			}
		}
		if(rst.size() < count){
			for(List<Node> list : tree.values()){
				for(Node node : list){
					if(rst.size() >= count){
						return rst;
					}else{
						rst.add(node);
					}
				}
			}
		}
		
		return rst;
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
	 * 获取所在树编号
	 * @param nid
	 * @return
	 */
	public int getPrefix(byte[] nid){
		
		for(int i = 0; i < id.length; i++){
			if(id[i] != nid[i]){
				int max = 0;
				for(int m = 0; m < 8; m++){
					if(id[i] >> m == nid[i] >>m){
						break;
					}else{
						max++;
					}
				}
				
				return i * 8 + (8 - max) +1;
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

	public ConcurrentHashMap<Integer, List<Node>> getTree() {
		return tree;
	}
	
	
	public int getNodeSize(){
		int size = 0;
		for(List<Node> list : tree.values()){
			size += list.size();
		}
		return size;
	}
	
}
