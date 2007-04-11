/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.rse.internal.importexport.files;

import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

/**
 * TreeScrollDropListener provides automatic scrolling for Trees during drag and drop
 * operations.
 * <p>
 * If the pointer drags over an item in the Tree near its upper or
 * lower edges, the Tree will scroll so as to make previous or successive items visible
 * onscreen.  This behaviour is consistent with that of popular GUI systems.
 * </p><p>
 * To use it send addDropListener(new TreeScrollDropListener(tree)) to the DropTarget
 * object attached to the Tree.
 * </p>
 */
public class TreeScrollDropListener extends DropTargetAdapter {
	private Tree tree;

	/**
	 * Constructs a Tree scrolling Drop Listener
	 * 
	 * @param tree the Tree that the DropTarget is attached to
	 */
	public TreeScrollDropListener(final Tree tree) {
		this.tree = tree;
	}

	/**
	 * Handles dragOver events.
	 * This is an implementation detail.
	 */
	public void dragOver(DropTargetEvent event) {
		Point point = tree.toControl(new Point(event.x, event.y));
		// Get the item directly under the point
		TreeItem item = tree.getItem(point);
		if (item == null) return;
		// Determine scroll direction according to whether we're nearer the top, middle, or bottom
		Rectangle clientArea = tree.getClientArea();
		int scrollRegionSize = Math.min(clientArea.height / 3, 24); // cut region into 3 parts
		if (scrollRegionSize < 8) return; // don't scroll if the control is too small to make sense
		TreeItem showItem = item;
		for (;;) {
			if (point.y < clientArea.y + scrollRegionSize) {
				// in upper region
				showItem = getPreviousVisibleItem(tree, showItem);
			} else if (point.y > clientArea.height + clientArea.y - scrollRegionSize) {
				// in lower region
				showItem = getNextVisibleItem(tree, showItem, false);
			} else {
				// in middle region
				break;
			}
			// Show the item (causes a scroll if it is outside of the visible region)
			if (showItem == null) break;
			tree.showItem(showItem);
			// Test that we actually scrolled, if we didn't try again with the next item
			if (item != tree.getItem(point)) break;
		}
	}

	/**
	 * Given a TreeItem, locates its last (lowest) visible item
	 * <p>
	 * Note that the item may not be actually rendered onscreen though it would be
	 * visible were the control scrolled appropriately.
	 * </p>
	 * 
	 * @param item the TreeItem whose last visible child is to be found
	 * @return the last visible child, or <code>item</code> if no visible children.
	 */
	private TreeItem getLastVisibleChild(TreeItem item) {
		if (!item.getExpanded()) return item;
		TreeItem[] items = item.getItems();
		if (items == null || items.length == 0) return item;
		return getLastVisibleChild(items[items.length - 1]);
	}

	/**
	 * Given a TreeItem, locates the following (below the specified item) visible TreeItem in a tree.
	 * <p>
	 * Note that the item may not be actually rendered onscreen though it would be
	 * visible were the control scrolled appropriately.
	 * </p>
	 * 
	 * @param tree the Tree containing the items
	 * @param item the TreeItem whose next visible neighbour is to be found
	 * @return the next visible item, or null if none.
	 */
	private TreeItem getNextVisibleItem(Tree tree, TreeItem item, boolean ignoreChildren) {
		TreeItem parent = item.getParentItem();
		TreeItem[] items = (parent != null) ? parent.getItems() : tree.getItems();
		if (items != null) {
			for (int i = 0; i < items.length; ++i) {
				if (items[i] == item) {
					if (!ignoreChildren && items[i].getExpanded()) {
						TreeItem[] children = items[i].getItems();
						if (children != null && children.length > 0) return children[0];
					}
					if (i + 1 < items.length) return items[i + 1];
					break;
				}
			}
			if (parent != null) return getNextVisibleItem(tree, parent, true);
		}
		return null;
	}

	/**
	 * Given a TreeItem, locates the previous (above the specified item) visible TreeItem in a tree.
	 * <p>
	 * Note that the item may not be actually rendered onscreen though it would be
	 * visible were the control scrolled appropriately.
	 * </p>
	 * 
	 * @param tree the Tree containing the items
	 * @param item the TreeItem whose previous visible neighbour is to be found
	 * @return the previous visible item, or null if none.
	 */
	private TreeItem getPreviousVisibleItem(Tree tree, TreeItem item) {
		TreeItem parent = item.getParentItem();
		TreeItem[] items = (parent != null) ? parent.getItems() : tree.getItems();
		if (items != null) {
			for (int i = items.length - 1; i > 0; --i) {
				if (items[i] == item) return getLastVisibleChild(items[i - 1]);
			}
		}
		return parent;
	}
}
