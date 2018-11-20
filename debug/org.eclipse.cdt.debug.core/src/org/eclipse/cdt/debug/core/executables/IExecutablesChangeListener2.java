/*******************************************************************************
 * Copyright (c) 2011 Freescale Semiconductor and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Freescale Semiconductor - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.core.executables;

import java.util.List;

/**
 * Extension of IExecutablesChangeListener which allows listeners to more
 * precisely find out when an Executable is added or removed from the workspace
 *
 * @since 7.1
 */
public interface IExecutablesChangeListener2 extends IExecutablesChangeListener {

	/**
	 * Called when one or more Executable objects have been added to the
	 * workspace
	 */
	public void executablesAdded(List<Executable> executables);

	/**
	 * Called when one or more Executable objects have been removed from the
	 * workspace
	 */
	public void executablesRemoved(List<Executable> executables);

}