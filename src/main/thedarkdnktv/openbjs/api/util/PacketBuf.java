package thedarkdnktv.openbjs.api.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.channels.GatheringByteChannel;
import java.nio.channels.ScatteringByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import io.netty.util.ByteProcessor;

/**
 * @author TheDarkDnKTv
 *
 */
public class PacketBuf extends ByteBuf {
	
	private final ByteBuf b;
	
	public PacketBuf(ByteBuf wrapped) {
		this.b = wrapped;
	}
	
	
	public static int getVarIntSize(int input) {
		for (int i = 1; i < 5; ++i) {
			if ((input & -1 << i * 7) == 0) {
				return i;
			}
		}
		
		return 5;
	}
	
	public PacketBuf writeByteArray(byte[] array) {
		this.writeVarInt(array.length);
		this.writeBytes(array);
		return this;
	}
	
	public byte[] readByteArray() {
		return this.readByteArray(this.readableBytes());
	}
	
	public byte[] readByteArray(int maxLength) {
		int i = this.readVarInt();
		
		if (i > maxLength) {
			throw new DecoderException("ByteArray with size " + i + " is bigger than allowed " + maxLength);
		} else {
			byte[] abyte = new byte[i];
			this.readBytes(abyte);
			return abyte;
		}
	}
	
	public PacketBuf writeVarIntArray(int[] array) {
		this.writeVarInt(array.length);
		for (int i : array) {
			this.writeVarInt(i);
		}
		
		return this;
	}
	
	public int[] readVarIntArray() {
		return this.readVarIntArray(this.readableBytes());
	}
	
	public int[] readVarIntArray(int maxLength) {
		int i = this.readVarInt();
		
		if (i > maxLength) {
			throw new DecoderException("VarIntArray with size " + i + " is bigger than allowed " + maxLength);
		} else {
			int[] aint = new int[i];
			
			for (int j = 0; j < aint.length; ++j) {
				aint[j] = this.readVarInt();
			}
			
			return aint;
		}
	}
	
	public PacketBuf writeLongArray(long[] array) {
		this.writeVarInt(array.length);
		
		for (long i : array) {
			this.writeLong(i);
		}
		
		return this;
	}
	
	public <E extends Enum<E>> E readEnumValue(Class<E> enumClass) {
		return enumClass.getEnumConstants()[this.readVarInt()];
	}
	
	public PacketBuf writeEnumValue(Enum<?> value) {
		this.writeVarInt(value.ordinal());
		return this;
	}
	
	public int readVarInt() {
		int i = 0;
		int j = 0;
		
		while (true) {
			byte b = this.readByte();
			i |= (b & 127) << j++ * 7;
			
			if (j > 5) {
				throw new RuntimeException("VarInt too big");
			}
			
			if ((b & 128) != 128) {
				break;
			}
		}
		
		return i;
	}
	
	public PacketBuf writeVarInt(int input) {
		while ((input & -128) != 0) {
			this.writeByte(input & 127 | 128);
			input >>>= 7;
		}
		
		this.writeByte(input);
		return this;
	}
	
	public String readString(int maxLength) {
		int i = this.readVarInt();
		
		if (i > maxLength * 4) {
			throw new DecoderException("The received encoded string buffer length is longer than maximum allowed (" + i + " > " + maxLength * 4 + ")");
		} else if (i < 0) {
			throw new DecoderException("The received encoded string buffer length is less than zero! Weird string!");
		} else {
			String s = this.toString(this.readerIndex(), i, StandardCharsets.UTF_8);
			this.readerIndex(this.readerIndex() + i);
			if (s.length() > maxLength) {
				throw new DecoderException("The received string length is longer than maximum allowed (" + i + " > " + maxLength + ")");
			} else return s;
		}
		
	}
	
