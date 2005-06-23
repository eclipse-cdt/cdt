/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.core.cdi.model;

/**
 * Describes the configuration of the target.
 * 
 */
public interface ICDITargetConfiguration extends ICDIObject {

	/**
	 * Returns whether this target supports termination.
	 * 
	 * @return whether this target supports termination
	 */
	boolean supportsTerminate();

	/**
	 * Returns whether this target supports disconnecting.
	 * 
	 * @return whether this target supports disconnecting
	 */
	boolean supportsDisconnect();

	/**
	 * Returns whether this target supports suspend.
	 * 
	 * @return whether this target supports suspend.
	 */
	boolean supportsSuspend();
	
	/**
	 * Returns whether this target supports Resume.
	 * 
	 * @return whether this target supports Resume.
	 */
	boolean supportsResume();

	/**
	 * Returns whether this target supports restarting.
	 * 
	 * @return whether this target supports restarting
	 */
	boolean supportsRestart();

	/**
	 * Returns whether this target supports stepping.
	 * 
	 * @return whether this target supports stepping
	 */
	boolean supportsStepping();

	/**
	 * Returns whether this target supports instruction stepping.
	 * 
	 * @return whether this target supports instruction stepping
	 */
	boolean supportsInstructionStepping();

	/**
	 * Returns whether this target supports breakpoints.
	 * 
	 * @return whether this target supports breakpoints
	 */
	boolean supportsBreakpoints();

	/**
	 * Returns whether this target supports registers.
	 * 
	 * @return whether this target supports registers
	 */
	boolean supportsRegisters();

	/**
	 * Returns whether this target supports register modification.
	 * 
	 * @return whether this target supports registers modification
	 */
	boolean supportsRegisterModification();

	/**
	 * Returns whether this target supports shared library.
	 * 
	 * @return whether this target supports registers modification
	 */
	boolean supportsSharedLibrary();

	/**
	 * Returns whether this target supports memory retrieval.
	 * 
	 * @return whether this target supports memory retrieval
	 */
	boolean supportsMemoryRetrieval();

	/**
	 * Returns whether this target supports memory modification.
	 * 
	 * @return whether this target supports memory modification
	 */
	boolean supportsMemoryModification();

	/**
	 * Returns whether this target supports expression evaluation.
	 * 
	 * @return whether this target supports expression evaluation
	 */
	boolean supportsExpressionEvaluation();

}
