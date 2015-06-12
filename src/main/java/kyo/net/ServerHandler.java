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

import kyo.Node;
import kyo.NodeServer;

import com.turn.ttorrent.bcodec.BDecoder;
import com.turn.ttorrent.bcodec.BEValue;
import com.turn.ttorrent.bcodec.BEncoder;

public class ServerHandler extends SimpleChannelInboundHandler<DatagramPacket> {

	 @Override
	    public void channelRead0(ChannelHandlerContext ctx, DatagramPacket packet) throws Exception {
	        System.err.println(packet);
	        /*if ("QOTM?".equals(packet.content().toString(CharsetUtil.UTF_8))) {
	            ctx.write(new DatagramPacket(
	                    Unpooled.copiedBuffer("QOTM: " + nextQuote(), CharsetUtil.UTF_8), packet.sender()));
	        }*/
	        int length = packet.content().readableBytes();
	        ByteBuffer buffer;
	        if(length > 0){
	        	buffer = packet.content().nioBuffer();
	        	byte[] bytes = new byte[length];
	        	for(int i = 0; i < length; i++){
	        		bytes[i] = buffer.get(i);
	        	}
	        	Map<String, BEValue> p = BDecoder.bdecode(new ByteArrayInputStream(bytes)).getMap();
	        	
	        	if(p.containsKey("q")){
	        		String q = p.get("q").getString();
	        		if(q.equals("ping")){
	        			//ping
	        			Map<String,BEValue> ping = new HashMap<String,BEValue>();
	        			ping.put("id", new BEValue(NodeServer.id));
	        			this.send(ctx, packet.sender(), new BEValue(ping));
	        		}else if(q.equals("find_node")){
	        			//find_node
	        			Map<String, BEValue> a = p.get("a").getMap();
	        			byte[] id = a.get("id").getBytes();
	        			byte[] target = a.get("target").getBytes();
	        			List<Node> nodes = new ArrayList<Node>();
	        			if(Node.match(NodeServer.id, target)){
	        				nodes.add(new Node())
	        			}else{
	        				
	        			}
	        			
	        		}else if(q.equals("get_peers")){
	        			//get_peers
	        			
	        		}else if(q.equals("announce_peer")){
	        			//announce_peer
	        			
	        		}
	        	}
	        	
	        	ctx.write(new DatagramPacket(Unpooled.copiedBuffer("d1:rd2:id20:mnopqrstuvwxyz123456e1:t2:aa1:y1:re".getBytes("utf-8")), packet.sender()));
	        }
	        	
	 }
	 
	 public void send(ChannelHandlerContext ctx, InetSocketAddress add, BEValue value) throws Exception{
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
		 	BEncoder.bencode(value, bos);
		 	byte[] ar = bos.toByteArray();
		 	ctx.writeAndFlush(new DatagramPacket(
		                Unpooled.copiedBuffer(ar),
		                add));
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
