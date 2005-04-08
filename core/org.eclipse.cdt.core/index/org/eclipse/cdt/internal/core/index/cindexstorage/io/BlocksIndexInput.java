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

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

import org.eclipse.cdt.internal.core.CharOperation;
import org.eclipse.cdt.internal.core.Util;
import org.eclipse.cdt.internal.core.index.IEntryResult;
import org.eclipse.cdt.internal.core.index.IQueryResult;
import org.eclipse.cdt.internal.core.index.cindexstorage.EntryResult;
import org.eclipse.cdt.internal.core.index.cindexstorage.ICIndexStorageConstants;
import org.eclipse.cdt.internal.core.index.cindexstorage.IncludeEntry;
import org.eclipse.cdt.internal.core.index.cindexstorage.IndexedFileEntry;
import org.eclipse.cdt.internal.core.index.cindexstorage.WordEntry;
import org.eclipse.cdt.internal.core.util.LRUCache;
 
/**
 * This input is used for reading indexes saved using a BlocksIndexOutput.
 */
public class BlocksIndexInput extends IndexInput {
	public static final int CACHE_SIZE= 16; // Cache 16 blocks of 8K each, for a cache size of 128K
	protected FileListBlock currentFileListBlock;
	protected int currentFileListBlockNum;
	protected int currentIndexBlockNum;
	protected int currentIncludeBlockNum;
	protected IndexBlock currentIndexBlock;
	protected IndexBlock currentIncludeIndexBlock;
	private RandomAccessFile raf;
	protected File indexFile;
	protected LRUCache blockCache;
	protected boolean opened= false;
	protected IndexSummary summary;

	public BlocksIndexInput(File inputFile) {
		this.indexFile= inputFile;
		blockCache= new LRUCache(CACHE_SIZE);
	}
	/**
	 * @see IndexInput#clearCache()
	 */
	public void clearCache() {
		blockCache= new LRUCache(CACHE_SIZE);
	}
	/**
	 * @see IndexInput#close()
	 */
	public void close() throws IOException {
		if (opened) {
			summary= null;
			opened= false;
			if (raf != null)
				raf.close();
		}
	}
	/**
	 * @see IndexInput#getCurrentFile()
	 */
	public IndexedFileEntry getCurrentFile() throws IOException {
		if (!hasMoreFiles())
			return null;
		IndexedFileEntry file= null;
		if ((file= currentFileListBlock.getFile(filePosition)) == null) {
			currentFileListBlockNum= summary.getBlockNumForFileNum(filePosition);
			currentFileListBlock= getFileListBlock(currentFileListBlockNum);
			file= currentFileListBlock.getFile(filePosition);
		}
		return file;
	}
	/**
	 * Returns the entry corresponding to the given word.
	 */
	protected WordEntry getEntry(char[] word) throws IOException {
		int blockNum= summary.getBlockNumForWord(word);
		if (blockNum == -1) return null;
		IndexBlock block= getIndexBlock(blockNum);
		return block.findExactEntry(word);
	}
	/**
	 * Returns the FileListBlock with the given number.
	 */
	protected FileListBlock getFileListBlock(int blockNum) throws IOException {
		Integer key= new Integer(blockNum);
		Block block= (Block) blockCache.get(key);
		if (block != null && block instanceof FileListBlock)
			return (FileListBlock) block;
		FileListBlock fileListBlock= new FileListBlock(ICIndexStorageConstants.BLOCK_SIZE);
		fileListBlock.read(raf, blockNum);
		blockCache.put(key, fileListBlock);
		return fileListBlock;
	}
	/**
	 * Returns the IndexBlock (containing words) with the given number.
	 */
	protected IndexBlock getIndexBlock(int blockNum) throws IOException {
		Integer key= new Integer(blockNum);
		Block block= (Block) blockCache.get(key);
		if (block != null && block instanceof IndexBlock)
			return (IndexBlock) block;
		IndexBlock indexBlock= new GammaCompressedIndexBlock(ICIndexStorageConstants.BLOCK_SIZE);
		indexBlock.read(raf, blockNum);
		blockCache.put(key, indexBlock);
		return indexBlock;
	}
	/**
	 * @see IndexInput#getIndexedFile(int)
	 */
	public IndexedFileEntry getIndexedFile(int fileNum) throws IOException {
		int blockNum= summary.getBlockNumForFileNum(fileNum);
		if (blockNum == -1)
			return null;
		FileListBlock block= getFileListBlock(blockNum);
		return block.getFile(fileNum);
	}
	/**
	 * @see IndexInput#getIndexedFile(IDocument)
	 */
	public IndexedFileEntry getIndexedFile(String fullPath) throws java.io.IOException {
		setFirstFile();
		while (hasMoreFiles()) {
			IndexedFileEntry file= getCurrentFile();
			String path= file.getPath();
			if (path.equals(fullPath))
				return file;
			moveToNextFile();
		}
		return null;
	}
	/**
	 * Returns the list of numbers of files containing the given word.
	 */

