/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.core;

import org.eclipse.core.resources.IProject;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.ISourceLocator;

/**
 * 
 * A C/C++ extension of <code>ISourceLocator</code>. 
 * Provides constants and methods to manage different source modes.
 * 
 * @since Aug 19, 2002
 */
public interface ICSourceLocator extends ISourceLocator
{
	static final public int MODE_SOURCE = 0;
	static final public int MODE_DISASSEMBLY = 1;
	static final public int MODE_MIXED = 2;

	/**
	 * Returns the current source presentation mode.
	 * 
	 * @return the current source presentation mode
	 */
	int getMode();

	/**
	 * Sets the source presentation mode.
	 * 
	 * @param the source presentation mode to set
	 */
	void setMode( int mode );
	
	/**
	 * Returns the line number of the instruction pointer in the specified 
	 * stack frame that corresponds to a line in an associated source element, 
	 * or -1 if line number information is unavailable.
	 * 
	 * @param frameInfo the frame data
	 * @return line number of instruction pointer in this stack frame, 
	 * 		   or -1 if line number information is unavailable
	 */
	int getLineNumber( IStackFrameInfo frameInfo );
}
