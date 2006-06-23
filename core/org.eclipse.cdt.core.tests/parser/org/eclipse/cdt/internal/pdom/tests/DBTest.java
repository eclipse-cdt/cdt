/*******************************************************************************
 * Copyright (c) 2005, 2006 QNX Software Systems
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.pdom.tests;

import java.io.File;

import junit.framework.TestCase;

import org.eclipse.cdt.core.testplugin.CTestPlugin;
import org.eclipse.cdt.internal.core.pdom.db.BTree;
import org.eclipse.cdt.internal.core.pdom.db.Database;
import org.eclipse.cdt.internal.core.pdom.db.IBTreeComparator;
import org.eclipse.cdt.internal.core.pdom.db.IBTreeVisitor;
import org.eclipse.cdt.internal.core.pdom.db.IString;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

public class DBTest extends TestCase {

	protected IPath getTestDir() {
		IPath path = CTestPlugin.getDefault().getStateLocation().append("tests/");
		File file = path.toFile();
		if (!file.exists())
			file.mkdir();
		return path;
	}
	
	public void test1() throws Exception {
		// Tests block size and simple first block
		File f = getTestDir().append("test1.dat").toFile();
		f.delete();
		Database db = new Database(f.getCanonicalPath());
		assertEquals(0, db.getVersion());

		final int realsize = 42;
		final int blocksize = (realsize / Database.MIN_SIZE + 1) * Database.MIN_SIZE;
		
		int mem = db.malloc(realsize);
		assertEquals(-blocksize, db.getInt(mem - Database.INT_SIZE));
		db.free(mem);
		assertEquals(blocksize, db.getInt(mem - Database.INT_SIZE));
		assertEquals(mem - Database.INT_SIZE, db.getInt((blocksize / Database.MIN_SIZE) * Database.INT_SIZE));
		assertEquals(mem - Database.INT_SIZE + blocksize, db.getInt(((Database.CHUNK_SIZE - blocksize) / Database.MIN_SIZE) * Database.INT_SIZE));
	}

	public void test2() throws Exception {
		// Tests free block linking
		File f = getTestDir().append("test2.dat").toFile();
		f.delete();
		Database db = new Database(f.getCanonicalPath());
		
		final int realsize = 42;
		final int blocksize = (realsize / Database.MIN_SIZE + 1) * Database.MIN_SIZE;

		int mem1 = db.malloc(realsize);
		int mem2 = db.malloc(realsize);
		db.free(mem1);
		db.free(mem2);
		assertEquals(mem2 - Database.INT_SIZE, db.getInt((blocksize / Database.MIN_SIZE) * Database.INT_SIZE));
		assertEquals(0, db.getInt(mem2));
		assertEquals(mem1 - Database.INT_SIZE, db.getInt(mem2 + Database.INT_SIZE));
		assertEquals(mem2 - Database.INT_SIZE, db.getInt(mem1));
		assertEquals(0, db.getInt(mem1 + Database.INT_SIZE));
	}
	
	public void test3() throws Exception {
		// 
		File f = getTestDir().append("test2.dat").toFile();
		f.delete();
		Database db = new Database(f.getCanonicalPath());
		
		int mem1 = db.malloc(42);
		db.free(mem1);
		int mem2 = db.malloc(42);
		assertEquals(mem2, mem1);
	}
	
	private static class FindVisitor implements IBTreeVisitor {
		private Database db;
		private String key;
		private int record;
		
		public FindVisitor(Database db, String key) {
			this.db = db;
			this.key = key;
		}

		public int compare(int record) throws CoreException {
			return db.getString(db.getInt(record + 4)).compare(key);
		}
		
		public boolean visit(int record) throws CoreException {
			this.record = record;
			return false;
		}
		
		public int getRecord() {
			return record;
		}
		
	}
	
	public void testStrings() throws Exception {
		// Tests inserting and retrieving strings
		File f = getTestDir().append("testStrings.dat").toFile();
		f.delete();
		final Database db = new Database(f.getCanonicalPath());

		String[] names = {
				"ARLENE",
				"BRET",
				"CINDY",
				"DENNIS",
				"EMILY",
				"FRANKLIN",
				"GERT",
				"HARVEY",
				"IRENE",
				"JOSE",
				"KATRINA",
				"LEE",
				"MARIA",
				"NATE",
				"OPHELIA",
				"PHILIPPE",
				"RITA",
				"STAN",
				"TAMMY",
				"VINCE",
				"WILMA",
				"ALPHA",
				"BETA"
		};
		
		BTree btree = new BTree(db, Database.DATA_AREA);
		for (int i = 0; i < names.length; ++i) {
			String name = names[i];
			int record = db.malloc(8);
			db.putInt(record + 0, i);
			IString string = db.newString(name);
			db.putInt(record + 4, string.getRecord());
			btree.insert(record, new IBTreeComparator() {
				public int compare(int record1, int record2) throws CoreException {
					IString string1 = db.getString(db.getInt(record1 + 4));
					IString string2 = db.getString(db.getInt(record2 + 4));
					return string1.compare(string2);
				}
			});
		}
		
		for (int i = 0; i < names.length; ++i) {
			String name = names[i];
			FindVisitor finder = new FindVisitor(db, name);
			btree.accept(finder);
			int record = finder.getRecord();
			assertTrue(record != 0);
			assertEquals(i, db.getInt(record));
			IString rname = db.getString(db.getInt(record + 4));
			assertTrue(rname.equals(name));
		}
	}

}
