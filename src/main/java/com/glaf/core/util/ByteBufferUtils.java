/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.glaf.core.util;

import org.apache.commons.lang3.ArrayUtils;

import java.io.DataOutput;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.util.Arrays;

/**
 * Utility methods to make ByteBuffers less painful The following should
 * illustrate the different ways byte buffers can be used
 * 
 * public void testArrayOffet() {
 * 
 * byte[] b = "test_slice_array".getBytes(); ByteBuffer bb =
 * ByteBuffer.allocate(1024);
 * 
 * assert bb.position() == 0; assert bb.limit() == 1024; assert bb.capacity() ==
 * 1024;
 * 
 * bb.put(b);
 * 
 * assert bb.position() == b.length; assert bb.remaining() == bb.limit() -
 * bb.position();
 * 
 * ByteBuffer bb2 = bb.slice();
 * 
 * assert bb2.position() == 0;
 * 
 * //slice should begin at other buffers current position assert
 * bb2.arrayOffset() == bb.position();
 * 
 * //to match the position in the underlying array one needs to //track
 * arrayOffset assert bb2.limit()+bb2.arrayOffset() == bb.limit();
 * 
 * 
 * assert bb2.remaining() == bb.remaining();
 * 
 * }
 * 
 * }
 * 
 */
public class ByteBufferUtils {
	private static final ByteBuffer EMPTY_BYTE_BUFFER = ByteBuffer
			.wrap(ArrayUtils.EMPTY_BYTE_ARRAY);

	/**
	 * Decode a String representation.
	 * 
	 * @param buffer
	 *            a byte buffer holding the string representation
	 * @param position
	 *            the starting position in {@code buffer} to start decoding from
	 * @param length
	 *            the number of bytes from {@code buffer} to use
	 * @param charset
	 *            the String encoding charset
	 * @return the decoded string
	 */
	public static String string(ByteBuffer buffer, int position, int length,
			Charset charset) throws CharacterCodingException {
		ByteBuffer copy = buffer.duplicate();
		copy.position(position);
		copy.limit(copy.position() + length);
		return string(copy, charset);
	}

	/**
	 * Decode a String representation.
	 * 
	 * @param buffer
	 *            a byte buffer holding the string representation
	 * @param charset
	 *            the String encoding charset
	 * @return the decoded string
	 */
	private static String string(ByteBuffer buffer, Charset charset)
			throws CharacterCodingException {
		return charset.newDecoder().decode(buffer.duplicate()).toString();
	}

	/**
	 * You should almost never use this. Instead, use the write* methods to
	 * avoid copies.
	 */
	public static byte[] getArray(ByteBuffer buffer) {
		int length = buffer.remaining();

		if (buffer.hasArray()) {
			int boff = buffer.arrayOffset() + buffer.position();
			if (boff == 0 && length == buffer.array().length)
				return buffer.array();
			else
				return Arrays.copyOfRange(buffer.array(), boff, boff + length);
		}
		// else, DirectByteBuffer.get() is the fastest route
		byte[] bytes = new byte[length];
		buffer.duplicate().get(bytes);

		return bytes;
	}

	/**
	 * ByteBuffer adaptation of org.apache.commons.lang3.ArrayUtils.lastIndexOf
	 * method
	 * 
	 * @param buffer
	 *            the array to traverse for looking for the object, may be
	 *            <code>null</code>
	 * @param valueToFind
	 *            the value to find
	 * @param startIndex
	 *            the start index (i.e. BB position) to travers backwards from
	 * @return the last index (i.e. BB position) of the value within the array
	 *         [between buffer.position() and buffer.limit()]; <code>-1</code>
	 *         if not found.
	 */
	public static int lastIndexOf(ByteBuffer buffer, byte valueToFind,
			int startIndex) {
		assert buffer != null;

		if (startIndex < buffer.position()) {
			return -1;
		} else if (startIndex >= buffer.limit()) {
			startIndex = buffer.limit() - 1;
		}

		for (int i = startIndex; i >= buffer.position(); i--) {
			if (valueToFind == buffer.get(i))
				return i;
		}

		return -1;
	}

