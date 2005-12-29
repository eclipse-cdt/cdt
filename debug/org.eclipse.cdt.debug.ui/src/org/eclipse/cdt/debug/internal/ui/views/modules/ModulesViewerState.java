/*******************************************************************************
 * Copyright (c) 2004, 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.views.modules; 

import java.util.ArrayList;
import java.util.List;
import org.eclipse.cdt.debug.internal.ui.views.AbstractViewerState;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.internal.ui.viewers.AsynchronousTreeViewer;
import org.eclipse.debug.internal.ui.viewers.TreePath;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
 
/**
 * Memento of the expanded and selected items in a modules viewer.
 */
public class ModulesViewerState extends AbstractViewerState {

	/** 
	 * Constructor for ModulesViewerState. 
	 */
	public ModulesViewerState( AsynchronousTreeViewer viewer ) {
		super( viewer );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.ui.views.AbstractViewerState#encodeElement(org.eclipse.swt.widgets.TreeItem)
	 */
	protected IPath encodeElement( TreeItem item ) throws DebugException {
		StringBuffer path = new StringBuffer( item.getText() );
		TreeItem parent = item.getParentItem();
		while( parent != null ) {
			path.insert( 0, parent.getText() + IPath.SEPARATOR );
			parent = parent.getParentItem();
		}
		return new Path( path.toString() );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.ui.views.AbstractViewerState#decodePath(org.eclipse.core.runtime.IPath, org.eclipse.debug.internal.ui.viewers.AsynchronousTreeViewer)
	 */
	protected TreePath decodePath( IPath path, AsynchronousTreeViewer viewer ) throws DebugException {
		String[] names = path.segments();
		Tree tree = viewer.getTree();
		TreeItem[] items = tree.getItems();
		List elements = new ArrayList();
		elements.add( viewer.getInput() );
		boolean pathFound = false;
		for( int i = 0; i < names.length; i++ ) {
			String name = names[i];
			TreeItem item = findItem( name, items );
			if ( item != null ) {
				pathFound = true;
				elements.add( item.getData() );
				items = item.getItems();
			}
		}
		if ( pathFound ) {
			return new TreePath( elements.toArray() );
		}
		return null;
	}

	private TreeItem findItem( String name, TreeItem[] items ) {
		for( int i = 0; i < items.length; i++ ) {
			TreeItem item = items[i];
			if ( item.getText().equals( name ) ) {
				return item;
			}
		}
		return null;
	}
}