	public PacketBuf writeString(String string) {
		byte[] abyte = string.getBytes(StandardCharsets.UTF_8);
		
		if (abyte.length > 32767) {
			throw new EncoderException("String too big (was " + abyte.length + " bytes encoded, max " + 32767 + ")");
		} else {
			this.writeVarInt(abyte.length);
            this.writeBytes(abyte);
            return this;
		}
	} 
	
	public Date readTime() {
        return new Date(this.readLong());
    }
	
	public PacketBuf writeTime(Date time) {
		this.writeLong(time.getTime());
		return this;
	}
	
	@Override
	public int refCnt() {
		return b.refCnt();
	}

	@Override
	public boolean release() {
		return b.release();
	}

	@Override
	public boolean release(int decrement) {
		return b.release(decrement);
	}

	@Override
	public int capacity() {
		return b.capacity();
	}

	@Override
	public ByteBuf capacity(int newCapacity) {
		return b.capacity(newCapacity);
	}

	@Override
	public int maxCapacity() {
		return b.maxCapacity();
	}

	@Override
	public ByteBufAllocator alloc() {
		return b.alloc();
	}

	@Deprecated
	@Override
	public ByteOrder order() {
		return b.order();
	}

	@Deprecated
	@Override
	public ByteBuf order(ByteOrder endianness) {
		return b.order(endianness);
	}

	@Override
	public ByteBuf unwrap() {
		return b.unwrap();
	}

	@Override
	public boolean isDirect() {
		return b.isDirect();
	}

	@Override
	public boolean isReadOnly() {
		return b.isReadOnly();
	}

	@Override
	public ByteBuf asReadOnly() {
		return b.asReadOnly();
	}

	@Override
	public int readerIndex() {
		return b.readerIndex();
	}

	@Override
	public ByteBuf readerIndex(int readerIndex) {
		return b.readerIndex(readerIndex);
	}

	@Override
	public int writerIndex() {
		return b.writerIndex();
	}

	@Override
	public ByteBuf writerIndex(int writerIndex) {
		return b.writerIndex(writerIndex);
	}

	@Override
	public ByteBuf setIndex(int readerIndex, int writerIndex) {
		return b.setIndex(readerIndex, writerIndex);
	}

	@Override
	public int readableBytes() {
		return b.readableBytes();
	}

	@Override
	public int writableBytes() {
		return b.writableBytes();
	}

	@Override
	public int maxWritableBytes() {
		return b.maxWritableBytes();
	}

	@Override
	public boolean isReadable() {
		return b.isReadable();
	}

	@Override
	public boolean isReadable(int size) {
		return b.isReadable();
	}

	@Override
	public boolean isWritable() {
		return b.isWritable();
	}

	@Override
	public boolean isWritable(int size) {
		return b.isWritable(size);
	}

	@Override
	public ByteBuf clear() {
		return b.clear();
	}

	@Override
	public ByteBuf markReaderIndex() {
		return b.markReaderIndex();
	}

	@Override
	public ByteBuf resetReaderIndex() {
		return b.resetReaderIndex();
	}

	@Override
	public ByteBuf markWriterIndex() {
		return b.markWriterIndex();
	}

	@Override
	public ByteBuf resetWriterIndex() {
		return b.resetWriterIndex();
	}

	@Override
	public ByteBuf discardReadBytes() {
		return b.discardReadBytes();
	}

	@Override
	public ByteBuf discardSomeReadBytes() {
		return b.discardSomeReadBytes();
	}

	@Override
	public ByteBuf ensureWritable(int minWritableBytes) {
		return b.ensureWritable(minWritableBytes);
	}

	@Override
	public int ensureWritable(int minWritableBytes, boolean force) {
		return b.ensureWritable(minWritableBytes, force);
	}

	@Override
	public boolean getBoolean(int index) {
		return b.getBoolean(index);
	}

	@Override
	public byte getByte(int index) {
		return b.getByte(index);
	}

	@Override
	public short getUnsignedByte(int index) {
		return b.getUnsignedByte(index);
	}

