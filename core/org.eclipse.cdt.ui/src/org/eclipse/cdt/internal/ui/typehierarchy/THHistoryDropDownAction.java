/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.typehierarchy;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import org.eclipse.cdt.core.model.ICElement;

import org.eclipse.cdt.internal.ui.CPluginImages;

public class THHistoryDropDownAction extends Action implements IMenuCreator {
	
	public static class ClearHistoryAction extends Action {

		private THViewPart fView;
		
		public ClearHistoryAction(THViewPart view) {
			super(Messages.THHistoryDropDownAction_ClearHistory);
			fView= view;
		}
			
		@Override
		public void run() {
			fView.setHistoryEntries(new ICElement[0]);
			fView.setInput(null, null);
		}
	}

	public static final int RESULTS_IN_DROP_DOWN= 10;

	private THViewPart fHierarchyView;
	private Menu fMenu;
	
	public THHistoryDropDownAction(THViewPart viewPart) {
		fHierarchyView= viewPart;
		fMenu= null;
		setToolTipText(Messages.THHistoryDropDownAction_tooltip); 
		CPluginImages.setImageDescriptors(this, CPluginImages.T_LCL, "history_list.gif"); //$NON-NLS-1$
		setMenuCreator(this);
	}

	@Override
	public void dispose() {
		// action is reused, can be called several times.
		if (fMenu != null) {
			fMenu.dispose();
			fMenu= null;
		}
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
		fMenu= new Menu(parent);
		ICElement[] elements= fHierarchyView.getHistoryEntries();
		addEntries(fMenu, elements);
		new MenuItem(fMenu, SWT.SEPARATOR);
		addActionToMenu(fMenu, new THHistoryListAction(fHierarchyView));
		addActionToMenu(fMenu, new ClearHistoryAction(fHierarchyView));
		return fMenu;
	}
	
	private boolean addEntries(Menu menu, ICElement[] elements) {
		boolean checked= false;
		
		int min= Math.min(elements.length, RESULTS_IN_DROP_DOWN);
		for (int i= 0; i < min; i++) {
			THHistoryAction action= new THHistoryAction(fHierarchyView, elements[i]);
			action.setChecked(elements[i].equals(fHierarchyView.getInput()));
			checked= checked || action.isChecked();
			addActionToMenu(menu, action);
		}
		
		
		return checked;
	}
	

	protected void addActionToMenu(Menu parent, Action action) {
		ActionContributionItem item= new ActionContributionItem(action);
		item.fill(parent, -1);
	}

	@Override
	public void run() {
		(new THHistoryListAction(fHierarchyView)).run();
	}
}
