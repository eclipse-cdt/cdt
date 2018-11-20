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

import org.eclipse.debug.core.model.IDebugElement;

/**
 * C/C++ specific extension of <code>IDebugElement</code>.
 */
public interface ICDebugElement extends IDebugElement {

	/**
	 * Returns the current state of this element.
	 *
	 * @return the current state
	 */
	public CDebugElementState getState();

	/**
	 * Returns the info object associated with the current state.
	 *
	 * @return the info object associated with the current state
	 */
	public Object getCurrentStateInfo();
}