	/**
	 * Encode a String in a ByteBuffer using the provided charset.
	 * 
	 * @param s
	 *            the string to encode
	 * @param charset
	 *            the String encoding charset to use
	 * @return the encoded string
	 */
	public static ByteBuffer bytes(String s, Charset charset) {
		return ByteBuffer.wrap(s.getBytes(charset));
	}

	/**
	 * @return a new copy of the data in @param buffer USUALLY YOU SHOULD USE
	 *         ByteBuffer.duplicate() INSTEAD, which creates a new Buffer (so
	 *         you can mutate its position without affecting the original)
	 *         without copying the underlying array.
	 */
	public static ByteBuffer clone(ByteBuffer buffer) {
		assert buffer != null;

		if (buffer.remaining() == 0)
			return EMPTY_BYTE_BUFFER;

		ByteBuffer clone = ByteBuffer.allocate(buffer.remaining());

		if (buffer.hasArray()) {
			System.arraycopy(buffer.array(),
					buffer.arrayOffset() + buffer.position(), clone.array(), 0,
					buffer.remaining());
		} else {
			clone.put(buffer.duplicate());
			clone.flip();
		}

		return clone;
	}

	public static void arrayCopy(ByteBuffer buffer, int position, byte[] bytes,
			int offset, int length) {
		if (buffer.hasArray())
			System.arraycopy(buffer.array(), buffer.arrayOffset() + position,
					bytes, offset, length);
		else
			((ByteBuffer) buffer.duplicate().position(position)).get(bytes,
					offset, length);
	}

	/**
	 * Transfer bytes from one ByteBuffer to another. This function acts as
	 * System.arrayCopy() but for ByteBuffers.
	 * 
	 * @param src
	 *            the source ByteBuffer
	 * @param srcPos
	 *            starting position in the source ByteBuffer
	 * @param dst
	 *            the destination ByteBuffer
	 * @param dstPos
	 *            starting position in the destination ByteBuffer
	 * @param length
	 *            the number of bytes to copy
	 */
	public static void arrayCopy(ByteBuffer src, int srcPos, ByteBuffer dst,
			int dstPos, int length) {
		if (src.hasArray() && dst.hasArray()) {
			System.arraycopy(src.array(), src.arrayOffset() + srcPos,
					dst.array(), dst.arrayOffset() + dstPos, length);
		} else {
			if (src.limit() - srcPos < length || dst.limit() - dstPos < length)
				throw new IndexOutOfBoundsException();

			for (int i = 0; i < length; i++)

				dst.put(dstPos++, src.get(srcPos++));
		}
	}

	public static void writeWithLength(ByteBuffer bytes, DataOutput out)
			throws IOException {
		out.writeInt(bytes.remaining());
		write(bytes, out); // writing data bytes to output source
	}

	private static void write(ByteBuffer buffer, DataOutput out)
			throws IOException {
		if (buffer.hasArray()) {
			out.write(buffer.array(), buffer.arrayOffset() + buffer.position(),
					buffer.remaining());
		} else {
			for (int i = buffer.position(); i < buffer.limit(); i++) {
				out.writeByte(buffer.get(i));
			}
		}
	}

	/**
	 * Convert a byte buffer to an integer. Does not change the byte buffer
	 * position.
	 * 
	 * @param bytes
	 *            byte buffer to convert to integer
	 * @return int representation of the byte buffer
	 */
	public static int toInt(ByteBuffer bytes) {
		return bytes.getInt(bytes.position());
	}

	public static long toLong(ByteBuffer bytes) {
		return bytes.getLong(bytes.position());
	}

	public static float toFloat(ByteBuffer bytes) {
		return bytes.getFloat(bytes.position());
	}

	public static double toDouble(ByteBuffer bytes) {
		return bytes.getDouble(bytes.position());
	}

	public static ByteBuffer bytes(int i) {
		return ByteBuffer.allocate(4).putInt(0, i);
	}

	public static ByteBuffer bytes(long n) {
		return ByteBuffer.allocate(8).putLong(0, n);
	}

	public static ByteBuffer bytes(float f) {
		return ByteBuffer.allocate(4).putFloat(0, f);
	}

	public static ByteBuffer bytes(double d) {
		return ByteBuffer.allocate(8).putDouble(0, d);
	}

