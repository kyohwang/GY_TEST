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

import kyo.net.NettyService;
import kyo.net.UdpSender;

import org.apache.commons.io.FileUtils;
import org.apache.xmlbeans.impl.util.HexBin;

import com.turn.ttorrent.bcodec.BEValue;

public class Utils {

	/**±¾»úID*/
	public static byte[]  id  = null;
	static byte[] info_hash = HexBin.stringToBytes("835d549747ebbb2bc4a09c7be91644fa3e9e9de9");
//	static byte[] info_hash = {85, -115, 15, 124, -10, 6, 15, 53, 46, 118, -74, -99, 39, 59, -110, -61, -64, -115, -112, 23}; 
//	static byte[] info_hash = {85, -115, 14, -123, 89, -54, 42, 124, 91, 58, 43, 102, -53, 36, -44, -48, 24, -126, -45, 46}; 
	public static String path = "E:/test/bittorrent/files";

	static{
		try {
			id = "niubi098765432345787".getBytes("utf-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
	
	
//	public static List<Node> bucket = new ArrayList<Node>();
	public static Bucket bucket = new Bucket(id);
	public static List<Node> pingBucket = new ArrayList<Node>();
	public static ConcurrentHashMap<String,WorkList> works = new ConcurrentHashMap<String, WorkList>();
	
	
	
	public static void startFromTorrentFiles(){
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
			System.out.println(f.getName());
			try {
				System.out.println(HexBin.bytesToString(sha(FileUtils.readFileToByteArray(f))));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static ConcurrentHashMap<String,Integer> nodes = new ConcurrentHashMap<String, Integer>();
	
	public static void main(String[] args) throws Exception {
			NettyService ns = new NettyService();
			ns.startup();
			
			/*nodes.clear();
			nodes.put("42.202.213.210", 6881);*/
			
			startFromTorrentFiles();
			
			for(String ip : nodes.keySet()){
//				get_peer(ip,nodes.get(ip));
				ping(ip,nodes.get(ip));
			}
			
			test();
	}
	
	public static HashMap<String,Integer> getNodesFromTorrents(File file){
		Torrent t;
		try {
			t = Torrent.load(file);
			HashMap<String,Integer> nodes = t.getNodes();
			return nodes;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static void ping(String ip, int port){
		try {
		Map<String,BEValue> ping = new HashMap<String,BEValue>();
		ping.put("t", new BEValue("pn"));
		ping.put("y", new BEValue("q"));
		ping.put("q", new BEValue("ping"));

		Map<String,BEValue> a = new HashMap<String,BEValue>();
		a.put("id", new BEValue(id));
		ping.put("a", new BEValue(a));
		UdpSender sender = new UdpSender();
		sender.send(new InetSocketAddress(ip,port), new BEValue(ping));
		sender = null;
		} catch (Exception e) {
			e.printStackTrace();
		}
	
	}
	
	public static void get_peer(String tr, String ip, int port){
		try {
		Map<String,BEValue> ping = new HashMap<String,BEValue>();
		ping.put("t", new BEValue(tr));
		ping.put("y", new BEValue("q"));
		ping.put("q", new BEValue("get_peers"));

		Map<String,BEValue> a = new HashMap<String,BEValue>();
		a.put("id", new BEValue(id));
		a.put("info_hash", new BEValue(info_hash));
		ping.put("a", new BEValue(a));
		UdpSender sender = new UdpSender();
		sender.send(new InetSocketAddress(ip,port), new BEValue(ping));
		sender = null;
		} catch (Exception e) {
			e.printStackTrace();
		}
	
	}
	
	public static void test(){
		String tr = "gp1234";
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
	}
	
	
	public static void findNode(String ip, int port){
		try {
		Map<String,BEValue> ping = new HashMap<String,BEValue>();
		ping.put("t", new BEValue("fd"));
		ping.put("y", new BEValue("q"));
		ping.put("q", new BEValue("find_node"));

		Map<String,BEValue> a = new HashMap<String,BEValue>();
		a.put("id", new BEValue(id));
		a.put("target", new BEValue("shabi0987654323niubi"));
		ping.put("a", new BEValue(a));
		UdpSender sender = new UdpSender();
		sender.send(new InetSocketAddress(ip,port), new BEValue(ping));
		sender = null;
		} catch (Exception e) {
			e.printStackTrace();
		}
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
	
	
	
	
	
	
	
	

}
