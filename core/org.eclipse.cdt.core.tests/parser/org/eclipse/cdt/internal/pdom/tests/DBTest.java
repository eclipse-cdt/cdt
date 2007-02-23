/*******************************************************************************
 * Copyright (c) 2005, 2006 QNX Software Systems
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *     Andrew Ferguson (Symbian)
 *******************************************************************************/
package org.eclipse.cdt.internal.pdom.tests;

import java.io.File;
import java.util.Random;

import junit.framework.Test;

import org.eclipse.cdt.core.testplugin.CTestPlugin;
import org.eclipse.cdt.core.testplugin.util.BaseTestCase;
import org.eclipse.cdt.internal.core.pdom.db.BTree;
import org.eclipse.cdt.internal.core.pdom.db.Database;
import org.eclipse.cdt.internal.core.pdom.db.IBTreeComparator;
import org.eclipse.cdt.internal.core.pdom.db.IBTreeVisitor;
import org.eclipse.cdt.internal.core.pdom.db.IString;
import org.eclipse.cdt.internal.core.pdom.db.ShortString;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

public class DBTest extends BaseTestCase {
	protected Database db;
	
	protected void setUp() throws Exception {
		super.setUp();
		db = new Database(getTestDir().append(getName()+System.currentTimeMillis()+".dat").toFile());
	}
	
	public static Test suite() {
		return suite(DBTest.class);
	}
	
	protected IPath getTestDir() {
		IPath path = CTestPlugin.getDefault().getStateLocation().append("tests/");
		File file = path.toFile();
		if (!file.exists())
			file.mkdir();
		return path;
	}
	
	protected void tearDown() throws Exception {
		if(!db.getLocation().delete()) {
			db.getLocation().deleteOnExit();
		}
	}
	
	public void testBlockSizeAndFirstBlock() throws Exception {
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

	public void testFreeBlockLinking() throws Exception {
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
	
	public void testSimpleAllocationLifecycle() throws Exception {		
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
			return db.getString(db.getInt(record + 4)).compare(key, true);
		}
		
		public boolean visit(int record) throws CoreException {
			this.record = record;
			return false;
		}
		
		public int getRecord() {
			return record;
		}
		
	}
	
	public void testStringsInBTree() throws Exception {
		// Tests inserting and retrieving strings
		File f = getTestDir().append("testStrings.dat").toFile();
		f.delete();
		final Database db = new Database(f);

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
		
		IBTreeComparator comparator = new IBTreeComparator() {
			public int compare(int record1, int record2) throws CoreException {
				IString string1 = db.getString(db.getInt(record1 + 4));
				IString string2 = db.getString(db.getInt(record2 + 4));
				return string1.compare(string2, true);
			}
		};
		BTree btree = new BTree(db, Database.DATA_AREA, comparator);
		for (int i = 0; i < names.length; ++i) {
			String name = names[i];
			int record = db.malloc(8);
			db.putInt(record + 0, i);
			IString string = db.newString(name);
			db.putInt(record + 4, string.getRecord());
			btree.insert(record);
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
	
	private final int GT = 1, LT = -1, EQ = 0;
	
	public void testShortStringComparison() throws CoreException {
		Random r= new Random(90210);
		
		assertCMP("",  EQ, "", true);
		assertCMP("",  EQ, "", false);
		
		doTrials(1000, 1, ShortString.MAX_LENGTH, r, true);
		
		doTrials(1000, 1, ShortString.MAX_LENGTH, r, false);
		
		assertCMP("a",  LT, "b", true);
		assertCMP("aa", LT, "ab", true);
		assertCMP("a",  EQ, "a", true);
		
		assertCMP("a",  GT, "A", true);
		assertCMP("aa", GT, "aA", true);
		assertCMP("a",  GT, "B", true);
		
		assertCMP("a",  EQ, "a", false);
		assertCMP("a",  EQ, "A", false);
	}
	
	public void testLongStringComparison() throws CoreException {
		Random r= new Random(314159265);
		doTrials(600, ShortString.MAX_LENGTH+1, ShortString.MAX_LENGTH*2, r, true);
		doTrials(600, ShortString.MAX_LENGTH+1, ShortString.MAX_LENGTH*2, r, false);
	}
	
	private void doTrials(int n, int min, int max, Random r, boolean caseSensitive) throws CoreException {
		long start = System.currentTimeMillis();
		for(int i=0; i<n; i++) {
			String a = randomString(min, max, r);
			String b = randomString(min, max, r);
			int expected = caseSensitive ? a.compareTo(b) : a.compareToIgnoreCase(b);
			assertCMP(a, expected, b, caseSensitive);
		}
		System.out.print("Trials: "+n+" Max length: "+max+" ignoreCase: "+!caseSensitive);
		System.out.println(" Time: "+(System.currentTimeMillis()-start));
	}
	
	private String randomString(int min, int max, Random r) {
		StringBuffer result = new StringBuffer();
		int len = min + r.nextInt(max-min);
		for(int i=0; i<len; i++) {
			result.append(randomChar(r));
		}
		return result.toString();
	}
	
	private char randomChar(Random r) {
		// we only match String.compareToIgnoreCase behaviour within this limited range
		return (char) (32 + r.nextInt(40)); 
	}
	
	private void assertCMP(String a, int expected, String b, boolean caseSensitive) 
		throws CoreException
	{
		char[] acs = a.toCharArray();
		char[] bcs = b.toCharArray();
		IString aiss = db.newString(a);
		IString biss = db.newString(b);
		IString aisc = db.newString(acs);
		IString bisc = db.newString(bcs);
		
		assertSignEquals(expected, aiss.compare(bcs, caseSensitive));
		assertSignEquals(expected, aiss.compare(biss, caseSensitive));
		assertSignEquals(expected, aiss.compare(bisc, caseSensitive));
		assertSignEquals(expected, aiss.compare(b, caseSensitive));
		assertSignEquals(expected, aiss.comparePrefix(bcs, caseSensitive));
		
		assertSignEquals(expected, -biss.compare(acs, caseSensitive));
		assertSignEquals(expected, -biss.compare(aiss, caseSensitive));
		assertSignEquals(expected, -biss.compare(aisc, caseSensitive));
		assertSignEquals(expected, -biss.compare(a, caseSensitive));
		assertSignEquals(expected, -biss.comparePrefix(acs, caseSensitive));
	}
	
	private void assertSignEquals(int a, int b) {
		a= a<0 ? -1 : (a>0 ? 1 : 0);
		b= b<0 ? -1 : (b>0 ? 1 : 0);
		assertEquals(a, b);
	}
}
