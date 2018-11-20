/*******************************************************************************
 * Copyright (c) 2010 Nokia and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Nokia - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.internal.core.sourcelookup;

/**
 * This interface is used to provide a description of a debug element,
 * usually a stack frame, when no source can be located for it.
 * An instance is usually provided by an adapter.
 */
public interface ICSourceNotFoundDescription {

	/**
	 * Returns a description of the debug element suitable for use by the
	 * CSourceNotFoundEditor. This description is then used by the editor to
	 * inform the user when describing what it can't locate source for.
	 *
	 * @return the description of the debug element, or null if not available
	 */
	String getDescription();

	/**
	 * Return true if the debug element only is an address, false if not. This
	 * is used by the editor to know wich type of message he should use.
	 *
	 * @return a boolean that is true if the debug element only is an address
	 */
	default boolean isAddressOnly() {
		return false;
	}

}
