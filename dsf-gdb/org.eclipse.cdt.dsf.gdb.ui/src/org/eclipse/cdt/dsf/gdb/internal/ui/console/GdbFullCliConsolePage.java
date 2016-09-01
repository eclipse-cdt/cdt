/*******************************************************************************
 * Copyright (c) 2016 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.console;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.RejectedExecutionException;

import org.eclipse.cdt.debug.internal.ui.views.debuggerconsole.DebuggerConsoleView;
import org.eclipse.cdt.debug.ui.debuggerconsole.IDebuggerConsole;
import org.eclipse.cdt.debug.ui.debuggerconsole.IDebuggerConsoleView;
import org.eclipse.cdt.dsf.concurrent.DsfRunnable;
import org.eclipse.cdt.dsf.gdb.IGdbDebugPreferenceConstants;
import org.eclipse.cdt.dsf.gdb.internal.ui.GdbUIPlugin;
import org.eclipse.cdt.dsf.gdb.launching.GdbLaunch;
import org.eclipse.cdt.dsf.gdb.service.IGDBBackend;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.contexts.DebugContextEvent;
import org.eclipse.debug.ui.contexts.IDebugContextListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.tm.internal.terminal.control.ITerminalListener;
import org.eclipse.tm.internal.terminal.control.ITerminalViewControl;
import org.eclipse.tm.internal.terminal.control.TerminalViewControlFactory;
import org.eclipse.tm.internal.terminal.provisional.api.ITerminalConnector;
import org.eclipse.tm.internal.terminal.provisional.api.ITerminalControl;
import org.eclipse.tm.internal.terminal.provisional.api.TerminalState;
import org.eclipse.tm.terminal.view.core.interfaces.ITerminalServiceOutputStreamMonitorListener;
import org.eclipse.tm.terminal.view.core.interfaces.constants.ITerminalsConnectorConstants;
import org.eclipse.tm.terminal.view.ui.interfaces.ILauncherDelegate;
import org.eclipse.tm.terminal.view.ui.launcher.LauncherDelegateManager;
import org.eclipse.ui.part.Page;

public class GdbFullCliConsolePage extends Page implements IDebugContextListener {

	private DsfSession fSession;
	private ILaunch fLaunch;
	private Composite fMainComposite;
	private IDebuggerConsoleView fView;
	private IDebuggerConsole fConsole;
	
	private MenuManager fMenuManager;

	private GdbConsoleInvertColorsAction fInvertColorsAction;
	private GdbConsoleTerminateLaunchAction fTerminateLaunchAction;

	/** The control for the terminal widget embedded in the console */
	private ITerminalViewControl fTerminalControl;

	public GdbFullCliConsolePage(GdbFullCliConsole gdbConsole, IDebuggerConsoleView view) {
		fConsole = gdbConsole;
		fView = view;
		fLaunch = gdbConsole.getLaunch();
		if (fLaunch instanceof GdbLaunch) {
			fSession = ((GdbLaunch)fLaunch).getSession();
		} else {
			assert false;
		}
	}

	@Override
	public void dispose() {
		super.dispose();
		DebugUITools.getDebugContextManager().getContextService(getSite().getWorkbenchWindow()).removeDebugContextListener(this);
		fTerminalControl.disposeTerminal();
		fMenuManager.dispose();
	}
	
	@Override
	public void createControl(Composite parent) {
		fMainComposite = new Composite(parent, SWT.NONE);
		fMainComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		fMainComposite.setLayout(new FillLayout());

		DebugUITools.getDebugContextManager().getContextService(getSite().getWorkbenchWindow()).addDebugContextListener(this);

		createTerminalControl();
		createContextMenu();
		
		// Hook the terminal control to the GDB process
		attachTerminalToGdbProcess();
	}

	private void createTerminalControl() {
		// Create the terminal control that will be used to interact with GDB
		fTerminalControl = TerminalViewControlFactory.makeControl(
				new ITerminalListener() {
					@Override public void setState(TerminalState state) {}
					@Override public void setTerminalTitle(final String title) {}
		        },
				fMainComposite,
				new ITerminalConnector[] {}, 
				true);
		
		try {
			fTerminalControl.setEncoding(Charset.defaultCharset().name());
		} catch (UnsupportedEncodingException e) {
		}
	
		// Set the inverted colors option based on the stored preference
		IPreferenceStore store = GdbUIPlugin.getDefault().getPreferenceStore();
		boolean enabled = store.getBoolean(IGdbDebugPreferenceConstants.PREF_CONSOLE_INVERTED_COLORS);
		setInvertedColors(enabled);
	}

	protected void createContextMenu() {
		fMenuManager = new MenuManager();
		fMenuManager.setRemoveAllWhenShown(true);
		fMenuManager.addMenuListener((menuManager) -> { contextMenuAboutToShow(menuManager); });
		Menu menu = fMenuManager.createContextMenu(fTerminalControl.getControl());
		fTerminalControl.getControl().setMenu(menu);

		createActions();

		getSite().registerContextMenu(null, fMenuManager, getSite().getSelectionProvider());
		configureToolBar(getSite().getActionBars().getToolBarManager());
	}

	protected void createActions() {
		fInvertColorsAction = new GdbConsoleInvertColorsAction();
		fTerminateLaunchAction = new GdbConsoleTerminateLaunchAction(fLaunch);
    }

	protected void configureToolBar(IToolBarManager mgr) {
		mgr.insertBefore(DebuggerConsoleView.DROP_DOWN_ACTION_ID, fTerminateLaunchAction);
		mgr.add(fInvertColorsAction);
	}

	protected void contextMenuAboutToShow(IMenuManager menuManager) {
		menuManager.add(fInvertColorsAction);
	}

	@Override
	public Control getControl() {
		return fMainComposite;
	}

	@Override
	public void setFocus() {
		fTerminalControl.setFocus();
	}
	
	protected void attachTerminalToGdbProcess() {
		if (fSession == null) {
			return;
		}

		try {
			fSession.getExecutor().submit(new DsfRunnable() {
	        	@Override
	        	public void run() {
	            	DsfServicesTracker tracker = new DsfServicesTracker(GdbUIPlugin.getBundleContext(), fSession.getId());
	            	IGDBBackend backend = tracker.getService(IGDBBackend.class);
	            	tracker.dispose();

	            	if (backend != null) {
	            		if (backend.getProcess() != null) {
	            			attachTerminal(backend.getProcess());
	            		}
	            	}
	        	}
	        });
		} catch (RejectedExecutionException e) {
		}
    }
	
	protected void attachTerminal(Process process) {
		ILauncherDelegate delegate = 
				LauncherDelegateManager.getInstance().getLauncherDelegate("org.eclipse.tm.terminal.connector.streams.launcher.streams", false); //$NON-NLS-1$
		if (delegate != null) {
			Map<String, Object> properties = createNewSettings(process);
			
			ITerminalConnector connector = delegate.createTerminalConnector(properties);
			fTerminalControl.setConnector(connector);
			if (fTerminalControl instanceof ITerminalControl) {
				((ITerminalControl)fTerminalControl).setConnectOnEnterIfClosed(false);
				((ITerminalControl)fTerminalControl).setVT100LineWrapping(true);
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
		}
	}
	
	protected Map<String, Object> createNewSettings(Process process) {
		
		// Create the terminal connector
		Map<String, Object> properties = new HashMap<String, Object>();
		properties.put(ITerminalsConnectorConstants.PROP_LOCAL_ECHO, Boolean.FALSE);
		properties.put(ITerminalsConnectorConstants.PROP_STREAMS_STDIN, process.getOutputStream());
		properties.put(ITerminalsConnectorConstants.PROP_STREAMS_STDOUT, process.getInputStream());
		properties.put(ITerminalsConnectorConstants.PROP_STREAMS_STDERR, process.getErrorStream());
		properties.put(ITerminalsConnectorConstants.PROP_STDOUT_LISTENERS, 
				new ITerminalServiceOutputStreamMonitorListener[0]);
		properties.put(ITerminalsConnectorConstants.PROP_STDERR_LISTENERS, 
				new ITerminalServiceOutputStreamMonitorListener[0]);
		return properties;
	}
	
	/**
	 * Returns the launch to which the current selection belongs.
	 * 
	 * @return the launch to which the current selection belongs.
	 */
	protected ILaunch getCurrentLaunch() {
		IAdaptable context = DebugUITools.getDebugContext();
		if (context != null) {
			return context.getAdapter(ILaunch.class);
		}
		return null;
	}
	
	@Override
	public void debugContextChanged(DebugContextEvent event) {
		if ((event.getFlags() & DebugContextEvent.ACTIVATED) > 0) {
			// Show this GDB console if it matches with the currently
			// selected debug session
			if (fLaunch.equals(getCurrentLaunch())) {
				fView.display(fConsole);
			}
		}
	}

	public void setInvertedColors(boolean enable) {
		fTerminalControl.setInvertedColors(enable);
	}
}
