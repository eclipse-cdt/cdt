/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.core;

import java.util.ArrayList;
import java.util.Iterator;
import org.eclipse.cdt.debug.core.ICSharedLibraryManager;
import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.model.ICDISharedLibrary;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.core.model.ICSharedLibrary;
import org.eclipse.cdt.debug.internal.core.model.CDebugElement;
import org.eclipse.cdt.debug.internal.core.model.CDebugTarget;
import org.eclipse.cdt.debug.internal.core.model.CSharedLibrary;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;

/**
 * Manages the collection of the shared libraries loaded on a debug target.
 */
public class CSharedLibraryManager implements ICSharedLibraryManager {

	/**
	 * The debug target associated with this manager.
	 */
	private CDebugTarget fDebugTarget;

	/**
	 * The collection of the shared libraries loaded on this target.
	 */
	private ArrayList fSharedLibraries;

	/**
	 * Constructor for CSharedLibraryManager.
	 */
	public CSharedLibraryManager( CDebugTarget target ) {
		fDebugTarget = target;
		fSharedLibraries = new ArrayList( 5 );
	}

	public void sharedLibraryLoaded( ICDISharedLibrary cdiLibrary ) {
		CSharedLibrary library = new CSharedLibrary( getDebugTarget(), cdiLibrary );
		synchronized( fSharedLibraries ) {
			fSharedLibraries.add( library );
		}
		library.fireCreationEvent();
	}

	public synchronized void sharedLibraryUnloaded( ICDISharedLibrary cdiLibrary ) {
		CSharedLibrary library = find( cdiLibrary );
		if ( library != null ) {
			synchronized( fSharedLibraries ) {
				fSharedLibraries.remove( library );
			}
			library.dispose();
			library.fireTerminateEvent();
		}
	}

	public void symbolsLoaded( ICDISharedLibrary cdiLibrary ) {
		CSharedLibrary library = find( cdiLibrary );
		if ( library != null ) {
			library.fireChangeEvent( DebugEvent.STATE );
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.ICSharedLibraryManager#getSharedLibraries()
	 */
	public ICSharedLibrary[] getSharedLibraries() {
		return (ICSharedLibrary[])fSharedLibraries.toArray( new ICSharedLibrary[fSharedLibraries.size()] );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.ICSharedLibraryManager#dispose()
	 */
	public void dispose() {
		Iterator it = fSharedLibraries.iterator();
		while( it.hasNext() ) {
			((CSharedLibrary)it.next()).dispose();
		}
		fSharedLibraries.clear();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	public Object getAdapter( Class adapter ) {
		if ( adapter.equals( ICSharedLibraryManager.class ) ) {
			return this;
		}
		if ( adapter.equals( CSharedLibraryManager.class ) ) {
			return this;
		}
		return null;
	}

	protected CSharedLibrary find( ICDISharedLibrary cdiLibrary ) {
		Iterator it = fSharedLibraries.iterator();
		while( it.hasNext() ) {
			CSharedLibrary library = (CSharedLibrary)it.next();
			if ( library.getCDISharedLibrary().equals( cdiLibrary ) )
				return library;
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.ICSharedLibraryManager#loadSymbols(org.eclipse.cdt.debug.core.model.ICSharedLibrary[])
	 */
	public void loadSymbols( ICSharedLibrary[] libraries ) throws DebugException {
		for( int i = 0; i < libraries.length; ++i ) {
			try {
				((CSharedLibrary)libraries[i]).getCDISharedLibrary().loadSymbols();
			}
			catch( CDIException e ) {
				CDebugElement.targetRequestFailed( e.getMessage(), null );
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.ICSharedLibraryManager#loadSymbolsForAll()
	 */
	public void loadSymbolsForAll() throws DebugException {
		ICDITarget target = getDebugTarget().getCDITarget();
		try {
			ICDISharedLibrary[] libraries = target.getSharedLibraries();
			for( int i = 0; i < libraries.length; ++i ) {
				libraries[i].loadSymbols();
			}
		}
		catch( CDIException e ) {
			CDebugElement.targetRequestFailed( e.getMessage(), null );
		}
	}

	protected CDebugTarget getDebugTarget() {
		return fDebugTarget;
	}
}