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

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.debug.core.model.ICModule;
import org.eclipse.cdt.debug.internal.ui.views.AbstractViewerState;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.DebugException;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.TreeItem;
 
/**
 * Memento of the expanded and selected items in a modules viewer.
 */
public class ModulesViewerState extends AbstractViewerState {

	/** 
	 * Constructor for ModulesViewerState. 
	 */
	public ModulesViewerState( TreeViewer viewer ) {
		super( viewer );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.ui.views.AbstractViewerState#encodeElement(org.eclipse.swt.widgets.TreeItem)
	 */
	protected IPath encodeElement( TreeItem item ) throws DebugException {
		String name = getTreeItemName( item );
		IPath path = new Path( name );
		TreeItem parent = item.getParentItem();
		while( parent != null ) {
			name = getTreeItemName( parent );
			path = new Path( name ).append( path );
			parent = parent.getParentItem();
		}
		return path;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.ui.views.AbstractViewerState#decodePath(org.eclipse.core.runtime.IPath, org.eclipse.jface.viewers.TreeViewer)
	 */
	protected Object decodePath( IPath path, TreeViewer viewer ) throws DebugException {
		ITreeContentProvider contentProvider = (ITreeContentProvider)viewer.getContentProvider();
		String[] names = path.segments();
		Object parent = viewer.getInput();
		Object element = null;
		for( int i = 0; i < names.length; i++ ) {
			element = null;
			Object[] children = contentProvider.getChildren( parent );
			for( int j = 0; j < children.length; j++ ) {
				String name = getElementName( children[j] );
				if ( names[i].equals( name ) ) {
					element = children[j];
					break;
				}
			}
			if ( element == null ) {
				return null;
			}
			parent = element;
		}
		return element;
	}

	private String getTreeItemName( TreeItem item ) {
		Object data = item.getData();
		String name = null;
		if ( data instanceof ICModule ) {
			name = ((ICModule)data).getName();
		}
		else if ( data instanceof ICElement ) {
			name = ((ICElement)data).getElementName();
		}
		return name;
	}

	private String getElementName( Object element ) {
		if ( element instanceof ICModule ) {
			return ((ICModule)element).getName();
		}
		if ( element instanceof ICElement ) {
			return ((ICElement)element).getElementName();
		}
		return null;
	}
}
