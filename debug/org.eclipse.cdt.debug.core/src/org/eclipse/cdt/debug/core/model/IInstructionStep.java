/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.core.model;
import org.eclipse.debug.core.DebugException;

/**
 * Provides the ability to step into and over machine instruction 
 * at the current execution location.
 * 
 */
public interface IInstructionStep
{
	/**
	 * Returns whether this element can currently perform a step 
	 * into the instruction.
	 *
	 * @return whether this element can currently perform a step 
	 * into the instruction
	 */
	boolean canStepIntoInstruction();
	
	/**
	 * Returns whether this element can currently perform a step 
	 * over the instruction (nexti command).
	 *
	 * @return whether this element can currently perform a step 
	 * over the instruction
	 */
	boolean canStepOverInstruction();
	
	/**
	 * Steps into the current instruction.
	 *
	 * @exception DebugException on failure. Reasons include:<ul>
	 * </ul>
	 */
	void stepIntoInstruction() throws DebugException;
	
	/**
	 * Steps over the current instruction.
	 *
	 * @exception DebugException on failure. Reasons include:<ul>
	 * </ul>
	 */
	void stepOverInstruction() throws DebugException;	
}
