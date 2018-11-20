/*******************************************************************************
 * Copyright (c) 2004, 2006 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.core.model;

import org.eclipse.debug.core.DebugException;

/**
 * Comment for .
 */
public interface IModuleRetrieval {

	/**
	 * Returns whether there are modules currently loaded in this debug target.
	 *
	 * @return whether there are modules currently loaded in this debug target
	 *
	 * @throws DebugException
	 */
	public boolean hasModules() throws DebugException;

	/**
	 * Returns the array of the currently loaded modules.
	 *
	 * @return the array of the currently loaded modules
	 * @throws DebugException if this method fails. Reasons include:
	 */
	public ICModule[] getModules() throws DebugException;

	/**
	 * Load symbols for all currently loaded modules.
	 *
	 * @throws DebugException if this method fails. Reasons include:
	 */
	public void loadSymbolsForAllModules() throws DebugException;
}
