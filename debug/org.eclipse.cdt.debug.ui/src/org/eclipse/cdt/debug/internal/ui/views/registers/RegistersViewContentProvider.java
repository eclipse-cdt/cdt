/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.internal.ui.views.registers;

import java.util.HashMap;

import org.eclipse.cdt.debug.core.ICRegisterManager;
import org.eclipse.cdt.debug.internal.ui.views.IDebugExceptionHandler;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IRegisterGroup;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * 
 * Provide the contents for a registers viewer.
 * 
 * @since Jul 23, 2002
 */
public class RegistersViewContentProvider implements ITreeContentProvider
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
	 * Constructor for RegistersViewContentProvider.
	 */
	public RegistersViewContentProvider()
	{
		fParentCache = new HashMap( 10 );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(Object)
	 */
	public Object[] getChildren( Object parent )
	{
		Object[] children= null;
		try
		{
			if ( parent instanceof ICRegisterManager )
			{
				children = ((ICRegisterManager)parent).getRegisterGroups();
			}
			else if ( parent instanceof IRegisterGroup )
			{
				children = ((IRegisterGroup)parent).getRegisters();
			}
			else if ( parent instanceof IVariable )
			{
				children = ((IVariable)parent).getValue().getVariables();
			}
			if ( children != null )
			{
				cache( parent, children );
				return children;
			}
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
	public boolean hasChildren( Object element )
	{
		try
		{
			if ( element instanceof IVariable )
			{
				return ((IVariable)element).getValue().hasVariables();
			}
			if ( element instanceof IValue )
			{
				return ((IValue)element).hasVariables();
			}
			if ( element instanceof IRegisterGroup )
			{
				return ((IRegisterGroup)element).hasRegisters();
			}
			if ( element instanceof IStackFrame )
			{
				return ((IStackFrame)element).hasRegisterGroups();
			}
		}
		catch( DebugException e )
		{
			CDebugUIPlugin.log( e );
			return false;
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
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(Viewer, Object, Object)
	 */
	public void inputChanged( Viewer viewer, Object oldInput, Object newInput )
	{
		clearCache();
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
