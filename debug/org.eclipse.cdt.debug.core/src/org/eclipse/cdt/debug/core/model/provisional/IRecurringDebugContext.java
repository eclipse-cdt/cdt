/*******************************************************************************
 * Copyright (c) 2011, Texas Instruments and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Texas Instruments - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.core.model.provisional;

import org.eclipse.debug.core.DebugException;

/**
 * This interface should be implemented by context objects that can provide an
 * identifier that is unique among other contexts of the same type in a debug
 * session, but which will be reused if the underlying thing appears in a future
 * debug session. For example, the executable name can typically be used to
 * identify that the process context in two successive debug sessions represent
 * the same program. Where threads are programatically given meaningful names,
 * the thread name can be used to detect that we're dealing with the same thread
 * in successive debug sessions. Implementations should stay away from using
 * identifiers that are assigned by the underlying runtime system (e.g., PIDs
 * and TIDs), since such IDs are often assigned and reused in non-deterministic
 * ways.
 *
 * @author Alain Lee
 */
public interface IRecurringDebugContext {

	/**
	 * Returns the unique identifier associated with this recurring context. If
	 * this context re-appears in a future debug session, it should return the
	 * same value.
	 */
	String getContextID() throws DebugException;
}
