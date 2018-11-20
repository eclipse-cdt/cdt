/*******************************************************************************
 * Copyright (c) 2016 Ericsson and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.console;

import org.eclipse.cdt.debug.internal.ui.views.debuggerconsole.DebuggerConsoleView;
import org.eclipse.cdt.debug.ui.debuggerconsole.IDebuggerConsole;
import org.eclipse.cdt.dsf.gdb.internal.ui.console.actions.GdbConsoleShowPreferencesAction;
import org.eclipse.cdt.dsf.gdb.internal.ui.console.actions.GdbConsoleTerminateLaunchAction;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.contexts.DebugContextEvent;
import org.eclipse.debug.ui.contexts.IDebugContextListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.console.IConsoleView;
import org.eclipse.ui.internal.console.IOConsolePage;

/**
 * Page used for a basic GDB console.  Each Debug session which uses the basic GDB console will
 * use its own instance of this page.  The basic console is used for older GDB versions.
 *
 * Contributions to this page's context menu can be done using id "GdbBasicCliConsole.#ContextMenu".
 * For example, using the extension point:<br>
 * <code>
 *       menuContribution locationURI="popup:GdbBasicCliConsole.#ContextMenu?after=additions"
 * </code>
 */
@SuppressWarnings("restriction")
public class GdbBasicCliConsolePage extends IOConsolePage implements IDebugContextListener {

	private final ILaunch fLaunch;
	private final IConsoleView fView;
	private final IDebuggerConsole fConsole;

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
		DebugUITools.getDebugContextManager().getContextService(getSite().getWorkbenchWindow())
				.removeDebugContextListener(this);
	}

	@Override
	public void createControl(Composite parent) {
		super.createControl(parent);
		DebugUITools.getDebugContextManager().getContextService(getSite().getWorkbenchWindow())
				.addDebugContextListener(this);
	}

	@Override
	protected void configureToolBar(IToolBarManager mgr) {
		mgr.insertBefore(DebuggerConsoleView.DROP_DOWN_ACTION_ID, fTerminateLaunchAction);
	}

	@Override
	protected void createActions() {
		fTerminateLaunchAction = new GdbConsoleTerminateLaunchAction(fLaunch);
		fShowPreferencePageAction = new GdbConsoleShowPreferencesAction(getSite().getShell());
	}

	@Override
	protected void contextMenuAboutToShow(IMenuManager menuManager) {
		menuManager.add(fTerminateLaunchAction);
		menuManager.add(new Separator());

		// Other plug-ins can contribute their actions here
		menuManager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));

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
