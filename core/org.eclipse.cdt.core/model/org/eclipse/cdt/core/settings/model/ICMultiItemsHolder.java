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

import java.util.Comparator;

/**
 * Implementors of this interface are intended 
 * to hold 1 or more items and perform
 * some simultaneous operations on them.
 * 
 * There are no any restrictions for items
 * types to be held. 
 * 
 * As common rule, items are set in constructor
 * and their list cannot be changed in life time. 
 *
 */
public interface ICMultiItemsHolder {
	/* 
	 * Constants for String list display mode 
	 */
	// display conjunction of lists entries (common ones)
	public static final int DMODE_CONJUNCTION = 1;
	// display empty list if item's lists are different
	public static final int DMODE_EMPTY = 2;
	// display all items from all lists (except doubles)
	public static final int DMODE_ALL = 4;
	// display modes mask
	public static final int DMODES = DMODE_CONJUNCTION | DMODE_EMPTY | DMODE_ALL; 
	/*
	 * Constants for string list apply mode
	 */
	// write to all items the list presented now. 
	public static final int WMODE_CURRENT = 8;
	// apply to all items all insertions/deletions made 
	public static final int WMODE_DIFF = 16;
	// write modes mask
	public static final int WMODES = WMODE_CURRENT | WMODE_DIFF;
	
	// default setting
	public static final int MODE_DEFAULT = DMODE_CONJUNCTION | WMODE_CURRENT;

	/*
	 * General purpose objects
	 */
	public static final String EMPTY_STR = "";
	public static final Object[] EMPTY_ARRAY = new Object[0];
	

	/**
	 * Returns array of items which it holds 
	 * @return 
	 */
	Object[] getItems();

	/**
	 * @see DMODE_CONJUNCTION
	 * @see DMODE_EMPTY
	 * @see DMODE_ALL
	 * @see WMODE_DIFF
	 * @see WMODE_CURRENT

	 * @return current string list mode (OR'ed display and write modes)
	 */
	int getStringListMode();
	
	/**
	 * @see DMODE_CONJUNCTION
	 * @see DMODE_EMPTY
	 * @see DMODE_ALL
	 * @see WMODE_DIFF
	 * @see WMODE_CURRENT
	 * 
	 * @param mode: OR'ed display and write modes
	 */
	void setStringListMode(int mode);
	
	/*
	 * A set of methods which form an array of objects
	 * on a basis of 2-dim array and DISPLAY MODE
	 */
	String[] getStrListForDisplay(String[][] input);
	String[] getStrListForDisplay(String[][] input, int mode);
//	Object[] getListForDisplay(Object[][] input);
//	Object[] getListForDisplay(Object[][] input, int mode);
	Object[] getListForDisplay(Object[][] input, Comparator cmp);
//	Object[] getListForDisplay(Object[][] input, int mode, Comparator cmp);

}
