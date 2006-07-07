/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 

package org.eclipse.cdt.internal.ui.viewsupport;

import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

/**
 * Utility to perform next/previous navigation on a tree.
 * @author markus.schorn@windriver.com
 */
public class TreeNavigator {
	private Tree fTree;
	private Class fDataClass;

	/**
	 * Creates a tree navigator for the given tree. It allows for finding tree items.
	 * In case you supply a dataClass only nodes with data of this class are considered.
	 * @param tree the tree to operate on
	 * @param dataClass the required class for the data of the tree nodes, or <code>null</code>.
	 */
	public TreeNavigator(Tree tree, Class dataClass) {
		fTree= tree;
		fDataClass= dataClass;
	}

	/**
	 * Find the first valid item of the selection. 
	 * @return the first valid item in the selection or <code>null</code>
	 */	
	public TreeItem getSelectedItem() {
        return getItemOfClass(fTree.getSelection(), true);
	}
	
	private TreeItem getItemOfClass(TreeItem[] items, boolean fwd) {
        for (int i = 0; i < items.length; i++) {
            TreeItem item = items[fwd ? i : items.length-1-i];
            if (fDataClass==null || fDataClass.isInstance(item.getData())) {
                return item;
            }
        }
        return null;
	}

	/**
	 * Find the first valid item on the given level. All parents have to be valid also. 
	 * @param level the level to search, use <code>0</code> for the root nodes of the tree.
	 * @param fwd if set to false the tree is searched reverse from the buttom.
	 * @return the first item on the given level, or <code>null</code>
	 */
	public TreeItem getFirstItemOnLevel(int level, boolean fwd) {
		return getFirstOnLevel(fTree.getItems(), level, fwd);
	}
	private TreeItem getFirstOnLevel(TreeItem[] items, int level, boolean fwd) {
		TreeItem item= getItemOfClass(items, fwd);
		if (level <= 0 || item == null) {
			return item;
		}
		return getFirstOnLevel(item.getItems(), level-1, fwd);
	}

	/** 
	 * Combines the methods {@link TreeNavigator#getSelectedItem()} and
	 * {@link TreeNavigator#getFirstItemOnLevel(int, boolean)}.
	 * @param level the level to search, use <code>0</code> for the root nodes of the tree.
	 * @param fwd if set to false the tree is searched reverse from the buttom.
	 * @return the first valid item of the selection or the first item on the given level, or <code>null</code>
	 */
	public TreeItem getSelectedItemOrFirstOnLevel(int level, boolean fwd) {
		TreeItem result= getSelectedItem();
		if (result == null) {
			result= getFirstItemOnLevel(level, fwd);
		}
		return result;
	}
	
	/**
	 * Searches for the next valid sibbling of the given item.
	 * @param current a tree item to start the search
	 * @param forward if false the previous sibbling is returned
	 * @return the next sibbling after the given one, or <code>null</code>
	 */
	public TreeItem getNextSibbling(TreeItem current, boolean forward) {
        TreeItem parentItem= current.getParentItem();
        int itemCount = parentItem.getItemCount();
        if (parentItem != null && itemCount > 1) {
            int index= parentItem.indexOf(current);
            index = (index + (forward ? 1 : itemCount-1)) % itemCount;
            return parentItem.getItem(index);
        }
        return null;
    }
}
