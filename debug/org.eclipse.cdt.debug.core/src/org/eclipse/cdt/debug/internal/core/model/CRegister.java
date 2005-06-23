/*******************************************************************************
 * Copyright (c) 2000, 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.core.model;

import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.debug.core.ICDebugConstants;
import org.eclipse.cdt.debug.core.model.CVariableFormat;
import org.eclipse.cdt.debug.core.model.IRegisterDescriptor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IRegister;
import org.eclipse.debug.core.model.IRegisterGroup;

/**
 * Represents a register in the CDI model.
 */
public class CRegister extends CGlobalVariable implements IRegister {

	/**
	 * Constructor for CRegister.
	 */
	protected CRegister( CRegisterGroup parent, IRegisterDescriptor descriptor ) {
		super( parent, null, ((CRegisterDescriptor)descriptor).getCDIDescriptor() );
		setFormat( CVariableFormat.getFormat( CDebugCorePlugin.getDefault().getPluginPreferences().getInt( ICDebugConstants.PREF_DEFAULT_REGISTER_FORMAT ) ) );
	}

	/**
	 * Constructor for CRegister.
	 */
	protected CRegister( CRegisterGroup parent, IRegisterDescriptor descriptor, String message ) {
		super( parent, null, ((CRegisterDescriptor)descriptor).getCDIDescriptor(), message );
		setFormat( CVariableFormat.getFormat( CDebugCorePlugin.getDefault().getPluginPreferences().getInt( ICDebugConstants.PREF_DEFAULT_REGISTER_FORMAT ) ) );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IRegister#getRegisterGroup()
	 */
	public IRegisterGroup getRegisterGroup() throws DebugException {
		return (IRegisterGroup)getParent();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.core.model.CVariable#isBookkeepingEnabled()
	 */
	protected boolean isBookkeepingEnabled() {
		boolean result = false;
		try {
			result = getLaunch().getLaunchConfiguration().getAttribute( ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_ENABLE_REGISTER_BOOKKEEPING, false );
		}
		catch( CoreException e ) {
		}
		return result;
	}
}
