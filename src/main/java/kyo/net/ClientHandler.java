package kyo.net;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;

import java.io.ByteArrayInputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import kyo.Node;
import kyo.NodeServer;
import kyo.Peer;
import kyo.Utils;
import kyo.Worker;

import com.turn.ttorrent.bcodec.BDecoder;
import com.turn.ttorrent.bcodec.BEValue;
import com.turn.ttorrent.bcodec.InvalidBEncodingException;

public class ClientHandler extends SimpleChannelInboundHandler<DatagramPacket> {
	
	
	private void pingReturn(Map<String, BEValue> r, DatagramPacket packet){
		try {
			BEValue id = r.get("id");
			Node node = new Node(id.getBytes(),packet.sender().getAddress().getHostAddress(),
					packet.sender().getPort());
			NodeServer.addNode(node);
		} catch (InvalidBEncodingException e) {
			e.printStackTrace();
		}
	}

	 @Override
	    public void channelRead0(ChannelHandlerContext ctx, DatagramPacket packet) throws Exception {
		 int length = packet.content().readableBytes();
	        ByteBuffer buffer;
	        if(length > 0){
	        	buffer = packet.content().nioBuffer();
	        	byte[] bytes = new byte[length];
	        	for(int i = 0; i < length; i++){
	        		bytes[i] = buffer.get(i);
	        	}
	        	Map<String, BEValue> p = null;
	        	try{
	        		p = BDecoder.bdecode(new ByteArrayInputStream(bytes)).getMap();
	        	}catch(Exception e){
	        		e.printStackTrace();
	        		return;
	        	}
//	        	System.out.println("Response by: " + packet.sender().toString());
	        	if(p.containsKey("t")){
	        		String t = p.get("t").getString();
	        		Map<String, BEValue> r = p.get("r").getMap();
	        		if(t.equals("pn")){
	        			//ping return
	        			this.pingReturn(r, packet);
	        		}else if(t.startsWith("fd_")){
	        			//find_node return
	        			byte[] nodeBytes = r.get("nodes").getBytes();
	        			ByteArrayInputStream in = new ByteArrayInputStream(nodeBytes);
	        			for(int i = 0; i*26 < nodeBytes.length; i++){
	        				byte[]  nodeId = new byte[20];
	        				in.read(nodeId, 0, 20);
	        				StringBuffer sb = new StringBuffer();
	        				sb.append(in.read()).append(".");
	        				sb.append(in.read()).append(".");
	        				sb.append(in.read()).append(".");
	        				sb.append(in.read());
	        				String ip = sb.toString();
	        				int port = (in.read() << 8)+ in.read();
	        				Node node = new Node(nodeId,ip,port);
	        				
	        				NodeServer.addNode(node);
	        				NodeServer.checkFinishFindNode(t.split("_")[1], node);
	        			}
	        			
	        		}else if(t.startsWith("gp_")){
	        			
	        			BEValue id = r.get("id");
	        			if(r.containsKey("values")){
	        				List<BEValue> values = r.get("values").getList();
	        				List<Peer> peers = new ArrayList<Peer>();
	        				for(BEValue value : values){
	        					byte[] vbs = value.getBytes();
	        					ByteArrayInputStream in = new ByteArrayInputStream(vbs);
	        					StringBuffer sb = new StringBuffer();
	        					sb.append(in.read()).append(".");
		        				sb.append(in.read()).append(".");
		        				sb.append(in.read()).append(".");
		        				sb.append(in.read());
		        				String ip = sb.toString();
		        				int port = (in.read() << 8)+ in.read();
		        				peers.add(new Peer(ip,port));
	        				}
	        				
	        				NodeServer.finishGetPeer(t.split("_")[1], peers);
	        			}else{
	        				byte[] nodeBytes = r.get("nodes").getBytes();
		        			ByteArrayInputStream in = new ByteArrayInputStream(nodeBytes);
	        				Worker worker =NodeServer.getWorker(t.split("_")[1]);
	        				if(worker == null) return;
		        			for(int i = 0; i*26 < nodeBytes.length; i++){
		        				byte[]  nodeId = new byte[20];
		        				in.read(nodeId, 0, 20);
		        				StringBuffer sb = new StringBuffer();
		        				sb.append(in.read()).append(".");
		        				sb.append(in.read()).append(".");
		        				sb.append(in.read()).append(".");
		        				sb.append(in.read());
		        				String ip = sb.toString();
		        				int port = (in.read() << 8)+ in.read();
		        				Node node = new Node(nodeId, ip, port);
		        				worker.add(node);
		        			}
		        			worker.goOn();
	        			}
	        			
	        		}
	        	}
	        }
	       
	 }

	    @Override
	    public void channelReadComplete(ChannelHandlerContext ctx) {
	        ctx.flush();
	    }

	    @Override
	    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
	        cause.printStackTrace();
	        ctx.close();
	    }

	}
