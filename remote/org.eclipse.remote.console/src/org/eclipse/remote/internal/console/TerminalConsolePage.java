/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.remote.internal.console;

import java.io.UnsupportedEncodingException;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.remote.console.actions.ConsoleAction;
import org.eclipse.remote.console.actions.IConsoleActionFactory;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.internal.console.actions.ConsoleActionConnect;
import org.eclipse.remote.internal.console.actions.ConsoleActionDisconnect;
import org.eclipse.remote.internal.console.actions.ConsoleActionScrollLock;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.tm.internal.terminal.control.ITerminalListener;
import org.eclipse.tm.internal.terminal.control.ITerminalViewControl;
import org.eclipse.tm.internal.terminal.control.TerminalViewControlFactory;
import org.eclipse.tm.internal.terminal.provisional.api.ITerminalConnector;
import org.eclipse.tm.internal.terminal.provisional.api.TerminalState;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.actions.CloseConsoleAction;
import org.eclipse.ui.part.IPageSite;
import org.eclipse.ui.part.Page;
import org.eclipse.ui.progress.UIJob;

public class TerminalConsolePage extends Page implements IAdaptable {
	public static final String TOOLBAR_GROUP_ID = "org.eclipse.remote.internal.terminal.console.Toolbar"; //$NON-NLS-1$
	public static final String CONTRIBUTIONS_GROUP_ID = "org.eclipse.remote.internal.terminal.console.Toolbar.contributions"; //$NON-NLS-1$

	private final ConsoleActionConnect connectAction;
	private final ConsoleActionDisconnect disconnectAction;

	private final TerminalConsole terminalConsole;
	private final String encoding;
	private Composite mainComposite;
	private ITerminalViewControl tViewCtrl;

	private Job connectTerminalJob = new ConnectTerminalJob();

	private final ITerminalListener listener = new ITerminalListener() {
		@Override
		public void setState(TerminalState state) {
			if (state == TerminalState.CONNECTING || state == TerminalState.CONNECTED) {
				disconnectAction.setEnabled(true);
				connectAction.setEnabled(false);
			} else if (state == TerminalState.CLOSED) {
				disconnectAction.setEnabled(false);
				connectAction.setEnabled(true);
			}
			terminalConsole.setState(state);
		}

		@Override
		public void setTerminalTitle(final String title) {
			// ignore titles coming from the widget
		}
	};

	public TerminalConsolePage(TerminalConsole console, String encoding) {
		terminalConsole = console;
		this.encoding = encoding;
		connectAction = new ConsoleActionConnect(console);
		disconnectAction = new ConsoleActionDisconnect(console);
	}

	public TerminalConsole getConsole() {
		return terminalConsole;
	}

	@Override
	public void init(IPageSite pageSite) {
		super.init(pageSite);
		IToolBarManager toolBarManager = pageSite.getActionBars().getToolBarManager();
		toolBarManager.insertBefore(IConsoleConstants.OUTPUT_GROUP, new GroupMarker(TOOLBAR_GROUP_ID));

		toolBarManager.appendToGroup(TOOLBAR_GROUP_ID, connectAction);
		toolBarManager.appendToGroup(TOOLBAR_GROUP_ID, disconnectAction);
		toolBarManager.appendToGroup(TOOLBAR_GROUP_ID, new ConsoleActionScrollLock(this));
		toolBarManager.appendToGroup(TOOLBAR_GROUP_ID, new CloseConsoleAction(terminalConsole));

		toolBarManager.insertAfter(TOOLBAR_GROUP_ID, new GroupMarker(CONTRIBUTIONS_GROUP_ID));

		addToolbarContributions(toolBarManager);
	}

	private void addToolbarContributions(IToolBarManager toolBarManager) {
		String connectionType = terminalConsole.getConnection().getConnectionType().getId();
		List<String> actions = TerminalConsoleExtensionManager.getInstance().getActionsForType(connectionType);
		for (String id : actions) {
			IConsoleActionFactory factory = TerminalConsoleExtensionManager.getInstance().getFactory(id);
			if (factory != null) {
				ConsoleAction action = factory.createAction(id, connectionType, this);
				if (action != null) {
					toolBarManager.appendToGroup(CONTRIBUTIONS_GROUP_ID, action);
				}
			}
		}
	}

	@Override
	public void createControl(Composite parent) {
		mainComposite = new Composite(parent, SWT.NONE);
		mainComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		mainComposite.setLayout(new FillLayout());

		tViewCtrl = TerminalViewControlFactory.makeControl(listener, mainComposite, new ITerminalConnector[] {});
		tViewCtrl.setConnector(terminalConsole.getTerminalConnector().newPageConnector());

		try {
			tViewCtrl.setEncoding(encoding);
		} catch (UnsupportedEncodingException e) {
			IStatus status = new Status(IStatus.ERROR, Activator.PLUGIN_ID,
					NLS.bind(ConsoleMessages.ENCODING_UNAVAILABLE_1, encoding));
			Activator.log(status);
			ErrorDialog.openError(PlatformUI.getWorkbench().getDisplay().getActiveShell(),
					ConsoleMessages.OPEN_CONSOLE_ERROR, ConsoleMessages.ENCODING_UNAVAILABLE_0, status);
		}
		connectTerminalJob.schedule();
	}

	@Override
	public Control getControl() {
		return mainComposite;
	}

	@Override
	public void setFocus() {
		tViewCtrl.setFocus();
	}

	@Override
	public void dispose() {
		tViewCtrl.disposeTerminal();
		super.dispose();
	}

	public TerminalState getTerminalState() {
		return tViewCtrl.getState();
	}

	public void connectTerminal() {
		if (!tViewCtrl.isConnected()) {
			connectTerminalJob.schedule();
		}
	}

	public void disconnectTerminal() {
		if (tViewCtrl.getState() != TerminalState.CLOSED) {
			tViewCtrl.disconnectTerminal();
		}
	}

	public void setScrollLock(boolean enabled) {
		tViewCtrl.setScrollLock(enabled);
	}

	public boolean getScrollLock() {
		return tViewCtrl.isScrollLock();
	}

	class ConnectTerminalJob extends UIJob {
		public ConnectTerminalJob() {
			super(ConsoleMessages.CONNECTING_TO_TERMINAL);
			setSystem(true);
		}

		@Override
		public IStatus runInUIThread(IProgressMonitor monitor) {
			if (tViewCtrl != null && !tViewCtrl.isDisposed()) {
				tViewCtrl.clearTerminal();
				tViewCtrl.connectTerminal();
			}
			return Status.OK_STATUS;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getAdapter(Class<T> adapter) {
		if (adapter.equals(IRemoteConnection.class)) {
			return (T) terminalConsole.getConnection();
		}
		if (adapter.equals(TerminalConsole.class)) {
			return (T) terminalConsole;
		}
		if (adapter.equals(TerminalConsolePage.class)) {
			return (T) this;
		}
		return null;
	}
}
