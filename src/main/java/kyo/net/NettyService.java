package kyo.net;

import java.net.InetAddress;
import java.net.InetSocketAddress;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import kyo.NodeServer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Deprecated
public class NettyService {
Logger log = LoggerFactory.getLogger(NettyService.class);

/*	EventLoopGroup bossGroup = new NioEventLoopGroup(); 
    EventLoopGroup workerGroup = new NioEventLoopGroup();*/

	EventLoopGroup group;

	public void shutdown(){
		if(group != null){
			group.shutdownGracefully();
		}
	}

	public void startup() throws Exception {
        try {
        	group = new NioEventLoopGroup();
            Bootstrap b = new Bootstrap();
            b.group(group)
             .channel(NioDatagramChannel.class)
             .handler(new ServerHandler());
            InetSocketAddress add = new InetSocketAddress("0.0.0.0", NodeServer.LOCAL_PORT);
            b.bind(/*NodeServer.LOCAL_PORT*/add).sync().channel();
            
            log.info("listen on : " +NodeServer.LOCAL_PORT);
        }catch(Exception e){
        	e.printStackTrace();
        	throw e;
        }
	}

}
