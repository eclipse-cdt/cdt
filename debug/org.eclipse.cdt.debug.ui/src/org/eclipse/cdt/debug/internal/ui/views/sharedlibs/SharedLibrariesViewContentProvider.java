/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.internal.ui.views.sharedlibs;

import java.util.HashMap;

import org.eclipse.cdt.debug.core.ICSharedLibraryManager;
import org.eclipse.cdt.debug.internal.ui.views.IDebugExceptionHandler;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * Enter type comment.
 * 
 * @since: Jan 21, 2003
 */
public class SharedLibrariesViewContentProvider implements ITreeContentProvider
{
	/**
	 * A table that maps children to their parent element
	 * such that this content provider can walk back up the
	 * parent chain (since values do not know their
	 * parent).
	 * Map of <code>IVariable</code> (child) -> <code>IVariable</code> (parent).
	 */
	private HashMap fParentCache;

	/**
	 * Handler for exceptions as content is retrieved
	 */
	private IDebugExceptionHandler fExceptionHandler = null;


	/**
	 * Constructor for SharedLibrariesViewContentProvider.
	 */
	public SharedLibrariesViewContentProvider()
	{
		fParentCache = new HashMap( 10 );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(Object)
	 */
	public Object[] getChildren( Object parent )
	{
		Object[] children = null;
		if ( parent != null && parent instanceof ICSharedLibraryManager )
		{
			children = ((ICSharedLibraryManager)parent).getSharedLibraries();
		}
		if ( children != null )
		{
			cache( parent, children );
			return children;
		}
		return new Object[0];
	}

	/**
	 * Caches the given elememts as children of the given
	 * parent.
	 * 
	 * @param parent parent element
	 * @param children children elements
	 */
	protected void cache( Object parent, Object[] children )
	{
		for ( int i = 0; i < children.length; i++ )
		{
			fParentCache.put( children[i], parent );
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(Object)
	 */
	public Object getParent( Object element )
	{
		return fParentCache.get( element );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(Object)
	 */
	public boolean hasChildren( Object parent )
	{
		if ( parent != null && parent instanceof ICSharedLibraryManager )
		{
			return ( ((ICSharedLibraryManager)parent).getSharedLibraries().length > 0 );
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(Object)
	 */
	public Object[] getElements( Object inputElement )
	{
		return getChildren( inputElement );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
	 */
	public void dispose()
	{
		fParentCache = null;
		setExceptionHandler( null );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(Viewer, Object, Object)
	 */
	public void inputChanged( Viewer viewer, Object oldInput, Object newInput )
	{
		clearCache();
	}

	protected void clearCache()
	{
		if ( fParentCache != null )
		{
			fParentCache.clear();
		}
	}
	
	/**
	 * Remove the cached parent for the given children
	 * 
	 * @param children for which to remove cached parents
	 */
	public void removeCache( Object[] children )
	{
		if ( fParentCache != null )
		{
			for ( int i = 0; i < children.length; i++ )
			{
				fParentCache.remove( children[i] );
			}
		}
	}

	/**
	 * Sets an exception handler for this content provider.
	 * 
	 * @param handler debug exception handler or <code>null</code>
	 */
	protected void setExceptionHandler( IDebugExceptionHandler handler ) 
	{
		fExceptionHandler = handler;
	}
	
	/**
	 * Returns the exception handler for this content provider.
	 * 
	 * @return debug exception handler or <code>null</code>
	 */
	protected IDebugExceptionHandler getExceptionHandler() 
	{
		return fExceptionHandler;
	}	
}
