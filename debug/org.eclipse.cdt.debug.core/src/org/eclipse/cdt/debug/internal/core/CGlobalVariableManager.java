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
package org.eclipse.cdt.debug.internal.core; 

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import org.eclipse.cdt.debug.core.CDIDebugModel;
import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.cdt.debug.core.ICGlobalVariableManager;
import org.eclipse.cdt.debug.core.model.ICGlobalVariable;
import org.eclipse.cdt.debug.core.model.IGlobalVariableDescriptor;
import org.eclipse.cdt.debug.internal.core.model.CDebugTarget;
import org.eclipse.cdt.debug.internal.core.model.CVariable;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;

/**
 * Manages all global variables registered with a debug target.
 */
public class CGlobalVariableManager implements ICGlobalVariableManager {
	
	private CDebugTarget fDebugTarget;
	
	private ArrayList fGlobals = new ArrayList( 10 );

	/** 
	 * Constructor for CGlobalVariableManager. 
	 */
	public CGlobalVariableManager( CDebugTarget target ) {
		super();
		setDebugTarget( target );
	}

	protected CDebugTarget getDebugTarget() {
		return fDebugTarget;
	}
	
	private void setDebugTarget( CDebugTarget debugTarget ) {
		fDebugTarget = debugTarget;
	}

	public ICGlobalVariable[] getGlobals() {
		return (ICGlobalVariable[])fGlobals.toArray( new ICGlobalVariable[fGlobals.size()] );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.ICGlobalVariableManager#addGlobals(IGlobalVariableDescriptor[])
	 */
	public void addGlobals( IGlobalVariableDescriptor[] descriptors ) throws DebugException {
		MultiStatus ms = new MultiStatus( CDebugCorePlugin.getUniqueIdentifier(), 0, "", null ); //$NON-NLS-1$
		ArrayList globals = new ArrayList( descriptors.length );
		for ( int i = 0; i < descriptors.length; ++i ) {
			try {
				globals.add( CDIDebugModel.createGlobalVariable( getDebugTarget(), descriptors[i] ) );
			}
			catch( DebugException e ) {
				ms.add( e.getStatus() );
			}
		}
		if ( globals.size() > 0 ) {
			synchronized( fGlobals ) {
				fGlobals.addAll( globals );
			}
			getDebugTarget().fireChangeEvent( DebugEvent.CONTENT );
		}
		if ( !ms.isOK() ) {
			throw new DebugException( ms );
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.ICGlobalVariableManager#removeGlobals(ICGlobalVariable[])
	 */
	public void removeGlobals( ICGlobalVariable[] globals ) {
		synchronized( fGlobals ) {
			fGlobals.removeAll( Arrays.asList( globals ) );
		}
		for ( int i = 0; i < globals.length; ++i ) {
			if ( globals[i] instanceof CVariable )
				((CVariable)globals[i]).dispose();
		}
		getDebugTarget().fireChangeEvent( DebugEvent.CONTENT );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.ICGlobalVariableManager#removeAllGlobals()
	 */
	public void removeAllGlobals() {
		ICGlobalVariable[] globals = new ICGlobalVariable[0];
		synchronized( fGlobals ) {
			globals = (ICGlobalVariable[])fGlobals.toArray( new ICGlobalVariable[fGlobals.size()] );
			fGlobals.clear();
		}
		for ( int i = 0; i < globals.length; ++i ) {
			if ( globals[i] instanceof CVariable )
				((CVariable)globals[i]).dispose();
		}
		getDebugTarget().fireChangeEvent( DebugEvent.CONTENT );
	}

	public void dispose() {
		Iterator it = fGlobals.iterator();
		while( it.hasNext() ) {
			((CVariable)it.next()).dispose();
		}
		fGlobals.clear();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.ICGlobalVariableManager#getDescriptors()
	 */
	public IGlobalVariableDescriptor[] getDescriptors() {
		// TODO Auto-generated method stub
		return null;
	}
}
