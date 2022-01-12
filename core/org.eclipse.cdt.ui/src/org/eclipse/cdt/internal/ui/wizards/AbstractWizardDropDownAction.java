/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.wizards;

import org.eclipse.core.runtime.IRegistryChangeEvent;
import org.eclipse.core.runtime.IRegistryChangeListener;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowPulldownDelegate2;

public abstract class AbstractWizardDropDownAction extends Action
		implements IMenuCreator, IWorkbenchWindowPulldownDelegate2 {

	protected final static IAction[] NO_ACTIONS = new IAction[0];
	private Menu fMenu;
	private IAction[] fActions;
	private IRegistryChangeListener fListener;
	private Object fLock = new Object();

	public AbstractWizardDropDownAction() {
		fMenu = null;
		fActions = null;
		setMenuCreator(this);

		// listen for changes to wizard extensions
		fListener = new IRegistryChangeListener() {
			@Override
			public void registryChanged(IRegistryChangeEvent event) {
				refreshActions();
			}
		};
		Platform.getExtensionRegistry().addRegistryChangeListener(fListener);
	}

	public void refreshActions() {
		// force menu and actions to be created again
		Menu oldMenu = null;
		synchronized (fLock) {
			oldMenu = fMenu;
			fActions = null;
			fMenu = null;
		}
		if (oldMenu != null)
			oldMenu.dispose();
	}

	@Override
	public void dispose() {
		if (fListener != null) {
			Platform.getExtensionRegistry().removeRegistryChangeListener(fListener);
			fListener = null;
		}
		refreshActions();
	}

	@Override
	public Menu getMenu(Menu parent) {
		return null;
	}

	@Override
	public Menu getMenu(Control parent) {
		synchronized (fLock) {
			fMenu = new Menu(parent);
			IAction[] actions = getActions();
			for (int i = 0; i < actions.length; i++) {
				ActionContributionItem item = new ActionContributionItem(actions[i]);
				item.fill(fMenu, -1);
			}
			return fMenu;
		}
	}

	@Override
	public void run() {
		// for now, run the default action
		// we might want the last run action at some point
		IAction action = getDefaultAction();
		if (action != null) {
			action.run();
		}
	}

	public IAction getDefaultAction() {
		IAction[] actions = getActions();
		if (actions.length > 0) {
			actions[0].getId();
			return actions[0];
			//		    for (int i = 0; i < actions.length; ++i) {
			//		        IAction action = actions[i];
			//			    if (action.isEnabled()) {
			//			        return action;
			//			    }
			//		    }
		}
		return null;
	}

	private IAction[] getActions() {
		synchronized (fLock) {
			fActions = getWizardActions();
			if (fActions == null)
				fActions = NO_ACTIONS;

			//TODO provide a way to sort the actions

			return fActions;
		}
	}

	protected abstract IAction[] getWizardActions();

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#init(org.eclipse.ui.IWorkbenchWindow)
	 */
	@Override
	public void init(IWorkbenchWindow window) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	@Override
	public void run(IAction action) {
		run();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	@Override
	public void selectionChanged(IAction action, ISelection selection) {
	}

}
