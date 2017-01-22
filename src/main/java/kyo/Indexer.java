package kyo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.common.SolrInputDocument;
import org.apache.xmlbeans.impl.util.HexBin;

import com.google.common.collect.Queues;
import com.google.common.collect.Sets;
import com.turn.ttorrent.bcodec.InvalidBEncodingException;

public class Indexer implements Runnable {
	private static Logger  log = Logger.getLogger("world");
	private static Logger  logIndex = Logger.getLogger("index");
	public static Set<String> success = Sets.newHashSet();
	public static LinkedBlockingQueue<String> todos = 	Queues.newLinkedBlockingQueue();
	
	static HttpSolrClient ss ;
	
	public void init(){
		long start = System.currentTimeMillis();
		log.info("Indexer init start");
		String dir = System.getProperty("user.dir");
		File file = new File(dir,"index");
		FilenameFilter filter = new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				if(name.contains("index")){
					return true;
				}else{
					return false;
				}
			}
		};
		for(File f : file.listFiles(filter)){
			log.info(f.getName());
			try {
				BufferedReader reader = new BufferedReader(new FileReader(f));
				String line = null;
				while((line = reader.readLine()) != null){
					success.add(line);
//					todos.remove(line);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		log.info("ma");
		todos.removeAll(success);
		log.info("mb");
		
		log.info("Init Index todo size: " + todos.size() + " success size:" + success.size());
		log.info("Indexer init cost: " + (System.currentTimeMillis() - start));
	}
	
	@Override
	public void run() {
		long logMark = -1;
		while(true){
			try{
				Thread.sleep(5);
				String infoHash = todos.poll();
				if(infoHash != null){
					if(!index(infoHash)){
						//索引提交失败，则继续放入todo列表
						todos.add(infoHash);
					}
				}
				
				long mark = (System.currentTimeMillis() / 10000);
				if(logMark != mark){
					logMark = mark;
					log.info("Index:  todoSize="+todos.size() +"   successSize="+success.size());
				}
				
			}catch(Exception e){
				e.printStackTrace();
			}
		}

	}
	
	/**
	 * @param hash
	 * @return true：无需继续索引   false：下次继续创建索引
	 */
	public boolean index(String hash){
		
		if(success.contains(hash)){
			return true;
		}
		
		File file = null;
		SolrInputDocument doc = new SolrInputDocument();
		try{
				file = new File("files/" +hash +".torrent");
//				log.info(file.getName() + "  " + file.length());
				Torrent t = Torrent.load(file);
				doc.addField("hash", HexBin.bytesToString(t.getInfoHash()));
				doc.addField("size", t.getSize());
				doc.addField("comment", t.getComment());
				doc.addField("name", t.getName());
				doc.addField("files", t.getFilenames());
				ss.add(doc);
				ss.commit();
				
				success.add(hash);
				logIndex.info(hash);
				return true;
		}catch(NullPointerException n){
			//种子文件没有内容
			//tor 文件问题，需要重新下载
			if(file != null && file.exists()){
				file.delete();
			}
			Downloader.success.remove(hash);
			return true;
		}catch(InvalidBEncodingException e){
			//tor 文件问题，需要重新下载
			if(file != null && file.exists()){
				file.delete();
			}
			Downloader.success.remove(hash);
//			e.printStackTrace();
			return true;
		}catch(Exception e){
			ss = new HttpSolrClient(NodeServer.INDEX_URL);
			e.printStackTrace();
		}
		
		return false;
	}

}
