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
 * Interface representing a class that is capable of
 * resolving the variable portion of build variables
 * at run time.
 * <p>
 * See also the <a href="../../../../../../CBuildConfig.html">CBuildConfig</a>
 * extension point documentation.
 * <p>
 * @see ICBuildVariable
 * @see ICBuildVariableProvider
 * @see CBuildVariable
 */

public interface ICBuildVariableResolver {
	
	/**
	 * Given a build variable, determine what it's
	 * resolved value should be.
	 * 
	 * @return resolved value, or <code>null</code>.
	 */
	public String resolveValue(ICBuildVariable var);

}
