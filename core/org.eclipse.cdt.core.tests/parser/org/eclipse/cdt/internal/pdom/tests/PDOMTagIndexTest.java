/*******************************************************************************
 * Copyright (c) 2013, 2015 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrew Eidsness - Initial implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.pdom.tests;

import java.io.File;
import java.util.Arrays;

import org.eclipse.cdt.core.dom.ast.tag.ITag;
import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.cdt.core.index.IIndexLocationConverter;
import org.eclipse.cdt.core.model.LanguageManager;
import org.eclipse.cdt.core.testplugin.util.BaseTestCase;
import org.eclipse.cdt.internal.core.dom.ast.tag.Tag;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.WritablePDOM;
import org.eclipse.cdt.internal.core.pdom.tag.PDOMTag;
import org.eclipse.cdt.internal.core.pdom.tag.PDOMTagIndex;

import junit.framework.Test;

// copy/pasted from BTreeTests
public class PDOMTagIndexTests extends BaseTestCase {
	private File pdomFile;
	private PDOM pdom;

	public static Test suite() {
		return suite(PDOMTagIndexTests.class);
	}

	private static class MockIndexLocationConverter implements IIndexLocationConverter {
		@Override
		public IIndexFileLocation fromInternalFormat(String raw) {
			return null;
		}

		@Override
		public String toInternalFormat(IIndexFileLocation location) {
			return null;
		}
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		pdomFile = File.createTempFile(getClass().getSimpleName() + '.' + Double.toString(Math.random()).substring(2),
				null);
		pdom = new WritablePDOM(pdomFile, new MockIndexLocationConverter(),
				LanguageManager.getInstance().getPDOMLinkageFactoryMappings());
		pdom.acquireWriteLock(null);
	}

	@Override
	protected void tearDown() throws Exception {
		try {
			pdom.close();
		} finally {
			pdom.releaseWriteLock();
		}
		pdomFile.delete();
		super.tearDown();
	}

	// return the nearest valid record that is less than the specified base
	private static long lastRecordBase = 1000;

	private static long computeValidRecord() {
		lastRecordBase += 1000;
		return (lastRecordBase & ~7L) | 2;
	}

	// A quick sanity test to confirm basic functionality.
	public void testSimple() throws Exception {
		String tagger = "tagger_a";
		long rec = computeValidRecord();

		assertNotNull(PDOMTagIndex.createTag(pdom, rec, tagger, 1));
		assertNotNull(PDOMTagIndex.getTag(pdom, rec, tagger));
	}

	public void testMultipleTaggers() throws Exception {
		String tagger_a = "tagger_a";
		String tagger_b = "tagger_b";
		long rec1 = computeValidRecord();
		long rec2 = computeValidRecord();

		assertNotNull(PDOMTagIndex.createTag(pdom, rec1, tagger_a, 1));
		assertNotNull(PDOMTagIndex.createTag(pdom, rec1, tagger_b, 1));
		assertNotNull(PDOMTagIndex.createTag(pdom, rec2, tagger_a, 1));

		assertNotNull(PDOMTagIndex.getTag(pdom, rec2, tagger_a));
		assertNull(PDOMTagIndex.getTag(pdom, rec2, tagger_b));

		Iterable<ITag> tags1 = PDOMTagIndex.getTags(pdom, rec1);
		int tag_count = 0;
		for (ITag tag : tags1) {
			++tag_count;
			assertTrue(tag.getTaggerId().equals(tagger_a) || tag.getTaggerId().equals(tagger_b));
			assertEquals(1, tag.getDataLen());
		}
		assertEquals(2, tag_count);
	}

	public void testReplaceTags() throws Exception {
		String tagger_a = "tagger_a";
		String tagger_b = "tagger_b";
		long rec = computeValidRecord();

		ITag taga = PDOMTagIndex.createTag(pdom, rec, tagger_a, 2);
		assertNotNull(taga);
		assertTrue(taga instanceof PDOMTag);
		PDOMTag taga_pdom = (PDOMTag) taga;
		ITag tagb = PDOMTagIndex.createTag(pdom, rec, tagger_a, 2);
		assertNotNull(tagb);

		// replacement should delete tags for taggers that are no longer present
		// and shorter tags
		// should be modified in place
		PDOMTagIndex.setTags(pdom, rec, Arrays.<ITag>asList(new Tag(tagger_a, 1)));
		assertNull(PDOMTagIndex.getTag(pdom, rec, tagger_b));
		ITag shorter_ = PDOMTagIndex.getTag(pdom, rec, tagger_a);
		assertNotNull(shorter_);
		assertTrue(shorter_ instanceof PDOMTag);
		PDOMTag shorter_pdom = (PDOMTag) shorter_;
		assertEquals(taga_pdom.getRecord(), shorter_pdom.getRecord());

		// longer tags should create a new record
		PDOMTagIndex.setTags(pdom, rec, Arrays.<ITag>asList(new Tag(tagger_a, 4)));
		ITag longer_ = PDOMTagIndex.getTag(pdom, rec, tagger_a);
		assertNotNull(longer_);
		assertTrue(longer_ instanceof PDOMTag);
		PDOMTag longer_pdom = (PDOMTag) longer_;
		assertTrue(taga_pdom.getRecord() != longer_pdom.getRecord());

		// TODO figure out how to confirm that the original tag was free'd
	}
}
