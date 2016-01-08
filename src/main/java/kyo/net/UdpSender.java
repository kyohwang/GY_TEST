package kyo.net;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;

import java.io.ByteArrayOutputStream;
import java.net.InetSocketAddress;

import kyo.NodeServer;

import com.turn.ttorrent.bcodec.BEValue;
import com.turn.ttorrent.bcodec.BEncoder;

public class UdpSender {
	 EventLoopGroup group = new NioEventLoopGroup();
	 Channel ch = null;
	 
	 public UdpSender(){
		try {
	        Bootstrap b = new Bootstrap();
	        b.group(group)
	         .channel(NioDatagramChannel.class)
	         .option(ChannelOption.SO_BROADCAST, true)
	         .option(ChannelOption.SO_REUSEADDR, true)
	         .option(ChannelOption.SO_RCVBUF, 1024*1024*100)
	         .option(ChannelOption.SO_SNDBUF, 1024*1024*100)
	         .handler(new ClientHandler());
	        ch = b.bind(NodeServer.LOCAL_PORT).sync().channel();
	    } catch(Exception e) {
	        e.printStackTrace();
	    }
	}
	
	public void shutdown(){
		if(group != null){
			group.shutdownGracefully();
			group = null;
		}
	}
	
@Override
	protected void finalize() throws Throwable {
		super.finalize();
	}



public void send(InetSocketAddress add, BEValue value) throws Exception{
	if(NodeServer.blackList.contains(add.getHostString())){
 		return;
 	}
	if(add.getHostString().contains(NodeServer.LOCAL_IP)){
		return;
	}
	if(!ch.isActive() || !ch.isOpen() || !ch.isRegistered() || !ch.isWritable()){
		synchronized(UdpSender.class){
			if(!ch.isActive() || !ch.isOpen() || !ch.isRegistered() || !ch.isWritable()){
				try {
			        Bootstrap b = new Bootstrap();
			        b.group(group)
			         .channel(NioDatagramChannel.class)
			         .option(ChannelOption.SO_BROADCAST, true)
			         .option(ChannelOption.SO_REUSEADDR, true)
			         .option(ChannelOption.TCP_NODELAY, true)
			         .option(ChannelOption.SO_RCVBUF, 1024*1024*100)
			         .option(ChannelOption.SO_SNDBUF, 1024*1024*100)
			         .handler(new ClientHandler());
			        ch = b.bind(NodeServer.LOCAL_PORT).sync().channel();
			    } catch(Exception e) {
			        e.printStackTrace();
			    }
			}
		}
	}
	
	try{
	ByteArrayOutputStream bos = new ByteArrayOutputStream();
 	BEncoder.bencode(value, bos);
 	byte[] ar = bos.toByteArray();
 	
        ch.writeAndFlush(new DatagramPacket(
                Unpooled.copiedBuffer(ar),
                add));
        
     /*   if (!ch.closeFuture().await(5000)) {
            System.err.println("send timed out.");
        }else{
        	System.out.println("send ok.");
        }*/
	}catch(Exception e){
		e.printStackTrace();
	}
}

}
