/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.index.cindexstorage.io;

import java.io.UTFDataFormatException;

import org.eclipse.cdt.internal.core.index.cindexstorage.IncludeEntry;
import org.eclipse.cdt.internal.core.index.cindexstorage.Util;
import org.eclipse.cdt.internal.core.index.cindexstorage.WordEntry;

/**
 * Uses prefix coding on words, and gamma coding of document numbers differences.
 */
public class GammaCompressedIndexBlock extends IndexBlock {
	CodeByteStream writeCodeStream= new CodeByteStream();
	CodeByteStream readCodeStream;
	char[] prevWord= null;
	int offset= 0;

	public GammaCompressedIndexBlock(int blockSize) {
		super(blockSize);
		readCodeStream= new CodeByteStream(field.buffer());
	}
	/**
	 * @see IndexBlock#addEntry
	 */
	public boolean addEntry(WordEntry entry) {
		writeCodeStream.reset();
		encodeEntry(entry, prevWord, writeCodeStream);
		if (offset + writeCodeStream.byteLength() > this.blockSize - 2) {
			return false;
		}
		byte[] bytes= writeCodeStream.toByteArray();
		field.put(offset, bytes);
		offset += bytes.length;
		prevWord= entry.getWord();
		return true;
	}
	protected void encodeEntry(WordEntry entry, char[] prevWord, CodeByteStream codeStream) {
		char[] word= entry.getWord();
		int prefixLen= prevWord == null ? 0 : Util.prefixLength(prevWord, word);
		codeStream.writeByte(prefixLen);
		codeStream.writeUTF(word, prefixLen, word.length);
		int n= entry.getNumRefs();
		codeStream.writeGamma(n);
		//encode file references
		int prevRef= 0;
		for (int i= 0; i < n; ++i) {
			int ref= entry.getRef(i);
			if (ref <= prevRef)
				throw new IllegalArgumentException();
			codeStream.writeGamma(ref - prevRef);
			prevRef= ref;
		}
		//encode offsets
		//same number of offsets arrays as file references
		for (int i=0; i<n; i++){
		    int[]offsetArray = entry.getOffsets(i);
		    //write offset array length
		    codeStream.writeGamma(offsetArray.length);
			prevRef=0;
		    for (int j=0; j<offsetArray.length; j++){
		        int ref = offsetArray[j];
		        if (ref <= prevRef)
		            throw new IllegalArgumentException();
		        codeStream.writeGamma(ref - prevRef);
		        prevRef=ref;
		    }
		}
		//encode offset lengths
		for (int i=0; i<n; i++){
			int[] offsetLengthArray = entry.getOffsetLengths(i);
		    //write offset array length
		    codeStream.writeGamma(offsetLengthArray.length);
		    for (int j=0; j<offsetLengthArray.length; j++){
		        int ref = offsetLengthArray[j];
		        codeStream.writeGamma(ref);
		    }
		}
		//encode modifiers
		//number of modifiers same as number of files
		for (int i= 0; i < n; ++i) {
			int ref= entry.getModifiers(i);
			  if (ref <= 0)
		            throw new IllegalArgumentException();
			codeStream.writeGamma(ref);
		}
	}
	/**
	 * @see IndexBlock#addEntry
	 */
	public boolean addIncludeEntry(IncludeEntry entry) {
		writeCodeStream.reset();
		encodeEntry(entry, prevWord, writeCodeStream);
		if (offset + writeCodeStream.byteLength() > this.blockSize - 2) {
			return false;
		}
		byte[] bytes= writeCodeStream.toByteArray();
		field.put(offset, bytes);
		offset += bytes.length;
		prevWord= entry.getFile();
		return true;
	}
	/**
	 * @param entry
	 * @param prevWord
	 * @param writeCodeStream
	 */
	protected void encodeEntry(IncludeEntry entry, char[] prevWord, CodeByteStream codeStream) {
		char[] file= entry.getFile();
		int prefixLen= prevWord == null ? 0 : Util.prefixLength(prevWord, file);
		codeStream.writeByte(prefixLen);
		codeStream.writeUTF(file, prefixLen, file.length);
		int n= entry.getNumRefs();
		codeStream.writeGamma(n);
		int prevRef= 0;
		for (int i= 0; i < n; ++i) {
			int ref= entry.getRef(i);
			if (ref <= prevRef)
				throw new IllegalArgumentException();
			codeStream.writeGamma(ref - prevRef);
			prevRef= ref;
		}
		
	}
	/**
	 * @see IndexBlock#flush
	 */
	public void flush() {
		if (offset > 0) {
			field.putInt2(offset, 0);
			offset= 0;
			prevWord= null;
		}
	}
	/**
	 * @see IndexBlock#isEmpty
	 */
	public boolean isEmpty() {
		return offset == 0;
	}
	/**
	 * @see IndexBlock#nextEntry
	 */
	public boolean nextEntry(WordEntry entry) {
		try {
			readCodeStream.reset(field.buffer(), offset);
			int prefixLength= readCodeStream.readByte();
			char[] word= readCodeStream.readUTF();
			if (prevWord != null && prefixLength > 0) {
				char[] temp= new char[prefixLength + word.length];
				System.arraycopy(prevWord, 0, temp, 0, prefixLength);
				System.arraycopy(word, 0, temp, prefixLength, word.length);
				word= temp;
			}
			if (word.length == 0) {
				return false;
			}
			entry.reset(word);
			int n= readCodeStream.readGamma();
			int prevRef= 0;
			for (int i= 0; i < n; ++i) {
				int ref= prevRef + readCodeStream.readGamma();
				if (ref < prevRef)
					throw new InternalError();
				entry.addRef(ref);
				prevRef= ref;
			}
			
			
			for (int i=0; i<n; ++i) {
				int offsetArrayLength = readCodeStream.readGamma();
				int[] tempOffsetArray = new int[offsetArrayLength];
				prevRef=0;
				for (int j=0; j<offsetArrayLength; j++){
				    int ref = prevRef + readCodeStream.readGamma();
				    if (ref < prevRef)
				        throw new InternalError();
				    tempOffsetArray[j] = ref;
				    prevRef = ref;
				}
				entry.setOffsets(i, tempOffsetArray);
			}
			
			for (int i=0; i<n; ++i) {
				int offsetLengthArrayLength = readCodeStream.readGamma();
				int[] tempOffsetLengthArray = new int[offsetLengthArrayLength];
				for (int j=0; j<offsetLengthArrayLength; j++){
				    int ref = readCodeStream.readGamma();
					tempOffsetLengthArray[j] = ref;
				}
				entry.setOffsetLengths(i, tempOffsetLengthArray);
			}
			
			//read in modifiers
			for (int i= 0; i < n; ++i) {
				int ref= readCodeStream.readGamma();
				entry.setModifier(i,ref);
			}
	
			offset= readCodeStream.byteLength();
			prevWord= word;
			return true;
		} catch (UTFDataFormatException e) {
			return false;
		}
	}
	/**
	 * @see IndexBlock#nextEntry
	 */
	public boolean nextEntry(IncludeEntry entry) {
		try {
			readCodeStream.reset(field.buffer(), offset);
			int prefixLength= readCodeStream.readByte();
			char[] file= readCodeStream.readUTF();
			if (prevWord != null && prefixLength > 0) {
				char[] temp= new char[prefixLength + file.length];
				System.arraycopy(prevWord, 0, temp, 0, prefixLength);
				System.arraycopy(file, 0, temp, prefixLength, file.length);
				file= temp;
			}
			if (file.length == 0) {
				return false;
			}
			entry.reset(file);
			int n= readCodeStream.readGamma();
			int prevRef= 0;
			for (int i= 0; i < n; ++i) {
				int ref= prevRef + readCodeStream.readGamma();
				if (ref < prevRef)
					throw new InternalError();
				entry.addRef(ref);
				prevRef= ref;
			}
			offset= readCodeStream.byteLength();
			prevWord= file;
			return true;
		} catch (UTFDataFormatException e) {
			return false;
		}
	}
	/**
	 * @see IndexBlock#reset
	 */
	public void reset() {
		super.reset();
		offset= 0;
		prevWord= null;
	}
	
}

