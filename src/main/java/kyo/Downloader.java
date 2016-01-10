package kyo;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.MessageFormat;
import java.util.concurrent.CopyOnWriteArraySet;

import kyo.net.ClientHandler;

import org.apache.commons.codec.binary.Base32;

import com.sun.org.apache.xerces.internal.impl.dv.util.HexBin;


public class Downloader implements Runnable{
	
	public static CopyOnWriteArraySet<String> hashes = new CopyOnWriteArraySet<String>();
	
	static final String XunLei = "http://bt.box.n0808.com/{0}/{1}/{2}.torrent";
	static final String Vuze = "http://magnet.vuze.com/magnetLookup?hash={0}";
	static final String TorCache = "http://torcache.net/torrent/{0}.torrent";

	@Override
	public void run() {
		while(true){
			try{
				Thread.sleep(20000);
				for(String infoHash : ClientHandler.infohashes){
					if(!hashes.contains(infoHash)){
						if(downFromXL(infoHash)
								|| downFromVuze(infoHash)
								){
							hashes.add(infoHash);
							Utils.printTorrents(infoHash);
						}
					}
				}
				
			}catch(Exception e){
				e.printStackTrace();
			}
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
			URL httpUrl = new URL(MessageFormat.format(TorCache, infoHash));
			HttpURLConnection http = (HttpURLConnection) httpUrl.openConnection();
			http.setConnectTimeout(5000);
			http.setReadTimeout(30000);
			
		    int byteread = 0;  
		    System.out.println(http.getResponseCode());
		  
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

}
