/*******************************************************************************
 * Copyright (c) 2015 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.console;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.dsf.gdb.launching.LaunchUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.tm.internal.terminal.control.ITerminalListener;
import org.eclipse.tm.internal.terminal.control.ITerminalViewControl;
import org.eclipse.tm.internal.terminal.control.TerminalViewControlFactory;
import org.eclipse.tm.internal.terminal.provisional.api.ITerminalConnector;
import org.eclipse.tm.internal.terminal.provisional.api.ITerminalControl;
import org.eclipse.tm.internal.terminal.provisional.api.TerminalState;
import org.eclipse.tm.terminal.view.core.interfaces.constants.ITerminalsConnectorConstants;
import org.eclipse.tm.terminal.view.ui.interfaces.ILauncherDelegate;
import org.eclipse.tm.terminal.view.ui.launcher.LauncherDelegateManager;
import org.eclipse.ui.part.IPageSite;
import org.eclipse.ui.part.Page;
import org.eclipse.ui.progress.UIJob;

public class GdbConsolePage extends Page {

	private final GdbConsole terminalConsole;
	private final String encoding;
	private Composite mainComposite;
	private ITerminalViewControl tViewCtrl;

	private final ITerminalListener listener = new ITerminalListener() {
		@Override
		public void setState(TerminalState state) {
		}

		@Override
		public void setTerminalTitle(final String title) {
			// ignore titles coming from the widget
		}
	};

	public GdbConsolePage(GdbConsole console, String encoding) {
		terminalConsole = console;
		this.encoding = encoding;
	}

	public GdbConsole getConsole() {
		return terminalConsole;
	}

	@Override
	public void init(IPageSite pageSite) {
		super.init(pageSite);
	}

	@Override
	public void createControl(Composite parent) {
		mainComposite = new Composite(parent, SWT.NONE);
		mainComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		mainComposite.setLayout(new FillLayout());

		tViewCtrl = TerminalViewControlFactory.makeControl(listener,
				mainComposite,
				new ITerminalConnector[] {}, true);
		
		try {
			tViewCtrl.setEncoding(encoding);
		} catch (UnsupportedEncodingException e) {
		}
				
		ILauncherDelegate delegate = LauncherDelegateManager.getInstance().getLauncherDelegate("org.eclipse.tm.terminal.connector.local.launcher.local", false); //$NON-NLS-1$
		if (delegate != null) {
			// Create the terminal connector
			Map<String, Object> properties = new HashMap<String, Object>();
			properties.put(ITerminalsConnectorConstants.PROP_TITLE, "My Local Terminal");
			properties.put(ITerminalsConnectorConstants.PROP_ENCODING, encoding);
			properties.put(ITerminalsConnectorConstants.PROP_PROCESS_WORKING_DIR, "/tmp"); //$NON-NLS-1$
			properties.put(ITerminalsConnectorConstants.PROP_PROCESS_PATH, "/usr/bin/gdb"); //$NON-NLS-1$
			properties.put(ITerminalsConnectorConstants.PROP_DATA_NO_RECONNECT, Boolean.FALSE);
			try {
				String[] env = LaunchUtils.getLaunchEnvironment(terminalConsole.getLaunch().getLaunchConfiguration());
				properties.put(ITerminalsConnectorConstants.PROP_PROCESS_ENVIRONMENT, env);
			} catch (CoreException e) {
			}
			ITerminalConnector connector = delegate.createTerminalConnector(properties);
			tViewCtrl.setConnector(connector);
			if (tViewCtrl instanceof ITerminalControl) {
				((ITerminalControl)tViewCtrl).setConnectOnEnterIfClosed(false);
			}
			
			new UIJob(ConsoleMessages.ConsoleMessages_gdb_console_job) {
				@Override
				public IStatus runInUIThread(IProgressMonitor monitor) { 
					if (tViewCtrl != null && !tViewCtrl.isDisposed()) {
						tViewCtrl.clearTerminal();
						tViewCtrl.connectTerminal();
					}
					return Status.OK_STATUS;
				}
			}.schedule();
		}
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
		super.dispose();
		tViewCtrl.disposeTerminal();
	}

//	public TerminalState getTerminalState() {
//		return tViewCtrl.getState();
//	}

//	public void connectTerminal() {
//		if (!tViewCtrl.isConnected()) {
//			connectTerminalJob.schedule();
//		}
//	}

	public void disconnectTerminal() {
		if (tViewCtrl.getState() != TerminalState.CLOSED) {
			tViewCtrl.disconnectTerminal();
		}
	}

//	public void setScrollLock(boolean enabled) {
//		tViewCtrl.setScrollLock(enabled);
//	}
//
//	public boolean getScrollLock() {
//		return tViewCtrl.isScrollLock();
//	}
}
