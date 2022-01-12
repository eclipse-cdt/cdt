/*******************************************************************************
 * Copyright (c) 2008, 2016 ARM Limited and others.
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

package org.eclipse.cdt.debug.internal.core.disassembly;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.eclipse.cdt.debug.core.disassembly.IDisassemblyContextListener;
import org.eclipse.cdt.debug.core.disassembly.IDisassemblyContextService;
import org.eclipse.core.runtime.ListenerList;

public class DisassemblyContextService implements IDisassemblyContextService {

	private ListenerList<IDisassemblyContextListener> fListeners;
	private Set<Object> fContexts;

	public DisassemblyContextService() {
		fContexts = new CopyOnWriteArraySet<>();
		fListeners = new ListenerList<>();
	}

	@Override
	public void addDisassemblyContextListener(IDisassemblyContextListener listener) {
		fListeners.add(listener);
	}

	@Override
	public void removeDisassemblyContextListener(IDisassemblyContextListener listener) {
		fListeners.remove(listener);
	}

	@Override
	public void register(Object context) {
		fContexts.add(context);
		for (IDisassemblyContextListener listener : fListeners) {
			listener.contextAdded(context);
		}
	}

	@Override
	public void unregister(Object context) {
		fContexts.remove(context);
		for (IDisassemblyContextListener listener : fListeners) {
			listener.contextRemoved(context);
		}
	}

	public void dispose() {
		for (Object context : fContexts) {
			for (IDisassemblyContextListener listener : fListeners) {
				listener.contextRemoved(context);
			}
		}
		fListeners.clear();
		fContexts.clear();
	}
}
