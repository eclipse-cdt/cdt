/**********************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.core.indexer.tests;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.cdt.internal.core.index.cindexstorage.ICIndexStorageConstants;
import org.eclipse.cdt.internal.core.index.cindexstorage.WordEntry;
import org.eclipse.cdt.internal.core.index.cindexstorage.io.GammaCompressedIndexBlock;
import org.eclipse.cdt.internal.core.index.cindexstorage.io.IndexBlock;

/**
 * @author Bogdan Gheorghe
 */
public class IndexerOffsetTests extends TestCase {

    public static void main(String[] args) {
    }
    
	public static Test suite() {
		TestSuite suite = new TestSuite(IndexerOffsetTests.class.getName());

		suite.addTest(new IndexerOffsetTests("testOffsetsResizing")); //$NON-NLS-1$
	
		return suite;
	
	}
	
	/**
	 * Constructor for IndexerOffsetTests.
	 * @param name
	 */
	public IndexerOffsetTests(String name) {
		super(name);
	}
	
	public void testOffsetsResizing() throws Exception{
	    WordEntry word = new WordEntry("typeDecl/C/Test".toCharArray());
	    word.addRef(2);
	    word.addOffset(235,5,2,ICIndexStorageConstants.OFFSET);
	    word.addOffset(512,3,2,ICIndexStorageConstants.OFFSET);
	    word.addOffset(512,3,2,ICIndexStorageConstants.OFFSET);
	    word.addOffset(512,3,2,ICIndexStorageConstants.OFFSET);
	    word.addRef(5);
	    word.addOffset(43,6,5,ICIndexStorageConstants.OFFSET);
	    word.addOffset(2,3,5,ICIndexStorageConstants.LINE);
	    word.addOffset(89,8,5,ICIndexStorageConstants.OFFSET);
	    word.addOffset(63,2,5,ICIndexStorageConstants.LINE);
	    word.addOffset(124,7,5,ICIndexStorageConstants.OFFSET);
	    word.addRef(9);
	    word.addOffset(433,5,9,ICIndexStorageConstants.OFFSET);
	    word.addOffset(234,3,9,ICIndexStorageConstants.OFFSET);
	    word.addRef(11);
	    word.addOffset(4233,2,11,ICIndexStorageConstants.OFFSET);
	    word.addOffset(2314,7,11,ICIndexStorageConstants.OFFSET);
	    word.addRef(17);
	    word.addOffset(2,7,17,ICIndexStorageConstants.OFFSET);
	    word.addOffset(52,8,17,ICIndexStorageConstants.OFFSET);
	    int[] test =word.getOffsets(1);
	    
	    WordEntry word2 = new WordEntry("typeDecl/C/Test".toCharArray());
	    word2.addRef(4);
	    word2.addOffset(13,4,4, ICIndexStorageConstants.OFFSET);
	    word2.addOffset(17,3,4, ICIndexStorageConstants.OFFSET);
	    word2.addOffset(20,6,4,ICIndexStorageConstants.OFFSET);
	    word2.addRef(7);
	    word2.addOffset(21,2,7, ICIndexStorageConstants.OFFSET);
	    word2.addOffset(24,3,7, ICIndexStorageConstants.OFFSET);
	    word2.addOffset(28,7,7,ICIndexStorageConstants.OFFSET);
	    
	    word.addWordInfo(word2.getRefs(), word2.getOffsets(), word2.getOffsetLengths(), word2.getOffsetCount());
	    
	    word.mapRefs(new int[]{-1, 1, 17, 3, 4, 11, 6, 7, 8, 24, 10, 5, 12, 13, 14, 15, 16, 2});
	   
	    IndexBlock block= new GammaCompressedIndexBlock(ICIndexStorageConstants.BLOCK_SIZE);
	    block.addEntry(word);
	    block.flush();
	    
	    WordEntry entry= new WordEntry();
		block.nextEntry(entry);
	}
}
