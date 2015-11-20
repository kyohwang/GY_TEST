package kyo.net;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import kyo.NodeServer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

            b.bind(NodeServer.LOCAL_PORT).sync().channel();
            
            log.info("listen on : " +NodeServer.LOCAL_PORT);
        }catch(Exception e){
        	e.printStackTrace();
        	throw e;
        }
	}

}
