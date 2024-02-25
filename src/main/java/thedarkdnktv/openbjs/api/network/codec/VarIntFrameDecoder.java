package thedarkdnktv.openbjs.api.network.codec;

import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.CorruptedFrameException;
import thedarkdnktv.openbjs.api.util.PacketBuf;

/** This class collecting all frames data to one Packet, reading Var Int (21-bit header of data lenght)
 * @author TheDarkDnKTv
 *
 */
public class VarIntFrameDecoder extends ByteToMessageDecoder {

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf buf, List<Object> out) throws Exception {
		buf.markReaderIndex();
		byte[] bytes = new byte[3];
		
		for (int i = 0; i < bytes.length; ++i) {
			if (!buf.isReadable()) { // Return is not enough bytes
				buf.resetReaderIndex();
				return;
			}
			
			bytes[i] = buf.readByte();
			
			// Check is we have next byte presenting lenght
			if (bytes[i] >= 0) {
				PacketBuf pBuf = new PacketBuf(Unpooled.wrappedBuffer(bytes));
				
				try {
					int varInt = pBuf.readVarInt();
					
					if (buf.readableBytes() >= varInt) {
						out.add(buf.readBytes(varInt));
						return;
					}
					
					buf.resetReaderIndex();
				} finally {
					pBuf.release();
				}
				
				return;
			}
		}
		
		throw new CorruptedFrameException("Var int wider than 21-bit");
	}
}