	@Override
	public short getShort(int index) {
		return b.getShort(index);
	}

	@Override
	public short getShortLE(int index) {
		return b.getShortLE(index);
	}

	@Override
	public int getUnsignedShort(int index) {
		return b.getUnsignedShort(index);
	}

	@Override
	public int getUnsignedShortLE(int index) {
		return b.getUnsignedShortLE(index);
	}

	@Override
	public int getMedium(int index) {
		return b.getMedium(index);
	}

	@Override
	public int getMediumLE(int index) {
		return b.getMediumLE(index);
	}

	@Override
	public int getUnsignedMedium(int index) {
		return b.getUnsignedMedium(index);
	}

	@Override
	public int getUnsignedMediumLE(int index) {
		return b.getUnsignedMediumLE(index);
	}

	@Override
	public int getInt(int index) {
		return b.getInt(index);
	}

	@Override
	public int getIntLE(int index) {
		return b.getIntLE(index);
	}

	@Override
	public long getUnsignedInt(int index) {
		return b.getUnsignedInt(index);
	}

	@Override
	public long getUnsignedIntLE(int index) {
		return b.getUnsignedIntLE(index);
	}

	@Override
	public long getLong(int index) {
		return b.getLong(index);
	}

	@Override
	public long getLongLE(int index) {
		return b.getLongLE(index);
	}

	@Override
	public char getChar(int index) {
		return b.getChar(index);
	}

	@Override
	public float getFloat(int index) {
		return b.getFloat(index);
	}

	@Override
	public double getDouble(int index) {
		return b.getDouble(index);
	}

	@Override
	public ByteBuf getBytes(int index, ByteBuf dst) {
		return b.getBytes(index, dst);
	}

	@Override
	public ByteBuf getBytes(int index, ByteBuf dst, int length) {
		return b.getBytes(index, dst, length);
	}

	@Override
	public ByteBuf getBytes(int index, ByteBuf dst, int dstIndex, int length) {
		return  b.getBytes(index, dst, dstIndex, length);
	}

	@Override
	public ByteBuf getBytes(int index, byte[] dst) {
		return  b.getBytes(index, dst);
	}

	@Override
	public ByteBuf getBytes(int index, byte[] dst, int dstIndex, int length) {
		return  b.getBytes(index, dst, dstIndex, length);
	}

	@Override
	public ByteBuf getBytes(int index, ByteBuffer dst) {
		return  b.getBytes(index, dst);
	}

	@Override
	public ByteBuf getBytes(int index, OutputStream out, int length) throws IOException {
		return  b.getBytes(index, out, length);
	}

	@Override
	public int getBytes(int index, GatheringByteChannel out, int length) throws IOException {
		return b.getBytes(index, out, length);
	}

	@Override
	public int getBytes(int index, FileChannel out, long position, int length) throws IOException {
		return b.getBytes(index, out, position, length);
	}

	@Override
	public CharSequence getCharSequence(int index, int length, Charset charset) {
		return b.getCharSequence(index, length, charset);
	}

	@Override
	public ByteBuf setBoolean(int index, boolean value) {
		return b.setBoolean(index, value);
	}

	@Override
	public ByteBuf setByte(int index, int value) {
		return b.setByte(index, value);
	}

	@Override
	public ByteBuf setShort(int index, int value) {
		return b.setShort(index, value);
	}

	@Override
	public ByteBuf setShortLE(int index, int value) {
		return b.setShortLE(index, value);
	}

	@Override
	public ByteBuf setMedium(int index, int value) {
		return b.setMedium(index, value);
	}

	@Override
	public ByteBuf setMediumLE(int index, int value) {
		return b.setMediumLE(index, value);
	}

	@Override
	public ByteBuf setInt(int index, int value) {
		return b.setInt(index, value);
	}

	@Override
	public ByteBuf setIntLE(int index, int value) {
		return b.setIntLE(index, value);
	}

