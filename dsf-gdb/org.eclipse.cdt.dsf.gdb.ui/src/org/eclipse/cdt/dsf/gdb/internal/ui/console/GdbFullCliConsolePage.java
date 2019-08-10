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

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

import org.eclipse.cdt.debug.internal.ui.views.debuggerconsole.DebuggerConsoleView;
import org.eclipse.cdt.debug.ui.debuggerconsole.IDebuggerConsole;
import org.eclipse.cdt.debug.ui.debuggerconsole.IDebuggerConsoleView;
import org.eclipse.cdt.dsf.gdb.IGdbDebugPreferenceConstants;
import org.eclipse.cdt.dsf.gdb.internal.ui.GdbUIPlugin;
import org.eclipse.cdt.dsf.gdb.internal.ui.console.actions.GdbAutoTerminateAction;
import org.eclipse.cdt.dsf.gdb.internal.ui.console.actions.GdbConsoleClearAction;
import org.eclipse.cdt.dsf.gdb.internal.ui.console.actions.GdbConsoleCopyAction;
import org.eclipse.cdt.dsf.gdb.internal.ui.console.actions.GdbConsolePasteAction;
import org.eclipse.cdt.dsf.gdb.internal.ui.console.actions.GdbConsoleScrollLockAction;
import org.eclipse.cdt.dsf.gdb.internal.ui.console.actions.GdbConsoleSelectAllAction;
import org.eclipse.cdt.dsf.gdb.internal.ui.console.actions.GdbConsoleShowPreferencesAction;
import org.eclipse.cdt.dsf.gdb.internal.ui.console.actions.GdbConsoleTerminateLaunchAction;
import org.eclipse.cdt.utils.pty.PTY;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.contexts.DebugContextEvent;
import org.eclipse.debug.ui.contexts.IDebugContextListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
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
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.part.Page;

/**
 * Page used for a full GDB console.  Each Debug session which uses the full GDB console will
 * use its own instance of this page.  GDB 7.12 is required to use the full GDB console.
 *
 * Contributions to this page's context menu can be done using id "GdbFullCliConsole.#ContextMenu".
 * For example, using the extension point:<br>
 * <code>
 *       menuContribution locationURI="popup:GdbFullCliConsole.#ContextMenu?after=additions"
 * </code>
 */
public class GdbFullCliConsolePage extends Page implements IDebugContextListener {

	private final ILaunch fLaunch;
	private final PTY fGdbPty;

	private Composite fMainComposite;
	private final IDebuggerConsoleView fView;
	private final IDebuggerConsole fConsole;
	private final IGdbTerminalControlConnector fGdbTerminalControlConnector;

	private MenuManager fMenuManager;

	private GdbConsoleTerminateLaunchAction fTerminateLaunchAction;

	/** The control for the terminal widget embedded in the console */
	private ITerminalViewControl fTerminalControl;

	private GdbConsoleClearAction fClearAction;
	private GdbConsoleCopyAction fCopyAction;
	private GdbConsolePasteAction fPasteAction;
	private GdbConsoleScrollLockAction fScrollLockAction;
	private GdbConsoleSelectAllAction fSelectAllAction;
	private GdbAutoTerminateAction fAutoTerminateAction;

	private GdbConsoleShowPreferencesAction fShowPreferencePageAction;

	private GdbAbstractConsolePreferenceListener fPreferenceListener = new GdbAbstractConsolePreferenceListener() {

		@Override
		protected void handleAutoTerminatePref(boolean enabled) {
			if (fAutoTerminateAction != null) {
				fAutoTerminateAction.setChecked(enabled);
			}
		}

		@Override
		protected void handleInvertColorsPref(boolean enabled) {
			setInvertedColors(enabled);
		}

		@Override
		protected void handleBufferLinesPref(int bufferLines) {
			setBufferLineLimit(bufferLines);
		}
	};

	public GdbFullCliConsolePage(GdbFullCliConsole gdbConsole, IDebuggerConsoleView view, PTY pty) {
		fConsole = gdbConsole;
		fGdbTerminalControlConnector = gdbConsole.getTerminalControlConnector();
		fView = view;
		fLaunch = gdbConsole.getLaunch();
		fGdbPty = pty;

		GdbUIPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(fPreferenceListener);
	}

	@Override
	public void dispose() {
		DebugUITools.getDebugContextManager().getContextService(getSite().getWorkbenchWindow())
				.removeDebugContextListener(this);
		fTerminalControl.disposeTerminal();
		fMenuManager.dispose();
		GdbUIPlugin.getDefault().getPreferenceStore().removePropertyChangeListener(fPreferenceListener);
		super.dispose();
	}

	@Override
	public void createControl(Composite parent) {
		fMainComposite = new Composite(parent, SWT.NONE);
		fMainComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		fMainComposite.setLayout(new FillLayout());

		DebugUITools.getDebugContextManager().getContextService(getSite().getWorkbenchWindow())
				.addDebugContextListener(this);

		createTerminalControl();
		createContextMenu();
		configureToolBar(getSite().getActionBars().getToolBarManager());
	}

