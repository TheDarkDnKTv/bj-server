package thedarkdnktv.openbjs.api.network.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.handler.codec.MessageToByteEncoder;
import thedarkdnktv.openbjs.api.util.PacketBuf;

/**
 * @author TheDarkDnKTv
 * @see VarIntFrameDecoder
 */
@Sharable
public class VarIntFrameEncoder extends MessageToByteEncoder<ByteBuf> {

	@Override
	protected void encode(ChannelHandlerContext ctx, ByteBuf msg, ByteBuf out) throws Exception {
		int bytesAvailable = msg.readableBytes();
		int varInt = PacketBuf.getVarIntSize(bytesAvailable);
		
		if (varInt > 3) {
			throw new IllegalArgumentException("Unable to fit " + varInt + " into " + 3);
		} else {
			PacketBuf buf = new PacketBuf(out);
			buf.ensureWritable(bytesAvailable + varInt);
			buf.writeVarInt(bytesAvailable);
			buf.writeBytes(msg, msg.readerIndex(), bytesAvailable);
		}
	}
}
