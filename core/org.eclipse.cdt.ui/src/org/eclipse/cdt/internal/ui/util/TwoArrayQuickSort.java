/*******************************************************************************
 * Copyright (c) 2000 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     QNX Software System
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.util;

import org.eclipse.jface.util.Assert;


/**
 * Quick sort to sort two arrays in parallel.
 */
public class TwoArrayQuickSort {

	private static void internalSort(String[] keys, Object[] values, int left, int right, boolean ignoreCase) { 
	
		int original_left= left;
		int original_right= right;
		
		String mid= keys[(left + right) / 2]; 
		do { 
			while (smaller(keys[left], mid, ignoreCase)) { 
				left++; 
			} 
			while (smaller(mid, keys[right], ignoreCase)) { 
				right--; 
			} 
			if (left <= right) { 
				String tmp= keys[left]; 
				keys[left]= keys[right]; 
				keys[right]= tmp;
				
				Object tmp2= values[left]; 
				values[left]= values[right]; 
				values[right]= tmp2;
				
				left++; 
				right--; 
			} 
		} while (left <= right);
		
		if (original_left < right) {
			internalSort(keys , values, original_left, right, ignoreCase); 
		}	
		if (left < original_right) {
			 internalSort(keys, values, left, original_right, ignoreCase); 
		} 
	}
	private static boolean smaller(String left, String right, boolean ignoreCase) {
		if (ignoreCase)
			return left.compareToIgnoreCase(right) < 0;
		return left.compareTo(right) < 0;	
	}
	/**
	 * Sorts keys and values in parallel.
	 */
	public static void sort(String[] keys, Object[] values, boolean ignoreCase) { 
		if (keys != null && values != null) {
			Assert.isTrue(keys.length == values.length);
			if (keys.length > 1)
				internalSort(keys, values, 0, keys.length - 1, ignoreCase);	
		} else {
			if (keys != null || values != null)
				Assert.isTrue(false, "Either keys or values in null"); //$NON-NLS-1$
		}
	}
}
