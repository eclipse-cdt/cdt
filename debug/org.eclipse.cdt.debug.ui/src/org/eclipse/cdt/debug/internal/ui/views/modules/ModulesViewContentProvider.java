/**********************************************************************
 * Copyright (c) 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 ***********************************************************************/ 
package org.eclipse.cdt.debug.internal.ui.views.modules; 

import java.util.HashMap;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.cdt.core.model.IParent;
import org.eclipse.cdt.debug.core.model.ICDebugTarget;
import org.eclipse.cdt.debug.core.model.ICModule;
import org.eclipse.cdt.debug.internal.ui.views.IDebugExceptionHandler;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.debug.core.DebugException;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
 
/**
 * Provides content for the Modules view.
 */
public class ModulesViewContentProvider implements ITreeContentProvider {

	/**
	 * A table that maps children to their parent element such that this
	 * content provider can walk back up the parent chain.
	 */
	private HashMap fParentCache;

	/**
	 * Handler for exceptions as content is retrieved
	 */
	private IDebugExceptionHandler fExceptionHandler;

	/** 
	 * Constructor for ModulesViewContentProvider. 
	 */
	public ModulesViewContentProvider() {
		setParentCache( new HashMap( 10 ) );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
	 */
	public Object[] getChildren( Object parent ) {
		if ( parent instanceof ICDebugTarget ) {
			Object[] children = null;
			ICDebugTarget target = (ICDebugTarget)parent;
			try {
				if ( target != null )
					children = target.getModules();
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
		else if ( parent instanceof ICModule ) {
			IBinary binary = (IBinary)((ICModule)parent).getAdapter( IBinary.class );
			if ( binary != null ) {
				try {
					return binary.getChildren();
				}
				catch( CModelException e ) {
				}
			}
		}
		else if ( parent instanceof IParent ) {
			try {
				return ((IParent)parent).getChildren();
			}
			catch( CModelException e ) {
			}
		}
		return new Object[0];
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
	 */
	public Object getParent( Object element ) {
		return getParentCache().get( element );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
	 */
	public boolean hasChildren( Object parent ) {
		if ( parent instanceof ICDebugTarget ) {
			try {
				ICDebugTarget target = (ICDebugTarget)parent;
				return target.hasModules();
			}
			catch( DebugException e ) {
				CDebugUIPlugin.log( e );
			}
		}
		else if ( parent instanceof ICModule ) {
			IBinary binary = (IBinary)((ICModule)parent).getAdapter( IBinary.class );
			if ( binary != null ) {
				return binary.hasChildren();
			}
		}
		else if ( parent instanceof IParent ) {
			return ((IParent)parent).hasChildren();
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
	 */
	public Object[] getElements( Object inputElement ) {
		return getChildren( inputElement );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
	 */
	public void dispose() {
		setParentCache( null );
		setExceptionHandler( null );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	public void inputChanged( Viewer viewer, Object oldInput, Object newInput ) {
		clearCache();
	}

	protected void setExceptionHandler( IDebugExceptionHandler handler ) {
		fExceptionHandler = handler;
	}

	protected IDebugExceptionHandler getExceptionHandler() {
		return fExceptionHandler;
	}	

	private HashMap getParentCache() {
		return fParentCache;
	}

	private void setParentCache( HashMap parentCache ) {
		fParentCache = parentCache;
	}

	protected void cache( Object parent, Object[] children ) {
		for ( int i = 0; i < children.length; i++ ) {
			getParentCache().put( children[i], parent );
		}
	}

	protected void clearCache() {
		if ( getParentCache() != null ) {
			getParentCache().clear();
		}
	}
}
