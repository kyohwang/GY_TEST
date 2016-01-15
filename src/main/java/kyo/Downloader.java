package kyo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArraySet;

import kyo.net.ClientHandler;

import org.apache.commons.codec.binary.Base32;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Logger;

import com.sun.org.apache.xerces.internal.impl.dv.util.HexBin;


public class Downloader implements Runnable{
	private static Logger  log = Logger.getLogger("world");
	private static CopyOnWriteArraySet<String> success = new CopyOnWriteArraySet<String>();
	private static CopyOnWriteArraySet<String> failures = new CopyOnWriteArraySet<String>();
	private static ConcurrentLinkedQueue<String> todos = new ConcurrentLinkedQueue<String>();
	
	static final String XunLei = "http://bt.box.n0808.com/{0}/{1}/{2}.torrent";
	static final String Vuze = "http://magnet.vuze.com/magnetLookup?hash={0}";
	static final String TorCache = "http://torcache.net/torrent/{0}.torrent";
	
	
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
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}
	
	public static void download(String infoHash){
		if(success.contains(infoHash) || failures.contains(infoHash)){
			return;
		}
		
		if(downFromXL(infoHash)
				|| downFromVuze(infoHash)
				|| downFromTorCache(infoHash)
				){
			success.add(infoHash);
			Utils.printTorrents(infoHash);
		}else{
			failures.add(infoHash);
		}
	}
	
	public static void main(String[] args){
		Downloader.downFromTorCache("66B106B04F931DA3485282C43CF66F6BD795C8C4");
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
		     return true;  
	
		} catch (Exception e) {
			e.printStackTrace();
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
		  
		    byte[] buffer = new byte[1204];  
		    while ((byteread = inStream.read(buffer)) != -1) {  
		               fs.write(buffer, 0, byteread);  
		     }
		    fs.close();
		     return true;  
	
		} catch (Exception e) {
			e.printStackTrace();
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
		     return true;  
	
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return false;
	}

}
