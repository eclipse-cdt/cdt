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

import java.util.ArrayList;

import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
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
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.IWorkbenchConstants;

public abstract class AbstractWizardDropDownAction extends Action implements IMenuCreator, IWorkbenchWindowPulldownDelegate2 {

	private final static String TAG_WIZARD = "wizard"; //$NON-NLS-1$
	private final static String ATT_CATEGORY = "category";//$NON-NLS-1$
	private Menu fMenu;
	
	public AbstractWizardDropDownAction() {
		fMenu= null;
		setMenuCreator(this);
	}

	public void dispose() {
		if (fMenu != null) {
			fMenu.dispose();
			fMenu= null;
		}
	}

	public Menu getMenu(Menu parent) {
		return null;
	}

	public Menu getMenu(Control parent) {
		if (fMenu == null) {
			fMenu= new Menu(parent);
			
			Action[] actions= getActionFromDescriptors();
			for (int i= 0; i < actions.length; i++) {
				ActionContributionItem item= new ActionContributionItem(actions[i]);
				item.fill(fMenu, -1);				
			}			
		
		}
		return fMenu;
	}
	
	public void run() {
	    // for now, just run the first available action
		Action[] actions = getActionFromDescriptors();
		if (actions != null) {
		    for (int i = 0; i < actions.length; ++i) {
		        AbstractOpenWizardAction action = (AbstractOpenWizardAction) actions[0];
			    if (action.isEnabled()) {
			        action.run();
			        return;
			    }
		    }
		}
	}
	
	public Action[] getActionFromDescriptors() {
		ArrayList CActions = new ArrayList();
		ArrayList CCActions = new ArrayList();
		
		IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint(PlatformUI.PLUGIN_ID, IWorkbenchConstants.PL_NEW);
		if (extensionPoint != null) {
			IConfigurationElement[] elements = extensionPoint.getConfigurationElements();
			for (int i = 0; i < elements.length; i++) {
				IConfigurationElement element= elements[i];
				if (element.getName().equals(TAG_WIZARD)) {
				    String category = element.getAttribute(ATT_CATEGORY);
				    if (category != null) {
				        if (category.equals(CUIPlugin.CCWIZARD_CATEGORY_ID)) {
					        AbstractOpenWizardAction action = createWizardAction(element);
					        if (action != null) {
							    CCActions.add(action);
							}
				        } else if (category.equals(CUIPlugin.CWIZARD_CATEGORY_ID)) {
					        AbstractOpenWizardAction action = createWizardAction(element);
					        if (action != null) {
							    CActions.add(action);
							}
				        }
				    }
				}
			}
		}

		//TODO: check for duplicate actions
		// show C actions, then C++ Actions
		CActions.addAll(CCActions);
		return (Action[]) CActions.toArray(new Action[CActions.size()]);
	}
	
	public abstract AbstractOpenWizardAction createWizardAction(IConfigurationElement element);
		
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
