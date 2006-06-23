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
	 * The name of the group.
	 * 
	 * @return String name
	 */
	String getName();

	/**
	 * Returns the register descriptors in this register group.
	 * 
	 * @return ICDIRegisterDescriptor[] in this register group
	 * @throws CDIException if this method fails.  Reasons include:
	 */
	ICDIRegisterDescriptor[] getRegisterDescriptors() throws CDIException;

	/**
	 * Returns whether this register group currently contains any registers.
	 * 
	 * @return whether this register group currently contains any registers
	 * @exception CDIException if this method fails.  Reasons include:
	 */
	public boolean hasRegisters() throws CDIException;	

}
