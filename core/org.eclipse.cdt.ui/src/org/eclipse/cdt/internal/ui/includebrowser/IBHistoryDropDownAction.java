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
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.includebrowser;

import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.ui.CPluginImages;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

public class IBHistoryDropDownAction extends Action implements IMenuCreator {

	public static class ClearHistoryAction extends Action {

		private IBViewPart fView;

		public ClearHistoryAction(IBViewPart view) {
			super(IBMessages.IBHistoryDropDownAction_ClearHistory_label);
			fView = view;
		}

		@Override
		public void run() {
			fView.setHistoryEntries(new ITranslationUnit[0]);
			fView.setInput(null);
		}
	}

	public static final int RESULTS_IN_DROP_DOWN = 10;

	private IBViewPart fHierarchyView;
	private Menu fMenu;

	public IBHistoryDropDownAction(IBViewPart view) {
		fHierarchyView = view;
		fMenu = null;
		setToolTipText(IBMessages.IBHistoryDropDownAction_tooltip);
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
		ITranslationUnit[] elements = fHierarchyView.getHistoryEntries();
		addEntries(fMenu, elements);
		new MenuItem(fMenu, SWT.SEPARATOR);
		addActionToMenu(fMenu, new IBHistoryListAction(fHierarchyView));
		addActionToMenu(fMenu, new ClearHistoryAction(fHierarchyView));
		return fMenu;
	}

	private boolean addEntries(Menu menu, ITranslationUnit[] elements) {
		boolean checked = false;

		int min = Math.min(elements.length, RESULTS_IN_DROP_DOWN);
		for (int i = 0; i < min; i++) {
			IBHistoryAction action = new IBHistoryAction(fHierarchyView, elements[i]);
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
		(new IBHistoryListAction(fHierarchyView)).run();
	}
}
