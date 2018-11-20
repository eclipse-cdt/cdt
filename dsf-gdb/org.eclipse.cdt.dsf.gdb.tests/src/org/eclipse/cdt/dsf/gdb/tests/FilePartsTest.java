/*******************************************************************************
 * Copyright (c) 2016 Kichwa Coders AB and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Jonah Graham (Kichwa Coders) - Initial Implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.tests;

import static org.junit.Assert.assertEquals;

import org.eclipse.cdt.debug.core.CDebugUtils;
import org.junit.Test;

/**
 * TODO: Move this test closer to the code it is testing.
 *
 * At the time I wrote this test, there was no obvious place to put it, a test
 * suite for org.eclipse.cdt.debug.core is needed.
 *
 */
public class FilePartsTest {
	@Test
	public void testFileParts() {
		assertEquals("", CDebugUtils.getFileParts("").getFolder());
		assertEquals("", CDebugUtils.getFileParts("").getFileName());
		assertEquals("", CDebugUtils.getFileParts("").getExtension());

		assertEquals("", CDebugUtils.getFileParts(".").getFolder());
		assertEquals(".", CDebugUtils.getFileParts(".").getFileName());
		assertEquals("", CDebugUtils.getFileParts(".").getExtension());

		assertEquals("", CDebugUtils.getFileParts(".d").getFolder());
		assertEquals(".d", CDebugUtils.getFileParts(".d").getFileName());
		assertEquals("d", CDebugUtils.getFileParts(".d").getExtension());

		assertEquals("", CDebugUtils.getFileParts(".dot").getFolder());
		assertEquals(".dot", CDebugUtils.getFileParts(".dot").getFileName());
		assertEquals("dot", CDebugUtils.getFileParts(".dot").getExtension());

		assertEquals("", CDebugUtils.getFileParts("file").getFolder());
		assertEquals("file", CDebugUtils.getFileParts("file").getFileName());
		assertEquals("", CDebugUtils.getFileParts("file").getExtension());

		assertEquals("", CDebugUtils.getFileParts("file.").getFolder());
		assertEquals("file.", CDebugUtils.getFileParts("file.").getFileName());
		assertEquals("", CDebugUtils.getFileParts("file.").getExtension());

		assertEquals("", CDebugUtils.getFileParts("file.d").getFolder());
		assertEquals("file.d", CDebugUtils.getFileParts("file.d").getFileName());
		assertEquals("d", CDebugUtils.getFileParts("file.d").getExtension());

		assertEquals("", CDebugUtils.getFileParts("file.dot").getFolder());
		assertEquals("file.dot", CDebugUtils.getFileParts("file.dot").getFileName());
		assertEquals("dot", CDebugUtils.getFileParts("file.dot").getExtension());

		assertEquals("/folder/", CDebugUtils.getFileParts("/folder/file.dot").getFolder());
		assertEquals("file.dot", CDebugUtils.getFileParts("/folder/file.dot").getFileName());
		assertEquals("dot", CDebugUtils.getFileParts("/folder/file.dot").getExtension());

		assertEquals("/folder1/folder2/folder3/",
				CDebugUtils.getFileParts("/folder1/folder2/folder3/file.dot").getFolder());
		assertEquals("file.dot", CDebugUtils.getFileParts("/folder/file.dot").getFileName());
		assertEquals("dot", CDebugUtils.getFileParts("/folder/file.dot").getExtension());

		assertEquals("/folder/", CDebugUtils.getFileParts("/folder/.dot").getFolder());
		assertEquals(".dot", CDebugUtils.getFileParts("/folder/.dot").getFileName());
		assertEquals("dot", CDebugUtils.getFileParts("/folder/.dot").getExtension());

		assertEquals("/folder/../other/", CDebugUtils.getFileParts("/folder/../other/.dot").getFolder());
		assertEquals(".dot", CDebugUtils.getFileParts("/folder/../other/.dot").getFileName());
		assertEquals("dot", CDebugUtils.getFileParts("/folder/../other/.dot").getExtension());

		assertEquals("/folder//", CDebugUtils.getFileParts("/folder//.dot").getFolder());
		assertEquals(".dot", CDebugUtils.getFileParts("/folder//.dot").getFileName());
		assertEquals("dot", CDebugUtils.getFileParts("/folder//.dot").getExtension());

		assertEquals("C:\\folder\\", CDebugUtils.getFileParts("C:\\folder\\.dot").getFolder());
		assertEquals(".dot", CDebugUtils.getFileParts("C:\\folder\\.dot").getFileName());
		assertEquals("dot", CDebugUtils.getFileParts("C:\\folder\\.dot").getExtension());

		assertEquals("C:\\\\folder\\", CDebugUtils.getFileParts("C:\\\\folder\\.dot").getFolder());
		assertEquals(".dot", CDebugUtils.getFileParts("C:\\\\folder\\.dot").getFileName());
		assertEquals("dot", CDebugUtils.getFileParts("C:\\\\folder\\.dot").getExtension());

		assertEquals("/folder1/folder2/folder3/", CDebugUtils.getFileParts("/folder1/folder2/folder3/").getFolder());
		assertEquals("", CDebugUtils.getFileParts("/folder1/folder2/folder3/").getFileName());
		assertEquals("", CDebugUtils.getFileParts("/folder1/folder2/folder3/").getExtension());

		assertEquals("/", CDebugUtils.getFileParts("/").getFolder());
		assertEquals("", CDebugUtils.getFileParts("/").getFileName());
		assertEquals("", CDebugUtils.getFileParts("/").getExtension());

		assertEquals("\\\\unc\\path\\", CDebugUtils.getFileParts("\\\\unc\\path\\file.dot").getFolder());
		assertEquals("file.dot", CDebugUtils.getFileParts("\\\\unc\\path\\file.dot").getFileName());
		assertEquals("dot", CDebugUtils.getFileParts("\\\\unc\\path\\file.dot").getExtension());

	}
}
