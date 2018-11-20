/*******************************************************************************
 * Copyright (c) 2013 Google, Inc and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.refactoring.includes;

import org.eclipse.cdt.internal.ui.refactoring.includes.IncludeMap;

import junit.framework.TestCase;

/**
 * Tests for {@link IncludeMap}.
 */
public class IncludeMapTest extends TestCase {

	private void assertEqualMaps(IncludeMap expected, IncludeMap actual) {
		assertEquals(expected.toString(), actual.toString());
	}

	public void testOptionalCyclicMap() {
		IncludeMap map = new IncludeMap(false, new String[] { "a", "b", "b", "c", "c", "d", "d", "b", });
		map.transitivelyClose();
		IncludeMap expected = new IncludeMap(false, new String[] { "a", "b", "a", "d", "a", "c", "b", "d", "b", "c",
				"c", "d", "c", "b", "d", "b", "d", "c", });
		assertEqualMaps(expected, map);
	}

	public void testUnconditionalCyclicMap() {
		IncludeMap map = new IncludeMap(true, new String[] { "a", "b", "b", "c", "c", "d", "d", "b", });
		map.transitivelyClose();
		IncludeMap expected = new IncludeMap(true, new String[] { "a", "b", "c", "b", "d", "b", });
		assertEqualMaps(expected, map);
	}

	public void testOptionalMap() {
		IncludeMap map = new IncludeMap(false, new String[] { "a", "b", "a", "c", "c", "d", "c", "e", "d", "f", });
		map.transitivelyClose();
		IncludeMap expected = new IncludeMap(false, new String[] { "a", "b", "a", "f", "a", "d", "a", "e", "a", "c",
				"c", "f", "c", "d", "c", "e", "d", "f", });
		assertEqualMaps(expected, map);
	}

	public void testUpconditionalMap() {
		IncludeMap map = new IncludeMap(true, new String[] { "a", "b", "a", "c", "c", "d", "c", "e", "d", "f", });
		map.transitivelyClose();
		IncludeMap expected = new IncludeMap(true, new String[] { "a", "b", "c", "f", "d", "f", });
		assertEqualMaps(expected, map);
	}
}
