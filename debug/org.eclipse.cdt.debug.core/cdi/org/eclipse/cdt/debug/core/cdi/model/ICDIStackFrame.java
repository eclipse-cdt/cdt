/*******************************************************************************
 * Copyright (c) 2000, 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.core.cdi.model;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDILocator;

/**
 * 
 * A stack frame in a suspended thread.
 * A stack frame contains variables representing visible locals and 
 * arguments at the current execution location.
 * 
 * @since Jul 8, 2002
 */
public interface ICDIStackFrame extends ICDIExecuteStepReturn, ICDIObject {

	/**
	 * Returns the location of the instruction pointer in this 
	 * stack frame.
	 *  
	 * @return the location of the instruction pointer
	 */
	ICDILocator getLocator();
	
	/**
	 * Returns the visible variables in this stack frame. An empty 
	 * collection is returned if there are no visible variables.
	 * 
	 * @return a collection of visible variables 
	 * @throws CDIException if this method fails.  Reasons include:
	 */
	ICDILocalVariableDescriptor[] getLocalVariableDescriptors() throws CDIException;
	
	/**
	 * Returns the arguments in this stack frame. An empty collection 
	 * is returned if there are no arguments.
	 * 
	 * @return a collection of arguments 
	 * @throws CDIException if this method fails.  Reasons include:
	 */
	ICDIArgumentDescriptor[] getArgumentDescriptors() throws CDIException;

	/**
	 * Returns the thread this stackframe is contained in.
	 *  
	 * @return the thread
	 */
	ICDIThread getThread();

	/**
	 * Returns the level of the stack frame, 1 based.
	 * 
	 * @return the level of the stack frame 
	 */
	int getLevel();
	
	/**
	 * Return true if the frames are the same.
	 */
	boolean equals(ICDIStackFrame stackframe);
}
