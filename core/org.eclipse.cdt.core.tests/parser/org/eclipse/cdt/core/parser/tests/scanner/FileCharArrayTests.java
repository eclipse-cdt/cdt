/*******************************************************************************
 * Copyright (c) 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.parser.tests.scanner;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import junit.framework.TestSuite;

import org.eclipse.cdt.core.testplugin.util.BaseTestCase;
import org.eclipse.cdt.internal.core.parser.scanner.AbstractCharArray;
import org.eclipse.cdt.internal.core.parser.scanner.FileCharArray;
import org.eclipse.cdt.internal.core.parser.scanner.LazyCharArray;

public class FileCharArrayTests extends BaseTestCase {
	
	public static TestSuite suite() {
		return suite(FileCharArrayTests.class);
	}

	private File fFile;

	@Override
	protected void tearDown() throws Exception {
		if (fFile != null) {
			fFile.delete();
		}
	}

	public void testAlignedMinus() throws IOException {
		testFile(true, LazyCharArray.CHUNK_SIZE*3-1);
	}

	public void testAlignedEven() throws IOException {
		testFile(true, LazyCharArray.CHUNK_SIZE*3);
	}

	public void testAlignedPlus() throws IOException {
		testFile(true, LazyCharArray.CHUNK_SIZE*3+1);
	}

	public void testUnAlignedMinus() throws IOException {
		testFile(false, LazyCharArray.CHUNK_SIZE*3-1);
	}

	public void testUnAlignedEven() throws IOException {
		testFile(false, LazyCharArray.CHUNK_SIZE*3);
	}

	public void testUnAlignedPlus() throws IOException {
		testFile(false, LazyCharArray.CHUNK_SIZE*3+1);
	}

	private void testFile(boolean aligned, int charSize) throws IOException {
		createFile(aligned, charSize);
		
		AbstractCharArray charArray;
		final FileInputStream inputStream = new FileInputStream(fFile);
		try {
			charArray = FileCharArray.create(fFile.getPath(), "utf-8", inputStream);
		} finally {
			inputStream.close();
		}
		
		checkContent(charArray, LazyCharArray.CHUNK_SIZE, charSize);
		assertEquals(charSize, charArray.getLength());
		
		((LazyCharArray) charArray).testClearData();
		
		checkContent(charArray, LazyCharArray.CHUNK_SIZE, charSize);
		assertEquals(charSize, charArray.getLength());	

	}

	public void checkContent(AbstractCharArray charArray, int from, int to) {
		for (int i = from; i < to; i++) {
			assertEquals(i % 127, charArray.get(i));
			if (i+3<=to) {
				char[] dest= new char[3];
				charArray.arraycopy(i, dest, 0, 3);
				for (int j = 0; j < dest.length; j++) {
					assertEquals((i+j) % 127, dest[j]);
				}
			}
		}
	}

	private void createFile(boolean aligned, int charSize) throws IOException {
		fFile= File.createTempFile("data", ".txt");
		OutputStream out= new BufferedOutputStream(new FileOutputStream(fFile));
		try {
			if (!aligned) {
				out.write(0xc2);
				out.write(0xa2);
			} else {
				out.write(0);
			}
			for (int i = 1; i < charSize; i++) {
				out.write(i % 127);
			}
		} finally {
			out.close();
		}
	}
}
