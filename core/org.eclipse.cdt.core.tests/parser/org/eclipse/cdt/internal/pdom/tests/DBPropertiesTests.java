/*******************************************************************************
 * Copyright (c) 2007, 2016 Symbian Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrew Ferguson (Symbian) - Initial implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.pdom.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.util.Iterator;
import java.util.Properties;

import org.eclipse.cdt.core.testplugin.util.BaseTestCase5;
import org.eclipse.cdt.internal.core.pdom.db.ChunkCache;
import org.eclipse.cdt.internal.core.pdom.db.DBProperties;
import org.eclipse.cdt.internal.core.pdom.db.Database;
import org.eclipse.core.runtime.CoreException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for the {@link DBProperties} class.
 */
public class DBPropertiesTests extends BaseTestCase5 {
	File dbLoc;
	Database db;

	@BeforeEach
	protected void beforeEach() throws Exception {
		dbLoc = File.createTempFile("test", "db");
		dbLoc.deleteOnExit();
		db = new Database(dbLoc, new ChunkCache(), 0, false);
		db.setExclusiveLock();
	}

	@AfterEach
	protected void afterEach() throws Exception {
		db.close();
	}

	@Test
	public void testBasic() throws CoreException {
		DBProperties properties = new DBProperties(db);
		Properties expected = System.getProperties();
		for (Iterator i = expected.keySet().iterator(); i.hasNext();) {
			String key = (String) i.next();
			String value = expected.getProperty(key);
			if (value != null) {
				properties.setProperty(key, value);
			}
		}
		for (Iterator i = expected.keySet().iterator(); i.hasNext();) {
			String key = (String) i.next();
			String aValue = properties.getProperty(key);
			assertEquals(expected.getProperty(key), aValue);
		}
		for (Iterator i = expected.keySet().iterator(); i.hasNext();) {
			String key = (String) i.next();
			properties.removeProperty(key);
		}
		assertEquals(0, properties.getKeySet().size());

		properties.delete();
	}

	@Test
	public void testLong() throws Exception {
		DBProperties ps = new DBProperties(db);

		StringBuilder largeValue = new StringBuilder();
		for (int i = 0; i < Database.CHUNK_SIZE * 2; i += 64) {
			largeValue.append("********");
			ps.setProperty("key", largeValue.toString());
			ps.setProperty(largeValue.toString(), "value");
		}

		assertEquals(largeValue.toString(), ps.getProperty("key"));
		assertEquals("value", ps.getProperty(largeValue.toString()));

		ps.delete();
	}

	@Test
	public void testNulls() throws Exception {
		DBProperties ps = new DBProperties(db);
		try {
			ps.setProperty(null, "val1");
			fail("NullPointerException expected");
		} catch (NullPointerException e) {
		} catch (AssertionError e) {
		}

		try {
			ps.setProperty("key", null);
			fail("NullPointerException expected");
		} catch (NullPointerException e) {
		} catch (AssertionError e) {
		}

		try {
			ps.setProperty(null, null);
			fail("NullPointerException expected");
		} catch (NullPointerException e) {
		} catch (AssertionError e) {
		}

		assertFalse(ps.removeProperty(null));

		assertNull(ps.getProperty(null));

		String s = "" + System.currentTimeMillis();
		assertEquals(s, ps.getProperty(null, s));
	}

	@Test
	public void testSeq() throws Exception {
		DBProperties ps = new DBProperties(db);

		ps.setProperty("a", "b");
		assertEquals("b", ps.getProperty("a"));
		assertEquals(1, ps.getKeySet().size());

		ps.setProperty("b", "c");
		assertEquals("c", ps.getProperty("b"));
		assertEquals(2, ps.getKeySet().size());

		ps.setProperty("a", "c");
		assertEquals("c", ps.getProperty("a"));
		assertEquals(2, ps.getKeySet().size());

		boolean deleted = ps.removeProperty("c");
		assertEquals(false, deleted);
		assertEquals(2, ps.getKeySet().size());

		deleted = ps.removeProperty("a");
		assertEquals(true, deleted);
		assertEquals(1, ps.getKeySet().size());

		ps.delete();
	}
}
