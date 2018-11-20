/*******************************************************************************
 * Copyright (c) 2007, 2010 Intel Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.settings.model.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings("rawtypes")
public class ListComparator {
	private static Comparator fDefaultComparator;

	public static Comparator getDefaultComparator() {
		if (fDefaultComparator == null)
			fDefaultComparator = new Comparator();
		return fDefaultComparator;

	}

	public static List[] compare(Object a1[], Object a2[]) {
		return compare(a1, a2, getDefaultComparator());
	}

	public static List[] compare(Object a1[], Object a2[], Comparator c) {
		List added = getAdded(a1, a2, c);
		List removed = getAdded(a2, a1, c);

		if (added == null && removed == null)
			return null;

		return new List[] { added, removed };
	}

	public static List getAdded(Object a1[], Object a2[]) {
		return getAdded(a1, a2, getDefaultComparator());
	}

	public static List getAdded(Object a1[], Object a2[], Comparator c) {
		if (a1 == null || a1.length == 0)
			return null;
		if (a2 == null || a2.length == 0) {
			List<Object> list = new ArrayList<>(a1.length);
			for (int i = 0; i < a1.length; i++) {
				list.add(a1[i]);
			}
			return list;
		}

		List<Object> list = new ArrayList<>(a1.length);
		Object o1;
		for (int i = 0; i < a1.length; i++) {
			o1 = a1[i];
			for (int j = 0; j < a2.length; j++) {
				if (!c.equal(o1, a2[j]))
					list.add(o1);
			}
		}

		return list.size() != 0 ? list : null;
	}

	@SuppressWarnings("unchecked")
	public static boolean match(Object a1[], Object a2[], Comparator c) {
		if (a1 == null)
			return a2 == null;

		if (a2 == null)
			return false;

		if (a1.length != a2.length)
			return false;

		int size = a1.length;
		List list = new ArrayList(Arrays.asList(a1));
		List otherList = new ArrayList(Arrays.asList(a2));
		for (int i = size - 1; i >= 0; i--) {
			Object o1 = list.remove(i);
			int j = i;
			for (; j >= 0; j--) {
				if (c.equal(o1, otherList.get(j))) {
					otherList.remove(j);
					break;
				}
			}

			if (j < 0)
				return false;
		}

		return true;
	}

	public static int indexOf(Object o, Object[] a) {
		return indexOf(o, a, getDefaultComparator());
	}

	public static int indexOf(Object o, Object[] a, Comparator c) {
		for (int i = 0; i < a.length; i++) {
			if (c.equal(a[i], o))
				return i;
		}
		return -1;
	}

	public static boolean haveMatches(Object[] a1, Object[] a2) {
		return haveMatches(a1, a2, getDefaultComparator());
	}

	public static boolean haveMatches(Object[] a1, Object[] a2, Comparator c) {
		for (int i = 0; i < a1.length; i++) {
			if (indexOf(a1[i], a2, c) != -1)
				return true;
		}
		return false;
	}

}
