/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.internal.core;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.cdt.debug.core.ICSharedLibraryManager;
import org.eclipse.cdt.debug.core.ICUpdateManager;
import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDISharedLibraryManager;
import org.eclipse.cdt.debug.core.cdi.model.ICDISharedLibrary;
import org.eclipse.cdt.debug.core.model.ICSharedLibrary;
import org.eclipse.cdt.debug.internal.core.model.CDebugTarget;
import org.eclipse.cdt.debug.internal.core.model.CSharedLibrary;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IDebugTarget;

/**
 * Enter type comment.
 * 
 * @since: Jan 16, 2003
 */
public class CSharedLibraryManager implements ICSharedLibraryManager
{
	private CDebugTarget fDebugTarget = null;
	private ArrayList fSharedLibraries;

	/**
	 * Constructor for CSharedLibraryManager.
	 */
	public CSharedLibraryManager( CDebugTarget target )
	{
		setDebugTarget( target );
		fSharedLibraries = new ArrayList( 5 );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.ICSharedLibraryManager#sharedLibararyLoaded(ICDISharedLibrary)
	 */
	public synchronized void sharedLibraryLoaded( ICDISharedLibrary cdiLibrary )
	{
		CSharedLibrary library = new CSharedLibrary( fDebugTarget, cdiLibrary );
		fSharedLibraries.add( library );
		library.fireCreationEvent();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.ICSharedLibraryManager#sharedLibraryUnloaded(ICDISharedLibrary)
	 */
	public synchronized void sharedLibraryUnloaded( ICDISharedLibrary cdiLibrary )
	{
		CSharedLibrary library = find( cdiLibrary );
		if ( library != null )
		{
			fSharedLibraries.remove( library );
			library.dispose();
			library.fireTerminateEvent();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.ICSharedLibraryManager#symbolsLoaded(ICDISharedLibrary)
	 */
	public void symbolsLoaded( ICDISharedLibrary cdiLibrary )
	{
		CSharedLibrary library = find( cdiLibrary );
		if ( library != null )
		{
			((CDebugTarget)getDebugTarget()).setRetryBreakpoints( true );
			library.fireChangeEvent( DebugEvent.STATE );
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.ICSharedLibraryManager#getSharedLibraries()
	 */
	public ICSharedLibrary[] getSharedLibraries()
	{
		return (ICSharedLibrary[])fSharedLibraries.toArray( new ICSharedLibrary[fSharedLibraries.size()] );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.ICSharedLibraryManager#dispose()
	 */
	public void dispose()
	{
		Iterator it = fSharedLibraries.iterator();
		while( it.hasNext() )
		{
			((ICSharedLibrary)it.next()).dispose();
		}
		fSharedLibraries.clear();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(Class)
	 */
	public Object getAdapter( Class adapter )
	{
		if ( adapter.equals( ICUpdateManager.class ) )
		{
			return this;
		}
		if ( adapter.equals( ICSharedLibraryManager.class ) )
		{
			return this;
		}
		if ( adapter.equals( CSharedLibraryManager.class ) )
		{
			return this;
		}
		if ( adapter.equals( IDebugTarget.class ) )
		{
			return fDebugTarget;
		}
		return null;
	}

	public IDebugTarget getDebugTarget()
	{
		return fDebugTarget;
	}
	
	protected void setDebugTarget( CDebugTarget target )
	{
		fDebugTarget = target;
	}
	
	protected CSharedLibrary find( ICDISharedLibrary cdiLibrary )
	{
		Iterator it = fSharedLibraries.iterator();
		while( it.hasNext() )
		{
			CSharedLibrary library = (CSharedLibrary)it.next();
			if ( library.getCDISharedLibrary().equals( cdiLibrary ) )
				return library;
		}
		return null;
	}	

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.ICUpdateManager#getAutoModeEnabled()
	 */
	public boolean getAutoModeEnabled()
	{
		if ( getCDIManager() != null )
		{
			return getCDIManager().isAutoUpdate();
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.ICUpdateManager#setAutoModeEnabled(boolean)
	 */
	public void setAutoModeEnabled( boolean enable )
	{
		if ( getCDIManager() != null )
		{
			getCDIManager().setAutoUpdate( enable );
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.ICUpdateManager#update()
	 */
	public void update() throws DebugException
	{
		if ( getCDIManager() != null )
		{
			try
			{
				getCDIManager().update();
			}
			catch( CDIException e )
			{
				((CDebugTarget)getDebugTarget()).targetRequestFailed( e.toString(), null );
			}
		}
	}
	
	private ICDISharedLibraryManager getCDIManager()
	{
		if ( getDebugTarget() != null )
		{
			return ((CDebugTarget)getDebugTarget()).getCDISession().getSharedLibraryManager();
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.ICUpdateManager#canUpdate()
	 */
	public boolean canUpdate()
	{
		if ( getDebugTarget() != null )
		{
			return getDebugTarget().isSuspended();
		}
		return false;
	}
}
