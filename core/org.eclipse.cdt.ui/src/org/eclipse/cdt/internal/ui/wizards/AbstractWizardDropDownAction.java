/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
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

public abstract class AbstractWizardDropDownAction extends Action implements IMenuCreator, IWorkbenchWindowPulldownDelegate2 {

	protected final static IAction[] NO_ACTIONS = new IAction[0];
	private Menu fMenu;
	private IAction[] fActions;
	private IRegistryChangeListener fListener;
	private Object fLock = new Object();
	
	public AbstractWizardDropDownAction() {
		fMenu= null;
		fActions= null;
		setMenuCreator(this);
		
		// listen for changes to wizard extensions
		fListener = new IRegistryChangeListener() {
		    public void registryChanged(IRegistryChangeEvent event) {
		        refreshActions();
		    }
		};
		Platform.getExtensionRegistry().addRegistryChangeListener(fListener);
	}
	
	public void refreshActions() {
        // force menu and actions to be created again
		Menu oldMenu = null;
		synchronized(fLock) {
			oldMenu = fMenu;
	        fActions = null;
	        fMenu = null;
		}
		if (oldMenu != null)
			oldMenu.dispose();
	}

	public void dispose() {
		if (fListener != null) {
			Platform.getExtensionRegistry().removeRegistryChangeListener(fListener);
			fListener= null;
		}
		refreshActions();
	}

	public Menu getMenu(Menu parent) {
		return null;
	}

	public Menu getMenu(Control parent) {
		synchronized(fLock) {
			if (fMenu == null) {
				fMenu= new Menu(parent);
				IAction[] actions= getActions();
				for (int i= 0; i < actions.length; i++) {
					ActionContributionItem item= new ActionContributionItem(actions[i]);
					item.fill(fMenu, -1);				
				}
			}
			return fMenu;
		}
	}
	
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
		synchronized(fLock) {
		    if (fActions == null) {
		        fActions = getWizardActions();
			    if (fActions == null)
			        fActions = NO_ACTIONS;
	
			    //TODO provide a way to sort the actions
		    }
		    return fActions;
		}
	}
	
	protected abstract IAction[] getWizardActions();
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#init(org.eclipse.ui.IWorkbenchWindow)
	 */
	public void init(IWorkbenchWindow window) {
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		run();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
	}

}
