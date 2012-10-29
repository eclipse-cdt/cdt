/*******************************************************************************
 * Copyright (c) 2009, 2012 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *     Freescale Semiconductor - [392962] - Improve working set build configurations usability
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.workingsets;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.actions.CompoundContributionItem;

import org.eclipse.cdt.ui.CUIPlugin;

/**
 * A dynamic contribution of sub-menus for working set configuration actions, with further sub-menus showing
 * the configurations to choose from.
 * 
 * @author Christian W. Damus (cdamus)
 * 
 * @since 6.0
 */
abstract class AbstractWorkingSetsContribution extends CompoundContributionItem {

	private final class EmptyContributionItem extends ContributionItem {
		private final String text;

		private EmptyContributionItem(String text) {
			this.text = text;
		}

		@Override
		public void fill(Menu menu, int index) {
			MenuItem item = new MenuItem(menu, SWT.NONE);
			item.setEnabled(false);
			item.setText(text);
		}

		@Override
		public boolean isEnabled() {
			return false;
		}
	}

	private IWorkingSetManager workingSetManager;

	/**
	 * Initializes me without an identifier.
	 */
	public AbstractWorkingSetsContribution() {
		super();
	}

	/**
	 * Initializes me with my identifier.
	 * 
	 * @param id
	 *            my identifier
	 */
	public AbstractWorkingSetsContribution(String id) {
		super(id);
	}

	@Override
	protected IContributionItem[] getContributionItems() {
		if (getWorkingsetManager().getWorkingSets().length == 0) {
			return new IContributionItem[] { new EmptyContributionItem(
					WorkingSetMessages.WorkingSetMenus_noWorkingSets) };
		}
		final IWorkingSet[] recentWorkingSets = getWorkingsetManager()
				.getRecentWorkingSets();
		if (recentWorkingSets.length == 0) {
			return new IContributionItem[] { new EmptyContributionItem(
					WorkingSetMessages.WorkingSetMenus_noRecentWorkingSets) };
		}
		// at most 5 recent working sets
		final int maxValidCount = 5;
		List<IContributionItem> items = new ArrayList<IContributionItem>();
		for (IWorkingSet recent : recentWorkingSets) {
			IWorkingSetProxy proxy = WorkingSetConfigurationManager.getDefault().getWorkingSet(
					recent.getName());
			if (proxy != null && proxy.isValid()) {
				items.add(createMenu(proxy, items.size()));
				if (items.size() == maxValidCount) {
					break;
				}
			}
		}
		if (items.size() == 0) {
			return new IContributionItem[] { new EmptyContributionItem(
					WorkingSetMessages.WorkingSetMenus_noProjects) };
		}
		return items.toArray(new IContributionItem[items.size()]);
	}

	private IWorkingSetManager getWorkingsetManager() {
		if (workingSetManager == null) {
			workingSetManager = CUIPlugin.getDefault().getWorkbench().getWorkingSetManager();
		}

		return workingSetManager;
	}

	private IContributionItem createMenu(IWorkingSetProxy workingSet, int index) {
		IWorkingSet ws = workingSet.resolve();
		String label = NLS.bind(WorkingSetMessages.WorkingSetMenus_enumPattern, index + 1, ws.getLabel());

		MenuManager submenu = new MenuManager(label, ws.getName());
		IContributionItem item = null;
		if (workingSet.getConfigurations().size() > 0) {
			item = createContribution(workingSet);
		} else {
			item = new EmptyContributionItem(WorkingSetMessages.WorkingSetMenus_noBuildConfigurations);
		}
		submenu.add(item);
		return submenu;
	}

	/**
	 * Creates a contribution item for a working set.
	 * 
	 * @param workingSet
	 *            a working set
	 * @return the contribution
	 */
	protected abstract IContributionItem createContribution(IWorkingSetProxy workingSet);
}
