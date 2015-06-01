package kyo.net;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.io.ByteArrayInputStream;
import java.util.List;

import com.turn.ttorrent.bcodec.BDecoder;

public class BeValueDecoder extends ByteToMessageDecoder {

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in,
			List<Object> out) throws Exception {
		
		byte[] bytes = new byte[in.readableBytes()];
		in.readBytes(bytes);
		out.add(BDecoder.bdecode(new ByteArrayInputStream(bytes)));
	}

}