	@Override
	public ByteBuf setLong(int index, long value) {
		return b.setLong(index, value);
	}

	@Override
	public ByteBuf setLongLE(int index, long value) {
		return b.setLongLE(index, value);
	}

	@Override
	public ByteBuf setChar(int index, int value) {
		return b.setChar(index, value);
	}

	@Override
	public ByteBuf setFloat(int index, float value) {
		return b.setFloat(index, value);
	}

	@Override
	public ByteBuf setDouble(int index, double value) {
		return b.setDouble(index, value);
	}

	@Override
	public ByteBuf setBytes(int index, ByteBuf src) {
		return b.setBytes(index, src);
	}

	@Override
	public ByteBuf setBytes(int index, ByteBuf src, int length) {
		return b.setBytes(index, src, length);
	}

	@Override
	public ByteBuf setBytes(int index, ByteBuf src, int srcIndex, int length) {
		return b.setBytes(index, src, srcIndex, length);
	}

	@Override
	public ByteBuf setBytes(int index, byte[] src) {
		return b.setBytes(index, src);
	}

	@Override
	public ByteBuf setBytes(int index, byte[] src, int srcIndex, int length) {
		return b.setBytes(index, src, srcIndex, length);
	}

	@Override
	public ByteBuf setBytes(int index, ByteBuffer src) {
		return b.setBytes(index, src);
	}

	@Override
	public int setBytes(int index, InputStream in, int length) throws IOException {
		return b.setBytes(index, in, length);
	}

	@Override
	public int setBytes(int index, ScatteringByteChannel in, int length) throws IOException {
		return b.setBytes(index, in, length);
	}

	@Override
	public int setBytes(int index, FileChannel in, long position, int length) throws IOException {
		return b.setBytes(index, in, position, length);
	}

	@Override
	public ByteBuf setZero(int index, int length) {
		return b.setZero(index, length);
	}

	@Override
	public int setCharSequence(int index, CharSequence sequence, Charset charset) {
		return b.setCharSequence(index, sequence, charset);
	}

	@Override
	public boolean readBoolean() {
		return b.readBoolean();
	}

	@Override
	public byte readByte() {
		return b.readByte();
	}

	@Override
	public short readUnsignedByte() {
		return b.readUnsignedByte();
	}

	@Override
	public short readShort() {
		return b.readShort();
	}

	@Override
	public short readShortLE() {
		return b.readShortLE();
	}

	@Override
	public int readUnsignedShort() {
		return b.readUnsignedShort();
	}

	@Override
	public int readUnsignedShortLE() {
		return b.readUnsignedShortLE();
	}

	@Override
	public int readMedium() {
		return b.readMedium();
	}

	@Override
	public int readMediumLE() {
		return b.readMediumLE();
	}

	@Override
	public int readUnsignedMedium() {
		return b.readUnsignedMedium();
	}

	@Override
	public int readUnsignedMediumLE() {
		return b.readUnsignedMediumLE();
	}

	@Override
	public int readInt() {
		return b.readInt();
	}

	@Override
	public int readIntLE() {
		return b.readIntLE();
	}

	@Override
	public long readUnsignedInt() {
		return b.readUnsignedInt();
	}

	@Override
	public long readUnsignedIntLE() {
		return b.readUnsignedIntLE();
	}

	@Override
	public long readLong() {
		return b.readLong();
	}

	@Override
	public long readLongLE() {
		return b.readLongLE();
	}

	@Override
	public char readChar() {
		return b.readChar();
	}

	@Override
	public float readFloat() {
		return b.readFloat();
	}

	@Override
	public double readDouble() {
		return b.readDouble();
	}

	@Override
	public ByteBuf readBytes(int length) {
		return b.readBytes(length);
	}

	@Override
	public ByteBuf readSlice(int length) {
		return b.readSlice(length);
	}