	private void setDefaults() {
		IPreferenceStore store = GdbUIPlugin.getDefault().getPreferenceStore();
		setInvertedColors(store.getBoolean(IGdbDebugPreferenceConstants.PREF_CONSOLE_INVERTED_COLORS));
		setBufferLineLimit(store.getInt(IGdbDebugPreferenceConstants.PREF_CONSOLE_BUFFERLINES));
	}

	private void createTerminalControl() {
		// Create the terminal control that will be used to interact with GDB
		// Don't use common terminal preferences as GDB consoles are having their own
		boolean useCommonPrefs = false;
		fTerminalControl = TerminalViewControlFactory.makeControl(new ITerminalListener() {
			@Override
			public void setState(TerminalState state) {
			}

			@Override
			public void setTerminalTitle(final String title) {
			}
		}, fMainComposite, new ITerminalConnector[] {}, useCommonPrefs);

		fTerminalControl.setConnector(new GdbTerminalPageConnector(fGdbTerminalControlConnector, fGdbPty));

		try {
			fTerminalControl.setEncoding(Charset.defaultCharset().name());
		} catch (UnsupportedEncodingException e) {
		}
		if (fTerminalControl instanceof ITerminalControl) {
			((ITerminalControl) fTerminalControl).setConnectOnEnterIfClosed(false);
			((ITerminalControl) fTerminalControl).setVT100LineWrapping(true);
		}

		// Must use syncExec because the logic within must complete before the rest
		// of the class methods (specifically getProcess()) is called
		fMainComposite.getDisplay().syncExec(() -> {
			if (fTerminalControl != null && !fTerminalControl.isDisposed()) {
				fTerminalControl.clearTerminal();
				fTerminalControl.connectTerminal();

				// The actual terminal widget initializes its defaults in the line above,
				// lets override them with our application defaults right after.
				setDefaults();
			}
		});
	}

	protected void createContextMenu() {
		// Choose the id to be similar in format to what
		// the GdbBasicCliConsole has as id, for consistency
		String id = "GdbFullCliConsole.#ContextMenu"; //$NON-NLS-1$
		fMenuManager = new MenuManager(id, id);
		fMenuManager.setRemoveAllWhenShown(true);
		fMenuManager.addMenuListener((menuManager) -> {
			contextMenuAboutToShow(menuManager);
		});
		Menu menu = fMenuManager.createContextMenu(fTerminalControl.getControl());
		fTerminalControl.getControl().setMenu(menu);

		createActions();

		getSite().registerContextMenu(id, fMenuManager, getSite().getSelectionProvider());
	}

	protected void createActions() {
		fTerminateLaunchAction = new GdbConsoleTerminateLaunchAction(fLaunch);
		fClearAction = new GdbConsoleClearAction(fTerminalControl);
		fCopyAction = new GdbConsoleCopyAction(fTerminalControl);
		fPasteAction = new GdbConsolePasteAction(fTerminalControl);
		fScrollLockAction = new GdbConsoleScrollLockAction(fTerminalControl);
		fSelectAllAction = new GdbConsoleSelectAllAction(fTerminalControl);
		fAutoTerminateAction = new GdbAutoTerminateAction();
		fShowPreferencePageAction = new GdbConsoleShowPreferencesAction(getSite().getShell());
	}

	protected void configureToolBar(IToolBarManager mgr) {
		mgr.insertBefore(DebuggerConsoleView.DROP_DOWN_ACTION_ID, fTerminateLaunchAction);
		mgr.insertBefore(DebuggerConsoleView.DROP_DOWN_ACTION_ID, fClearAction);
		mgr.insertBefore(DebuggerConsoleView.DROP_DOWN_ACTION_ID, fScrollLockAction);
	}

	protected void contextMenuAboutToShow(IMenuManager menuManager) {
		menuManager.add(fCopyAction);
		menuManager.add(fPasteAction);
		menuManager.add(fSelectAllAction);
		menuManager.add(new Separator());

		menuManager.add(fClearAction);
		menuManager.add(new Separator());

		menuManager.add(fScrollLockAction);
		menuManager.add(new Separator());

		menuManager.add(fTerminateLaunchAction);
		menuManager.add(fAutoTerminateAction);
		menuManager.add(new Separator());

		// Other plug-ins can contribute their actions here
		menuManager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));

		menuManager.add(fShowPreferencePageAction);
	}

	@Override
	public Control getControl() {
		return fMainComposite;
	}

	@Override
	public void setFocus() {
		fTerminalControl.setFocus();
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

	private void setInvertedColors(boolean enable) {
		if (fTerminalControl != null) {
			fTerminalControl.setInvertedColors(enable);
		}
	}

	private void setBufferLineLimit(int bufferLines) {
		if (fTerminalControl != null) {
			fTerminalControl.setBufferLineLimit(bufferLines);
		}
	}

	public ITerminalViewControl getTerminalViewControl() {
		return fTerminalControl;
	}
}
