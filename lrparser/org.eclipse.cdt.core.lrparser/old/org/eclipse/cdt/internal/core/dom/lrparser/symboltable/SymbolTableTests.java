/*******************************************************************************
 *  Copyright (c) 2008, 2016 IBM Corporation and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *      IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.lrparser.symboltable;

//import junit.framework.TestCase;

import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.internal.core.dom.lrparser.c99.bindings.C99Label;
import org.eclipse.cdt.internal.core.dom.lrparser.c99.bindings.C99Structure;
import org.eclipse.cdt.internal.core.dom.lrparser.c99.bindings.C99Variable;

@SuppressWarnings("nls")
public class SymbolTableTests {//extends TestCase {
	// TODO write tests for imperative symbol table

	private final String[] KEYS = { "pantera", "soulfly", "inflames", "megadeth", "archenemy", "carcass" };

	public void testPersistence() {
		FunctionalMap<String, Integer> st0 = FunctionalMap.emptyMap();
		assertTrue(st0.isEmpty());

		FunctionalMap<String, Integer> st1 = st0.insert(KEYS[0], 1);

		// empty symbol table does not change
		assertTrue(st0.isEmpty());
		assertNull(st0.lookup(KEYS[1]));

		// a new symbol table was created
		assertFalse(st1.isEmpty());
		assertEquals(Integer.valueOf(1), st1.lookup(KEYS[0]));

		FunctionalMap<String, Integer> st2 = st1.insert(KEYS[1], 2);
		FunctionalMap<String, Integer> st3 = st2.insert(KEYS[2], 3);
		FunctionalMap<String, Integer> st4 = st3.insert(KEYS[3], 4);
		FunctionalMap<String, Integer> st5 = st4.insert(KEYS[4], 5);

		assertMap(st0, KEYS, new Integer[] { null, null, null, null, null, null });
		assertMap(st1, KEYS, new Integer[] { 1, null, null, null, null, null });
		assertMap(st2, KEYS, new Integer[] { 1, 2, null, null, null, null });
		assertMap(st3, KEYS, new Integer[] { 1, 2, 3, null, null, null });
		assertMap(st4, KEYS, new Integer[] { 1, 2, 3, 4, null, null });
		assertMap(st5, KEYS, new Integer[] { 1, 2, 3, 4, 5, null });
	}

	// these are here just to get the tests to compile
	// JUnit is not available in this plugin, this is old code that I just didn't want to delete

	@SuppressWarnings("unused")
	private void assertEquals(Integer integer, Integer lookup) {
		// TODO Auto-generated method stub

	}

	@SuppressWarnings("unused")
	private void assertFalse(boolean empty) {
		// TODO Auto-generated method stub

	}

	@SuppressWarnings("unused")
	private void assertNull(Integer lookup) {
		// TODO Auto-generated method stub

	}

	@SuppressWarnings("unused")
	private void assertTrue(boolean empty) {
		// TODO Auto-generated method stub

	}

	public void testOverride() {
		FunctionalMap<String, Integer> map1 = FunctionalMap.emptyMap();
		for (int i = 0; i < KEYS.length; i++) {
			map1 = map1.insert(KEYS[i], i);
		}

		assertMap(map1, KEYS, new Integer[] { 0, 1, 2, 3, 4, 5 });

		FunctionalMap<String, Integer> map2 = map1.insert(KEYS[5], 999);
		FunctionalMap<String, Integer> map3 = map2.insert(KEYS[5], null);

		assertEquals(Integer.valueOf(5), map1.lookup(KEYS[5]));
		assertEquals(Integer.valueOf(999), map2.lookup(KEYS[5]));
		assertNull(map3.lookup(KEYS[5]));
	}

	@SuppressWarnings("unchecked")
	private static void assertMap(FunctionalMap map, Comparable[] keys, Object[] vals) {
		assert keys.length == vals.length;

		for (int i = 0; i < keys.length; i++) {
			assertEquals("the key '" + keys[i] + "' did not match", vals[i], map.lookup((keys[i])));
			if (vals[i] != null) {
				assertTrue("key '" + keys[i] + "' not in map", map.containsKey(keys[i]));
			}
		}
	}

	@SuppressWarnings("unused")
	private static void assertTrue(String string, boolean containsKey) {
		// TODO Auto-generated method stub

	}

	@SuppressWarnings("unused")
	private static void assertEquals(String string, Object object, Object lookup) {
		// TODO Auto-generated method stub

	}

	public void testFunctionalSymbolTable1() {
		C99SymbolTable st = C99SymbolTable.EMPTY_TABLE;

		for (String key : KEYS) {
			st = st.insert(CNamespace.IDENTIFIER, key, new C99Variable(key));
		}
		for (String key : KEYS) {
			st = st.insert(CNamespace.GOTO_LABEL, key, new C99Label(key));
		}
		for (String key : KEYS) {
			st = st.insert(CNamespace.STRUCT_TAG, key, new C99Structure(key));
		}

		assertFunctionalSymbolTableContainsAllThePairs(st);
	}

	public void testFunctionalSymbolTable2() {
		C99SymbolTable st = C99SymbolTable.EMPTY_TABLE;

		// same test as above but this time we insert the keys in a different order
		for (String key : KEYS) {
			st = st.insert(CNamespace.IDENTIFIER, key, new C99Variable(key));
			st = st.insert(CNamespace.GOTO_LABEL, key, new C99Label(key));
			st = st.insert(CNamespace.STRUCT_TAG, key, new C99Structure(key));
		}

		assertFunctionalSymbolTableContainsAllThePairs(st);
	}

	private void assertFunctionalSymbolTableContainsAllThePairs(C99SymbolTable st) {
		assertEquals(KEYS.length * 3, st.size());
		for (String key : KEYS) {
			IBinding b = st.lookup(CNamespace.IDENTIFIER, key);
			assertNotNull(b);
			C99Variable x = (C99Variable) b;
			assertEquals(key, x.getName());
		}
		for (String key : KEYS) {
			IBinding b = st.lookup(CNamespace.GOTO_LABEL, key);
			assertNotNull(b);
			C99Label x = (C99Label) b;
			assertEquals(key, x.getName());
		}
		for (String key : KEYS) {
			IBinding b = st.lookup(CNamespace.STRUCT_TAG, key);
			assertNotNull(b);
			C99Structure x = (C99Structure) b;
			assertEquals(key, x.getName());
		}
	}

	@SuppressWarnings("unused")
	private void assertEquals(String key, String name) {
		// TODO Auto-generated method stub

	}

	@SuppressWarnings("unused")
	private void assertNotNull(IBinding b) {
		// TODO Auto-generated method stub

	}

	public void testProperFail() {
		FunctionalMap<Integer, Integer> map = FunctionalMap.emptyMap();
		try {
			map.insert(null, 99);
			fail();
		} catch (NullPointerException e) {
		}

		try {
			map.containsKey(null);
			fail();
		} catch (NullPointerException e) {
		}

		try {
			map.lookup(null);
			fail();
		} catch (NullPointerException e) {
		}

		C99SymbolTable table = C99SymbolTable.EMPTY_TABLE;
		try {
			table.insert(null, null, new C99Variable("blah")); //$NON-NLS-1$
			fail();
		} catch (NullPointerException e) {
		}

	}

	private void fail() {
		// TODO Auto-generated method stub

	}

}
