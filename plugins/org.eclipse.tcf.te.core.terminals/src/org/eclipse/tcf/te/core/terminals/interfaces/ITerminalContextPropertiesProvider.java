/*******************************************************************************
 * Copyright (c) 2015 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.core.terminals.interfaces;

import java.util.Map;

/**
 * Terminal context properties provider.
 * <p>
 * The context properties provider allows querying desired properties
 * for a given context. The context is typically an element from a selection
 * and the inner structure of the element is unknown to the terminal.
 */
public interface ITerminalContextPropertiesProvider {

	/**
	 * Returns a unmodifiable map containing the target address and port for the given context,
	 * if it can be determined.
	 * <p>
	 * A context may return multiple target addresses and ports if the context can be reached using
	 * different connection methods.
	 * <p>
	 * <b>Note:</b>
	 * <ul>
	 * <li>See the constants defined in the context provider constants interface for default
	 * address and port types.</li>
	 * <li>The target address returned must <b>not</b> necessarily be an IP address.</li>
	 * <li>The values of the address or port properties might be <code>null</code>.</li>
	 * </ul>
	 *
	 * @param context The context to get the target addresses and ports from. Must not be <code>null</code>.
	 * @return The unmodifiable map containing the target addresses and ports, or <code>null</code>.
	 */
	public Map<String, String> getTargetAddress(Object context);

	/**
	 * Returns the property value stored under the given property key. If the property does not
	 * exist, <code>null</code> is returned.
	 *
	 * @param context The context to get the property from. Must not be <code>null</code>.
	 * @param key The property key. Must not be <code>null</code>.
	 *
	 * @return The stored property value or <code>null</code>.
	 */
	public Object getProperty(Object context, String key);
}