	protected int[] getMatchingFileNumbers(char[] word) throws IOException {
		int blockNum= summary.getBlockNumForWord(word);
		if (blockNum == -1)
			return new int[0];
		IndexBlock block= getIndexBlock(blockNum);
		WordEntry entry= block.findExactEntry(word);
		return entry == null ? new int[0] : entry.getRefs();
	}
	/**
	 * @see IndexInput#getNumFiles()
	 */
	public int getNumFiles() {
		return summary.getNumFiles();
	}
	/**
	 * @see IndexInput#getNumWords()
	 */
	public int getNumWords() {
		return summary.getNumWords();
	}
	/**
	 * @see IndexInput#getNumIncludes()
	 */
	public int getNumIncludes() {
		return summary.getNumIncludes();
	}
	/**
	 * @see IndexInput#getSource()
	 */
	public Object getSource() {
		return indexFile;
	}
	/**
	 * Initialises the blocksIndexInput
	 */
	protected void init() throws IOException {
		clearCache();
		setFirstFile();
		setFirstWord();
		setFirstInclude();
	}
	/**
	 * @see IndexInput#moveToNextFile()
	 */
	public void moveToNextFile() throws IOException {
		filePosition++;
	}
	/**
	 * @see IndexInput#moveToNextWordEntry()
	 */
	public void moveToNextWordEntry() throws IOException {
		wordPosition++;
		if (!hasMoreWords()) {
			return;
		}
		//if end of the current block, we load the next one.
		boolean endOfBlock= !currentIndexBlock.nextEntry(currentWordEntry);
		if (endOfBlock) {
			currentIndexBlock= getIndexBlock(++currentIndexBlockNum);
			currentIndexBlock.nextEntry(currentWordEntry);
		}
	}
	/**
	 * @see IndexInput#open()
	 */

