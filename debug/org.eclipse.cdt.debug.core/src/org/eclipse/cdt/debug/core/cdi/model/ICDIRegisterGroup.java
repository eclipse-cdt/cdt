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
