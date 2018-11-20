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
package org.eclipse.cdt.debug.internal.ui.views.debuggerconsole;

import org.eclipse.cdt.debug.internal.ui.CDebugImages;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.cdt.debug.ui.debuggerconsole.IDebuggerConsole;
import org.eclipse.cdt.debug.ui.debuggerconsole.IDebuggerConsoleManager;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleListener;
import org.eclipse.ui.progress.UIJob;
import org.eclipse.ui.texteditor.IUpdate;

/**
 * Drop down action in the DebuggerConsoleView to select which console to display.
 */
public class DebuggerConsoleDropDownAction extends Action implements IMenuCreator, IConsoleListener, IUpdate {

	private DebuggerConsoleView fView;
	private Menu fMenu;

	public DebuggerConsoleDropDownAction(DebuggerConsoleView view) {
		fView = view;
		setId(DebuggerConsoleView.DROP_DOWN_ACTION_ID);
		setText(ConsoleMessages.ConsoleDropDownAction_name);
		setToolTipText(ConsoleMessages.ConsoleDropDownAction_description);
		setImageDescriptor(CDebugImages.DESC_OBJS_DEBUGGER_CONSOLE_SELECT);
		setMenuCreator(this);
		getDebuggerConsoleManager().addConsoleListener(this);
		update();
	}

	@Override
	public void dispose() {
		if (fMenu != null) {
			fMenu.dispose();
		}

		fView = null;
		getDebuggerConsoleManager().removeConsoleListener(this);
	}

	private IDebuggerConsoleManager getDebuggerConsoleManager() {
		return CDebugUIPlugin.getDebuggerConsoleManager();
	}

	@Override
	public void update() {
		IDebuggerConsole[] consoles = getDebuggerConsoleManager().getConsoles();
		// Keep the button enabled as soon as there is at least one console.
		// Having it disabled for a single console can prove confusing to users,
		// so we enable it even for a single console.
		setEnabled(consoles.length > 0);
	}

	@Override
	public Menu getMenu(Menu parent) {
		return null;
	}

	@Override
	public Menu getMenu(Control parent) {
		if (fMenu != null) {
			fMenu.dispose();
		}

		fMenu = new Menu(parent);
		IDebuggerConsole[] consoles = getDebuggerConsoleManager().getConsoles();
		IDebuggerConsole current = fView.getCurrentConsole();
		for (int i = 0; i < consoles.length; i++) {
			IDebuggerConsole console = consoles[i];
			Action action = new DebuggerShowConsoleAction(fView, console);
			action.setChecked(console.equals(current));
			addActionToMenu(fMenu, action, i + 1);
		}
		return fMenu;
	}

	private void addActionToMenu(Menu parent, Action action, int accelerator) {
		if (accelerator < 10) {
			StringBuffer label = new StringBuffer();
			//add the numerical accelerator
			label.append('&');
			label.append(accelerator);
			label.append(' ');
			label.append(action.getText());
			action.setText(label.toString());
		}
		ActionContributionItem item = new ActionContributionItem(action);
		item.fill(parent, -1);
	}

	@Override
	public void run() {
		IDebuggerConsole[] consoles = getDebuggerConsoleManager().getConsoles();
		IDebuggerConsole current = fView.getCurrentConsole();
		int idx = 0;
		for (int i = 0; i < consoles.length; i++) {
			idx = i;
			if (consoles[i] == current) {
				break;
			}
		}
		int next = idx + 1;
		if (next >= consoles.length) {
			next = 0;
		}
		fView.display(consoles[next]);
	}

	@Override
	public void consolesAdded(IConsole[] consoles) {
		UIJob job = new UIJob("") { //$NON-NLS-1$
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				update();
				return Status.OK_STATUS;
			}
		};
		job.setSystem(true);
		job.setPriority(Job.INTERACTIVE);
		job.schedule();
	}

	@Override
	public void consolesRemoved(IConsole[] consoles) {
		UIJob job = new UIJob("") { //$NON-NLS-1$
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				if (fMenu != null) {
					fMenu.dispose();
				}
				update();
				return Status.OK_STATUS;
			}
		};
		job.setSystem(true);
		job.setPriority(Job.INTERACTIVE);
		job.schedule();
	}
}