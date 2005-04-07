/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.index.cindexstorage.io;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import org.eclipse.cdt.internal.core.index.cindexstorage.ICIndexStorageConstants;
import org.eclipse.cdt.internal.core.index.cindexstorage.IncludeEntry;
import org.eclipse.cdt.internal.core.index.cindexstorage.IndexedFileEntry;
import org.eclipse.cdt.internal.core.index.cindexstorage.WordEntry;

/**
 * A blocksIndexOutput is used to save an index in a file with the given structure:<br>
 *  - Signature of the file;<br>
 *  - FileListBlocks;<br>
 *  - IndexBlocks;<br>
 *  - Summary of the index.
 */

public class BlocksIndexOutput extends IndexOutput {

	protected RandomAccessFile indexOut;
	protected int blockNum;
	protected boolean opened= false;
	protected File indexFile;
	
	protected FileListBlock fileListBlock;
	protected IndexBlock indexBlock;
	protected IndexBlock includeIndexBlock;
	
	protected int numWords= 0;
	protected int numFiles= 0;
	protected int numIncludes= 0;
	protected IndexSummary summary;
	
	protected boolean firstInBlock;
	protected boolean firstIndexBlock;
	protected boolean firstFileListBlock;
	protected boolean firstIncludeIndexBlock;

	public BlocksIndexOutput(File indexFile) {
		this.indexFile= indexFile;
		summary= new IndexSummary();
		blockNum= 1;
		firstInBlock= true;
		firstIndexBlock= true;
		firstFileListBlock= true;
		firstIncludeIndexBlock=true;
	}
	/**
	 * @see IndexOutput#addFile
	 */
	public void addFile(IndexedFileEntry indexedFile) throws IOException {
		if (firstFileListBlock) {
			firstInBlock= true;
			fileListBlock= new FileListBlock(ICIndexStorageConstants.BLOCK_SIZE);
			firstFileListBlock= false;
		}
		if (fileListBlock.addFile(indexedFile)) {
			if (firstInBlock) {
				summary.addFirstFileInBlock(indexedFile, blockNum);
				firstInBlock= false;
			}
			numFiles++;
		} else {
			if (fileListBlock.isEmpty()) {
				return;
			}
			flushFiles();
			addFile(indexedFile);
		}
	}
	/**
	 * @see IndexOutput#addWord
	 */
	public void addWord(WordEntry entry) throws IOException {
		if (firstIndexBlock) {
			indexBlock= new GammaCompressedIndexBlock(ICIndexStorageConstants.BLOCK_SIZE);
			firstInBlock= true;
			firstIndexBlock= false;
		}
		if (entry.getNumRefs() == 0)
			return;
		if (indexBlock.addEntry(entry)) {
			if (firstInBlock) {
				summary.addFirstWordInBlock(entry.getWord(), blockNum);
				firstInBlock= false;
			}
			numWords++;
		} else {
			if (indexBlock.isEmpty()) {
				return;
			}
			flushWords();
			addWord(entry);
		}
	}
	/**
	 * @see IndexOutput#addInclude
	 */
	public void addInclude(IncludeEntry entry) throws IOException {
		if (firstIncludeIndexBlock) {
			includeIndexBlock= new GammaCompressedIndexBlock(ICIndexStorageConstants.BLOCK_SIZE);
			firstInBlock= true;
			firstIncludeIndexBlock= false;
		}
		if (entry.getNumRefs() == 0)
			return;
		if (includeIndexBlock.addIncludeEntry(entry)) {
			if (firstInBlock) {
				summary.addFirstIncludeInBlock(entry.getFile(), blockNum);
				firstInBlock= false;
			}
			numIncludes++;
		} else {
			if (includeIndexBlock.isEmpty()) {
				return;
			}
			flushIncludes();
			addInclude(entry);
		}
	}
	/**
	 * @see IndexOutput#close
	 */
	public void close() throws IOException {
		if (opened) {
			indexOut.close();
			summary= null;
			numFiles= 0;
			opened= false;
		}
	}
	/**
	 * @see IndexOutput#flush
	 */
	public void flush() throws IOException {
		
		summary.setNumFiles(numFiles);
		summary.setNumWords(numWords);
		summary.setNumIncludes(numIncludes);
		indexOut.seek(blockNum * (long) ICIndexStorageConstants.BLOCK_SIZE);
		summary.write(indexOut);
		indexOut.seek(0);
		indexOut.writeUTF(ICIndexStorageConstants.SIGNATURE);
		indexOut.writeInt(blockNum);
	}
	/**
	 * Writes the current fileListBlock on the disk and initialises it
	 * (when it's full or it's the end of the index).
	 */
	protected void flushFiles() throws IOException {
		if (!firstFileListBlock
				&& fileListBlock != null) {
			fileListBlock.flush();
			fileListBlock.write(indexOut, blockNum++);
			fileListBlock.clear();
			firstInBlock= true;
		}
	}
	/**
	 * Writes the current indexBlock on the disk and initialises it
	 * (when it's full or it's the end of the index).
	 */
	protected void flushWords() throws IOException {
		if (!firstInBlock 
				&& indexBlock != null) { // could have added a document without any indexed word, no block created yet
			indexBlock.flush();
			indexBlock.write(indexOut, blockNum++);
			indexBlock.clear();
			firstInBlock= true;
		}
	}
	/**
	 * 
	 */
	protected void flushIncludes() throws IOException {
		if (!firstInBlock 
			&& includeIndexBlock != null) { // could have added a document without any indexed word, no block created yet
		includeIndexBlock.flush();
		includeIndexBlock.write(indexOut, blockNum++);
		includeIndexBlock.clear();
		firstInBlock= true;
	}
		
	}
	/**
	 * @see IndexOutput#getDestination
	 */
	public Object getDestination() {
		return indexFile;
	}
	/**
	 * @see IndexOutput#open
	 */
	public void open() throws IOException {
		if (!opened) {
			summary= new IndexSummary();
			numFiles= 0;
			numWords= 0;
			numIncludes=0;
			blockNum= 1;
			firstInBlock= true;
			firstIndexBlock= true;
			firstFileListBlock= true;
			firstIncludeIndexBlock=true;
			indexOut= new SafeRandomAccessFile(this.indexFile, "rw"); //$NON-NLS-1$
			opened= true;
		}
	}
}
