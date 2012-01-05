/*******************************************************************************
 * Copyright (c) 2006, 2009 Symbian Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Symbian - Initial implementation
 * Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.pdom.tests;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.SortedSet;
import java.util.TreeSet;

import junit.framework.Test;

import org.eclipse.cdt.core.testplugin.util.BaseTestCase;
import org.eclipse.cdt.internal.core.pdom.db.BTree;
import org.eclipse.cdt.internal.core.pdom.db.ChunkCache;
import org.eclipse.cdt.internal.core.pdom.db.Database;
import org.eclipse.cdt.internal.core.pdom.db.IBTreeComparator;
import org.eclipse.cdt.internal.core.pdom.db.IBTreeVisitor;
import org.eclipse.core.runtime.CoreException;

/**
 * Test insertion/deletion of records of a mock record type in a B-tree
 * 
 * @author aferguso
 *
 */
public class BTreeTests extends BaseTestCase {
	private static int DEBUG= 0;
	protected File dbFile;
	protected Database db;
	protected BTree btree;
	protected int rootRecord;
	protected IBTreeComparator comparator;


	public static Test suite() {
		return suite(BTreeTests.class);
	}

	// setUp is not used since we need to parameterize this method,
	// and invoke it multiple times per Junit test
	protected void init(int degree) throws Exception {
		dbFile = File.createTempFile("pdomtest", "db");
		db = new Database(dbFile, new ChunkCache(), 0, false);
		db.setExclusiveLock();
		rootRecord = Database.DATA_AREA;
		comparator = new BTMockRecordComparator();
		btree = new BTree(db, rootRecord, degree, comparator);
	}

	// tearDown is not used for the same reason as above
	protected void finish() throws Exception {
		db.close();
		dbFile.deleteOnExit();
	}

	
	public void testBySortedSetMirrorLite() throws Exception {
		sortedMirrorTest(8);
	}
	
	/**
	 * Test random (but reproducible via known seed) sequences of insertions/deletions
	 * and use TreeSet as a reference implementation to check behaviour against.
	 * @throws Exception
	 */
	protected void sortedMirrorTest(int noTrials) throws Exception {
		Random seeder = new Random(90210);

		for(int i=0; i<noTrials; i++) {
			int seed = seeder.nextInt();
			if (DEBUG > 0)
				System.out.println("Iteration #"+i);
			trial(seed, false);
		}
	}

	/**
	 * Test random (but reproducible via known seed) sequence of insertions
	 * and use TreeSet as a reference implementation to check behaviour against.
	 * @throws Exception
	 */
	public void testInsertion() throws Exception {
		Random seeder = new Random();

		for(int i=0; i<6; i++) {
			int seed = seeder.nextInt();
			if (DEBUG > 0)
				System.out.println("Iteration #"+i);
			trialImp(seed, false, new Random(seed*2), 1);
		}
	}

	/**
	 * Insert/Delete a random number of records into/from the B-tree 
	 * @param seed the seed for obtaining the deterministic random testing
	 * @param checkCorrectnessEachIteration if true, then on every single insertion/deletion check that the B-tree invariants
	 * still hold
	 * @throws Exception
	 */
	protected void trial(int seed, final boolean checkCorrectnessEachIteration) throws Exception {		
		Random random = new Random(seed);

		// the probabilty that a particular iterations action will be an insertion
		double pInsert = Math.min(0.5 + random.nextDouble(), 1);

		trialImp(seed, checkCorrectnessEachIteration, random, pInsert);
	}

	private void trialImp(int seed, final boolean checkCorrectnessEachIteration, Random random, double pInsert) throws Exception {
		final int degree = 2 + random.nextInt(11);
		final int nIterations = random.nextInt(100000);
		final SortedSet expected = new TreeSet();
		final List history = new ArrayList();
		
		init(degree);
		
		if (DEBUG > 0)
			System.out.print("\t "+seed+" "+(nIterations/1000)+"K: ");
		for(int i=0; i<nIterations; i++) {
			if(random.nextDouble()<pInsert) {
				Integer value = new Integer(random.nextInt(Integer.MAX_VALUE));
				boolean newEntry = expected.add(value);
				if(newEntry) {
					BTMockRecord btValue = new BTMockRecord(db, value.intValue());
					history.add(btValue);
					if(DEBUG > 1)
						System.out.println("Add: "+value+" @ "+btValue.record);
					btree.insert(btValue.getRecord());
				}
			} else {
				if(!history.isEmpty()) {
					int index = random.nextInt(history.size());
					BTMockRecord btValue = (BTMockRecord) history.get(index);
					history.remove(index);
					expected.remove(new Integer(btValue.intValue()));
					if(DEBUG > 1)
						System.out.println("Remove: "+btValue.intValue()+" @ "+btValue.record);
					btree.delete(btValue.getRecord());
				}
			}
			if(i % 1000 == 0 && DEBUG > 0) {
				System.out.print(".");
			}
			if(checkCorrectnessEachIteration) {
				assertBTreeMatchesSortedSet("[iteration "+i+"] ", btree, expected);
				assertBTreeInvariantsHold("[iteration "+i+"] ");
			}
		}
		if (DEBUG > 0)
			System.out.println();

		assertBTreeMatchesSortedSet("[Trial end] ", btree, expected);
		assertBTreeInvariantsHold("[Trial end]");
		
		finish();
	}

	public void assertBTreeInvariantsHold(String msg) throws CoreException {
		String errorReport = btree.getInvariantsErrorReport();
		if(!errorReport.equals("")) {
			fail("Invariants do not hold: "+errorReport);
		}
	}

	public void assertBTreeMatchesSortedSet(final String msg, BTree actual, SortedSet expected) throws CoreException {
		final Iterator i = expected.iterator();
		btree.accept(new IBTreeVisitor(){
			int k;
			@Override
			public int compare(long record) throws CoreException {
				return 0;
			}
			@Override
			public boolean visit(long record) throws CoreException {
				if(record!=0) {
					BTMockRecord btValue = new BTMockRecord(record, db);
					if(i.hasNext()) {
						Integer exp = ((Integer)i.next());
						assertEquals(msg+" Differ at index: "+k, btValue.intValue(), exp.intValue());
						k++;
					} else {
						fail("Sizes different");
						return false;
					}
				}
				return true;
			}
		});
	}

	private static class BTMockRecord {
		public static final int VALUE_PTR = 0; 
		public static final int RECORD_SIZE = Database.INT_SIZE;
		long record;
		Database db;

		/**
		 * Make a new record
		 */
		public BTMockRecord(Database db, int value) throws CoreException {
			this.db = db;
			record = db.malloc(BTMockRecord.RECORD_SIZE);
			db.putInt(record + VALUE_PTR, value);
		}   

		/**
		 * Get an existing record
		 */
		public BTMockRecord(long record, Database db) {
			this.db = db;
			this.record = record;
		}

		public int intValue() throws CoreException {
			return db.getInt(record);
		}

		public long getRecord() {
			return record;
		}
	}

	private class BTMockRecordComparator implements IBTreeComparator {
		@Override
		public int compare(long record1, long record2) throws CoreException {
			return db.getInt(record1) - db.getInt(record2); 
		}
	}
}
