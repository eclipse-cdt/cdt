/*******************************************************************************
 * Copyright (c) 2000, 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.core.model;

import org.eclipse.core.runtime.IPath;

/**
 * Provides the description of a global variable.
 */
public interface IGlobalVariableDescriptor {

	/**
	 * Returns the name of the global variable
	 * 
	 * @return the name of the global variable
	 */
	public String getName();

	/**
	 * Returns the path of the source file that contains 
	 * the definition of the global variable.
	 *  
	 * @return the path of the source file
	 */
	public IPath getPath();
}
