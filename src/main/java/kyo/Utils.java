package kyo;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import kyo.net.NettyService;
import kyo.net.UdpSender;
import net.sf.json.JSONObject;

import org.apache.commons.io.FileUtils;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.common.SolrInputDocument;
import org.apache.xmlbeans.impl.util.HexBin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.turn.ttorrent.bcodec.BEValue;

public class Utils {
	static Logger log = LoggerFactory.getLogger(NettyService.class);
	static Logger tor = LoggerFactory.getLogger("torrent");
	
	private static UdpSender sender;
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
	public static HashMap<String,Integer> getNodesFromTorrentFiles(String fileName){
		HashMap<String,Integer> nodes = new HashMap<String, Integer>();
		String dir = System.getProperty("user.dir");
		File file = new File(dir,fileName);
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
		int count = 100;
		for(File f : file.listFiles(filter)){
			try{
				if(count-- <= 0){
					break;
				}
				HashMap<String,Integer> ls = getNodesFromTorrents(f);
				for(String ip : ls.keySet()){
					nodes.put(ip, ls.get(ip));
				}
			}catch(Exception e){
				
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
		
	/*	try{
			File file = new File("files/" +"33EE6E6AE87F24E3E9A0AA308C57CB289A927A04.torrent");
			HttpSolrClient ss = new HttpSolrClient("http://vpn.shangua.com:8984/solr/tor");
			
			Utils.indexSolr(ss, file);
					
		}catch(Exception e){
			e.printStackTrace();
		}*/
		
		analyse();
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
	
	public static void printTorrents(String infoHash){
		try{
			File file = new File("files/" +infoHash+".torrent");
			printTorrents(file);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public static String jsonTorrents(File file){
		Torrent t;
		try {
			Map<String,Object> map = new HashMap<String, Object>();
			t = Torrent.load(file);
			map.put("hash", HexBin.bytesToString(t.getInfoHash()));
			map.put("size", t.getSize());
			map.put("comment", t.getComment());
			map.put("name", t.getName());
			map.put("files", t.getFilenames());
		
			return JSONObject.fromObject(map).toString();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	public static void indexSolr(HttpSolrClient  solr,File file) throws Exception{
		  if(solr == null || file == null) return;
		  
		  try {
			  SolrInputDocument doc = new SolrInputDocument();
				Torrent t = Torrent.load(file);
				doc.addField("hash", HexBin.bytesToString(t.getInfoHash()));
				doc.addField("size", t.getSize());
				doc.addField("comment", t.getComment());
				doc.addField("name", t.getName());
				doc.addField("files", t.getFilenames());
				solr.add(doc);
				solr.commit();
//				solr.optimize();
		  }catch(Exception e){
			  e.printStackTrace();
			  throw e;
		  }
	}
	
	public static void printTorrents(File file){
		Torrent t;
		try {
			t = Torrent.load(file);
			HashMap<String,Integer> nodes = t.getNodes();
			
			StringBuilder sb = new StringBuilder();
			sb.append(HexBin.bytesToString(t.getInfoHash())).append(",");
			sb.append(t.getName()).append(",");
			sb.append(t.getSize()).append(",");
			sb.append(t.getComment()).append(",");
			for(String fn : t.getFilenames()){
				sb.append(fn).append(",");
			}
			
			tor.info("Torrent: " + sb.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 *  ping 指定Node
	 * @param selfId
	 * @param CNode
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
			ping.put("t", new BEValue("pn_"+getNextTaskId()));
			ping.put("y", new BEValue("q"));
			ping.put("q", new BEValue("ping"));
	
			Map<String,BEValue> a = new HashMap<String,BEValue>();
			a.put("id", new BEValue(selfId));
			ping.put("a", new BEValue(a));
			sender.send(new InetSocketAddress(ip,port), new BEValue(ping));
			log.info("PING : " + ip + " " + port);
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
		get_peer(NodeServer.getLOCAL_ID(), infoHash, taskId, node.getIp(), node.getPort());
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
	 * @param CNode 询问对象
	 */
	public static void findNode(byte[] targetID, String taskId, Node node){
		 findNode(NodeServer.getLOCAL_ID(),targetID,taskId,node.getIp(),node.getPort());
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
	
	
	public static void startup(){
		sender = new UdpSender();
	}
	
	public static void shutdown(){
		sender.shutdown();
	}
	
	public static void analyse(){
		try {
			             // 要处理的文本
//			             String text = "lucene分析器使用分词器和过滤器构成一个“管道”，文本在流经这个管道后成为可以进入索引的最小单位，因此，一个标准的分析器有两个部分组成，一个是分词器tokenizer,它用于将文本按照规则切分为一个个可以进入索引的最小单位。另外一个是TokenFilter，它主要作用是对切出来的词进行进一步的处理（如去掉敏感词、英文大小写转换、单复数处理）等。lucene中的Tokenstram方法首先创建一个tokenizer对象处理Reader对象中的流式文本，然后利用TokenFilter对输出流进行过滤处理";
			             String text = "芈月传";
			             // 自定义停用词
			             String[] self_stop_words = { "的", "在","了", "呢", "，", "0", "：", ",", "是", "流" };
			             CharArraySet cas = new CharArraySet(0, true);
			             for (int i = 0; i < self_stop_words.length; i++) {
			                 cas.add(self_stop_words[i]);
			             }
			 
			             // 加入系统默认停用词
			             Iterator<Object> itor = SmartChineseAnalyzer.getDefaultStopSet().iterator();
			             while (itor.hasNext()) {
			                 cas.add(itor.next());
			             }
			             
			 
			             // 中英文混合分词器(其他几个分词器对中文的分析都不行)
			             SmartChineseAnalyzer sca = new SmartChineseAnalyzer( cas);
			 
			             TokenStream ts = sca.tokenStream("field", text);
			             CharTermAttribute ch = ts.addAttribute(CharTermAttribute.class);
			 
			             ts.reset();
			             while (ts.incrementToken()) {
			                 System.out.println(ch.toString());
			             }
			             ts.end();
			             ts.close();
			         } catch (Exception ex) {
			             ex.printStackTrace();
			         }
	}
	
	

}
