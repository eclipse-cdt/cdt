/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.internal.core;

import org.eclipse.cdt.debug.core.cdi.ICSession;

/**
 * 
 * Provides the convenience access methods to the configuration 
 * parameters of the debug session.
 * 
 * @since Aug 6, 2002
 */
public class CDebugConfiguration
{
	private ICSession fSession;

	/**
	 * Constructor for CDebugConfiguration.
	 */
	public CDebugConfiguration( ICSession session )
	{
		fSession = session;
	}

	/**
	 * Returns whether this session supports termination.
	 * 
	 * @return whether this session supports termination
	 */
	public boolean supportsTerminate()
	{
		return true;
	}

	/**
	 * Returns whether this session supports disconnecting.
	 * 
	 * @return whether this session supports disconnecting
	 */
	public boolean supportsDisconnect()
	{
		return true;
	}

	/**
	 * Returns whether this session supports suspend/resume.
	 * 
	 * @return whether this session supports suspend/resume
	 */
	public boolean supportsSuspendResume()
	{
		return true;
	}

	/**
	 * Returns whether this session supports restarting.
	 * 
	 * @return whether this session supports restarting
	 */
	public boolean supportsRestart()
	{
		return true;
	}

	/**
	 * Returns whether this session supports stepping.
	 * 
	 * @return whether this session supports stepping
	 */
	public boolean supportsStepping()
	{
		return true;
	}

	/**
	 * Returns whether this session supports instruction stepping.
	 * 
	 * @return whether this session supports instruction stepping
	 */
	public boolean supportsInstructionStepping()
	{
		return true;
	}

	/**
	 * Returns whether this session supports breakpoints.
	 * 
	 * @return whether this session supports breakpoints
	 */
	public boolean supportsBreakpoints()
	{
		return true;
	}

	/**
	 * Returns whether this session supports registers.
	 * 
	 * @return whether this session supports registers
	 */
	public boolean supportsRegisters()
	{
		return true;
	}

	/**
	 * Returns whether this session supports register modification.
	 * 
	 * @return whether this session supports registers modification
	 */
	public boolean supportsRegisterModification()
	{
		return true;
	}

	/**
	 * Returns whether this session supports memory retrieval.
	 * 
	 * @return whether this session supports memory retrieval
	 */
	public boolean supportsMemoryRetrieval()
	{
		return true;
	}

	/**
	 * Returns whether this session supports memory modification.
	 * 
	 * @return whether this session supports memory modification
	 */
	public boolean supportsMemoryModification()
	{
		return true;
	}

	/**
	 * Returns whether this session supports expression evaluation.
	 * 
	 * @return whether this session supports expression evaluation
	 */
	public boolean supportsExpressionEvaluation()
	{
		return true;
	}
}
