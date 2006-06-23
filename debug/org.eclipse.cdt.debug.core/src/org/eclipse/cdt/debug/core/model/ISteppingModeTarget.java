/*******************************************************************************
 * Copyright (c) 2004, 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.core.model;

/**
 * Support for the instruction stepping mode for a debug target.
 */
public interface ISteppingModeTarget {

	/**
	 * Returns whether this debug target supports instruction stepping.
	 *
	 * @return whether this debug target supports instruction stepping
	 */
	boolean supportsInstructionStepping();

	/**
	 * Sets whether the instruction stepping are enabled in this debug target.
	 * 
	 * @param enabled whether the instruction stepping are enabled in this debug target
	 */
	void enableInstructionStepping( boolean enabled );
	
	/**
	 * Returns whether the instruction stepping are currently enabled in this
	 * debug target.
	 * 
	 * @return whether the instruction stepping are currently enabled in this
	 * debug target
	 */
	boolean isInstructionSteppingEnabled();
}
