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
package org.eclipse.cdt.debug.core.model;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IValueModification;
import org.eclipse.debug.core.model.IVariable;

/**
 * C/C++ specific extension <code>IVariable</code>. 
 */
public interface ICVariable extends IVariable, ICDebugElement, IFormatSupport, ICastToArray, IValueModification, IEnableDisableTarget {

	/**
	 * Returns the type of this variable.
	 * 
	 * @return the type of this variable
	 * @throws DebugException
	 */
	ICType getType() throws DebugException;

	/**
	 * Returns whether this variable is an argument.
	 * 
	 * @return whether this variable is an argument
	 */
	boolean isArgument();
}
