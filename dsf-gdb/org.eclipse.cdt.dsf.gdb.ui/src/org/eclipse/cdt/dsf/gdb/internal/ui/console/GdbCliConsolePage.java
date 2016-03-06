/*******************************************************************************
 * Copyright (c) 2015 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.console;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.RejectedExecutionException;

import org.eclipse.cdt.dsf.concurrent.ConfinedToDsfExecutor;
import org.eclipse.cdt.dsf.concurrent.DsfRunnable;
import org.eclipse.cdt.dsf.gdb.internal.ui.GdbUIPlugin;
import org.eclipse.cdt.dsf.gdb.launching.GdbLaunch;
import org.eclipse.cdt.dsf.gdb.service.IGDBBackendWithConsole;
import org.eclipse.cdt.dsf.mi.service.IMIBackend;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.debug.core.ILaunch;
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
import org.eclipse.tm.terminal.view.core.interfaces.ITerminalServiceOutputStreamMonitorListener;
import org.eclipse.tm.terminal.view.core.interfaces.constants.ILineSeparatorConstants;
import org.eclipse.tm.terminal.view.core.interfaces.constants.ITerminalsConnectorConstants;
import org.eclipse.tm.terminal.view.ui.interfaces.ILauncherDelegate;
import org.eclipse.tm.terminal.view.ui.launcher.LauncherDelegateManager;
import org.eclipse.ui.part.IPageSite;
import org.eclipse.ui.part.Page;

public class GdbCliConsolePage extends Page {

	private final GdbCliConsole fGdbConsole;
	private DsfSession fSession;
	private Composite fMainComposite;
	private Process fProcess;
	
	/** The control for the terminal widget embedded in the console */
	private ITerminalViewControl fTerminalControl;

	public GdbCliConsolePage(GdbCliConsole gdbConsole) {
		fGdbConsole = gdbConsole;
		ILaunch launch = gdbConsole.getLaunch();
		if (launch instanceof GdbLaunch) {
			fSession = ((GdbLaunch)launch).getSession();
		} else {
			assert false;
		}
	}

	@Override
	public void init(IPageSite pageSite) {
		super.init(pageSite);
	}

	@Override
	public void dispose() {
		super.dispose();
		fTerminalControl.disposeTerminal();
	}
	
	@Override
	public void createControl(Composite parent) {
		fMainComposite = new Composite(parent, SWT.NONE);
		fMainComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		fMainComposite.setLayout(new FillLayout());

		fTerminalControl = TerminalViewControlFactory.makeControl(
				new ITerminalListener() {
					@Override public void setState(TerminalState state) {}
					@Override public void setTerminalTitle(final String title) {}
		        },
				fMainComposite,
				new ITerminalConnector[] {}, 
				true);
		
		try {
			fTerminalControl.setEncoding("UTF-8"); //$NON-NLS-1$
		} catch (UnsupportedEncodingException e) {
		}
		
		startGdbProcess();
	}

	@Override
	public Control getControl() {
		return fMainComposite;
	}

	@Override
	public void setFocus() {
		fTerminalControl.setFocus();
	}
	
	public void disconnectTerminal() {
		if (fTerminalControl.getState() != TerminalState.CLOSED) {
			fTerminalControl.disconnectTerminal();
		}
	}	
	
	private void startProcess(Map<String, Object> properties) {
		ILauncherDelegate delegate = 
				LauncherDelegateManager.getInstance().getLauncherDelegate("org.eclipse.tm.terminal.connector.streams.launcher.streams", false); //$NON-NLS-1$
		if (delegate != null) {
			ITerminalConnector connector = delegate.createTerminalConnector(properties);
			fTerminalControl.setConnector(connector);
			if (fTerminalControl instanceof ITerminalControl) {
				((ITerminalControl)fTerminalControl).setConnectOnEnterIfClosed(false);
			}

			// Must use syncExec because the logic within must complete before the rest
			// of the class methods (specifically getProcess()) is called
			fMainComposite.getDisplay().syncExec(new Runnable() {
				@Override
				public void run() {
					if (fTerminalControl != null && !fTerminalControl.isDisposed()) {
						fTerminalControl.clearTerminal();
						fTerminalControl.connectTerminal();
					}
				}
			});
			if (outThread == null) {
				outThread = new OutThread();
				outThread.start();
			}
		}
	}
	
	private Map<String, Object> createNewSettings() {
		
		// Create the terminal connector
		Map<String, Object> properties = new HashMap<String, Object>();
		properties.put(ITerminalsConnectorConstants.PROP_LOCAL_ECHO, Boolean.FALSE);
		properties.put(ITerminalsConnectorConstants.PROP_STREAMS_STDIN, fProcess.getOutputStream());
		properties.put(ITerminalsConnectorConstants.PROP_STREAMS_STDOUT, fProcess.getInputStream());
		properties.put(ITerminalsConnectorConstants.PROP_STREAMS_STDERR, fProcess.getErrorStream());
		properties.put(ITerminalsConnectorConstants.PROP_LINE_SEPARATOR, ILineSeparatorConstants.LINE_SEPARATOR_LF);
		properties.put(ITerminalsConnectorConstants.PROP_STDOUT_LISTENERS, 
				new ITerminalServiceOutputStreamMonitorListener[0]);
		properties.put(ITerminalsConnectorConstants.PROP_STDERR_LISTENERS, 
				new ITerminalServiceOutputStreamMonitorListener[0]);
		return properties;
	}
	
    @ConfinedToDsfExecutor("fsession.getExecutor()")
    private void startGdbProcess() {
		if (fSession == null) {
			return;
		}

		try {
			fSession.getExecutor().submit(new DsfRunnable() {
	        	@Override
	        	public void run() {
	            	DsfServicesTracker tracker = new DsfServicesTracker(GdbUIPlugin.getBundleContext(), fSession.getId());
	            	IMIBackend miBackend = tracker.getService(IMIBackend.class);
	            	tracker.dispose();

	            	if (miBackend instanceof IGDBBackendWithConsole) {
	            		IGDBBackendWithConsole backend = (IGDBBackendWithConsole)miBackend;
	            		
	            		if (backend.getProcess() != null) {
	            			fProcess = backend.getProcess();
	            			startProcess(createNewSettings());
	            		}
	            	}
	        	}
	        });
		} catch (RejectedExecutionException e) {
		}
    }

//	public TerminalState getTerminalState() {
//		return tViewCtrl.getState();
//	}
//
//	public void connectTerminal() {
//		if (!tViewCtrl.isConnected()) {
//			connectTerminalJob.schedule();
//		}
//	}
//
//
//	public void setScrollLock(boolean enabled) {
//		tViewCtrl.setScrollLock(enabled);
//	}
//
//	public boolean getScrollLock() {
//		return tViewCtrl.isScrollLock();
//	}
    
	private class OutThread extends Thread {
		public OutThread() {
			super("Terminal Output"); //$NON-NLS-1$
		}

		@Override
		public void run() {
			try {
				byte[] buff = new byte[1024];
				if (fProcess != null) {
					InputStream in = fProcess.getInputStream();
					for (int n = in.read(buff); n >= 0; n = in.read(buff)) {
						if (fTerminalControl instanceof ITerminalControl) {
							ITerminalControl control = (ITerminalControl)fTerminalControl;
							if (control != null) {
								control.getRemoteToTerminalOutputStream().write(buff, 0, n);
							}
						}
					}
				}
//				synchronized (TerminalConsoleConnector.this) {
					outThread = null;
//				}
			} catch (IOException e) {
			}
		}
	}

	private OutThread outThread;

}
