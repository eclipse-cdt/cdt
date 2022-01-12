/*******************************************************************************
 * Copyright (c) 2007, 2012 Nokia and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Marc Dumais (Ericsson) - initial implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.core.breakpointactions;

/**
 * @since 7.3
 */
public interface IReverseDebugEnabler {

	/**
	 * Toggles the state of the reverse debugging mode.
	 * @throws Exception
	 */
	void toggle() throws Exception;

	/**
	 * Enables the reverse debugging mode.  No effect if already enabled.
	 * @throws Exception
	 */
	void enable() throws Exception;

	/**
	 * Disables the reverse debugging mode.  No effect if it's not enabled.
	 * @throws Exception
	 */
	void disable() throws Exception;

}
