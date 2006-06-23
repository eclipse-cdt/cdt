/*******************************************************************************
 * Copyright (c) 2004, 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.core.model;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.debug.core.DebugException;

/**
 * Provides the ability to resume a debug target at the given address.
 */
public interface IJumpToAddress {

	/**
	 * Returns whether this operation is currently available for this element.
	 * 
	 * @return whether this operation is currently available
	 */
	public boolean canJumpToAddress( IAddress address );

	/**
	 * Causes this element to resume the execution at the specified address.
	 * 
	 * @exception DebugException on failure. Reasons include:
	 */
	public void jumpToAddress( IAddress address ) throws DebugException;
}
