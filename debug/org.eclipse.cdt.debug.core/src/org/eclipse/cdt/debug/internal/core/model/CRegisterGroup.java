/**********************************************************************
 * Copyright (c) 2004 QNX Software Systems and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.debug.internal.core.model;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.model.ICDIRegister;
import org.eclipse.cdt.debug.core.cdi.model.ICDIRegisterObject;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IRegister;
import org.eclipse.debug.core.model.IRegisterGroup;

/**
 * Represents a group of registers.
 */
public class CRegisterGroup extends CDebugElement implements IRegisterGroup {

	private String fName;

	private ICDIRegisterObject[] fRegisterObjects;

	private IRegister[] fRegisters;

	/**
	 * Constructor for CRegisterGroup.
	 */
	public CRegisterGroup( CDebugTarget target, String name, ICDIRegisterObject[] regObjects ) {
		super( target );
		fName = name;
		fRegisterObjects = regObjects;
		fRegisters = new IRegister[regObjects.length];
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IRegisterGroup#getName()
	 */
	public String getName() throws DebugException {
		return fName;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IRegisterGroup#getRegisters()
	 */
	public IRegister[] getRegisters() throws DebugException {
		for ( int i = 0; i < fRegisters.length; ++i ) {
			if ( fRegisters[i] == null ) {
				try {
					fRegisters[i] = new CRegister( this, getCDIRegister( fRegisterObjects[i] ) );
				}
				catch( DebugException e ) {
					fRegisters[i] = new CRegister( this, fRegisterObjects[i], e.getMessage() );
				}
			}
		}
		return fRegisters;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IRegisterGroup#hasRegisters()
	 */
	public boolean hasRegisters() throws DebugException {
		return fRegisterObjects.length > 0;
	}

	public void dispose() {
		for ( int i = 0; i < fRegisters.length; ++i ) {
			if ( fRegisters[i] != null ) {
				((CRegister)fRegisters[i]).dispose();
				fRegisters[i] = null;
			}
		}
	}

	private ICDIRegister getCDIRegister( ICDIRegisterObject ro ) throws DebugException {
		ICDIRegister register = null;
		try {
			register = ((CDebugTarget)getDebugTarget()).getCDISession().getRegisterManager().createRegister( ro );
		}
		catch( CDIException e ) {
			requestFailed( e.getMessage(), null );
		}
		return register;
	}

	public void resetChangeFlags() {
		for ( int i = 0; i < fRegisters.length; ++i ) {
			if ( fRegisters[i] != null ) {
				((CRegister)fRegisters[i]).setChanged( false );
			}
		}
	}

	public void targetSuspended() {
		for ( int i = 0; i < fRegisters.length; ++i ) {
			if ( fRegisters[i] != null && ((CRegister)fRegisters[i]).hasErrors() ) {
				((CRegister)fRegisters[i]).dispose();
				fRegisters[i] = null;
			}
		}
	}
}