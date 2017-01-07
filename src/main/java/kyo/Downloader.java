package kyo;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicLong;

import kyo.utils.SslUtils;

import org.apache.commons.codec.binary.Base32;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Logger;

import com.sun.org.apache.xerces.internal.impl.dv.util.HexBin;


public class Downloader implements Runnable{
	private static Logger  log = Logger.getLogger("world");
	public static CopyOnWriteArraySet<String> success = new CopyOnWriteArraySet<String>();
	public static CopyOnWriteArraySet<String> failures = new CopyOnWriteArraySet<String>();
	public static ConcurrentLinkedQueue<String> todos = new ConcurrentLinkedQueue<String>();
	
	static final String XunLei = "http://bt.box.n0808.com/{0}/{1}/{2}.torrent";
	static final String Vuze = "http://magnet.vuze.com/magnetLookup?hash={0}";
	static final String TorCache = "http://torcache.net/torrent/{0}.torrent";
	static final String theTor = "http://thetorrent.org/{0}.torrent?_={1}";
	
	static final String[] noNames = {
		"https://torrasave.top/torrent/{0}.torrent",
		"https://178.73.198.210/torrent/{0}.torrent",
		"http://itorrents.org/torrent/{0}.torrent",
		"http://torrage.com/torrent/{0}.torrent"
	};
	
	
	static AtomicLong xCount = new AtomicLong(0);
	static AtomicLong vCount = new AtomicLong(0);
	static AtomicLong tCount = new AtomicLong(0);
	static AtomicLong theCount = new AtomicLong(0);
	
	
	public static void addInfoHash(String hash){
		if(failures.contains(hash) || success.contains(hash)){
			return;
		}
		
		todos.offer(hash);
	}
	
	public static String nextInfoHash(){
		synchronized(Downloader.class){
			return todos.poll();
		}
	}
	
	public void init(){
		String dir = System.getProperty("user.dir");
		File file = new File(dir,"files");
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
			success.add(f.getName().split("\\.")[0]);
		}
		
