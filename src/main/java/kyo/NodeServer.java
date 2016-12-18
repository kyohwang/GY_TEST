package kyo;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import kyo.utils.Counter;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.xmlbeans.impl.util.HexBin;


public class NodeServer {
	
	private static Logger  log = Logger.getLogger(NodeServer.class);
	
	/**本机ID*/
	private static byte[]  LOCAL_ID;
	public static String LOCAL_IP = "123.114.110.200";
	public static String INDEX_URL = "http://vpn.shangua.com:8984/solr/tor";
	public static int LOCAL_PORT = 6881;
	public static int DOWNLOAD_THREADS = 10;
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
	
	public static Set<String> blackList = new HashSet<String>();
	
	
	public static byte[] getLOCAL_ID(){
		
		
		return LOCAL_ID;
	}
	
	private static void init(){
		try {
			String dir = System.getProperty("user.dir");
			PropertyConfigurator.configure(dir+"/log4j.properties");
			
			LOCAL_ID = "niubi098765432345787".getBytes("utf-8");
			bucket = new Bucket(LOCAL_ID);
			pingList = new CopyOnWriteArrayList<Node>();
			works = new ConcurrentHashMap<String, Worker>();
			
			PropertiesConfiguration config = new PropertiesConfiguration("config.properties"); 
        	LOCAL_IP = config.getString("ip");
        	LOCAL_PORT = config.getInt("port");
        	LOCAL_ID = HexBin.stringToBytes(config.getString("clientid"));
        	DOWNLOAD_THREADS = config.getInt("downThreads");
        	INDEX_URL =  config.getString("indexUrl");
			
		} catch (Exception e) {
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

		NodeServer.init();
		//排行榜启动
		new Thread(new Counter(), "counter").start();
		
		Utils.startup();
		new Thread(new NodeChecker(bucket),"nodeChecker").start();
		new Thread(new NodeFinder(bucket),"nodeFinder").start();
		Downloader down = new Downloader();
		down.init();
		new Thread(down,"downloader").start();
		Indexer index = new Indexer();
		index.init();
//		new Thread(index,"indexer").start();
		
		
		while(true){
			Thread.sleep(600);
			for(Worker w : works.values()){
				w.goOn();
			}
		}
	}
	
	public static void addWorker(Worker w){
		if(works.size() < 1000){
			works.put(w.getTaskId(), w);
		}
	}
	
	public static void startFromFiles(){
		HashMap<String,Integer> nodes = Utils.getNodesFromTorrentFiles("data");
		for(String ip : nodes.keySet()){
			int port = nodes.get(ip);
			Utils.ping(LOCAL_ID, ip, port);
			log.info("Load File:" +ip + " " + port);
		}
	}

}
