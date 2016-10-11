package kyo.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import kyo.Torrent;

import org.apache.log4j.Logger;

public class Counter implements Runnable {
	private static long TOP_LAST = 1000*60*60*6;
	
	private static Logger  top = Logger.getLogger("top");
	
	public static Map<String,Integer> hashes = new HashMap<String,Integer>();
	
	private long lastClearDay = 0;

	@Override
	public void run() {
		top.info("run");
		while(true){
			try{
				Thread.sleep(100);
				long now = System.currentTimeMillis();
				if(this.lastClearDay == 0){
					this.lastClearDay = now;
				}
//				top.info("time:" + (now - this.lastClearDay));
				if(now - this.lastClearDay > TOP_LAST){
					//新的周期到来了，输出周期捏TOP，然后清空
					top.info("flush size: " + hashes.size());
					List<CNode> nodes = new ArrayList<CNode>();
					for(String key : hashes.keySet()){
						nodes.add(new CNode(hashes.get(key), key));
					}
					
					Collections.sort(nodes, new Comparator<CNode>(){
						@Override
						public int compare(CNode o1, CNode o2) {
							return o2.count - o1.count;
						}
					});
					top.info("node size: " + nodes.size());
					//输出前100
					for(int i = 0; i < 100 && i < nodes.size(); i++){
						String hash = nodes.get(i).hash;
						print(hash,nodes.get(i).count);
					}
					
					hashes.clear();
					this.lastClearDay = System.currentTimeMillis();
				}
				
			}catch(Throwable t){
				t.printStackTrace();
			}
		}
	}
	
	/**
	 * 记录一次发现
	 * @param hash
	 */
	public static synchronized void addHash(String hash){
		Integer count = hashes.get(hash);
		if(count == null){
			hashes.put(hash, 1);
		}else{
			hashes.put(hash, count+1);
		}
	}
	
	/**
	 * @param hash
	 *  打印指定hash的文件信息
	 */
	public static void  print(String hash,int count){
		StringBuilder sb = new StringBuilder();
		sb.append("TOP").append(",")
			.append(hash).append(",")
			.append(count).append(",");
		File file = null;
		try{
				file = new File("files/" +hash +".torrent");
				Torrent t = Torrent.load(file);
				sb.append(t.getName()).append("::");
				List<String> names = t.getFilenames();
				
				int m = 3;
				for(String name : names){
					//最多输出3个文件名即可
					if(m-- <= 0) break;
					
					if(isVidio(name)){
						sb.append(name).append(",");
					}
				}
				
		}catch(Exception e){
			e.printStackTrace();
		}finally{
		}
		top.info(sb.toString());
	}
	
	private static boolean isVidio(String fileName){
		String prefix = getPrefix(fileName);
		if(prefix != null){
			return vidios.contains(prefix.toLowerCase());
		}
		return false;
	}
	
	private static String getPrefix(String fileName){
		String prefix=fileName.substring(fileName.lastIndexOf(".")+1);
		return prefix;
	}
	
	private static Set<String> vidios = new HashSet<String>();
	static{
		vidios.add("3gp");
		vidios.add("3g2");
		vidios.add("avi");
		vidios.add("dv");
		vidios.add("dif");
		vidios.add("mov");
		vidios.add("qt");
		vidios.add("swf");
		vidios.add("mp2");
		vidios.add("mp3");
		vidios.add("ogg");
		vidios.add("aac");
		vidios.add("m4a");
		vidios.add("nut");
		vidios.add("ac3");
		vidios.add("h261");
		vidios.add(" h264");
		vidios.add("m4v");
		vidios.add("yuv");
		vidios.add("mp4");
		vidios.add("rm");
		vidios.add("ra");
		vidios.add("ram");
		vidios.add("rmvb");
		vidios.add("wma");
		vidios.add("wmv");
		vidios.add("asf");
		vidios.add("mpg");
		vidios.add("mpeg");
		vidios.add("mpa");
		vidios.add("vob");
		vidios.add("dat");
		vidios.add("ape");
		vidios.add("cue");
		vidios.add("mkv");
		vidios.add("wav");
		vidios.add("aiff");
		vidios.add("au");
		vidios.add("cda");
		vidios.add("avs");
		vidios.add("psp");
		vidios.add("smk");
		vidios.add("nsv");
		vidios.add("zip");
		vidios.add("rar");
	}
}
