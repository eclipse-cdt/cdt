/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.internal.core.model;

import org.eclipse.cdt.debug.core.cdi.model.ICDIRegister;
import org.eclipse.cdt.debug.core.cdi.model.ICDIVariable;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IRegister;
import org.eclipse.debug.core.model.IRegisterGroup;

/**
 * 
 * Enter type comment.
 * 
 * @since Sep 16, 2002
 */
public class CRegister extends CModificationVariable implements IRegister
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
}
