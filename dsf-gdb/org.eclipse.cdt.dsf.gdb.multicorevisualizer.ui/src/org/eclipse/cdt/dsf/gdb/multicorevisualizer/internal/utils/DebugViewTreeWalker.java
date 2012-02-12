/*******************************************************************************
 * Copyright (c) 2012 Tilera Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     William R. Swanson (Tilera Corporation) - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.utils;

import org.eclipse.debug.internal.ui.viewers.model.provisional.TreeModelViewer;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

/**
 * Base class that walks Debug View tree elements.
 * 
 * Intended to be subclassed by code that needs to walk the content
 * of the Debug View (e.g. to find elements or construct model deltas).
 * 
 * In the simplest case, a derived class should only need to
 * implement processElement(), and one should then only need
 * to call walk() to walk the tree and get an appropriate delta.
 */
@SuppressWarnings("restriction") // allow access to internal classes
public class DebugViewTreeWalker
{
	// --- members ---

	/** Debug View tree viewer */
	TreeModelViewer m_viewer = null;


	// --- constructors/destructors ---

	/** Constructor */
	public DebugViewTreeWalker()
	{
		m_viewer = DebugViewUtils.getDebugViewer();
	}
	
	/** Dispose method */
	public void dispose()
	{
		m_viewer = null;
	}


	// --- methods ---
	
	/** Walks the Debug View's tree,
	 *  calling processElement for each element.
	 */
	public void walk()
	{
		walk(getRootPath());
	}

	/**
	 * Walks the Debug View's tree from the specified element.
	 * This method should invoke processElement on the element
	 * itself, and walkChildren() to process the children of the element.
	 */
	public void walk(TreePath path)
	{
		if (path == null) return;
		boolean processChildren = processElement(path);
		if (processChildren) {
			walkChildren(path);
		}
	}
	
	/** Walks children of the specified element.
	 *  This method should invoke walk() to process
	 *  each child element.
	 */
	public void walkChildren(TreePath path)
	{
		if (path == null) return;
		int children = m_viewer.getChildCount(path);
		if (children > 0) {
			for (int i=0; i<children; ++i) {
				Object child = m_viewer.getChildElement(path, i);
				if (child != null) {
					TreePath childPath = path.createChildPath(child);
					walk(childPath);
				}
			}
		}
	}
	
	/** Processes an element of the tree view.
	 *  Returns true if children of this element should be processed,
	 *  and false if they can be skipped.
	 */
	public boolean processElement(TreePath path)
	{
		return true;
	}
	

	// --- tree path utilities ---

	/** Gets tree path of root element. */
	public TreePath getRootPath()
	{
		// (?) This doesn't always return the expected root element.
		//return m_viewer.getTopElementPath();
		
		TreePath path = null;
		if (m_viewer != null) {
			Tree tree = (Tree) m_viewer.getControl();
			TreeItem[] items = tree.getItems();
			Object root = (items == null || items.length == 0) ? null : items[0].getData();
			if (root != null) path = new TreePath(new Object[] {root});
		}
		return path;
	}

	/** Gets tree path for child element. */
	public static TreePath getChildPath(TreePath path, Object childElement)
	{
		return path.createChildPath(childElement);
	}
	
	/** Gets element from path. */
	public static Object getElement(TreePath path)
	{
		return (path == null) ? null : path.getLastSegment();
	}
}