	@Override
	public ByteBuf readRetainedSlice(int length) {
		return b.readRetainedSlice(length);
	}

	@Override
	public ByteBuf readBytes(ByteBuf dst) {
		return b.readBytes(dst);
	}

	@Override
	public ByteBuf readBytes(ByteBuf dst, int length) {
		return b.readBytes(dst, length);
	}

	@Override
	public ByteBuf readBytes(ByteBuf dst, int dstIndex, int length) {
		return b.readBytes(dst, dstIndex, length);
	}

	@Override
	public ByteBuf readBytes(byte[] dst) {
		return b.readBytes(dst);
	}

	@Override
	public ByteBuf readBytes(byte[] dst, int dstIndex, int length) {
		return b.readBytes(dst, dstIndex, length);
	}

	@Override
	public ByteBuf readBytes(ByteBuffer dst) {
		return b.readBytes(dst);
	}

	@Override
	public ByteBuf readBytes(OutputStream out, int length) throws IOException {
		return b.readBytes(out, length);
	}

	@Override
	public int readBytes(GatheringByteChannel out, int length) throws IOException {
		return b.readBytes(out, length);
	}

	@Override
	public CharSequence readCharSequence(int length, Charset charset) {
		return b.readCharSequence(length, charset);
	}

	@Override
	public int readBytes(FileChannel out, long position, int length) throws IOException {
		return b.readBytes(out, position, length);
	}

	@Override
	public ByteBuf skipBytes(int length) {
		return b.skipBytes(length);
	}

	@Override
	public ByteBuf writeBoolean(boolean value) {
		return b.writeBoolean(value);
	}

	@Override
	public ByteBuf writeByte(int value) {
		return b.writeByte(value);
	}

	@Override
	public ByteBuf writeShort(int value) {
		return b.writeShort(value);
	}

	@Override
	public ByteBuf writeShortLE(int value) {
		return b.writeShortLE(value);
	}

	@Override
	public ByteBuf writeMedium(int value) {
		return b.writeMedium(value);
	}

	@Override
	public ByteBuf writeMediumLE(int value) {
		return b.writeMediumLE(value);
	}

	@Override
	public ByteBuf writeInt(int value) {
		return b.writeInt(value);
	}

	@Override
	public ByteBuf writeIntLE(int value) {
		return b.writeIntLE(value);
	}

	@Override
	public ByteBuf writeLong(long value) {
		return b.writeLong(value);
	}

	@Override
	public ByteBuf writeLongLE(long value) {
		return b.writeLongLE(value);
	}

	@Override
	public ByteBuf writeChar(int value) {
		return b.writeChar(value);
	}

	@Override
	public ByteBuf writeFloat(float value) {
		return b.writeFloat(value);
	}

	@Override
	public ByteBuf writeDouble(double value) {
		return b.writeDouble(value);
	}

	@Override
	public ByteBuf writeBytes(ByteBuf src) {
		return b.writeBytes(src);
	}

	@Override
	public ByteBuf writeBytes(ByteBuf src, int length) {
		return b.writeBytes(src, length);
	}

	@Override
	public ByteBuf writeBytes(ByteBuf src, int srcIndex, int length) {
		return b.writeBytes(src, length);
	}

	@Override
	public ByteBuf writeBytes(byte[] src) {
		return b.writeBytes(src);
	}

	@Override
	public ByteBuf writeBytes(byte[] src, int srcIndex, int length) {
		return b.writeBytes(src, srcIndex, length);
	}

	@Override
	public ByteBuf writeBytes(ByteBuffer src) {
		return b.writeBytes(src);
	}

	@Override
	public int writeBytes(InputStream in, int length) throws IOException {
		return b.writeBytes(in, length);
	}

	@Override
	public int writeBytes(ScatteringByteChannel in, int length) throws IOException {
		return b.writeBytes(in, length);
	}