	public static InputStream inputStream(ByteBuffer bytes) {
		final ByteBuffer copy = bytes.duplicate();

		return new InputStream() {
			public int read() {
				if (!copy.hasRemaining())
					return -1;

				return copy.get() & 0xFF;
			}

			@Override
			public int read(byte[] bytes, int off, int len) {
				if (!copy.hasRemaining())
					return -1;

				len = Math.min(len, copy.remaining());
				copy.get(bytes, off, len);
				return len;
			}

			@Override
			public int available() {
				return copy.remaining();
			}
		};
	}

	public static String bytesToHex(ByteBuffer bytes) {
		final int offset = bytes.position();
		final int size = bytes.remaining();
		final char[] c = new char[size * 2];
		for (int i = 0; i < size; i++) {
			final int bint = bytes.get(i + offset);
			c[i * 2] = Hex.byteToChar[(bint & 0xf0) >> 4];
			c[1 + i * 2] = Hex.byteToChar[bint & 0x0f];
		}
		return Hex.wrapCharArray(c);
	}

	public static ByteBuffer hexToBytes(String str) {
		return ByteBuffer.wrap(Hex.hexToBytes(str));
	}

	/**
	 * Compare two ByteBuffer at specified offsets for length. Compares the non
	 * equal bytes as unsigned.
	 * 
	 * @param bytes1
	 *            First byte buffer to compare.
	 * @param offset1
	 *            Position to start the comparison at in the first array.
	 * @param bytes2
	 *            Second byte buffer to compare.
	 * @param offset2
	 *            Position to start the comparison at in the second array.
	 * @param length
	 *            How many bytes to compare?
	 * @return -1 if byte1 is less than byte2, 1 if byte2 is less than byte1 or
	 *         0 if equal.
	 */
	public static int compareSubArrays(ByteBuffer bytes1, int offset1,
			ByteBuffer bytes2, int offset2, int length) {
		if (null == bytes1) {
			if (null == bytes2)
				return 0;
			else
				return -1;
		}
		if (null == bytes2)
			return 1;

		assert bytes1.limit() >= offset1 + length : "The first byte array isn't long enough for the specified offset and length.";
		assert bytes2.limit() >= offset2 + length : "The second byte array isn't long enough for the specified offset and length.";
		for (int i = 0; i < length; i++) {
			byte byte1 = bytes1.get(offset1 + i);
			byte byte2 = bytes2.get(offset2 + i);
			if (byte1 == byte2)
				continue;
			// compare non-equal bytes as unsigned
			return (byte1 & 0xFF) < (byte2 & 0xFF) ? -1 : 1;
		}
		return 0;
	}

	/**
	 * 分配flush模式的ByteBuffer，limit和position都为0,在写入数据时，必须先翻为fill模式
	 * 
	 * @return
	 */
	private static ByteBuffer allocate() {
		ByteBuffer buff = ByteBuffer.allocate(8192);
		buff.limit(0);
		return buff;
	}

	public static ByteBuffer allocateDirect(int capacity) {
		ByteBuffer buff = ByteBuffer.allocateDirect(capacity);
		buff.limit(0);
		return buff;
	}

	/**
	 * 清空buffer, 只需把positoin 和limit 同时置为0
	 * 
	 * @param buffer
	 */
	public static void clear(ByteBuffer buffer) {
		if (buffer != null) {
			buffer.position(0);
			buffer.limit(0);
		}
	}

	/**
	 * 清空buffer, 置为fill模式
	 * 
	 * @param buffer
	 */
	public static void clearToFill(ByteBuffer buffer) {
		if (buffer != null) {
			buffer.position(0);
			buffer.limit(buffer.capacity());
		}
	}

	/**
	 * 翻转为fill模式
	 * 
	 * @param buffer
	 * @return
	 */
	private static int flipToFill(ByteBuffer buffer) {
		int position = buffer.position();
		int limit = buffer.limit();
		// 说明正好flush完，可以完全转换未fill模式
		if (position == limit) {
			buffer.position(0);
			buffer.limit(buffer.capacity());
			return 0;
		}
		// 当前limit equal capacity,另申请空间
		int capacity = buffer.capacity();
		if (limit == capacity) {
			buffer.compact();
			return 0;
		}
		// 一般情况，剩余的容量，可写的空间
		buffer.position(limit);
		buffer.limit(capacity);
		return position;
	}

