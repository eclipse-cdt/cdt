/*******************************************************************************
 * Copyright (c) 2009 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.workingsets;

import java.util.Collection;
import java.util.List;

import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.actions.CompoundContributionItem;

/**
 * A dynamic contribution of sub-menus for working set configuration actions, with further sub-menus showing
 * the configurations to choose from.
 *
 * @author Christian W. Damus (cdamus)
 *
 * @since 6.0
 */
abstract class AbstractWorkingSetsContribution extends CompoundContributionItem {

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
		// at most 5 recent working sets
		List<IContributionItem> result = new java.util.ArrayList<>(5);

		int i = 0;
		for (IWorkingSet recent : getWorkingsetManager().getRecentWorkingSets()) {
			IWorkingSetProxy proxy = WorkingSetConfigurationManager.getDefault().getWorkingSet(recent.getName());

			if (proxy != null) {
				IContributionItem item = createMenu(proxy, i++);
				if (item != null) {
					result.add(item);
				}
			}
		}

		return result.toArray(new IContributionItem[result.size()]);
	}

	private IWorkingSetManager getWorkingsetManager() {
		if (workingSetManager == null) {
			workingSetManager = CUIPlugin.getDefault().getWorkbench().getWorkingSetManager();
		}

		return workingSetManager;
	}

	private IContributionItem createMenu(IWorkingSetProxy workingSet, int index) {
		IContributionItem result = null;
		IWorkingSet ws = workingSet.resolve();
		String label = NLS.bind(WorkingSetMessages.WorkingSetMenus_enumPattern, index + 1, ws.getLabel());
		Collection<IWorkingSetConfiguration> configs = workingSet.getConfigurations();

		if (!configs.isEmpty()) {
			MenuManager submenu = new MenuManager(label, ws.getName());
			result = submenu;

			submenu.add(createContribution(workingSet));
		}

		return result;
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
