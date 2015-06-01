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
	         .handler(new ClientHandler());
	        ch = b.bind(0).sync().channel();
	    } catch(Exception e) {
	        e.printStackTrace();
	    }
	}
	
	
	
@Override
	protected void finalize() throws Throwable {
		super.finalize();
		if(group != null){
			group.shutdownGracefully();
			group = null;
		}
	}



public void send(InetSocketAddress add, BEValue value) throws Exception{
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
}

}
