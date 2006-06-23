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
 * Represents a contiguous segment of disassembly in an execution context.
 */
public interface IDisassemblyBlock {
	
	/**
	 * Returns the parent disassembly object.
	 * 
	 * @return the parent disassembly object
	 */
	IDisassembly getDisassembly();

	/**
	 * Returns the platform-dependent path of the executable associated 
	 * with this segment.
	 * 
	 * @return the platform-dependent path of the executable
	 */
	String getModuleFile();


	/**
	 * Returns the source element (<code>IFile</code> or <code>File></code>) 
	 * of the source file associated with this segment or null if no source file is associated.
	 * 
	 * @return the source element
	 */
	Object getSourceElement();

	/**
	 * Returns whether this block contains given stack frame.
	 *  
	 * @param frame the stack frame
	 * @return whether this block contains given stack frame
	 */
	boolean contains( ICStackFrame frame );

	/**
	 * Return the array of source lines associated with this block.
	 *  
	 * @return the array of source lines associated with this block
	 */
	IAsmSourceLine[] getSourceLines();

	/**
	 * Returns whether this block contains mixed source/disassembly information.
	 *  
	 * @return whether this block contains mixed source/disassembly information
	 */
	boolean isMixedMode();
}
