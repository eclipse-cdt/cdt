/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.internal.core.model;

import org.eclipse.cdt.debug.core.cdi.model.ICDIRegister;
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
	/**
	 * Constructor for CRegister.
	 * @param parent
	 * @param cdiVariable
	 */
	public CRegister( CRegisterGroup parent, ICDIRegister cdiRegister )
	{
		super( parent, cdiRegister );
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
		IValue value = getValue();
		if ( value != null )
		{
			return ( value.hasVariables() ) ? false : fChanged;
		}
		return false;
	}
}
