/*
 * Copyright (c) 2013 QNX Software Systems and others.
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
 */
package org.eclipse.cdt.internal.pdom.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;

import org.eclipse.cdt.core.testplugin.util.BaseTestCase5;
import org.eclipse.cdt.internal.core.pdom.db.ChunkCache;
import org.eclipse.cdt.internal.core.pdom.db.Database;
import org.eclipse.cdt.internal.core.pdom.db.PDOMStringSet;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

// copy/pasted from BTreeTests
public class PDOMStringSetTests extends BaseTestCase5 {
	protected File dbFile;
	protected Database db;
	protected PDOMStringSet stringSet;
	protected int rootRecord;

	@BeforeEach
	protected void beforeEach() throws Exception {
		dbFile = File.createTempFile("pdomstringsettest", "db");
		db = new Database(dbFile, new ChunkCache(), 0, false);
		db.setExclusiveLock();
		rootRecord = Database.DATA_AREA;
		stringSet = new PDOMStringSet(db, rootRecord);
	}

	@AfterEach
	protected void afterEach() throws Exception {
		db.close();
		dbFile.deleteOnExit();
	}

	// Quick tests to exercise the basic functionality.
	@Test
	public void testInterface() throws Exception {
		long val1_rec_a = stringSet.add("val1");
		long val2_rec_a = stringSet.add("val2");
		long val1_rec_b = stringSet.add("val1");
		assertTrue(val1_rec_a != 0);
		assertTrue(val2_rec_a != 0);
		assertEquals(val1_rec_a, val1_rec_b);

		long val1_find = stringSet.find("val1");
		long val2_find = stringSet.find("val2");
		assertEquals(val1_rec_a, val1_find);
		assertEquals(val2_rec_a, val2_find);

		long val1_rm = stringSet.remove("val1");
		assertEquals(val1_rec_a, val1_rm);
		assertEquals(0, stringSet.find("val1"));
		assertEquals(val2_rec_a, stringSet.find("val2"));

		stringSet.clearCaches();
		assertEquals(val2_rec_a, stringSet.find("val2"));
		assertEquals(0, stringSet.find("val1"));
	}
}
