package kyo;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import kyo.net.NettyService;
import kyo.net.UdpSender;

import org.apache.commons.io.FileUtils;
import org.apache.xmlbeans.impl.util.HexBin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.turn.ttorrent.bcodec.BEValue;

public class Utils {
	static Logger log = LoggerFactory.getLogger(NettyService.class);
	
	private static UdpSender sender = new UdpSender();
	private static AtomicInteger taskId = new AtomicInteger();

	/***/
//	static byte[] info_hash = HexBin.stringToBytes("835d549747ebbb2bc4a09c7be91644fa3e9e9de9");
//	static byte[] info_hash = {85, -115, 15, 124, -10, 6, 15, 53, 46, 118, -74, -99, 39, 59, -110, -61, -64, -115, -112, 23}; 
//	static byte[] info_hash = {85, -115, 14, -123, 89, -54, 42, 124, 91, 58, 43, 102, -53, 36, -44, -48, 24, -126, -45, 46}; 
	public static String path = "E:/test/bittorrent/files";

	
	/*public static Bucket bucket = new Bucket(id);
	public static List<Node> pingList = new ArrayList<Node>();*/
	/*public static ConcurrentHashMap<String,WorkList> works = new ConcurrentHashMap<String, WorkList>();*/
	
	
	
	/**
	 * @param path
	 * @return  从指定路径下的种子文件中获取node信息
	 */
	public static HashMap<String,Integer> getNodesFromTorrentFiles(String path){
		HashMap<String,Integer> nodes = new HashMap<String, Integer>();
		File file = new File(path);
		FilenameFilter filter = new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				if(name.contains("torrent")){
					return true;
				}else{
					return false;
				}
			}
		};
		for(File f : file.listFiles(filter)){
			HashMap<String,Integer> ls = getNodesFromTorrents(f);
			for(String ip : ls.keySet()){
				nodes.put(ip, ls.get(ip));
			}
		}
		
		return nodes;
	}
	
//	public static ConcurrentHashMap<String,Integer> nodes = new ConcurrentHashMap<String, Integer>();
	
	public static void main(String[] args) throws Exception {
			
			
			/*nodes.clear();
			nodes.put("42.202.213.210", 6881);*/
			
//			startFromTorrentFiles();
//			
//			for(String ip : nodes.keySet()){
////				get_peer(ip,nodes.get(ip));
//				ping(ip,nodes.get(ip));
//			}
//			
//			test();
	}
	
	/**
	 * @param file
	 * @return 返回种子文件中存储的node信息
	 */
	public static HashMap<String,Integer> getNodesFromTorrents(File file){
		Torrent t;
		try {
			t = Torrent.load(file);
			HashMap<String,Integer> nodes = t.getNodes();
			
			for(String ip : nodes.keySet()){
				log.info("Get node from file    " + ip + ":" +nodes.get(ip));
			}
			return nodes;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 *  ping 指定Node
	 * @param selfId
	 * @param Node
	 */
	public static void ping(byte[] selfId, Node node){
		ping(selfId, node.getIp(),node.getPort());
	}
	
	/**
	 *  ping 指定Node
	 * @param selfId
	 * @param ip
	 * @param port
	 */
	public static void ping(byte[] selfId, String ip, int port){
		try {
			Map<String,BEValue> ping = new HashMap<String,BEValue>();
			ping.put("t", new BEValue("pn"));
			ping.put("y", new BEValue("q"));
			ping.put("q", new BEValue("ping"));
	
			Map<String,BEValue> a = new HashMap<String,BEValue>();
			a.put("id", new BEValue(selfId));
			ping.put("a", new BEValue(a));
			sender.send(new InetSocketAddress(ip,port), new BEValue(ping));
		} catch (Exception e) {
			e.printStackTrace();
		}
	
	}
	
	/**
	 * 查询种子存放节点
	 * @param selfID
	 * @param taskId 事件序列号 
	 * @param node 询问对象
	 * @param infoHahs  种子文件的指纹
	 */
	public static void get_peer(byte[] infoHash, String taskId,Node node){
		get_peer(NodeServer.id, infoHash, taskId, node.getIp(), node.getPort());
	}
	
	/**
	 * 查询种子存放节点
	 * @param selfID
	 * @param taskId 事件序列号 
	 * @param ip  询问对象IP
	 * @param port 询问对象port
	 * @param infoHahs  种子文件的指纹
	 */
	public static void get_peer(byte[] selfID,byte[] infoHash, String taskId, String ip, int port){
		try {
			Map<String,BEValue> ping = new HashMap<String,BEValue>();
			ping.put("t", new BEValue("gp_"+taskId));
			ping.put("y", new BEValue("q"));
			ping.put("q", new BEValue("get_peers"));
	
			Map<String,BEValue> a = new HashMap<String,BEValue>();
			a.put("id", new BEValue(selfID));
			a.put("info_hash", new BEValue(infoHash));
			ping.put("a", new BEValue(a));
			sender.send(new InetSocketAddress(ip,port), new BEValue(ping));
		} catch (Exception e) {
			e.printStackTrace();
		}
	
	}
	
	public static void test(){
		/*String tr = "gp1234";
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		works.put(tr, new WorkList(info_hash,bucket));
		
		int count = 100;
		while(count-- >= 0 && works.containsKey(tr)){
//			try {
//				Thread.sleep(500);
//				
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
			System.out.println("Count: "+ count);
			WorkList list = works.get(tr);
			Node node = list.getTop();
			while(node == null){
				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				node = list.getTop();
			}
			
			get_peer(tr,node.getIp(),node.getPort());
			
		}
		 */
	}
	
	/**
	 * @param selfID  本节点ID
	 * @param targetID   目标节点ID
	 * @param taskId  事件序列号
	 * @param Node 询问对象
	 */
	public static void findNode(byte[] targetID, String taskId, Node node){
		 findNode(NodeServer.id,targetID,taskId,node.getIp(),node.getPort());
	}
	
	
	/**
	 * @param selfID  本节点ID
	 * @param targetID   目标节点ID
	 * @param taskId  事件序列号
	 * @param ip  询问对象IP
	 * @param port  询问对象Port
	 */
	public static void findNode(byte[] selfID, byte[] targetID, String taskId, String ip, int port){
		try {
			Map<String,BEValue> ping = new HashMap<String,BEValue>();
			ping.put("t", new BEValue("fd_"+taskId));
			ping.put("y", new BEValue("q"));
			ping.put("q", new BEValue("find_node"));
	
			Map<String,BEValue> a = new HashMap<String,BEValue>();
			a.put("id", new BEValue(selfID));
			a.put("target", new BEValue(targetID));
			ping.put("a", new BEValue(a));
			sender.send(new InetSocketAddress(ip,port), new BEValue(ping));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static String fileSHA(File file){
		try {
			return HexBin.bytesToString(sha(FileUtils.readFileToByteArray(file)));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static byte[] sha(byte[] src){
		MessageDigest messageDigest;
		try {
			messageDigest = MessageDigest.getInstance("SHA1");
			messageDigest.update(src);
			return messageDigest.digest();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	/**
	 * @return  获取一个不重复的taskId
	 */
	public static String getNextTaskId(){
		return  String.valueOf(taskId.incrementAndGet());
	}
	
	
	
	
	
	

}
