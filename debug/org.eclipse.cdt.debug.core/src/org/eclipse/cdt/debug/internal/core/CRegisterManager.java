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
import java.util.List;
import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.cdt.debug.core.ICDebugConstants;
import org.eclipse.cdt.debug.core.ICRegisterManager;
import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDIManager;
import org.eclipse.cdt.debug.core.cdi.model.ICDIRegisterGroup;
import org.eclipse.cdt.debug.internal.core.model.CDebugTarget;
import org.eclipse.cdt.debug.internal.core.model.CRegisterGroup;
import org.eclipse.cdt.debug.internal.core.model.CStackFrame;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IRegisterGroup;

/**
 * Manages all register groups in a debug target.
 */
public class CRegisterManager extends CUpdateManager implements ICRegisterManager {

	/**
	 * Collection of register groups added to this target. Values are of type <code>CRegisterGroup</code>.
	 */
	private List fRegisterGroups;

	/**
	 * The last stack frame.
	 */
	private CStackFrame fStackFrame;

	/** 
	 * Constructor for CRegisterManager. 
	 */
	public CRegisterManager( CDebugTarget target ) {
		super( target );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	public Object getAdapter( Class adapter ) {
		if ( ICRegisterManager.class.equals( adapter ) )
			return this;
		if ( CRegisterManager.class.equals( adapter ) )
			return this;
		return super.getAdapter( adapter );
	}

	public void dispose() {
		setStackFrame( null );
		removeAllRegisterGroups();
		super.dispose();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.ICRegisterManager#addRegisterGroup(org.eclipse.debug.core.model.IRegisterGroup)
	 */
	public void addRegisterGroup( IRegisterGroup group ) {
		// TODO Auto-generated method stub
	}

	public IRegisterGroup[] getRegisterGroups( CStackFrame frame ) throws DebugException {
		setStackFrame( frame );
		return (IRegisterGroup[])fRegisterGroups.toArray( new IRegisterGroup[fRegisterGroups.size()] );
	}

	public void initialize() {
		fRegisterGroups = new ArrayList( 20 );
		boolean autoRefresh = CDebugCorePlugin.getDefault().getPluginPreferences().getBoolean( ICDebugConstants.PREF_REGISTERS_AUTO_REFRESH );
		if ( getCDIManager() != null )
			getCDIManager().setAutoUpdate( autoRefresh );
		createMainRegisterGroup();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.ICRegisterManager#removeAllRegisterGroups()
	 */
	public void removeAllRegisterGroups() {
		Iterator it = fRegisterGroups.iterator();
		while( it.hasNext() ) {
			((CRegisterGroup)it.next()).dispose();
		}
		fRegisterGroups.clear();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.ICRegisterManager#removeRegisterGroup(org.eclipse.debug.core.model.IRegisterGroup)
	 */
	public void removeRegisterGroup( IRegisterGroup group ) {
		fRegisterGroups.remove( group );
	}

	private void createMainRegisterGroup() {
		ICDIRegisterGroup[] groups = null;
		try {
			groups = getDebugTarget().getCDITarget().getRegisterGroups();
		}
		catch( CDIException e ) {
			CDebugCorePlugin.log( e );
		}
		for (int i = 0; i < groups.length; ++i) {
			fRegisterGroups.add( new CRegisterGroup( getDebugTarget(), groups[i] ) ); //$NON-NLS-1$
		}
	}

	protected ICDIManager getCDIManager() {
		return null;
	}

	public void targetSuspended() {
		Iterator it = fRegisterGroups.iterator();
		while( it.hasNext() ) {
			((CRegisterGroup)it.next()).targetSuspended();
		}
	}

	public CStackFrame getStackFrame() {
		return fStackFrame;
	}

	private void setStackFrame( CStackFrame stackFrame ) {
		fStackFrame = stackFrame;
	}
}
