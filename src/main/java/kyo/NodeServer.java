package kyo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import kyo.net.UdpSender;
import kyo.utils.Counter;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import runner.RunnerManager;


public class NodeServer {
	
	private static Logger  log = Logger.getLogger(NodeServer.class);
	public static String LOCAL_IP = "123.114.110.200";
	public static String INDEX_URL = "http://vpn.shangua.com:8984/solr/tor";
	public static int DOWNLOAD_THREADS = 10;
	static Map<Integer,UdpSender> updSenders = new HashMap<Integer,UdpSender>();
	
	/**
	 *  任务列表
	 *  <任务序列号，任务数据>
	 */
	private static ConcurrentHashMap<String,Worker> works;
	
	public static Set<String> blackList = new HashSet<String>();
	
	
	private static void init(){
		try {
			String dir = System.getProperty("user.dir");
			PropertyConfigurator.configure(dir+"/log4j.properties");
			System.setProperty("javax.net.ssl.trustStore", dir+"/jssecacerts");
			works = new ConcurrentHashMap<String, Worker>();
			PropertiesConfiguration config = new PropertiesConfiguration("config.properties"); 
        	LOCAL_IP = config.getString("ip");
        	DOWNLOAD_THREADS = config.getInt("downThreads");
        	INDEX_URL =  config.getString("indexUrl");
        	
        	File nodes = new File("nodes.properties");
        	FileReader reader = new FileReader(nodes);
        	BufferedReader bf = new BufferedReader(reader);
        	String line = null;
        	while((line = bf.readLine()) != null){
        		String[] ls = line.split(",");
        		UdpSender udp  = new UdpSender(ls[1], ls[0]);
        		updSenders.put(udp.LOCAL_PORT, udp);
        	}
        	
        	bf.close();
        	reader.close();			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * @param node
	 *  添加节点到森林
	 */
	public static void addNode(int nodePort,Node node){
		log.info(nodePort + ": addNode"+node.log());
		UdpSender n = updSenders.get(nodePort);
		if(n != null){
			n.bucket.addNode(node);
		}
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
	
	public static Bucket getBucket(int nodePort) {
		return updSenders.get(nodePort).bucket;
	}

	public static void main(String[] args) throws Exception{

		RunnerManager rm = new RunnerManager();
		new Thread(rm,"RunnerManager").start();
		
		NodeServer.init();
		//排行榜启动
//		new Thread(new Counter(), "counter").start();
//		new Thread(new NodeChecker(),"nodeChecker").start();
//		new Thread(new NodeFinder(),"nodeFinder").start();
		Downloader down = new Downloader();
//		down.di();
		down.init();
//		new Thread(down,"downloader").start();
		Indexer index = new Indexer();
		index.init();
//		new Thread(index,"indexer").start();
		
		
		rm.addRunner(new Counter(), "counter");
		rm.addRunner(new NodeChecker(), "nodeChecker");
		rm.addRunner(new NodeFinder(), "nodeFinder");
		rm.addRunner(down, "downloader");
		rm.addRunner(index, "indexer");	
		
		for(int i = 0; i < NodeServer.DOWNLOAD_THREADS; i++){
			rm.addRunner(new DonloadWorker(),"downloadWorker-"+i);
		}
		
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
	
	public static void startFromFiles(int nodePort){
		HashMap<String,Integer> ns = Utils.getNodesFromTorrentFiles("data");
		for(String ip : ns.keySet()){
			int port = ns.get(ip);
			UdpSender udp = updSenders.get(nodePort);
			if(udp != null){
				Utils.ping(udp.LOCAL_ID, ip, port,nodePort);
			}
			log.info("Load File:" +ip + " " + port);
		}
	}
	
	public static byte[] getLOCAL_ID(int port){
		return updSenders.get(port).LOCAL_ID;
	}

}
