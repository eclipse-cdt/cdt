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
import org.eclipse.cdt.debug.core.cdi.model.ICDIRegisterDescriptor;
import org.eclipse.cdt.debug.core.cdi.model.ICDIRegisterGroup;
import org.eclipse.cdt.debug.core.model.IEnableDisableTarget;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IRegister;
import org.eclipse.debug.core.model.IRegisterGroup;

/**
 * Represents a group of registers.
 */
public class CRegisterGroup extends CDebugElement implements IRegisterGroup, IEnableDisableTarget {

	private ICDIRegisterGroup fCDIRegisterGroup;

	private IRegister[] fRegisters;

	private boolean fIsEnabled = true;

	/**
	 * Constructor for CRegisterGroup.
	 */
	public CRegisterGroup( CDebugTarget target, ICDIRegisterGroup regGroup ) {
		super( target );
		fCDIRegisterGroup = regGroup;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IRegisterGroup#getName()
	 */
	public String getName() throws DebugException {
		return fCDIRegisterGroup.getName();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IRegisterGroup#getRegisters()
	 */
	public IRegister[] getRegisters() throws DebugException {
		if ( fRegisters == null ) {
			try {
				ICDIRegisterDescriptor[] regDescs = fCDIRegisterGroup.getRegisterDescriptors();
				fRegisters = new IRegister[regDescs.length];
				for( int i = 0; i < fRegisters.length; ++i ) {
					fRegisters[i] = new CRegister( this, regDescs[i] );
					if ( ((CRegister)fRegisters[i]).isEnabled() ) {
						((CRegister)fRegisters[i]).setEnabled( isEnabled() );
					}
				}
			}
			catch( CDIException e ) {
				requestFailed( e.getMessage(), null );
			}
		}
		return fRegisters;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IRegisterGroup#hasRegisters()
	 */
	public boolean hasRegisters() throws DebugException {
		try {
			return fCDIRegisterGroup.hasRegisters();
		} catch( CDIException e ) {
			requestFailed( e.getMessage(), null );
		}
		return false;
	}

	public void dispose() {
		if (fRegisters == null) {
			return;
		}
		for ( int i = 0; i < fRegisters.length; ++i ) {
			if ( fRegisters[i] != null ) {
				((CRegister)fRegisters[i]).dispose();
				fRegisters[i] = null;
			}
		}
	}

	public void targetSuspended() {
		if (fRegisters == null) {
			return;
		}
		for ( int i = 0; i < fRegisters.length; ++i ) {
			if ( fRegisters[i] != null && ((CRegister)fRegisters[i]).hasErrors() ) {
				((CRegister)fRegisters[i]).dispose();
				fRegisters[i] = null;
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	public Object getAdapter( Class adapter ) {
		if ( IEnableDisableTarget.class.equals( adapter ) )
			return this;
		return super.getAdapter( adapter );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.IEnableDisableTarget#canEnableDisable()
	 */
	public boolean canEnableDisable() {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.IEnableDisableTarget#isEnabled()
	 */
	public boolean isEnabled() {
		return fIsEnabled;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.IEnableDisableTarget#setEnabled(boolean)
	 */
	public void setEnabled( boolean enabled ) throws DebugException {
		if ( fRegisters != null ) {
			synchronized( fRegisters ) {
				if ( fRegisters != null ) {
					for ( int i = 0; i < fRegisters.length; ++i ) {
						if ( fRegisters[i] instanceof CRegister ) {
							((CRegister)fRegisters[i]).setEnabled( enabled );
						}
					}
				}
			}
		}
		fIsEnabled = enabled;
		fireChangeEvent( DebugEvent.CONTENT );
	}
}