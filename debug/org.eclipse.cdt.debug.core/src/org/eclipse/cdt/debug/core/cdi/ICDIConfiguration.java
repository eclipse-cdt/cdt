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

package org.eclipse.cdt.debug.core.cdi;

/**
 * Describes the configuration of debug session.
 * 
 * @since Aug 6, 2002
 */
public interface ICDIConfiguration {
	/**
	 * Returns whether this session supports termination.
	 * 
	 * @return whether this session supports termination
	 */
	boolean supportsTerminate();

	/**
	 * Returns whether this session supports disconnecting.
	 * 
	 * @return whether this session supports disconnecting
	 */
	boolean supportsDisconnect();

	/**
	 * Returns whether this session supports suspend.
	 * 
	 * @return whether this session supports suspend.
	 */
	boolean supportsSuspend();
	
	/**
	 * Returns whether this session supports Resume.
	 * 
	 * @return whether this session supports Resume.
	 */
	boolean supportsResume();

	/**
	 * Returns whether this session supports restarting.
	 * 
	 * @return whether this session supports restarting
	 */
	boolean supportsRestart();

	/**
	 * Returns whether this session supports stepping.
	 * 
	 * @return whether this session supports stepping
	 */
	boolean supportsStepping();

	/**
	 * Returns whether this session supports instruction stepping.
	 * 
	 * @return whether this session supports instruction stepping
	 */
	boolean supportsInstructionStepping();

	/**
	 * Returns whether this session supports breakpoints.
	 * 
	 * @return whether this session supports breakpoints
	 */
	boolean supportsBreakpoints();

	/**
	 * Returns whether this session supports registers.
	 * 
	 * @return whether this session supports registers
	 */
	boolean supportsRegisters();

	/**
	 * Returns whether this session supports register modification.
	 * 
	 * @return whether this session supports registers modification
	 */
	boolean supportsRegisterModification();

	/**
	 * Returns whether this session supports shared library.
	 * 
	 * @return whether this session supports registers modification
	 */
	boolean supportsSharedLibrary();

	/**
	 * Returns whether this session supports memory retrieval.
	 * 
	 * @return whether this session supports memory retrieval
	 */
	boolean supportsMemoryRetrieval();

	/**
	 * Returns whether this session supports memory modification.
	 * 
	 * @return whether this session supports memory modification
	 */
	boolean supportsMemoryModification();

	/**
	 * Returns whether this session supports expression evaluation.
	 * 
	 * @return whether this session supports expression evaluation
	 */
	boolean supportsExpressionEvaluation();

	/**
	 * Returns whether the session should be terminated when the inferior exits.
	 *
	 * @return whether the session be terminated when the inferior exits
	 */
	boolean terminateSessionOnExit();
}
