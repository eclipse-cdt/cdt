/*******************************************************************************
 * Copyright (c) 2007 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.settings.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.internal.core.settings.model.MultiConfigDescription;

/**
 *
 *
 */
public abstract class MultiItemsHolder implements ICMultiItemsHolder {
	protected int fListMode = MODE_DEFAULT;
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.settings.model.ICMultiItemsHolder#getItems()
	 */
	public abstract Object[] getItems();

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.settings.model.ICMultiItemsHolder#getStringListMode()
	 */
	public int getStringListMode() {
		return fListMode;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.settings.model.ICMultiItemsHolder#setStringListMode(int)
	 */
	public void setStringListMode(int mode) {
		int a = mode & DMODES;
		int b = mode & WMODES;
		if (a == DMODES || b == WMODES) { // conflicting settings; 
			CCorePlugin.log("Wrong string list mode: " + mode);
			return;
		} 
		else if (a == 0) // display mode not set
			mode |= (fListMode & DMODES); // use existing
		else if (b == 0) // write mode not set
			mode |= (fListMode & WMODES); // use existing
		fListMode = mode & (DMODES | WMODES);
	}

	public final String[] getStrListForDisplay(String[][] input) {
		return getStrListForDisplay(input, getStringListMode());
	}
	
	public final String[] getStrListForDisplay(String[][] input, int mode) {
		Object[] ob = getListForDisplay(input, getStringListMode(), null);
		String[] ss = new String[ob.length];
		System.arraycopy(ob, 0, ss, 0, ob.length);
		return ss;
	}

	public final Object[] getListForDisplay(Object[][] input, Comparator cmp) {
		return getListForDisplay(input, getStringListMode(), cmp);
	}
	/**
	 * Utility method forms string list
	 * according to current list display mode
	 * 
	 * @param input - array of string arrays
	 * @return
	 */
	private final Object[] getListForDisplay(Object[][] input, int mode, Comparator cmp) {
		if (input == null || input.length == 0)
			return EMPTY_ARRAY;
		if (input.length == 1) {
			return (input[0] == null) ?
					EMPTY_ARRAY :
					input[0];
		}

		Object[] s1 = input[0];
		if (s1 == null || 
			s1.length == 0)
			return EMPTY_ARRAY;

		if ((mode & DMODES) == DMODE_EMPTY) {
			Arrays.sort(s1, cmp);
			for (int i=1; i<input.length; i++) {
				Object[] s2 = input[i];
				if (s2 == null || 
					s2.length == 0 || 
					s1.length != s2.length)
					return EMPTY_ARRAY;
				Arrays.sort(s2, cmp);
				if (! Arrays.equals(s1, s2))
					return EMPTY_ARRAY;
			}
			return s1; // returns sorted strings !
		} 
		else if ((getStringListMode() & DMODES) == DMODE_CONJUNCTION) 
		{ 
			ArrayList lst = new ArrayList();
			for (int i=0; i<s1.length; i++) {
				if (s1[i] == null)
					continue;
				boolean found = true;
				for (int k = 1; k<input.length; k++) {
					Object[] s2 = input[k];
					if (s2 == null || s2.length == 0)
						return EMPTY_ARRAY;
					if (i == 0)
						Arrays.sort(s2, cmp);
					if (Arrays.binarySearch(s2, s1[i], cmp) < 0) {
						found = false;
						break;
					}
				}
				if (found) {
					lst.add(s1[i]);
				}
			}
			return lst.toArray();
		}
		else // DMODE_ALL
		{
			HashSet lst = new HashSet(); // set, to avoid doubles
			for (int i=0; i<input.length; i++) {
				if (input[i] == null ||
					input[i].length == 0)
					continue;
				for (int j=0; j<input[i].length; j++)
					lst.add(input[i][j]);
			}
			s1 = lst.toArray();
			Arrays.sort(s1, cmp);
			return s1;
		}
	}

	/**
	 * This method is put here to prevent UI from 
	 * accessing constructors in "internal" dirs. 
	 * 
	 * Creates multiple configuration description.
	 * If there's 1 cfg.desc in array, 
	 * it's returned itself. 
	 * 
	 * @param rds - array of cfg.descs
	 *  
	 * @param mode - string list display and write mode
	 * @see DMODE_CONJUNCTION
	 * @see DMODE_EMPTY
	 * @see DMODE_ALL
	 * @see WMODE_DIFF
	 * @see WMODE_CURRENT
	 *
	 * @return multiple cfg.description or single cfg.desc.
	 */
	public static ICConfigurationDescription createCDescription(ICConfigurationDescription[] rds, int mode) {
		if (rds == null || rds.length == 0)
			return null;
		else if (rds.length == 1)
			return rds[0];
		else
			return new MultiConfigDescription(rds, mode);
	}
}
