/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.core;

/**
 * 
 * Provides the access to the configuration parameters of the debug session.
 * 
 * @since Aug 6, 2002
 */
public interface IDebugConfiguration
{
	/**
	 * Returns whether this configuration supports termination.
	 * 
	 * @return whether this configuration supports termination
	 */
	boolean supportsTerminate();
	
	/**
	 * Returns whether this configuration supports disconnecting.
	 * 
	 * @return whether this configuration supports disconnecting
	 */
	boolean supportsDisconnect();
	
	/**
	 * Returns whether this configuration supports suspend/resume.
	 * 
	 * @return whether this configuration supports suspend/resume
	 */
	boolean supportsSuspendResume();
	
	/**
	 * Returns whether this configuration supports restarting.
	 * 
	 * @return whether this configuration supports restarting
	 */
	boolean supportsRestart();

	/**
	 * Returns whether this configuration supports stepping.
	 * 
	 * @return whether this configuration supports stepping
	 */
	boolean supportsStepping();

	/**
	 * Returns whether this configuration supports instruction stepping.
	 * 
	 * @return whether this configuration supports instruction stepping
	 */
	boolean supportsInstructionStepping();

	/**
	 * Returns whether this configuration supports breakpoints.
	 * 
	 * @return whether this configuration supports breakpoints
	 */
	boolean supportsBreakpoints();

	/**
	 * Returns whether this configuration supports registers.
	 * 
	 * @return whether this configuration supports registers
	 */
	boolean supportsRegisters();

	/**
	 * Returns whether this configuration supports register modification.
	 * 
	 * @return whether this configuration supports registers modification
	 */
	boolean supportsRegisterModification();

	/**
	 * Returns whether this configuration supports memory retrieval.
	 * 
	 * @return whether this configuration supports memory retrieval
	 */
	boolean supportsMemoryRetrieval();

	/**
	 * Returns whether this configuration supports memory modification.
	 * 
	 * @return whether this configuration supports memory modification
	 */
	boolean supportsMemoryModification();
}
