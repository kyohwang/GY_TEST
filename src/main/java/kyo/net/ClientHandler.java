package kyo.net;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;

import java.io.ByteArrayInputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import kyo.Node;
import kyo.Utils;
import kyo.WorkList;

import com.turn.ttorrent.bcodec.BDecoder;
import com.turn.ttorrent.bcodec.BEValue;

public class ClientHandler extends SimpleChannelInboundHandler<DatagramPacket> {

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
	        			BEValue id = r.get("id");
	        			System.out.println("ping return:" + Arrays.toString(id.getBytes()));
	        			
//	        			System.out.println(packet.sender().getAddress().getHostAddress());
//	        			System.out.println(packet.sender().getPort());
	        			Node node = new Node(id.getBytes(),packet.sender().getAddress().getHostAddress(),
	        					packet.sender().getPort());
	        			Utils.bucket.add(node);
	        			
	        		}else if(t.equals("fd")){
	        			
	        			byte[] nodeBytes = r.get("nodes").getBytes();
	        			ByteArrayInputStream in = new ByteArrayInputStream(nodeBytes);
	        			for(int i = 0; i*26 < nodeBytes.length; i++){
	        				byte[]  nodeId = new byte[20];
	        				in.read(nodeId, 0, 20);
	        				StringBuffer sb = new StringBuffer();
	        				sb.append(/*nodeBytes[i*26+20]*/in.read()).append(".");
	        				sb.append(/*nodeBytes[i*26+21]*/in.read()).append(".");
	        				sb.append(/*nodeBytes[i*26+22]*/in.read()).append(".");
	        				sb.append(/*nodeBytes[i*26+23]*/in.read());
	        				String ip = sb.toString();
	        				int port = (/*nodeBytes[i*26+24]*/in.read() << 8)+ in.read()/*nodeBytes[i*26+25]*/;
//	        				System.out.println(Arrays.toString(nodeId));
//	        				System.out.println(ip + ":" + port);
	        				Utils.nodes.put(ip, port);
	        			}
	        			
	        		}else if(t.startsWith("gp")){
	        			
	        			BEValue id = r.get("id");
	        			
	        			if(r.containsKey("values")){
	        				Utils.works.remove(t);
	        				List<BEValue> values = r.get("values").getList();
	        				System.err.println("gp done!!!");
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
		        				System.out.println(ip + ":" + port);
	        				}
	        			}else{
	        				byte[] nodeBytes = r.get("nodes").getBytes();
		        			ByteArrayInputStream in = new ByteArrayInputStream(nodeBytes);
	        				WorkList list = Utils.works.get(t);
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
		        				Utils.nodes.put(ip, port);
		        				Node node = new Node(nodeId, ip, port);
		        				if(list != null){
		        					list.add(node);
		        				}
		        			}
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
