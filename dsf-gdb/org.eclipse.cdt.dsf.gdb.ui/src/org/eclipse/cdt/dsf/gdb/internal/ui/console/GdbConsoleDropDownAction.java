/*******************************************************************************
 * Copyright (c) 2016 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.console;

import org.eclipse.cdt.dsf.gdb.internal.ui.GdbUIPlugin;
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
 * Drop down action in the GdbConsoleView to select which console to display.
 */
public class GdbConsoleDropDownAction extends Action implements IMenuCreator, IConsoleListener, IUpdate {

	private GdbConsoleView fView;
	private Menu fMenu;


	public GdbConsoleDropDownAction(GdbConsoleView view) {
		fView = view;
		setText(ConsoleMessages.ConsoleDropDownAction_name);
		setToolTipText(ConsoleMessages.ConsoleDropDownAction_description);
		setImageDescriptor(GdbUIPlugin.getImageDescriptor(IConsoleImagesConst.IMG_GDB_CONSOLE));
		setMenuCreator(this);
		getGdbConsoleManager().addConsoleListener(this);
		update();
	}

	@Override
	public void dispose() {
		if (fMenu != null) {
			fMenu.dispose();
		}

		fView = null;
		getGdbConsoleManager().removeConsoleListener(this);
	}

	private GdbConsoleManager getGdbConsoleManager() {
		return GdbUIPlugin.getGdbConsoleManager();
	}

	@Override
	public void update() {
		IConsole[] consoles = getGdbConsoleManager().getCliConsoles();
		setEnabled(consoles.length > 1);
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
		IConsole[] consoles = getGdbConsoleManager().getCliConsoles();
		IConsole current = fView.getCurrentConsole();
		for (int i = 0; i < consoles.length; i++) {
			IConsole console = consoles[i];
			Action action = new GdbShowConsoleAction(fView, console);
			action.setChecked(console.equals(current));
			addActionToMenu(fMenu, action, i + 1);
		}
		return fMenu;
	}

	private void addActionToMenu(Menu parent, Action action, int accelerator) {
		if (accelerator < 10) {
			StringBuffer label= new StringBuffer();
			//add the numerical accelerator
			label.append('&');
			label.append(accelerator);
			label.append(' ');
			label.append(action.getText());
			action.setText(label.toString());
		}
		ActionContributionItem item= new ActionContributionItem(action);
		item.fill(parent, -1);
	}

	@Override
	public void run() {
		IConsole[] consoles = getGdbConsoleManager().getCliConsoles();
		IConsole current = fView.getCurrentConsole();
		int idx = 0;
		for (int i = 0; i < consoles.length; i++) {
			idx = i;
			if (consoles[i] == current) {
				break;
			}
		}
		int next = idx+1;
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