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

import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.Status;
import org.eclipse.tm.terminal.view.core.activator.CoreBundleActivator;
import org.eclipse.tm.terminal.view.core.interfaces.ITerminalService;
import org.eclipse.tm.terminal.view.core.interfaces.ITerminalTabListener;

/**
 * Terminal service factory implementation.
 * <p>
 * Provides access to the terminal service instance.
 */
@Deprecated(forRemoval = true)
public final class TerminalServiceFactory {

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
			delegate.terminateConsole(properties).handle((o, e) -> {
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
			delegate.openConsole(properties).handle((o, e) -> {
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
			delegate.closeConsole(properties).handle((o, e) -> {
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

	public static final StackWalker STACK_WALKER = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);

	private static final ITerminalService DELEGATE = new ITerminalServiceImplementation();

	public static ITerminalService getService() {
		ILog.of(STACK_WALKER.getCallerClass()).warn(
				"This bundle is using the deprecated terminal API consider migration of your bundle to the new 'org.eclipse.terminal.view.core'"); //$NON-NLS-1$
		return DELEGATE;
	}
}
