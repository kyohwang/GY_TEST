package kyo.net;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.impl.util.HexBin;

import kyo.Node;
import kyo.NodeServer;
import kyo.Peer;
import kyo.Utils;
import kyo.Worker;

import com.turn.ttorrent.bcodec.BDecoder;
import com.turn.ttorrent.bcodec.BEValue;
import com.turn.ttorrent.bcodec.BEncoder;
import com.turn.ttorrent.bcodec.InvalidBEncodingException;

public class ClientHandler extends SimpleChannelInboundHandler<DatagramPacket> {
	private static Logger  log = Logger.getLogger(ClientHandler.class);
	
	private void pingReturn(Map<String, BEValue> r, DatagramPacket packet){
		try {
			BEValue id = r.get("id");
			Node node = new Node(id.getBytes(),packet.sender().getAddress().getHostAddress(),
					packet.sender().getPort());
			NodeServer.addNode(node);
			log.info("PING BACK: " + node.getIp() + " " + node.getPort());
		} catch (InvalidBEncodingException e) {
			e.printStackTrace();
		}
	}
	
	private void findNodeReturn(Map<String, BEValue> r, DatagramPacket packet, String t) throws Exception{
		log.info("FD BACK: " + packet.sender().getAddress() + " " + packet.sender().getPort());
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
			
//			NodeServer.addNode(node);
			Utils.ping(NodeServer.LOCAL_ID, node);
			NodeServer.checkFinishFindNode(t.split("_")[1], node);
		}
	}
	
	private void getPointReturn(Map<String, BEValue> r, DatagramPacket packet, String t) throws Exception{
		log.info("GP BACK: " + packet.sender().getAddress() + " " + packet.sender().getPort());
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
	        	log.info("Response by: " + p);
	        	if(p.containsKey("r")){
	        		String t = p.get("t").getString();
	        		Map<String, BEValue> r = p.get("r").getMap();
	        		if(t.startsWith("pn")){
	        			//ping return
	        			this.pingReturn(r, packet);
	        		}else if(t.startsWith("fd_")){
	        			//find point return
	        			this.findNodeReturn(r, packet, t);
	        		}else if(t.startsWith("gp_")){
	        			// get point
	        			this.getPointReturn(r, packet, t);
	        		}
	        	}else if(p.containsKey("q")){
	        		String q = p.get("q").getString();
	        		String t = p.get("t").getString();
	        		if(q.equals("ping")){
	        			log.info("PING: " + packet.sender().getAddress() + " " + packet.sender().getPort());
	        			//ping
	        			Map<String,BEValue> ping = new HashMap<String,BEValue>();
	        			ping.put("id", new BEValue(NodeServer.LOCAL_ID));
	        			this.send(ctx, packet.sender(), new BEValue(ping));
	        		}else if(q.equals("find_node")){
	        			log.info("FD : " + packet.sender().getAddress() + " " + packet.sender().getPort());
	        			//find_node
	        			Map<String, BEValue> a = p.get("a").getMap();
	        			byte[] id = a.get("id").getBytes();
	        			byte[] target = a.get("target").getBytes();
	        			List<Node> nodes = new ArrayList<Node>();
	        			if(Node.match(NodeServer.LOCAL_ID, target)){
	        				nodes.add(new Node(NodeServer.LOCAL_ID,NodeServer.LOCAL_IP,NodeServer.LOCAL_PORT));
	        			}else{
	        				nodes.addAll(NodeServer.getBucket().getNodes(4,target));
	        			}
	        			
	        			Map<String,BEValue> rst = new HashMap<String,BEValue>();
	        			rst.put("t", new BEValue(t));
	        			rst.put("y", new BEValue("r"));
	        	
	        			Map<String,BEValue> r = new HashMap<String,BEValue>();
	        		
	        		/*	ByteArrayInputStream in = new ByteArrayInputStream(nodeBytes);
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
	        			}*/
	        			
	        			ByteArrayOutputStream out = new ByteArrayOutputStream();
	        			for(Node node : nodes){
	        				out.write(node.getId());
	        				String[] ls = node.getIp().split("\\.");
	        				for(String l : ls){
	        					Integer value = Integer.parseInt(l);
	        					out.write(value.byteValue());
	        				}
	        				byte p1= (byte) (node.getPort()>>8);
	        				byte p2= (byte) (node.getPort());
	        				out.write(p1);
	        				out.write(p2);
	        			}
	        			r.put("nodes", new BEValue(out.toByteArray()));
	        			r.put("id", new BEValue(NodeServer.LOCAL_ID));
	        			rst.put("r", new BEValue(r));
	        			this.send(ctx, packet.sender(), new BEValue(rst));
	        			
	        		}else if(q.equals("get_peers")){
	        			//get_peers
	        			log.info("GP: " + packet.sender().getAddress() + " " + packet.sender().getPort());
	        			Map<String, BEValue> a = p.get("a").getMap();
	        			byte[] id = a.get("id").getBytes();
	        			byte[] infoHash = a.get("id").getBytes();
	        			
	        			log.info("GP INFOHASH:" + HexBin.bytesToString(infoHash));
	        			
	        			
	        		}else if(q.equals("announce_peer")){
	        			//announce_peer
	        			log.info("ANNOUNCE: " + packet.sender().getAddress() + " " + packet.sender().getPort());
	        		}
	        	}else{
	        		log.info("ERROR RESPONSE.");
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
	    
		 public void send(ChannelHandlerContext ctx, InetSocketAddress add, BEValue value) throws Exception{
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
			 	BEncoder.bencode(value, bos);
			 	byte[] ar = bos.toByteArray();
			 	ctx.writeAndFlush(new DatagramPacket(
			                Unpooled.copiedBuffer(ar),
			                add));
		}

	}
