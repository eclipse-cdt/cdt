/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.core.cdi.model;

import org.eclipse.cdt.debug.core.cdi.CDIException;

/**
 * 
 * A register is a special kind of variable that is contained
 * in a register group. Each register has a name and a value.
 * 
 * @since Jul 9, 2002
 */
public interface ICDIRegister extends ICDIVariable
{
	/**
	 * Returns the register group this register is contained in.
	 * 
	 * @return the register group this register is contained in
	 * @exception CDIException if this method fails.  Reasons include:
	 */
	ICDIRegisterGroup getRegisterGroup() throws CDIException; 
}
