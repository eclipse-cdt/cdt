/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.core;

import org.eclipse.debug.core.model.IVariable;

/**
 * 
 * Provides the access to the stack frame information.
 * 
 * @since Aug 16, 2002
 */
public interface IStackFrameInfo
{
	/**
	 * Returns the address of this stack frame.
	 * 
	 * @return the address of this stack frame
	 */
	long getAddress();
	
	/**
	 * Returns the source file of this stack frame or <code>null</code>
	 * if the source file is unknown.
	 *  
	 * @return the source file of this stack frame
	 */
	String getFile();

	/**
	 * Returns the function of this stack frame or <code>null</code>
	 * if the function is unknown.
	 *  
	 * @return the function of this stack frame
	 */
	String getFunction();

	/**
	 * Returns the line number of this stack frame or <code>0</code>
	 * if the line number is unknown.
	 *  
	 * @return the line number of this stack frame
	 */
	int getFrameLineNumber();

	/**
	 * Returns the level of this stack frame.
	 * 
	 * @return the level of this stack frame 
	 */
	int getLevel();

	/**
	 * Returns the arguments of this stack frame.
	 * 
	 * @return the arguments of this stack frame 
	 */
	IVariable[] getArguments();
}
