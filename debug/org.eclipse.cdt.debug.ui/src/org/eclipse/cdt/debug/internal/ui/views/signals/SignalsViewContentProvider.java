/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.internal.ui.views.signals;

import org.eclipse.cdt.debug.core.ICSignalManager;
import org.eclipse.cdt.debug.internal.ui.views.IDebugExceptionHandler;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.debug.core.DebugException;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * Enter type comment.
 * 
 * @since: Jan 30, 2003
 */
public class SignalsViewContentProvider implements IStructuredContentProvider
{
	/**
	 * Handler for exceptions as content is retrieved
	 */
	private IDebugExceptionHandler fExceptionHandler = null;

	/**
	 * Constructor for SignalsViewContentProvider.
	 */
	public SignalsViewContentProvider()
	{
		super();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(Object)
	 */
	public Object[] getElements( Object inputElement )
	{
		if ( inputElement instanceof ICSignalManager )
		{
			try
			{
				return ((ICSignalManager)inputElement).getSignals();
			}
			catch( DebugException e )
			{
				if ( getExceptionHandler() != null )
				{
					getExceptionHandler().handleException( e );
				}
				else
				{
					CDebugUIPlugin.log( e );
				}
			}
		}
		return new Object[0];
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
