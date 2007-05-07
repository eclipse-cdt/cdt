/*******************************************************************************
 * Copyright (c) 2007 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Nokia - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.actions;

import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowPulldownDelegate2;
import org.eclipse.ui.actions.BuildAction;

/**
 * Implements a toolbar button that builds the active configuration
 * of selected projects. Also includes a menu that builds any of the
 * other configurations.
 *
 */
public class BuildActiveConfigMenuAction extends ChangeBuildConfigActionBase
		implements IWorkbenchWindowPulldownDelegate2 {

	private BuildAction buildaction;

	/**
	 * @see org.eclipse.ui.IWorkbenchWindowPulldownDelegate2#getMenu(org.eclipse.swt.widgets.Menu)
	 */
	public Menu getMenu(Menu parent) {
		Menu menu = new Menu(parent);
		addMenuListener(menu);
		return menu;
	}

	/**
	 * @see org.eclipse.ui.IWorkbenchWindowPulldownDelegate#getMenu(org.eclipse.swt.widgets.Control)
	 */
	public Menu getMenu(Control parent) {
		Menu menu = new Menu(parent);
		addMenuListener(menu);
		return menu;
	}

	/**
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#dispose()
	 */
	public void dispose() {
		// do nothing
	}

	/**
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#init(org.eclipse.ui.IWorkbenchWindow)
	 */
	public void init(IWorkbenchWindow window) {
		buildaction = new BuildAction(window.getShell(), IncrementalProjectBuilder.INCREMENTAL_BUILD);
	}

	/**
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		buildaction.selectionChanged(new StructuredSelection(fProjects.toArray()));
		buildaction.run();
	}

	/**
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		onSelectionChanged(action, selection);
	}
	
	/**
	 * Adds a listener to the given menu to repopulate it each time is is shown
	 * @param menu The menu to add listener to
	 */
	private void addMenuListener(Menu menu) {
		menu.addMenuListener(new MenuAdapter() {
			public void menuShown(MenuEvent e) {
				fillMenu((Menu)e.widget);
			}
		});
	}

	protected IAction makeAction(String sName, StringBuffer builder, int accel) {
		return new BuildConfigAction(fProjects, sName, builder.toString(), accel + 1, buildaction);
	}

}
