/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.indexer.tests;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.cdt.internal.core.index.IIndex;
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
	    word.addOffset(235,5,2,IIndex.OFFSET);
	    word.addOffset(512,3,2,IIndex.OFFSET);
	    word.addOffset(512,3,2,IIndex.OFFSET);
	    word.addOffset(512,3,2,IIndex.OFFSET);
	    word.addModifiers(18,2);
	    word.addRef(5);
	    word.addOffset(43,6,5,IIndex.OFFSET);
	    word.addOffset(2,3,5,IIndex.LINE);
	    word.addOffset(89,8,5,IIndex.OFFSET);
	    word.addOffset(63,2,5,IIndex.LINE);
	    word.addOffset(124,7,5,IIndex.OFFSET);
	    word.addModifiers(4,5);
	    word.addRef(9);
	    word.addOffset(433,5,9,IIndex.OFFSET);
	    word.addOffset(234,3,9,IIndex.OFFSET);
	    word.addModifiers(1,9);
	    word.addRef(11);
	    word.addOffset(4233,2,11,IIndex.OFFSET);
	    word.addOffset(2314,7,11,IIndex.OFFSET);
	    word.addModifiers(8,11);
	    word.addRef(17);
	    word.addOffset(2,7,17,IIndex.OFFSET);
	    word.addOffset(52,8,17,IIndex.OFFSET);
	    word.addModifiers(32,17);
	    
	    int[] test =word.getOffsets(1);
	    
	    int[] modifierTest=word.getModifiers();
	    
	    WordEntry word2 = new WordEntry("typeDecl/C/Test".toCharArray());
	    word2.addRef(4);
	    word2.addOffset(13,4,4, IIndex.OFFSET);
	    word2.addOffset(17,3,4, IIndex.OFFSET);
	    word2.addOffset(20,6,4,IIndex.OFFSET);
	    word2.addModifiers(64,4);
	    word2.addRef(7);
	    word2.addOffset(21,2,7, IIndex.OFFSET);
	    word2.addOffset(24,3,7, IIndex.OFFSET);
	    word2.addOffset(28,7,7,IIndex.OFFSET);
	    word2.addModifiers(128,7);
	    
	    word.addWordInfo(word2.getRefs(), word2.getOffsets(), word2.getOffsetLengths(), word2.getOffsetCount(),word2.getModifiers());
	    
	    word.mapRefs(new int[]{-1, 1, 17, 3, 4, 11, 6, 7, 8, 24, 10, 5, 12, 13, 14, 15, 16, 2});
	   
	    IndexBlock block= new GammaCompressedIndexBlock(ICIndexStorageConstants.BLOCK_SIZE);
	    block.addEntry(word);
	    block.flush();
	    
	    WordEntry entry= new WordEntry();
		block.nextEntry(entry);
	}
}
