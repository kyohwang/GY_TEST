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

import org.apache.xmlbeans.impl.util.HexBin;

import kyo.Bucket;
import kyo.NodeServer;

import com.turn.ttorrent.bcodec.BEValue;
import com.turn.ttorrent.bcodec.BEncoder;

public class UdpSender {

	public byte[]  LOCAL_ID;
	public int LOCAL_PORT;
	public Bucket bucket;
	
	 EventLoopGroup group = new NioEventLoopGroup();
	 Channel ch = null;

	 
	 public UdpSender(String id, String port){
		try {
			this.LOCAL_ID = HexBin.stringToBytes(id);
			this.LOCAL_PORT = Integer.parseInt(port);
			this.bucket = new Bucket(LOCAL_ID);
			
	        Bootstrap b = new Bootstrap();
	        b.group(group)
	         .channel(NioDatagramChannel.class)
	         .option(ChannelOption.SO_BROADCAST, true)
	         .option(ChannelOption.SO_REUSEADDR, true)
	         .option(ChannelOption.SO_RCVBUF, 1024*1024*100)
	         .option(ChannelOption.SO_SNDBUF, 1024*1024*100)
	         .handler(new ClientHandler(LOCAL_PORT));
	        ch = b.bind(LOCAL_PORT).sync().channel();
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
	if(NodeServer.blackList.contains(add.getAddress().getHostAddress())){
 		return;
 	}
	if(add.getAddress().getHostAddress().contains(NodeServer.LOCAL_IP)){
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
			         .handler(new ClientHandler(this.LOCAL_PORT));
			        ch = b.bind(LOCAL_PORT).sync().channel();
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

	 public static void main(String[] args){
		 byte[] bytes = HexBin.stringToBytes("327254EA20EF4C7C0CC2C2649FB3610A5E076952");
		 for(int i = 0; i < 256; i++){
			 bytes[0] = (byte) i;
			 for(int r = 1; r < bytes.length; r++){
				 bytes[r] = (byte) (Math.random()*256);
			 }
			 System.out.println(6000+i +","+HexBin.bytesToString(bytes));
		 }
	 }

}
