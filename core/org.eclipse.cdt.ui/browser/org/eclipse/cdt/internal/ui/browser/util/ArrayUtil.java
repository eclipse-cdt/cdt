/*******************************************************************************
 * Copyright (c) 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.browser.util;

/**
 * A helper class which allows you to perform some
 * simple set operations on int arrays.
 */
public class ArrayUtil {
	private ArrayUtil() {
	}

	// returns true if set contains elem
	public static boolean contains(int[] set, int elem) {
		for (int i= 0; i < set.length; ++i) {
			if (set[i] == elem)
				return true;
		}
		return false;
	}
	
	// returns true if set contains all of subset
	public static boolean containsAll(int[] set, int[] subset) {
		for (int i= 0; i < subset.length; ++i) {
			if (!contains(set, subset[i]))
				return false;
		}
		return true;
	}
	
	// return a copy of fromSet
	public static int[] clone(int[] fromSet) {
		int[] newSet= new int[fromSet.length];
		for (int i= 0; i < fromSet.length; ++i) {
			newSet[i]= newSet[i];
		}
		return newSet;
	}
}
