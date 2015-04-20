/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.remote.internal.console;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.tm.internal.terminal.provisional.api.TerminalState;
import org.eclipse.ui.console.AbstractConsole;
import org.eclipse.ui.console.IConsoleView;
import org.eclipse.ui.part.IPageBookViewPage;
import org.eclipse.ui.progress.UIJob;

@SuppressWarnings("restriction")
public class TerminalConsole extends AbstractConsole {
	private final String encoding;
	private final TerminalConsoleConnector terminalConnector;
	private final int index;

	public TerminalConsole(IRemoteConnection connection, int index, String encoding) {
		super(connection.getName(), Activator.getDefault().getImageRegistry().getDescriptor(ImageConsts.IMAGE_TERMINAL_VIEW));
		this.encoding = encoding;
		this.terminalConnector = new TerminalConsoleConnector(connection);
		this.index = index;
	}

	public TerminalConsoleConnector getTerminalConnector() {
		return terminalConnector;
	}

	public IRemoteConnection getConnection() {
		return terminalConnector.getConnection();
	}

	public int getIndex() {
		return index;
	}
	
	public synchronized void setState(TerminalState terminalState) {
		StringBuffer nameBuff = new StringBuffer(getConnection().getName());
		if (index > 0) {
			nameBuff.append(' ');
			nameBuff.append(String.valueOf(index));
		}
		nameBuff.append(" ("); //$NON-NLS-1$
		if (terminalState == TerminalState.CLOSED) {
			nameBuff.append(ConsoleMessages.STATUS_CLOSED);
		} else if (terminalState == TerminalState.CONNECTED) {
			nameBuff.append(ConsoleMessages.STATUS_CONNECTED);
		} else if (terminalState == TerminalState.CONNECTING) {
			nameBuff.append(ConsoleMessages.STATUS_CONNECTING);
		}
		nameBuff.append(")"); //$NON-NLS-1$

		final String name = nameBuff.toString();
		if (!name.equals(getName())) {
			UIJob job = new UIJob("updating name") { //$NON-NLS-1$
				public IStatus runInUIThread(IProgressMonitor monitor) {
					TerminalConsole.this.setName(name);
					return Status.OK_STATUS;
				}
			};
			job.setSystem(true);
			job.schedule();
		}
	}

	public IPageBookViewPage createPage(IConsoleView view) {
		view.setFocus();
		return new TerminalConsolePage(this, encoding);
	}
}
