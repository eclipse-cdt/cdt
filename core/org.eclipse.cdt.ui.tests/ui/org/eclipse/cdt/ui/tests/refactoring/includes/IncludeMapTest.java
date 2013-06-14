/*******************************************************************************
 * Copyright (c) 2013 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.refactoring.includes;

import junit.framework.TestCase;

import org.eclipse.cdt.internal.ui.refactoring.includes.IncludeMap;

/**
 * Tests for {@link IncludeMap}.
 */
public class IncludeMapTest extends TestCase {

	private void assertEqualMaps(IncludeMap expected, IncludeMap actual) {
		assertEquals(expected.toString(), actual.toString());
	}

	public void testOptionalCyclicMap() {
		IncludeMap map = new IncludeMap(false, new String[] {
				"a", "b",
				"b", "c",
				"c", "d",
				"d", "b",
		});
		map.transitivelyClose();
		IncludeMap expected = new IncludeMap(false, new String[] {
				"a", "b",
				"a", "d",
				"a", "c",
				"b", "d",
				"b", "c",
				"c", "d",
				"c", "b",
				"d", "b",
				"d", "c",
		});
		assertEqualMaps(expected, map);
	}

	public void testUnconditionalCyclicMap() {
		IncludeMap map = new IncludeMap(true, new String[] {
				"a", "b",
				"b", "c",
				"c", "d",
				"d", "b",
		});
		map.transitivelyClose();
		IncludeMap expected = new IncludeMap(true, new String[] {
				"a", "b",
				"c", "b",
				"d", "b",
		});
		assertEqualMaps(expected, map);
	}

	public void testOptionalMap() {
		IncludeMap map = new IncludeMap(false, new String[] {
				"a", "b",
				"a", "c",
				"c", "d",
				"c", "e",
				"d", "f",
		});
		map.transitivelyClose();
		IncludeMap expected = new IncludeMap(false, new String[] {
				"a", "b",
				"a", "f",
				"a", "d",
				"a", "e",
				"a", "c",
				"c", "f",
				"c", "d",
				"c", "e",
				"d", "f",
		});
		assertEqualMaps(expected, map);
	}

	public void testUpconditionalMap() {
		IncludeMap map = new IncludeMap(true, new String[] {
				"a", "b",
				"a", "c",
				"c", "d",
				"c", "e",
				"d", "f",
		});
		map.transitivelyClose();
		IncludeMap expected = new IncludeMap(true, new String[] {
				"a", "b",
				"c", "f",
				"d", "f",
		});
		assertEqualMaps(expected, map);
	}
}
