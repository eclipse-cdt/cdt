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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
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
import org.eclipse.tm.terminal.connector.local.launcher.LocalLauncherDelegate;
import org.eclipse.tm.terminal.view.core.interfaces.constants.ITerminalsConnectorConstants;
import org.eclipse.ui.part.IPageSite;
import org.eclipse.ui.part.Page;
import org.eclipse.ui.progress.UIJob;

public class GdbConsolePage extends Page {

	private final GdbConsole terminalConsole;
	private final String encoding;
	private Composite mainComposite;
	private ITerminalViewControl tViewCtrl;

	private Job connectTerminalJob = new ConnectTerminalJob();

	private final ITerminalListener listener = new ITerminalListener() {
		@Override
		public void setState(TerminalState state) {
//			terminalConsole.setState(state);
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

		
		Map<String, Object> properties = new HashMap<String, Object>();
		properties.put(ITerminalsConnectorConstants.PROP_TITLE, "My Local Terminal");
		properties.put(ITerminalsConnectorConstants.PROP_ENCODING, "UTF-8");
		properties.put(ITerminalsConnectorConstants.PROP_PROCESS_WORKING_DIR, "/tmp");
		properties.put(ITerminalsConnectorConstants.PROP_PROCESS_PATH, "/usr/bin/gdb");
		LocalLauncherDelegate delegate = new LocalLauncherDelegate();
		
		ITerminalConnector connector = delegate.createTerminalConnector(properties);
		tViewCtrl = TerminalViewControlFactory.makeControl(listener,
				mainComposite,
				new ITerminalConnector[] {});
//		ITerminalConnector connector = TerminalConnectorExtension.makeTerminalConnector("org.eclipse.tm.terminal.connector.local.LocalConnector");
////		ITerminalConnector connector = TerminalConnectorExtension.makeTerminalConnector("org.eclipse.tm.terminal.connector.process.ProcessConnector");
////		ITerminalConnector connector = new PageConnector();
//		
//		ISettingsStore store = new ISettingsStore() {
//			private final Map<String, Object> settings = new HashMap<String, Object>();
//
//			public final Map<String, Object> getSettings() {
//				return settings;
//			}
//
//
//			@Override
//			public final String get(String key, String defaultValue) {
//				Assert.isNotNull(key);
//				String value = settings.get(key) instanceof String ? (String) settings.get(key) : null;
//				return value != null ? value : defaultValue;
//			}
//
//			@Override
//			public final String get(String key) {
//				Assert.isNotNull(key);
//				return settings.get(key) instanceof String ? (String) settings.get(key) : null;
//			}
//
//			@Override
//			public final void put(String key, String value) {
//				Assert.isNotNull(key);
//				if (value == null) settings.remove(key);
//				else settings.put(key, value);
//			}
//			
//		};
//		
////		store.put()
//		connector.load(store);
		tViewCtrl.setConnector(connector);

		try {
			tViewCtrl.setEncoding(encoding);
		} catch (UnsupportedEncodingException e) {
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
//			super(ConsoleMessages.);
			super("Starting console");
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
}
