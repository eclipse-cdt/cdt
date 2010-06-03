/*******************************************************************************
 * Copyright (c) 2008, 2009 Freescale Secmiconductor and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Freescale Semiconductor - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.core.cdi.model;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDILocation;

/**
 * Provides the ability to perform a jump without changing the run state of a thread or debug target.
 * @since 6.0
 */
public interface ICDIExecuteMoveInstructionPointer {

	/**
	 * Moves the instruction pointer to the specified location without changing the run state
	 * The result is undefined if it moves outside of the stackframe.
	 * Can  only be called when the associated target is suspended.
	 * 
	 * @param location
	 * @throws CDIException if this method fails.  Reasons include:
	 */
	void moveInstructionPointer(ICDILocation location) throws CDIException;


}
