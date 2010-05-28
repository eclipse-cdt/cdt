/*******************************************************************************
 * Copyright (c) 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.internal.core.parser.scanner;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;

/**
 * Implementation of char array for a file referencing content via 
 * soft references.
 */
public class FileCharArray extends LazyCharArray {
	private static final String UTF8_CHARSET_NAME = "UTF-8"; //$NON-NLS-1$

	public static AbstractCharArray create(String fileName, String charSet, InputStream in) throws IOException {
		// no support for non-local files
		if (!(in instanceof FileInputStream)) {
			return null;
		}
		FileInputStream fis= (FileInputStream) in;
		if (!Charset.isSupported(charSet)) {
			charSet= System.getProperty("file.encoding"); //$NON-NLS-1$
		}
		FileChannel channel = fis.getChannel();
		final long lsize = channel.size();
		if (lsize < CHUNK_SIZE) {
			return decodeSmallFile(channel, (int) lsize, charSet);
		}

		return new FileCharArray(fileName, charSet);
	}
	
	private static AbstractCharArray decodeSmallFile(FileChannel channel, int lsize, String charSet) throws IOException {
		ByteBuffer byteBuffer = ByteBuffer.allocate(lsize);
		channel.read(byteBuffer);
		byteBuffer.flip();
		skipUTF8ByteOrderMark(byteBuffer, charSet);
		
		CharBuffer charBuffer = Charset.forName(charSet).decode(byteBuffer);
		char[] buf= extractChars(charBuffer);
		return new CharArray(buf);
	}

	private static void skipUTF8ByteOrderMark(ByteBuffer buf, String charset) {
		if (charset.equals(UTF8_CHARSET_NAME) && buf.remaining() >= 3) {
			int pos = buf.position();
			if (buf.get(pos) == (byte) 0xEF && buf.get(++pos) == (byte) 0xBB &&
					buf.get(++pos) == (byte) 0xBF) {
				buf.position(++pos);
			}
		}
	}
	
	private static char[] extractChars(CharBuffer charBuffer) {
		if (charBuffer.hasArray() && charBuffer.arrayOffset() == 0) {
			char[] buf = charBuffer.array();
			if (buf.length == charBuffer.remaining())
				return buf;
		}
		char[] buf = new char[charBuffer.remaining()];
		charBuffer.get(buf);
		return buf;
	}

	private String fFileName;
	private String fCharSet;
	private FileChannel fChannel;

	private FileCharArray(String fileName, String charSet) {
		fFileName= fileName;
		fCharSet= charSet;
	}

	@Override
	protected Chunk createChunk(int chunkOffset) {
		FileInputStream fis;
		try {
			fis = new FileInputStream(fFileName);
		} catch (FileNotFoundException e1) {
			// File has been deleted in the meantime
			return null;
		}
		fChannel= fis.getChannel();
		try {
			return super.createChunk(chunkOffset);
		} finally {
			fChannel= null;
			try {
				fis.close();
			} catch (IOException e) {
			}
		}
	}
	
	@Override
	protected char[] readChunkData(long fileOffset, long[] fileEndOffsetHolder) throws IOException {
		assert fChannel != null;
		final Charset charset = Charset.forName(fCharSet);
		final CharsetDecoder decoder = charset.newDecoder().onMalformedInput(CodingErrorAction.REPLACE)
				.onUnmappableCharacter(CodingErrorAction.REPLACE);

		int needBytes = 3 + (int) (CHUNK_SIZE * (double) decoder.averageCharsPerByte()); // avoid rounding errors.
		final ByteBuffer in = ByteBuffer.allocate(needBytes);
		final CharBuffer dest= CharBuffer.allocate(CHUNK_SIZE);

		boolean endOfInput= false;
		while (dest.position() < CHUNK_SIZE && !endOfInput) {
			fChannel.position(fileOffset);
			in.clear();
			int count= fChannel.read(in);
			if (count == -1) {
				break;
			}
			
			endOfInput= count < in.capacity();
			in.flip();
			if (fileOffset == 0) {
				skipUTF8ByteOrderMark(in, fCharSet);
			}
			decoder.decode(in, dest, endOfInput);
			fileOffset+= in.position();
		}
		fileEndOffsetHolder[0]= fileOffset;
		dest.flip();
		return extractChars(dest);
	}

	@Override
	protected void rereadChunkData(long fileOffset, long fileEndOffset, char[] dest) {
		FileInputStream fis;
		try {
			fis = new FileInputStream(fFileName);
		} catch (FileNotFoundException e1) {
			// File has been deleted in the meantime
			return;
		}
		try {
			FileChannel channel = fis.getChannel();
			decode(channel, fileOffset, fileEndOffset, CharBuffer.wrap(dest));
		} catch (IOException e) {
			// File cannot be read
		} finally {
			try {
				fis.close();
			} catch (IOException e) {
			}
		}
	}

	private void decode(FileChannel channel, long fileOffset, long fileEndOffset, CharBuffer dest) throws IOException {
		final Charset charset = Charset.forName(fCharSet);
		final CharsetDecoder decoder = charset.newDecoder().onMalformedInput(CodingErrorAction.REPLACE)
				.onUnmappableCharacter(CodingErrorAction.REPLACE);

		int needBytes = (int) (fileEndOffset - fileOffset);
		final ByteBuffer in = ByteBuffer.allocate(needBytes);

		channel.position(fileOffset);
		in.clear();
		channel.read(in);
		in.flip();
		if (fileOffset == 0) {
			skipUTF8ByteOrderMark(in, fCharSet);
		}
		decoder.decode(in, dest, true);
	}
}
