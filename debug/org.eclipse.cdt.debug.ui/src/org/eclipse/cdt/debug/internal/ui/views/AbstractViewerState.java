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
package org.eclipse.cdt.debug.internal.ui.views; 
import java.util.ArrayList;
import java.util.List;
import org.eclipse.core.runtime.IPath;
import org.eclipse.debug.core.DebugException;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.TreeItem;

/**
 * The abstract superclass for mementos of the expanded and 
 * selected items in a tree viewer.
 */
public abstract class AbstractViewerState {

	// paths to expanded elements
	private List fExpandedElements = null;
	// paths to selected elements
	private IPath[] fSelection = null;
	
	/**
	 * Constructs a memento for the given viewer.
	 */
	public AbstractViewerState( TreeViewer viewer ) {
		saveState( viewer );
	}

	/**
	 * Saves the current state of the given viewer into
	 * this memento.
	 * 
	 * @param viewer viewer of which to save the state
	 */
	public void saveState( TreeViewer viewer ) {
		List expanded = new ArrayList();
		fExpandedElements = null;
		TreeItem[] items = viewer.getTree().getItems();
		try {
			for( int i = 0; i < items.length; i++ ) {
				collectExandedItems( items[i], expanded );
			}
			if ( expanded.size() > 0 ) {
				fExpandedElements = expanded;
			}
		}
		catch( DebugException e ) {
			fExpandedElements = null;
		}
		TreeItem[] selection = viewer.getTree().getSelection();
		fSelection = new IPath[selection.length];
		try {
			for( int i = 0; i < selection.length; i++ ) {
				fSelection[i] = encodeElement( selection[i] );
			}
		}
		catch( DebugException e ) {
			fSelection = null;
		}
	}

	protected void collectExandedItems( TreeItem item, List expanded ) throws DebugException {
		if ( item.getExpanded() ) {
			expanded.add( encodeElement( item ) );
			TreeItem[] items = item.getItems();
			for( int i = 0; i < items.length; i++ ) {
				collectExandedItems( items[i], expanded );
			}
		}
	}

	/**
	 * Constructs a path representing the given tree item. The segments in the
	 * path denote parent items, and the last segment is the name of
	 * the given item.
	 *   
	 * @param item tree item to encode
	 * @return path encoding the given item
	 * @throws DebugException if unable to generate a path
	 */
	protected abstract IPath encodeElement( TreeItem item ) throws DebugException;

	/**
	 * Restores the state of the given viewer to this memento's
	 * saved state.
	 * 
	 * @param viewer viewer to which state is restored
	 */
	public void restoreState( TreeViewer viewer ) {
		if ( fExpandedElements != null ) {
			List expansion = new ArrayList( fExpandedElements.size() );
			for( int i = 0; i < fExpandedElements.size(); i++ ) {
				IPath path = (IPath)fExpandedElements.get( i );
				if ( path != null ) {
					Object obj;
					try {
						obj = decodePath( path, viewer );
						if ( obj != null ) {
							expansion.add( obj );
						}
					}
					catch( DebugException e ) {
					}
				}
			}
			viewer.setExpandedElements( expansion.toArray() );
		}
		if ( fSelection != null ) {
			List selection = new ArrayList( fSelection.length );
			for( int i = 0; i < fSelection.length; i++ ) {
				IPath path = fSelection[i];
				Object obj;
				try {
					obj = decodePath( path, viewer );
					if ( obj != null ) {
						selection.add( obj );
					}
				}
				catch( DebugException e ) {
				}
			}
			viewer.setSelection( new StructuredSelection( selection ) );
		}
	}
	
	/**
	 * Returns an element in the given viewer that corresponds to the given
	 * path, or <code>null</code> if none.
	 * 
	 * @param path encoded element path
	 * @param viewer viewer to search for the element in
	 * @return element represented by the path, or <code>null</code> if none
	 * @throws DebugException if unable to locate a variable
	 */
	protected abstract Object decodePath( IPath path, TreeViewer viewer ) throws DebugException;
}
