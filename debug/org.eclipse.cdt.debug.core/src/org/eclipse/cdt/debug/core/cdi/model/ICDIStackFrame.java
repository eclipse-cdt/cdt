/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.core.cdi.model;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDILocation;

/**
 * 
 * A stack frame in a suspended thread.
 * A stack frame contains variables representing visible locals and 
 * arguments at the current execution location.
 * 
 * @since Jul 8, 2002
 */
public interface ICDIStackFrame extends ICDIObject
{
	/**
	 * Returns the location of the instruction pointer in this 
	 * stack frame.
	 *  
	 * @return the location of the instruction pointer
	 */
	ICDILocation getLocation();
	
	/**
	 * Returns the visible variables in this stack frame. An empty 
	 * collection is returned if there are no visible variables.
	 * 
	 * @return a collection of visible variables 
	 * @throws CDIException if this method fails.  Reasons include:
	 */
	ICDIVariable[] getLocalVariables() throws CDIException;
	
	/**
	 * Returns the arguments in this stack frame. An empty collection 
	 * is returned if there are no arguments.
	 * 
	 * @return a collection of arguments 
	 * @throws CDIException if this method fails.  Reasons include:
	 */
	ICDIArgument[] getArguments() throws CDIException;
}
