/*******************************************************************************
 * Copyright (c) 2008 ARM Limited and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * ARM Limited - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.core.disassembly;

/**
 * Interface for registering the disassembly context objects.
 * <p>
 * Clients interested in the context change notifications
 * may register a listener.
 * </p>
 * <p>
 * This interface is not intended to be implemented by clients.
 * It can be accessed from <code>CDebugCorePlugin</code>.
 * </p>
 * @see org.eclipse.cdt.debug.core.IDisassemblyContextListener
 *
 * This interface is experimental.
 */
public interface IDisassemblyContextService {

	/**
	 * Adds the given listener to the collection of registered listeners.
	 * Has no effect if an identical listener is already registered.
	 *
	 * @param listener the listener to add
	 */
	public void addDisassemblyContextListener(IDisassemblyContextListener listener);

	/**
	 * Removes the given listener from the collection of registered listeners.
	 * Has no effect if an identical listener is not already registered.
	 *
	 * @param listener the listener to remove
	 */
	public void removeDisassemblyContextListener(IDisassemblyContextListener listener);

	/**
	 * Registers the given context with this service.
	 * Has no effect if an identical context has already been registered.
	 * The corresponding notifications will be sent to all registered listeners.
	 *
	 * @param disassemblyContext the context to register.
	 */
	public void register(Object disassemblyContext);

	/**
	 * Unregisters the given context with this service.
	 * Has no effect if an identical context has not been registered.
	 * The corresponding notifications will be sent to all registered listeners.
	 *
	 * @param disassemblyContext the context to unregister.
	 */
	public void unregister(Object disassemblyContext);
}
