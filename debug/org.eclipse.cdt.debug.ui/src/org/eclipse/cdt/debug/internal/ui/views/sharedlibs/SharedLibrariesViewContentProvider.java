/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.internal.ui.views.sharedlibs;

import org.eclipse.cdt.debug.core.ICSharedLibraryManager;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * Enter type comment.
 * 
 * @since: Jan 16, 2003
 */
public class SharedLibrariesViewContentProvider implements IStructuredContentProvider
{
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(Object)
	 */
	public Object[] getElements( Object parent )
	{
		if ( parent != null && parent instanceof ICSharedLibraryManager )
		{
			return ((ICSharedLibraryManager)parent).getSharedLibraries();
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
	 */
	public void dispose()
	{
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(Viewer, Object, Object)
	 */
	public void inputChanged( Viewer viewer, Object oldInput, Object newInput )
	{
	}
}
