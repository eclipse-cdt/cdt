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
package org.eclipse.cdt.debug.internal.core.model;

import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.cdt.debug.core.ICDebugConstants;
import org.eclipse.cdt.debug.core.cdi.model.ICDIRegister;
import org.eclipse.cdt.debug.core.cdi.model.ICDIVariableObject;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IRegister;
import org.eclipse.debug.core.model.IRegisterGroup;
import org.eclipse.debug.core.model.IValue;

/**
 * 
 * Enter type comment.
 * 
 * @since Sep 16, 2002
 */
public class CRegister extends CGlobalVariable implements IRegister
{
	public static class ErrorRegister extends ErrorVariable implements ICDIRegister
	{
		public ErrorRegister( ICDIVariableObject varObject, Exception e )
		{
			super( varObject, e );
		}
	}

	/**
	 * Constructor for CRegister.
	 * @param parent
	 * @param cdiVariable
	 */
	public CRegister( CRegisterGroup parent, ICDIRegister cdiRegister )
	{
		super( parent, cdiRegister );
		fFormat = CDebugCorePlugin.getDefault().getPluginPreferences().getInt( ICDebugConstants.PREF_DEFAULT_REGISTER_FORMAT );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IRegister#getRegisterGroup()
	 */
	public IRegisterGroup getRegisterGroup() throws DebugException
	{
		return (IRegisterGroup)getParent();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IVariable#hasValueChanged()
	 */
	public boolean hasValueChanged() throws DebugException
	{
		try {
			IValue value = getValue();
			if ( value != null )
			{
				return ( value.hasVariables() ) ? false : fChanged;
			}
		}
		catch( DebugException e ) {
			// ignore to prevent logging. 
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICVariable#isEnabled()
	 */
	public boolean isEnabled()
	{
		return true;
	}
}
