/**********************************************************************
 * Copyright (c) 2002,2003 Timesys Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * Timesys - Initial API and implementation
 **********************************************************************/

package org.eclipse.cdt.core.builder.model;

/**
 * Represents a named value that is used as a variable
 * within the build process.  Build variables represent
 * a variable prefix coupled with an optional static suffix.
 * Using Makefile syntax as an example, the following is
 * an examples of build variables:
 * <p>
 * <code>
 * $(CROSS_TOOLS)/include
 * </code>
 * <p>
 * For this particular build variable:
 * <ul>
 * <li>Calling <code>getVariable()</code> would return "CROSS_TOOLS"</li>
 * <li>Calling <code>getFixed()</code> would return "/include"</li>
 * <li>Calling <code>getValue()</code> would return the current value
 * of the variable.</li>
 * </ul>
 * <p>
 * The intent is to introduce a mechanism similar to that
 * used by the Eclipse IDE to handle ClassPath variables.
 * <p>
 * @see ICBuildVariableProvider
 * @see ICBuildVariableResolver
 * @see CBuildVariable
 */
public interface ICBuildVariable {

	/**
	 * Get the text that makes up the variable portion
	 * of this build variable.
	 * 
	 * @return variable portion of this build variable.
	 */
	String getVariable();

	/**
	 * Get the text that makes up the fixed portion
	 * of this build variable.
	 * 
	 * @return fixed portion of this build variable.
	 */
	String getFixed();

	/**
	 * Get the current value of this build variable,
	 * replacing the variable portion with whatever
	 * value is appropriate for teh current circumstances.
	 * 
	 * @return Value of this build variable.
	 */
	String getValue();
	
	/**
	 * Get the resolver for this build variable,
	 * 
	 * @return Resolver for this build variable.
	 */
	ICBuildVariableResolver getResolver();	
}
