/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.core.cdi.model;

import org.eclipse.cdt.debug.core.cdi.CDIException;

/**
 * 
 * Represents a group of registers that are assigned to a target.
 * 
 * @since Jul 9, 2002
 */
public interface ICDIRegisterGroup extends ICDIObject {
	/**
	 * Returns the registers in this register group.
	 * 
	 * @return the registers in this register group
	 * @throws CDIException if this method fails.  Reasons include:
	 */
	ICDIRegister[] getRegisters() throws CDIException;
}
