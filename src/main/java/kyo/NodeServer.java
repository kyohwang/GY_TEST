package kyo;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import kyo.net.NettyService;

import org.apache.log4j.Logger;

public class NodeServer {
	
	private static Logger  log = Logger.getLogger(NodeServer.class);
	
	/**本机ID*/
	public static byte[]  LOCAL_ID;
	public static String LOCAL_IP = "123.114.110.200";
	public static int LOCAL_PORT = 6881;
	private static Bucket bucket;
	/**
	 * 可能已经掉线的node，测试，再不返回就干掉
	 */
	private static List<Node> pingList ;
	
	/**
	 *  任务列表
	 *  <任务序列号，任务数据>
	 */
	private static ConcurrentHashMap<String,Worker> works;
	
	
	static{
		try {
			LOCAL_ID = "niubi098765432345787".getBytes("utf-8");
			bucket = new Bucket(LOCAL_ID);
			pingList = new CopyOnWriteArrayList<Node>();
			works = new ConcurrentHashMap<String, Worker>();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * @param node
	 *  添加节点到森林
	 */
	public static void addNode(Node node){
		log.info(",addNode"+node.log());
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
	 *  强制干掉某个任务
	 */
	public static void finishTask(String taskId){
		Worker worker = works.get(taskId);
		if(worker != null){
			works.remove(worker);
		}
	}
	
	public static Bucket getBucket() {
		return bucket;
	}

	public static void main(String[] args) throws Exception{
//		NettyService ns = new NettyService();
//		ns.startup();
		
/*		String ip = "192.168.1.1";
		String[] ips = ip.split(".");
		for(String i : ips){
			System.out.println(i);
		}*/
		
		Utils.startup();
		new Thread(new NodeChecker(bucket),"nodeChecker").start();
		new Thread(new NodeFinder(bucket),"nodeFinder").start();
		
		

		
		Thread.sleep(24*3600*1000);
		Utils.shutdown();
		
//		ns.shutdown();
	}
	
	public static void startFromFiles(){
		HashMap<String,Integer> nodes = Utils.getNodesFromTorrentFiles("E:/test/bittorrent/files");
		for(String ip : nodes.keySet()){
			int port = nodes.get(ip);
			Utils.ping(LOCAL_ID, ip, port);
			log.info("Load File:" +ip + " " + port);
		}
	}

}
