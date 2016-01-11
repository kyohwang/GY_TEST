package kyo;

import org.apache.xmlbeans.impl.util.HexBin;

public class Node {
	
		/**
		 * 节点ID
		 */
		private byte[] id;
		/**
		 * 节点IP
		 */
		private String ip;
		/**
		 * 节点端口
		 */
		private int port;
		
		private long lastActive = System.currentTimeMillis();
		private int triedTimes = 0;
				
		public byte[] getId() {
			return id;
		}
		public String getIp() {
			return ip;
		}
		public int getPort() {
			return port;
		}
		public void setId(byte[] id) {
			this.id = id;
		}
		public void setIp(String ip) {
			this.ip = ip;
		}
		public void setPort(int port) {
			this.port = port;
		}
		public Node() {
		}
		public Node(byte[] id, String ip, int port) {
			super();
			this.id = id;
			this.ip = ip;
			this.port = port;
		}
		/**ID必须完全一致
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			Node node = (Node)obj;
			for(int i = 0; i < id.length; i++){
				if(id[i] != node.id[i]){
					return false;
				}
			}
			return true;
		}
		
		/**
		 *  ID匹配
		 * @param nid
		 * @return
		 */
		public boolean match(byte[] nid) {
			for(int i = 0; i < id.length; i++){
				if(id[i] != nid[i]){
					return false;
				}
			}
			return true;
		}
		
		public static boolean match(byte[] id1, byte[] id2){
			for(int i = 0; i < id1.length; i++){
				if(id1[i] != id2[i]){
					return false;
				}
			}
			return true;
		}
		
		public String log(){
			StringBuilder sb = new StringBuilder();
			String CR = ",";
			sb.append(CR).append(HexBin.bytesToString(id))
			.append(CR).append(ip)
			.append(CR).append(port);
			return sb.toString();
		}
		
		/**
		 * 10分钟过期，需要重新检查是否活跃
		 * @return
		 */
		public boolean isOverdue(){
			if(System.currentTimeMillis() - this.lastActive > 1*60*1000){
				 Utils.ping(NodeServer.getLOCAL_ID(), this);
				 this.triedTimes++;
				 this.lastActive = System.currentTimeMillis();
			}
			return this.triedTimes >= 15;
		}
		public long getLastActive() {
			return lastActive;
		}
		public int getTriedTimes() {
			return triedTimes;
		}
		public void setLastActive(long lastActive) {
			this.lastActive = lastActive;
		}
		public void setTriedTimes(int triedTimes) {
			this.triedTimes = triedTimes;
		}
}
