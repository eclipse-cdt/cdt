/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.internal.core;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.cdt.debug.core.ICDebugConstants;
import org.eclipse.cdt.debug.core.ICSharedLibraryManager;
import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDIManager;
import org.eclipse.cdt.debug.core.cdi.ICDISharedLibraryManager;
import org.eclipse.cdt.debug.core.cdi.model.ICDISharedLibrary;
import org.eclipse.cdt.debug.core.model.ICSharedLibrary;
import org.eclipse.cdt.debug.internal.core.model.CDebugTarget;
import org.eclipse.cdt.debug.internal.core.model.CSharedLibrary;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;

/**
 * Enter type comment.
 * 
 * @since: Jan 16, 2003
 */
public class CSharedLibraryManager extends CUpdateManager implements ICSharedLibraryManager
{
	private ArrayList fSharedLibraries;

	/**
	 * Constructor for CSharedLibraryManager.
	 */
	public CSharedLibraryManager( CDebugTarget target )
	{
		super( target );
		fSharedLibraries = new ArrayList( 5 );
		boolean autoRefresh = CDebugCorePlugin.getDefault().getPluginPreferences().getBoolean( ICDebugConstants.PREF_SHARED_LIBRARIES_AUTO_REFRESH );
		if ( getCDIManager() != null )
			getCDIManager().setAutoUpdate( autoRefresh );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.ICSharedLibraryManager#sharedLibararyLoaded(ICDISharedLibrary)
	 */
	public void sharedLibraryLoaded( ICDISharedLibrary cdiLibrary )
	{
		CSharedLibrary library = new CSharedLibrary( getDebugTarget(), cdiLibrary );
		synchronized( fSharedLibraries ) {
			fSharedLibraries.add( library );
		}
		library.fireCreationEvent();
		if ( library.areSymbolsLoaded() )
			setBreakpoints();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.ICSharedLibraryManager#sharedLibraryUnloaded(ICDISharedLibrary)
	 */
	public synchronized void sharedLibraryUnloaded( ICDISharedLibrary cdiLibrary )
	{
		CSharedLibrary library = find( cdiLibrary );
		if ( library != null )
		{
			synchronized( fSharedLibraries ) {
				fSharedLibraries.remove( library );
			}
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
			library.fireChangeEvent( DebugEvent.STATE );
			setBreakpoints();
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
			((CSharedLibrary)it.next()).dispose();
		}
		fSharedLibraries.clear();
		super.dispose();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(Class)
	 */
	public Object getAdapter( Class adapter )
	{
		if ( adapter.equals( ICSharedLibraryManager.class ) )
		{
			return this;
		}
		if ( adapter.equals( CSharedLibraryManager.class ) )
		{
			return this;
		}
		return super.getAdapter( adapter );
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
	
	protected ICDIManager getCDIManager()
	{
		if ( getDebugTarget() != null )
		{
			return getDebugTarget().getCDISession().getSharedLibraryManager();
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.ICSharedLibraryManager#loadSymbols(org.eclipse.cdt.debug.core.model.ICSharedLibrary)
	 */
	public void loadSymbols( ICSharedLibrary[] libraries ) throws DebugException
	{
		ICDISharedLibraryManager slm = (ICDISharedLibraryManager)getCDIManager();
		if ( slm != null )
		{
			ArrayList cdiLibs = new ArrayList( libraries.length );
			for ( int i = 0; i < libraries.length; ++i )
			{
				cdiLibs.add( ((CSharedLibrary)libraries[i]).getCDISharedLibrary() );
			}
			try
			{
				slm.loadSymbols( (ICDISharedLibrary[])cdiLibs.toArray( new ICDISharedLibrary[cdiLibs.size()] ) );
			}
			catch( CDIException e )
			{
				getDebugTarget().targetRequestFailed( e.getMessage(), null );
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.ICSharedLibraryManager#loadSymbolsForAll()
	 */
	public void loadSymbolsForAll() throws DebugException
	{
		ICDISharedLibraryManager slm = (ICDISharedLibraryManager)getCDIManager();
		if ( slm != null )
		{
			try
			{
				slm.loadSymbols();
			}
			catch( CDIException e )
			{
				getDebugTarget().targetRequestFailed( e.getMessage(), null );
			}
		}
	}

	private void setBreakpoints()
	{
		getDebugTarget().setBreakpoints();
	}
}
