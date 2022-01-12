/*******************************************************************************
 * Copyright (c) 2006, 2011 Intel Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Intel Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.actions;

import org.eclipse.cdt.internal.ui.actions.ActionMessages;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.newui.IConfigManager;
import org.eclipse.cdt.ui.newui.ManageConfigSelector;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowPulldownDelegate2;

/**
 * Action which changes active build configuration of the current project
 *
 * @deprecated Replaced with menu contribution {@link org.eclipse.cdt.internal.ui.workingsets.ChangeBuildConfigContribution}.
 */
@Deprecated(forRemoval = true)
public class ChangeBuildConfigMenuAction extends ChangeBuildConfigActionBase
		implements IWorkbenchWindowPulldownDelegate2 {

	/**
	 * @see org.eclipse.ui.IWorkbenchWindowPulldownDelegate2#getMenu(org.eclipse.swt.widgets.Menu)
	 */
	@Override
	public Menu getMenu(Menu parent) {
		Menu menu = new Menu(parent);
		addMenuListener(menu);
		return menu;
	}

	/**
	 * @see org.eclipse.ui.IWorkbenchWindowPulldownDelegate#getMenu(org.eclipse.swt.widgets.Control)
	 */
	@Override
	public Menu getMenu(Control parent) {
		Menu menu = new Menu(parent);
		addMenuListener(menu);
		return menu;
	}

	/**
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#dispose()
	 */
	@Override
	public void dispose() {
		// do nothing
	}

	/**
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#init(org.eclipse.ui.IWorkbenchWindow)
	 */
	@Override
	public void init(IWorkbenchWindow window) {
		// do nothing
	}

	/**
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	@Override
	public void run(IAction action) {
		IProject[] obs = fProjects.toArray(new IProject[fProjects.size()]);
		IConfigManager cm = ManageConfigSelector.getManager(obs);
		if (cm != null) {
			cm.manage(obs, true);
		} else {
			MessageDialog.openInformation(CUIPlugin.getActiveWorkbenchShell(),
					ActionMessages.ChangeBuildConfigMenuAction_title, ActionMessages.ChangeBuildConfigMenuAction_text);
		}
	}

	/**
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		onSelectionChanged(action, selection);
	}

	/**
	 * Adds a listener to the given menu to repopulate it each time is is shown
	 * @param menu The menu to add listener to
	 */
	private void addMenuListener(Menu menu) {
		menu.addMenuListener(new MenuAdapter() {
			@Override
			public void menuShown(MenuEvent e) {
				fillMenu((Menu) e.widget);
			}
		});
	}
}