	public void open() throws IOException {
		if (!opened) {
			raf= new SafeRandomAccessFile(indexFile, "r"); //$NON-NLS-1$
			String sig= raf.readUTF();
			if (!sig.equals(ICIndexStorageConstants.SIGNATURE))
				throw new IOException(Util.bind("exception.wrongFormat")); //$NON-NLS-1$
			int summaryBlockNum= raf.readInt();
			raf.seek(summaryBlockNum * (long) ICIndexStorageConstants.BLOCK_SIZE);
			summary= new IndexSummary();
			summary.read(raf);
			init();
			opened= true;
		}
	}
	/**
	 * @see IndexInput#query(String)
	 */
	public IQueryResult[] query(String word) throws IOException {
		open();
		int[] fileNums= getMatchingFileNumbers(word.toCharArray());
		int size= fileNums.length;
		IQueryResult[] files= new IQueryResult[size];
		for (int i= 0; i < size; ++i) {
			files[i]= getIndexedFile(fileNums[i]);
		}
		return files;
	}
	/**
	 * If no prefix is provided in the pattern, then this operation will have to walk
	 * all the entries of the whole index.
	 */
	public IEntryResult[] queryEntriesMatching(char[] pattern/*, boolean isCaseSensitive*/) throws IOException {
		open();
	
		if (pattern == null || pattern.length == 0) return null;
		int[] blockNums = null;
		int firstStar = CharOperation.indexOf('*', pattern);
		switch (firstStar){
			case -1 :
				WordEntry entry = getEntry(pattern);
				if (entry == null) return null;
				return new IEntryResult[]{ new EntryResult(entry.getWord(), entry.getRefs(), entry.getOffsets(), entry.getOffsetLengths()) };
			case 0 :
				blockNums = summary.getAllBlockNums();
				break;
			default :
				char[] prefix = CharOperation.subarray(pattern, 0, firstStar);
				blockNums = summary.getBlockNumsForPrefix(prefix);
		}
		if (blockNums == null || blockNums.length == 0)	return null;
				
		IEntryResult[] entries = new IEntryResult[5];
		int count = 0;
		for (int i = 0, max = blockNums.length; i < max; i++) {
			IndexBlock block = getIndexBlock(blockNums[i]);
			block.reset();
			boolean found = false;
			WordEntry entry = new WordEntry();
			while (block.nextEntry(entry)) {
				if (CharOperation.match(entry.getWord(), pattern, true)) {
					if (count == entries.length){
						System.arraycopy(entries, 0, entries = new IEntryResult[count*2], 0, count);
					}
					entries[count++] = new EntryResult(entry.getWord(), entry.getRefs(), entry.getOffsets(), entry.getOffsetLengths());
					found = true;
				} else {
					if (found) break;
				}
			}
		}
		if (count != entries.length){
			System.arraycopy(entries, 0, entries = new IEntryResult[count], 0, count);
		}
		return entries;
	}
	public IEntryResult[] queryEntriesPrefixedBy(char[] prefix) throws IOException {
		open();
		
		int blockLoc = summary.getFirstBlockLocationForPrefix(prefix);
		if (blockLoc < 0) return null;
			
		IEntryResult[] entries = new IEntryResult[5];
		int count = 0;
		while(blockLoc >= 0){
			IndexBlock block = getIndexBlock(summary.getBlockNum(blockLoc));
			block.reset();
			boolean found = false;
			WordEntry entry = new WordEntry();
			while (block.nextEntry(entry)) {
				if (CharOperation.prefixEquals(prefix, entry.getWord())) {
					if (count == entries.length){
						System.arraycopy(entries, 0, entries = new IEntryResult[count*2], 0, count);
					}
					entries[count++] = new EntryResult(entry.getWord(), entry.getRefs(), entry.getOffsets(), entry.getOffsetLengths());
					found = true;
				} else {
					if (found) break;
				}
			}
			/* consider next block ? */
			blockLoc = summary.getNextBlockLocationForPrefix(prefix, blockLoc);				
		}
		if (count == 0) return null;
		if (count != entries.length){
			System.arraycopy(entries, 0, entries = new IEntryResult[count], 0, count);
		}
		return entries;
	}
	public IQueryResult[] queryFilesReferringToPrefix(char[] prefix) throws IOException {
		open();
		
		int blockLoc = summary.getFirstBlockLocationForPrefix(prefix);
		if (blockLoc < 0) return null;
			
		// each filename must be returned already once
		org.eclipse.cdt.internal.core.search.HashtableOfInt fileMatches = new  org.eclipse.cdt.internal.core.search.HashtableOfInt(20);
		int count = 0; 
		while(blockLoc >= 0){
			IndexBlock block = getIndexBlock(summary.getBlockNum(blockLoc));
			block.reset();
			boolean found = false;
			WordEntry entry = new WordEntry();
			while (block.nextEntry(entry)) {
				if (CharOperation.prefixEquals(prefix, entry.getWord()/*, isCaseSensitive*/)) {
					int [] refs = entry.getRefs();
					for (int i = 0, max = refs.length; i < max; i++){
						int ref = refs[i];
						if (!fileMatches.containsKey(ref)){
							count++;
							fileMatches.put(ref, getIndexedFile(ref));
						}
					}
					found = true;
				} else {
					if (found) break;
				}
			}
			/* consider next block ? */
			blockLoc = summary.getNextBlockLocationForPrefix(prefix, blockLoc);				
		}
		/* extract indexed files */
		IQueryResult[] files = new IQueryResult[count];
		Object[] indexedFiles = fileMatches.valueTable;
		for (int i = 0, index = 0, max = indexedFiles.length; i < max; i++){
			IndexedFileEntry indexedFile = (IndexedFileEntry) indexedFiles[i];
			if (indexedFile != null){
				files[index++] = indexedFile;
			}
		}	
		return files;
	}
	/**
	 * @see IndexInput#queryInDocumentNames(String)
	 */
	public IQueryResult[] queryInDocumentNames(String word) throws IOException {
		open();
		ArrayList matches= new ArrayList();
		setFirstFile();
		while (hasMoreFiles()) {
			IndexedFileEntry file= getCurrentFile();
			if (file.getPath().indexOf(word) != -1)
				matches.add(file);
			moveToNextFile();
		}
		IQueryResult[] match= new IQueryResult[matches.size()];
		matches.toArray(match);
		return match;
	}
	/**
	 * @see IndexInput#setFirstFile()
	 */

