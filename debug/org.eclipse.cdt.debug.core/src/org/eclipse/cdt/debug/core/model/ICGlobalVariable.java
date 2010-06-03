/*******************************************************************************
 * Copyright (c) 2004, 2008 QNX Software Systems and others.
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
 * Represents a global C/C++ variable.
 */
public interface ICGlobalVariable extends ICVariable {

	/**
	 * Returns the descriptor of this variable. Will be null if a child of a global.
	 * 
	 * @return the descriptor of this variable
	 */
	public IGlobalVariableDescriptor getDescriptor();
}
