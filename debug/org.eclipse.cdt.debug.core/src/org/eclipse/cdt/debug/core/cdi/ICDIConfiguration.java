/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.core.cdi;

/**
 * Describes the configuration of debug session.
 * 
 * @since Aug 6, 2002
 */
public interface ICDIConfiguration
{
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
	 * Returns whether this session supports suspend/resume.
	 * 
	 * @return whether this session supports suspend/resume
	 */
	boolean supportsSuspendResume();

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
}