		for(int i = 0; i < NodeServer.DOWNLOAD_THREADS; i++){
			new Thread(new DonloadWorker(),"downloadWorker-"+i).start();
		}
	}

	@Override
	public void run() {
		while(true){
			try{
				Thread.sleep(10000);
				log.info("down load:  todoSize="+todos.size() +"   successSize="+success.size()+"   failureSize:" +failures.size());
				log.info("down load:  Xunlei="+xCount +"   Vuze="+vCount+"   torCache:" +tCount);
			}catch(Exception e){
//				e.printStackTrace();
			}
		}
	}
	
	public static void download(String infoHash){
		if(success.contains(infoHash) || failures.contains(infoHash)){
			return;
		}
		
		if(
//				downFromXL(infoHash)
				/*||*/ downFromVuze(infoHash)
//				|| downFromTorCache(infoHash)
				|| downFromNoName(infoHash)
				){
			success.add(infoHash);
			try {
				Utils.printTorrents(infoHash);
			} catch (IOException e) {
				e.printStackTrace();
			}
			Indexer.todos.add(infoHash);
		}else{
			failures.add(infoHash);
		}
	}
	
	public static void theTorTest(){
		String infoHash = "C94C03C7F829F283A4F0C3BB2A32AE3A2E5C3E5C";
		Downloader.downFromTheTorCache(null,infoHash);
		
		File file = new File("files/" +infoHash+".torrent");
		HashMap<String,Integer> ls = Utils.getNodesFromTorrents(file);
		System.out.println(ls.toString());
	}
	
	public static void main(String[] args){
		System.setProperty("javax.net.ssl.trustStore", "jssecacerts");
//		for(int i = 0; i < 100; i++)
		Downloader.download("3C4BC61FAA3DA9C22AE857B4B14CC0E7472F789C");
//		Downloader.downFromNoName("6E4F1001FFCB4304E38A5157705CD46022CC9B7E");
	}
	
	public static boolean downFromXL(String infoHash){
		try {
			URL httpUrl = new URL(MessageFormat.format(XunLei, infoHash.substring(0, 2),infoHash.substring(38),infoHash));
			HttpURLConnection http = (HttpURLConnection) httpUrl.openConnection();
			http.setConnectTimeout(5000);
			http.setReadTimeout(30000);
			
		    int byteread = 0;  
		  
		    InputStream inStream = http.getInputStream();  
		    FileOutputStream fs = new FileOutputStream("files/" +infoHash+".torrent");  
		  
		    byte[] buffer = new byte[1204];  
		    while ((byteread = inStream.read(buffer)) != -1) {  
		               fs.write(buffer, 0, byteread);  
		     }
		    fs.close();
		    System.out.println("xl ok.");
		     return true;  
	
		} catch (Exception e) {
			e.printStackTrace();
			xCount.incrementAndGet();
		}
		
		return false;
	}
	
	public static boolean downFromVuze(String infoHash){
		try {
			byte[] bytes = HexBin.decode(infoHash);
			Base32 base = new Base32();
			URL httpUrl = new URL(MessageFormat.format(Vuze, base.encodeAsString(bytes)));
			HttpURLConnection http = (HttpURLConnection) httpUrl.openConnection();
			http.setConnectTimeout(5000);
			http.setReadTimeout(30000);
			
		    int byteread = 0;  
		  
		    InputStream inStream = http.getInputStream();  
		    FileOutputStream fs = new FileOutputStream("files/" +infoHash+".torrent");  
		    ByteArrayOutputStream bs = new ByteArrayOutputStream();
		    byte[] buffer = new byte[1204];  
		    while ((byteread = inStream.read(buffer)) != -1) {  
		               fs.write(buffer, 0, byteread); 
		               bs.write(buffer, 0, byteread);
		     }
		    fs.close();
		    new Torrent(bs.toByteArray());
		    return true;  
	
		} catch (Exception e) {
//			e.printStackTrace();
			vCount.incrementAndGet();
		}
		
		return false;
	}
	
	public static boolean downFromTorCache(String infoHash){
		try {
			HttpGet get = new HttpGet(MessageFormat.format(TorCache, infoHash));
			 get.setHeader("Accept", "Accept text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");    
			 get.setHeader("Accept-Charset", "GB2312,utf-8;q=0.7,*;q=0.7");    
			 get.setHeader("Accept-Encoding", "gzip, deflate");    
			 get.setHeader("Accept-Language", "zh-cn,zh;q=0.5");    
		     get.setHeader("Connection", "keep-alive");    
		     get.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; rv:6.0.2) Gecko/20100101 Firefox/6.0.2"); 
		     HttpResponse res = new DefaultHttpClient().execute(get);
			
		    int byteread = 0;  
		  
		    InputStream inStream = res.getEntity().getContent();  
		    FileOutputStream fs = new FileOutputStream("files/" +infoHash+".torrent");  
		  
		    byte[] buffer = new byte[1204];  
		    while ((byteread = inStream.read(buffer)) != -1) {  
		               fs.write(buffer, 0, byteread);  
		     }
		    fs.close();
		    System.out.println("tor ok.");
		     return true;  
	
		} catch (Exception e) {
//			e.printStackTrace();
			tCount.incrementAndGet();
		}
		
		return false;
	}
	
	public static boolean downFromNoName(String infoHash){
		for(String noName : noNames){
			try {
				SslUtils.ignoreSsl();
				String url = MessageFormat.format(noName, infoHash);
				HttpGet get = new HttpGet(url);
				 get.setHeader("Accept", "Accept text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");    
				 get.setHeader("Accept-Charset", "GB2312,utf-8;q=0.7,*;q=0.7");    
				 get.setHeader("Accept-Encoding", "gzip, deflate");    
				 get.setHeader("Accept-Language", "zh-cn,zh;q=0.5");    
			     get.setHeader("Connection", "keep-alive");    
			     get.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; rv:6.0.2) Gecko/20100101 Firefox/6.0.2"); 
			     HttpResponse res = new DefaultHttpClient().execute(get);
				
			    int byteread = 0;  
//			    System.out.println(res.getStatusLine().getStatusCode());
			    String type = res.getHeaders("Content-Type")[0].getValue();
//			    System.out.println(type);
			    InputStream inStream = res.getEntity().getContent();  
			    if(type.contains("html")){
			    	BufferedReader br = new BufferedReader(new InputStreamReader(inStream,"utf8"));
			    	String html = "";
			    	String tmp = null;
			    	while((tmp = br.readLine()) != null){
			    		html += tmp;
			    	}
			    	
//			    	System.out.println(html);
			    }else{
			    	FileOutputStream fs = new FileOutputStream("files/" +infoHash+".torrent");
			    	ByteArrayOutputStream bs = new ByteArrayOutputStream();
			    	
			    	byte[] buffer = new byte[1204];  
//			    	System.out.println(inStream.available());
			    	while ((byteread = inStream.read(buffer)) != -1) {  
			    		fs.write(buffer, 0, byteread);  
			    		bs.write(buffer, 0, byteread);
			    		/*if(byteread < buffer.length){
			    			break;
			    		}*/
			    	}
			    	fs.close();
			    	new Torrent(bs.toByteArray());
			    	log.info("dowanload by:" + noName);
			    	return true;  
			    }
			    
		
			} catch (Exception e) {
//				e.printStackTrace();
				tCount.incrementAndGet();
			}
		}
		
		return false;
	}
	
	public static boolean downFromTheTorCache(String url,String infoHash){
		try {
			if(url == null){
				url = MessageFormat.format(theTor, infoHash, (int)Math.random()*10000000);
			}
			HttpGet get = new HttpGet(url);
			 get.setHeader("Accept", "Accept text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");    
			 get.setHeader("Accept-Charset", "GB2312,utf-8;q=0.7,*;q=0.7");    
			 get.setHeader("Accept-Encoding", "gzip, deflate");    
			 get.setHeader("Accept-Language", "zh-cn,zh;q=0.5");    
		     get.setHeader("Connection", "keep-alive");    
		     get.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; rv:6.0.2) Gecko/20100101 Firefox/6.0.2"); 
		     HttpResponse res = new DefaultHttpClient().execute(get);
			
		    int byteread = 0;  
		    System.out.println(res.getStatusLine().getStatusCode());
		    String type = res.getHeaders("Content-Type")[0].getValue();
		    System.out.println(type);
		    InputStream inStream = res.getEntity().getContent();  
		    if(type.contains("html")){
		    	BufferedReader br = new BufferedReader(new InputStreamReader(inStream,"utf8"));
		    	String html = "";
		    	String tmp = null;
		    	while((tmp = br.readLine()) != null){
		    		html += tmp;
		    	}
		    	
		    	System.out.println(html);
		    }else{
		    	FileOutputStream fs = new FileOutputStream("files/" +infoHash+".torrent");  
		    	
		    	byte[] buffer = new byte[1204];  
		    	while ((byteread = inStream.read(buffer)) != -1) {  
		    		fs.write(buffer, 0, byteread);  
		    	}
		    	fs.close();
		    	return true;  
		    }
		    
	
		} catch (Exception e) {
//			e.printStackTrace();
			tCount.incrementAndGet();
		}
		
		return false;
	}

}
