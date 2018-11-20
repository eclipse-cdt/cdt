/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
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
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.callhierarchy;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.internal.ui.CPluginImages;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

public class CHHistoryDropDownAction extends Action implements IMenuCreator {

	public static class ClearHistoryAction extends Action {

		private CHViewPart fView;

		public ClearHistoryAction(CHViewPart view) {
			super(CHMessages.CHHistoryDropDownAction_ClearHistory_label);
			fView = view;
		}

		@Override
		public void run() {
			CallHierarchyUI.clearHistory();
			fView.setInput(null);
		}
	}

	public static final int RESULTS_IN_DROP_DOWN = 10;

	private CHViewPart fHierarchyView;
	private Menu fMenu;

	public CHHistoryDropDownAction(CHViewPart view) {
		fHierarchyView = view;
		fMenu = null;
		setToolTipText(CHMessages.CHHistoryDropDownAction_ShowHistoryList_tooltip);
		CPluginImages.setImageDescriptors(this, CPluginImages.T_LCL, "history_list.gif"); //$NON-NLS-1$
		setMenuCreator(this);
	}

	@Override
	public void dispose() {
		// action is reused, can be called several times.
		if (fMenu != null) {
			fMenu.dispose();
			fMenu = null;
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
		fMenu = new Menu(parent);
		ICElement[] elements = CallHierarchyUI.getHistoryEntries();
		addEntries(fMenu, elements);
		new MenuItem(fMenu, SWT.SEPARATOR);
		addActionToMenu(fMenu, new CHHistoryListAction(fHierarchyView));
		addActionToMenu(fMenu, new ClearHistoryAction(fHierarchyView));
		return fMenu;
	}

	private boolean addEntries(Menu menu, ICElement[] elements) {
		boolean checked = false;

		int min = Math.min(elements.length, RESULTS_IN_DROP_DOWN);
		for (int i = 0; i < min; i++) {
			CHHistoryAction action = new CHHistoryAction(fHierarchyView, elements[i]);
			action.setChecked(elements[i].equals(fHierarchyView.getInput()));
			checked = checked || action.isChecked();
			addActionToMenu(menu, action);
		}

		return checked;
	}

	protected void addActionToMenu(Menu parent, Action action) {
		ActionContributionItem item = new ActionContributionItem(action);
		item.fill(parent, -1);
	}

	@Override
	public void run() {
		(new CHHistoryListAction(fHierarchyView)).run();
	}
}