	protected void setFirstFile() throws IOException {
		filePosition= 1;
		if (getNumFiles() > 0) {
			currentFileListBlockNum= summary.getBlockNumForFileNum(1);
			currentFileListBlock= getFileListBlock(currentFileListBlockNum);
		}
	}
	/**
	 * @see IndexInput#setFirstWord()
	 */

	protected void setFirstWord() throws IOException {
		wordPosition= 1;
		if (getNumWords() > 0) {
			currentIndexBlockNum= summary.getFirstWordBlockNum();
			currentIndexBlock= getIndexBlock(currentIndexBlockNum);
			currentWordEntry= new WordEntry();
			currentIndexBlock.reset();
			currentIndexBlock.nextEntry(currentWordEntry);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.index.impl.IndexInput#moveToNextIncludeEntry()
	 */
	public void moveToNextIncludeEntry() throws IOException {
		includePosition++;
		if (!hasMoreIncludes()) {
			return;
		}
		//if end of the current block, we load the next one.
		boolean endOfBlock= !currentIncludeIndexBlock.nextEntry(currentIncludeEntry);
		if (endOfBlock) {
			currentIncludeIndexBlock= getIndexBlock(++currentIncludeBlockNum);
			currentIncludeIndexBlock.nextEntry(currentIncludeEntry);
		}
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.index.impl.IndexInput#setFirstInclude()
	 */
	protected void setFirstInclude() throws IOException {
		includePosition= 1;
		if (getNumIncludes() > 0) {
			currentIncludeBlockNum= summary.getFirstIncludeBlockNum();
			currentIncludeIndexBlock= getIndexBlock(currentIncludeBlockNum);
			currentIncludeEntry= new IncludeEntry(0);
			currentIncludeIndexBlock.reset();
			currentIncludeIndexBlock.nextEntry(currentIncludeEntry);
		}
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.index.impl.IndexInput#queryIncludeEntries()
	 */
	public IncludeEntry[] queryIncludeEntries() {
		// TODO Auto-generated method stub
		return null;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.index.impl.IndexInput#queryIncludeEntries(int)
	 */
	public IncludeEntry[] queryIncludeEntries(int fileNum) throws IOException {
		open();
		
		if (fileNum < 0) return null;
		int[] blockNums = null;
		blockNums = summary.getBlockNumsForIncludes();
		
		if (blockNums == null || blockNums.length == 0)	return null;
				
		IncludeEntry[] entries = new IncludeEntry[5];
		int count = 0;
		for (int i = 0, max = blockNums.length; i < max; i++) {
			IndexBlock block = getIndexBlock(blockNums[i]);
			block.reset();
			boolean found = false;
			IncludeEntry entry = new IncludeEntry(0);
			
			while (block.nextEntry(entry)) {
				if (count == entries.length){
					System.arraycopy(entries, 0, entries = new IncludeEntry[count*2], 0, count);
				}
				for (int j=0; j<entry.getNumRefs(); j++){
					if (entry.getRef(j) == fileNum){
						entries[count++] = new IncludeEntry(entry.getFile(),entry.getID());
						break; 
					}
				}
			}
		}
		if (count == 0) return null;
		if (count != entries.length){
			System.arraycopy(entries, 0, entries = new IncludeEntry[count], 0, count);
		}
		return entries;
	}

    /**
     * @return Returns the opened.
     */
    public boolean isOpened() {
        return opened;
    }
    /**
     * @param opened The opened to set.
     */
    public void setOpened(boolean opened) {
        this.opened = opened;
    }
}