	@Override
	public int writeBytes(FileChannel in, long position, int length) throws IOException {
		return b.writeBytes(in, position, length);
	}

	@Override
	public ByteBuf writeZero(int length) {
		return b.writeZero(length);
	}

	@Override
	public int writeCharSequence(CharSequence sequence, Charset charset) {
		return b.writeCharSequence(sequence, charset);
	}

	@Override
	public int indexOf(int fromIndex, int toIndex, byte value) {
		return b.indexOf(fromIndex, toIndex, value);
	}

	@Override
	public int bytesBefore(byte value) {
		return b.bytesBefore(value);
	}

	@Override
	public int bytesBefore(int length, byte value) {
		return b.bytesBefore(length, value);
	}

	@Override
	public int bytesBefore(int index, int length, byte value) {
		return b.bytesBefore(index, length, value);
	}

	@Override
	public int forEachByte(ByteProcessor processor) {
		return b.forEachByte(processor);
	}

	@Override
	public int forEachByte(int index, int length, ByteProcessor processor) {
		return b.forEachByte(index, length, processor);
	}

	@Override
	public int forEachByteDesc(ByteProcessor processor) {
		return b.forEachByteDesc(processor);
	}

	@Override
	public int forEachByteDesc(int index, int length, ByteProcessor processor) {
		return b.forEachByteDesc(index, length, processor);
	}

	@Override
	public ByteBuf copy() {
		return b.copy();
	}

	@Override
	public ByteBuf copy(int index, int length) {
		return b.copy(index, length);
	}

	@Override
	public ByteBuf slice() {
		return b.slice();
	}

	@Override
	public ByteBuf retainedSlice() {
		return b.retainedSlice();
	}

	@Override
	public ByteBuf slice(int index, int length) {
		return b.slice(index, length);
	}

	@Override
	public ByteBuf retainedSlice(int index, int length) {
		return b.retainedSlice(index, length);
	}

	@Override
	public ByteBuf duplicate() {
		return b.duplicate();
	}

	@Override
	public ByteBuf retainedDuplicate() {
		return b.retainedDuplicate();
	}

	@Override
	public int nioBufferCount() {
		return b.nioBufferCount();
	}

	@Override
	public ByteBuffer nioBuffer() {
		return b.nioBuffer();
	}

	@Override
	public ByteBuffer nioBuffer(int index, int length) {
		return b.nioBuffer(index, length);
	}

	@Override
	public ByteBuffer internalNioBuffer(int index, int length) {
		return b.internalNioBuffer(index, length);
	}

	@Override
	public ByteBuffer[] nioBuffers() {
		return b.nioBuffers();
	}

	@Override
	public ByteBuffer[] nioBuffers(int index, int length) {
		return b.nioBuffers(index, length);
	}

	@Override
	public boolean hasArray() {
		return b.hasArray();
	}

	@Override
	public byte[] array() {
		return b.array();
	}

	@Override
	public int arrayOffset() {
		return b.arrayOffset();
	}

	@Override
	public boolean hasMemoryAddress() {
		return b.hasMemoryAddress();
	}

	@Override
	public long memoryAddress() {
		return b.memoryAddress();
	}

	@Override
	public String toString(Charset charset) {
		return b.toString(charset);
	}

	@Override
	public String toString(int index, int length, Charset charset) {
		return b.toString(index, length, charset);
	}

	@Override
	public int hashCode() {
		return b.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return b.equals(obj);
	}

	@Override
	public int compareTo(ByteBuf buffer) {
		return b.compareTo(buffer);
	}

	@Override
	public String toString() {
		return b.toString();
	}

	@Override
	public ByteBuf retain(int increment) {
		return b.retain(increment);
	}

	@Override
	public ByteBuf retain() {
		return b.retain();
	}

	@Override
	public ByteBuf touch() {
		return b.touch();
	}

	@Override
	public ByteBuf touch(Object hint) {
		return b.touch(hint);
	}

}
