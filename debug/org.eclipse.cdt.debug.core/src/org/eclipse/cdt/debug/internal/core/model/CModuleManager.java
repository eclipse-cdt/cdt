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
package org.eclipse.cdt.debug.internal.core.model; 

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import org.eclipse.cdt.debug.core.CDIDebugModel;
import org.eclipse.cdt.debug.core.cdi.model.ICDIObject;
import org.eclipse.cdt.debug.core.cdi.model.ICDISharedLibrary;
import org.eclipse.cdt.debug.core.model.ICModule;
import org.eclipse.cdt.debug.core.model.IModuleRetrieval;
import org.eclipse.cdt.debug.internal.core.ICDebugInternalConstants;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
 
/**
 * Manages the modules loaded on this debug target.
 */
public class CModuleManager implements IModuleRetrieval {

	/**
	 * The debug target associated with this manager.
	 */
	private CDebugTarget fDebugTarget;

	/**
	 * The collection of the shared libraries loaded on this target.
	 */
	private ArrayList fModules;

	/** 
	 * Constructor for CModuleManager. 
	 */
	public CModuleManager( CDebugTarget target ) {
		fDebugTarget = target;
		fModules = new ArrayList( 5 );
	}

	public boolean hasModules() throws DebugException {
		return !fModules.isEmpty();
	}

	public ICModule[] getModules() throws DebugException {
		return (ICModule[])fModules.toArray( new ICModule[fModules.size()] );
	}

	public void loadSymbolsForAllModules() throws DebugException {
		MultiStatus ms = new MultiStatus( CDIDebugModel.getPluginIdentifier(), ICDebugInternalConstants.STATUS_CODE_ERROR, CoreModelMessages.getString( "CModuleManager.0" ), null ); //$NON-NLS-1$
		Iterator it = fModules.iterator();
		while( it.hasNext() ) {
			ICModule module = (ICModule)it.next();
			try {
				module.loadSymbols();
			}
			catch( DebugException e ) {
				ms.add(  new Status( IStatus.ERROR, CDIDebugModel.getPluginIdentifier(), ICDebugInternalConstants.STATUS_CODE_ERROR, e.getMessage(), e ) );
			}
		}
		if ( !ms.isOK() ) {
			throw new DebugException( ms );
		}
	}

	public void loadSymbols( ICModule[] modules ) throws DebugException {
		MultiStatus ms = new MultiStatus( CDIDebugModel.getPluginIdentifier(), ICDebugInternalConstants.STATUS_CODE_ERROR, CoreModelMessages.getString( "CModuleManager.1" ), null ); //$NON-NLS-1$
		for ( int i = 0; i < modules.length; ++i ) {
			try {
				modules[i].loadSymbols();
			}
			catch( DebugException e ) {
				ms.add(  new Status( IStatus.ERROR, CDIDebugModel.getPluginIdentifier(), ICDebugInternalConstants.STATUS_CODE_ERROR, e.getMessage(), e ) );
			}
		}
		if ( !ms.isOK() ) {
			throw new DebugException( ms );
		}
	}

	public void dispose() {
		Iterator it = fModules.iterator();
		while( it.hasNext() ) {
			((CModule)it.next()).dispose();
		}
		fModules.clear();
	}

	protected CDebugTarget getDebugTarget() {
		return fDebugTarget;
	}

	protected void addModules( ICModule[] modules ) {
		fModules.addAll( Arrays.asList( modules ) );
	}

	protected void removeModules( ICModule[] modules ) {
		fModules.removeAll( Arrays.asList( modules ) );
	}

	public void sharedLibraryLoaded( ICDISharedLibrary cdiLibrary ) {
		CModule library = null;
		synchronized( fModules ) {
			if ( find( cdiLibrary ) == null ) {
				library = CModule.createSharedLibrary( getDebugTarget(), cdiLibrary );
				fModules.add( library );
			}
		}
		if ( library != null )
			library.fireCreationEvent();
	}

	public void sharedLibraryUnloaded( ICDISharedLibrary cdiLibrary ) {
		CModule library = null;
		synchronized( fModules ) {
			library = find( cdiLibrary );
			if ( library != null ) {
				fModules.remove( library );
			}
		}
		if ( library != null ) {
			library.dispose();
			library.fireTerminateEvent();
		}
	}

	public void symbolsLoaded( ICDIObject cdiObject ) {
		CModule module = find( cdiObject );
		if ( module != null ) {
			module.fireChangeEvent( DebugEvent.STATE );
		}
	}

	private CModule find( ICDIObject cdiObject ) {
		Iterator it = fModules.iterator();
		while( it.hasNext() ) {
			CModule module = (CModule)it.next();
			if ( module.equals( cdiObject ) )
				return module;
		}
		return null;
	}
}
