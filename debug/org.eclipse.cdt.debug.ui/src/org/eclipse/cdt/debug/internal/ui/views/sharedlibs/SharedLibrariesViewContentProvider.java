/**********************************************************************
 * Copyright (c) 2004 QNX Software Systems and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
 ***********************************************************************/
package org.eclipse.cdt.debug.internal.ui.views.sharedlibs;

import java.util.HashMap;

import org.eclipse.cdt.debug.core.model.ICDebugTarget;
import org.eclipse.cdt.debug.internal.ui.views.IDebugExceptionHandler;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.debug.core.DebugException;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * Provides content for the shared libraries view.
 * 
 * @since: Jan 21, 2003
 */
public class SharedLibrariesViewContentProvider implements ITreeContentProvider {

	/**
	 * A table that maps children to their parent element such that this
	 * content provider can walk back up the parent chain (since values do not
	 * know their parent). Map of <code>IVariable</code> (child) -><code>IVariable</code>
	 * (parent).
	 */
	private HashMap fParentCache;

	/**
	 * Handler for exceptions as content is retrieved
	 */
	private IDebugExceptionHandler fExceptionHandler;

	/**
	 * Constructor for SharedLibrariesViewContentProvider.
	 */
	public SharedLibrariesViewContentProvider() {
		setParentCache( new HashMap( 10 ) );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(Object)
	 */
	public Object[] getChildren( Object parent ) {
		if ( parent instanceof ICDebugTarget ) {
			Object[] children = null;
			ICDebugTarget target = (ICDebugTarget)parent;
			try {
				if ( target != null )
					children = target.getSharedLibraries();
				if ( children != null ) {
					cache( parent, children );
					return children;
				}
			}
			catch( DebugException e ) {
				if ( getExceptionHandler() != null )
					getExceptionHandler().handleException( e );
				else
					CDebugUIPlugin.log( e );
			}
		}
		return new Object[0];
	}

	/**
	 * Caches the given elememts as children of the given parent.
	 * 
	 * @param parent
	 *            parent element
	 * @param children
	 *            children elements
	 */
	protected void cache( Object parent, Object[] children ) {
		for ( int i = 0; i < children.length; i++ ) {
			getParentCache().put( children[i], parent );
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(Object)
	 */
	public Object getParent( Object element ) {
		return getParentCache().get( element );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(Object)
	 */
	public boolean hasChildren( Object parent ) {
		if ( parent instanceof ICDebugTarget ) {
			try {
				ICDebugTarget target = (ICDebugTarget)parent;
				return target.hasSharedLibraries();
			}
			catch( DebugException e ) {
				CDebugUIPlugin.log( e );
			}
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(Object)
	 */
	public Object[] getElements( Object inputElement ) {
		return getChildren( inputElement );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
	 */
	public void dispose() {
		setParentCache( null );
		setExceptionHandler( null );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(Viewer,
	 *      Object, Object)
	 */
	public void inputChanged( Viewer viewer, Object oldInput, Object newInput ) {
		clearCache();
	}

	protected void clearCache() {
		if ( getParentCache() != null ) {
			getParentCache().clear();
		}
	}

	/**
	 * Remove the cached parent for the given children
	 * 
	 * @param children
	 *            for which to remove cached parents
	 */
	public void removeCache( Object[] children ) {
		if ( getParentCache() != null ) {
			for ( int i = 0; i < children.length; i++ ) {
				getParentCache().remove( children[i] );
			}
		}
	}

	/**
	 * Sets an exception handler for this content provider.
	 * 
	 * @param handler debug exception handler or <code>null</code>
	 */
	protected void setExceptionHandler( IDebugExceptionHandler handler ) {
		this.fExceptionHandler = handler;
	}

	/**
	 * Returns the exception handler for this content provider.
	 * 
	 * @return debug exception handler or <code>null</code>
	 */
	protected IDebugExceptionHandler getExceptionHandler() {
		return this.fExceptionHandler;
	}	

	private HashMap getParentCache() {
		return this.fParentCache;
	}

	private void setParentCache( HashMap parentCache ) {
		this.fParentCache = parentCache;
	}
}
