package org.eclipse.cdt.internal.pdom.tests;

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;

import org.eclipse.cdt.core.testplugin.CTestPlugin;
import org.eclipse.cdt.internal.core.pdom.db.BTree;
import org.eclipse.cdt.internal.core.pdom.db.Database;
import org.eclipse.cdt.internal.core.pdom.db.StringComparator;
import org.eclipse.cdt.internal.core.pdom.db.StringVisitor;
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
		Database db = new Database(f.getCanonicalPath(), 0);

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
		Database db = new Database(f.getCanonicalPath(), 0);
		
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
		Database db = new Database(f.getCanonicalPath(), 0);
		
		int mem1 = db.malloc(42);
		db.free(mem1);
		int mem2 = db.malloc(42);
		assertEquals(mem2, mem1);
	}
	
	private static class FindVisitor extends StringVisitor {
		
		private int record;
		
		public FindVisitor(Database db, String key) {
			super(db, Database.INT_SIZE, key);
		}
		
		public boolean visit(int record) throws IOException {
			this.record = record;
			return false;
		}
		
		public int findIn(BTree btree) throws IOException {
			btree.visit(this);
			return record;
		}
		
	}
	
	public void testStrings() throws Exception {
		// Tests inserting and retrieving strings
		File f = getTestDir().append("testStrings.dat").toFile();
		f.delete();
		Database db = new Database(f.getCanonicalPath(), 0);

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
			int record = db.malloc((name.length() + 1) * Database.CHAR_SIZE + Database.INT_SIZE);
			db.putInt(record, i);
			db.putString(record + Database.INT_SIZE, name);
			btree.insert(record, new StringComparator(db, Database.INT_SIZE));
		}
		
		for (int i = 0; i < names.length; ++i) {
			String name = names[i];
			int record = new FindVisitor(db, name).findIn(btree);
			assertTrue(record != 0);
			assertEquals(i, db.getInt(record));
			String rname = db.getString(record + Database.INT_SIZE);
			assertEquals(name, rname);
		}
	}

}