	/**
	 * 转为flush模式，即读模式，把当前写到的位置至为limit,动态传入读开始位置position, 如果position未0 ,该方法的作用和
	 * ByteBuffer.flip()的作用等价
	 * 
	 * @param buffer
	 * @param position
	 */
	private static void flipToFlush(ByteBuffer buffer, int position) {
		buffer.limit(buffer.position());
		buffer.position(position);
	}

	/**
	 * 把buffer转换为数组。
	 * 
	 * @param buffer
	 * @return
	 */
	public static byte[] toArray(ByteBuffer buffer) {
		// 主要针对heap buffer
		if (buffer.hasArray()) {
			byte[] array = buffer.array();
			int from = buffer.arrayOffset() + buffer.position();
			return Arrays.copyOfRange(array, from, from + buffer.remaining());
		}
		// 针对 direct buffer
		else {
			byte[] to = new byte[buffer.remaining()];
			buffer.slice().get(to);
			return to;
		}
	}

	/**
	 * 是否为空 ，remaining() 主要是 limit - position
	 * 
	 * @param buff
	 * @return
	 */
	public static boolean isEmpty(ByteBuffer buff) {
		return buff == null || buff.remaining() == 0;
	}

	public static boolean hasContent(ByteBuffer buff) {
		return buff != null && buff.remaining() > 0;
	}

	public static boolean isFull(ByteBuffer buff) {
		return buff != null && buff.limit() == buff.capacity();
	}

	public static int length(ByteBuffer buffer) {
		return buffer == null ? 0 : buffer.remaining();
	}

	public static int space(ByteBuffer buffer) {
		if (buffer == null) {
			return 0;
		}
		return buffer.capacity() - buffer.limit();
	}

	public static boolean compact(ByteBuffer buffer) {
		if (buffer.position() == 0) {
			return false;
		}
		boolean full = buffer.limit() == buffer.capacity();
		buffer.compact().flip();
		return full && buffer.limit() < buffer.capacity();
	}

	/**
	 * 把from中未读的，写到 to 中
	 * 
	 * @param fromBuffer
	 *            Buffer 读模式 flush
	 * @param toBuffer
	 *            Buffer 写模式 fill
	 * @return number of bytes moved
	 */
	public static int put(ByteBuffer fromBuffer, ByteBuffer toBuffer) {
		int put;
		int remaining = fromBuffer.remaining();
		if (remaining > 0) { // 如果空间足够，直接写入
			if (remaining <= toBuffer.remaining()) {
				toBuffer.put(fromBuffer);
				put = remaining;
				// 把from 读完
				fromBuffer.position(fromBuffer.limit());
			}
			// heap buffer
			else if (fromBuffer.hasArray()) {
				put = toBuffer.remaining();
				// 只读部分数据
				toBuffer.put(fromBuffer.array(), fromBuffer.arrayOffset()
						+ fromBuffer.position(), put);
				fromBuffer.position(fromBuffer.position() + put);
			}
			// direct buffer
			else {
				// 只读部分数据
				put = toBuffer.remaining();
				ByteBuffer slice = fromBuffer.slice();
				slice.limit(put);
				toBuffer.put(slice);
				fromBuffer.position(fromBuffer.position() + put);
			}
		} else {
			put = 0;
		}
		return put;
	}

	/**
	 * 添加 byte[] 到buffer中
	 * 
	 * @param toBuffer
	 * @param bytes
	 * @param offset
	 * @param len
	 */
	public static void append(ByteBuffer toBuffer, byte[] bytes, int offset,
			int len) { // 置为写模式
		int pos = flipToFill(toBuffer);
		try {
			toBuffer.put(bytes, offset, len);
		} finally {
			// 置为读模式
			flipToFlush(toBuffer, pos);
		}
	}

	public static void readFrom(InputStream is, int needed, ByteBuffer buffer)
			throws IOException {
		ByteBuffer tmp = allocate();
		while (needed > 0 && buffer.hasRemaining()) {
			int l = is.read(tmp.array(), 0, 8192);
			if (l < 0) {
				break;
			}
			tmp.position(0);
			tmp.limit(l);
			buffer.put(tmp);
		}
	}

}