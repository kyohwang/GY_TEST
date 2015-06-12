package kyo;

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
		
		
		
}
