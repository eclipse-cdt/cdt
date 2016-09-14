/*******************************************************************************
 * Copyright (c) 2016 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.console;

import org.eclipse.cdt.debug.internal.ui.views.debuggerconsole.DebuggerConsoleView;
import org.eclipse.cdt.debug.ui.debuggerconsole.IDebuggerConsole;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.contexts.DebugContextEvent;
import org.eclipse.debug.ui.contexts.IDebugContextListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.console.IConsoleView;
import org.eclipse.ui.internal.console.IOConsolePage;

@SuppressWarnings("restriction")
public class GdbBasicCliConsolePage extends IOConsolePage implements IDebugContextListener {

	private final ILaunch fLaunch;
	private final IConsoleView fView;
	private final IDebuggerConsole fConsole;
	
	private GdbConsoleInvertColorsAction fInvertColorsAction;
	private GdbConsoleTerminateLaunchAction fTerminateLaunchAction;
	private GdbConsoleShowPreferencesAction fShowPreferencePageAction;

	public GdbBasicCliConsolePage(GdbBasicCliConsole gdbConsole, IConsoleView view) {
		super(gdbConsole, view);
		fConsole = gdbConsole;
		fView = view;
		fLaunch = gdbConsole.getLaunch();
	}

	@Override
	public void dispose() {
		super.dispose();
		DebugUITools.getDebugContextManager().getContextService(getSite().getWorkbenchWindow()).removeDebugContextListener(this);
	}
	
	@Override
	public void createControl(Composite parent) {
		super.createControl(parent);
		DebugUITools.getDebugContextManager().getContextService(getSite().getWorkbenchWindow()).addDebugContextListener(this);
	}

	@Override
	protected void configureToolBar(IToolBarManager mgr) {
		mgr.insertBefore(DebuggerConsoleView.DROP_DOWN_ACTION_ID, fTerminateLaunchAction);
	}

	@Override
	protected void createActions() {
		fInvertColorsAction = new GdbConsoleInvertColorsAction();
		fTerminateLaunchAction = new GdbConsoleTerminateLaunchAction(fLaunch);
		fShowPreferencePageAction = new GdbConsoleShowPreferencesAction(getSite().getShell());
	}

	@Override
	protected void contextMenuAboutToShow(IMenuManager menuManager) {
		menuManager.add(fTerminateLaunchAction);
		menuManager.add(fInvertColorsAction);
		menuManager.add(new Separator());
		menuManager.add(fShowPreferencePageAction);
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
}
