package kyo.net;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import java.io.ByteArrayOutputStream;

import com.turn.ttorrent.bcodec.BEValue;
import com.turn.ttorrent.bcodec.BEncoder;

public class BeValueEncoder extends MessageToByteEncoder<BEValue> {

	 @Override
	    protected void encode(
	            ChannelHandlerContext ctx, BEValue msg, ByteBuf out) throws Exception {
	        
//	        out = out.order(ByteOrder.BIG_ENDIAN);
     	ByteArrayOutputStream bos = new ByteArrayOutputStream();
     	BEncoder.bencode(msg, bos);
     	byte[] ar = bos.toByteArray();
     	out.writeBytes(ar);
	    }
}
