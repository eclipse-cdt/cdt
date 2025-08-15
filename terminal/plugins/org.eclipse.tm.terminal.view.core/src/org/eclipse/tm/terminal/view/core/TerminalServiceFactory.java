/*******************************************************************************
 * Copyright (c) 2015, 2018 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.terminal.view.core;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.core.runtime.Status;
import org.eclipse.tm.terminal.view.core.activator.CoreBundleActivator;
import org.eclipse.tm.terminal.view.core.interfaces.ITerminalService;
import org.eclipse.tm.terminal.view.core.interfaces.ITerminalTabListener;

/**
 * Terminal service factory implementation.
 * <p>
 * Provides access to the terminal service instance.
 */
public final class TerminalServiceFactory {

	private static final String TM_TERMINAL = ".tm.terminal."; //$NON-NLS-1$

	private static final class ITerminalServiceImplementation
			implements ITerminalService, org.eclipse.terminal.view.core.ITerminalTabListener {

		private List<ITerminalTabListener> listeners = new CopyOnWriteArrayList<>();

		@Override
		public void terminateConsole(Map<String, Object> properties, Done done) {
			org.eclipse.terminal.view.core.ITerminalService delegate = CoreBundleActivator.getTerminalService();
			if (delegate == null) {
				done.done(Status.error("Not running!")); //$NON-NLS-1$
				return;
			}
			delegate.terminateConsole(convert(properties)).handle((o, e) -> {
				if (e != null) {
					done.done(Status.error("Operation failed", e)); //$NON-NLS-1$
				} else {
					done.done(Status.OK_STATUS);
				}
				return null;
			});
		}

		@Override
		public void openConsole(Map<String, Object> properties, Done done) {
			org.eclipse.terminal.view.core.ITerminalService delegate = CoreBundleActivator.getTerminalService();
			if (delegate == null) {
				done.done(Status.error("Not running!")); //$NON-NLS-1$
				return;
			}
			delegate.openConsole(convert(properties)).handle((o, e) -> {
				if (e != null) {
					done.done(Status.error("Operation failed", e)); //$NON-NLS-1$
				} else {
					done.done(Status.OK_STATUS);
				}
				return null;
			});

		}

		@Override
		public void closeConsole(Map<String, Object> properties, Done done) {
			org.eclipse.terminal.view.core.ITerminalService delegate = CoreBundleActivator.getTerminalService();
			if (delegate == null) {
				done.done(Status.error("Not running!")); //$NON-NLS-1$
				return;
			}
			delegate.closeConsole(convert(properties)).handle((o, e) -> {
				if (e != null) {
					done.done(Status.error("Operation failed", e)); //$NON-NLS-1$
				} else {
					done.done(Status.OK_STATUS);
				}
				return null;
			});

		}

		@Override
		public void addTerminalTabListener(ITerminalTabListener listener) {
			if (!listeners.contains(listener)) {
				if (listeners.add(listener)) {
					org.eclipse.terminal.view.core.ITerminalService delegate = CoreBundleActivator.getTerminalService();
					if (delegate == null) {
						return;
					}
					delegate.addTerminalTabListener(this);
				}
			}
		}

		@Override
		public void removeTerminalTabListener(ITerminalTabListener listener) {
			listeners.remove(listener);
		}

		@Override
		public void terminalTabDisposed(Object source, Object data) {
			for (ITerminalTabListener listener : listeners) {
				listener.terminalTabDisposed(source, data);
			}
		}
	}

	private static final ITerminalService DELEGATE = new ITerminalServiceImplementation();

	public static ITerminalService getService() {
		return DELEGATE;
	}

	private static Map<String, Object> convert(Map<String, Object> properties) {
		if (properties == null) {
			return null;
		}
		LinkedHashMap<String, Object> enhanced = new LinkedHashMap<>(properties);
		for (Entry<String, Object> entry : properties.entrySet()) {
			String key = (String) transform(entry.getKey());
			Object value = transform(entry.getValue());
			enhanced.put(key, value);
		}
		return enhanced;
	}

	private static Object transform(Object object) {
		if (object instanceof String s) {
			if (s.contains(TM_TERMINAL)) {
				//like org.eclipse.tm.terminal.view.core...
				return s.replace(TM_TERMINAL, ".terminal."); //$NON-NLS-1$
			}
		}
		return object;
	}
}
