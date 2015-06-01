package kyo;

public class Node {
	
		private byte[] id;
		private String ip;
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
		
		public boolean match(byte[] nid) {
			for(int i = 0; i < id.length; i++){
				if(id[i] != nid[i]){
					return false;
				}
			}
			return true;
		}
		
		
		
}
