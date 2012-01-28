/*******************************************************************************
 *  Copyright (c) 2012, 2012 Andrew Gvozdev and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      Andrew Gvozdev - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.utils;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.cdt.internal.core.WeakHashSet;
import org.eclipse.cdt.internal.core.WeakHashSetSynchronized;

/**
 * Test suite to test {@link WeakHashSet}.
 */
public class WeakHashSetTest extends TestCase {
	/**
	 * Sample mock class with specialized hashCode()
	 */
	private class MockClass {
		private String str;
		private MockClass(String str) {
			super();
			this.str = str;
		}
		@Override
		public int hashCode() {
			// for test purpose make hashcodes equal for all "str" stating with the same letter
			// note that "equals()" still reports difference
			String s = str.substring(0,1);
			return s.hashCode();
		}
		@Override
		public boolean equals(Object obj) {
			MockClass other = (MockClass) obj;
			return str.equals(other.str);
		}
	}

	public static Test suite() {
		return new TestSuite(WeakHashSetTest.class);
	}

	/**
	 * Test basic methods of {@link WeakHashSet}.
	 */
	public void testHashSetBasic() {
		// create sample objects
		WeakHashSet<MockClass> weakSet = new WeakHashSet<MockClass>();
		MockClass a1 = new MockClass("a");
		MockClass a2 = new MockClass("a");

		// check contains()
		assertEquals(false, weakSet.contains(a1));
		assertEquals(null, weakSet.get(a1));
		// check add() and get()
		assertSame(a1, weakSet.add(a1));
		assertSame(a1, weakSet.add(a2));
		assertSame(a1, weakSet.get(a1));
		assertSame(a1, weakSet.get(a2));
		assertEquals(true, weakSet.contains(a1));
		assertEquals(true, weakSet.contains(a2));
		// check remove()
		MockClass aOld = weakSet.remove(a2);
		assertSame(a1, aOld);
		assertEquals(null, weakSet.get(a1));
		assertSame(a2, weakSet.add(a2));
		assertSame(a2, weakSet.add(a1));

		// create sample objects with the same hashcode
		MockClass aa = new MockClass("aa");
		MockClass ab = new MockClass("ab");
		assertEquals(aa.hashCode(), ab.hashCode());
		assertEquals(false, aa.equals(ab));
		// check add() and get()
		assertEquals(false, weakSet.contains(aa));
		assertEquals(false, weakSet.contains(ab));
		assertEquals(null, weakSet.get(aa));
		assertEquals(null, weakSet.get(ab));
		assertSame(aa, weakSet.add(aa));
		assertSame(ab, weakSet.add(ab));
		assertEquals(true, weakSet.contains(aa));
		assertEquals(true, weakSet.contains(ab));
		assertSame(aa, weakSet.get(aa));
		assertSame(ab, weakSet.get(ab));
	}

	/**
	 * Test synchronized {@link WeakHashSetSynchronized}.
	 * Note that regular {@link WeakHashSet} would fail the test.
	 */
	public void testHashSetSyncronization() throws Exception {
		final WeakHashSet<Integer> weakSet = new WeakHashSetSynchronized<Integer>(1);

		Thread[] threads= new Thread[5000];
		for (int i = 0; i < threads.length; i++) {
			final Integer n = i;
			Thread t= new Thread() {
				@Override
				public void run() {
					weakSet.add(n);
				}
			};
			threads[i] = t;
			t.start();
		}

		for (int i = 0; i < threads.length; i++) {
			threads[i].join();
		}
		assertEquals(threads.length, weakSet.size());

		for (int i = 0; i < threads.length; i++) {
			assertEquals(true, weakSet.contains(i));
		}
	}
}
