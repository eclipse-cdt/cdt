/*******************************************************************************
 * Copyright (c) 2009 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.workingsets;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.actions.CompoundContributionItem;

import com.ibm.icu.text.Collator;

import org.eclipse.cdt.ui.CUIPlugin;

/**
 * Common API of dynamic contribution of items to manipulate configurations of a working set.
 * 
 * @author Christian W. Damus (cdamus)
 * 
 * @since 6.0
 */
abstract class AbstractWorkingSetConfigsContribution extends CompoundContributionItem {

	private IWorkingSetProxy workingSet;

	private Comparator<IWorkingSetConfiguration> configOrdering = new Comparator<IWorkingSetConfiguration>() {
		private Collator collator = Collator.getInstance();

		@Override
		public int compare(IWorkingSetConfiguration o1, IWorkingSetConfiguration o2) {
			return collator.compare(o1.getName(), o2.getName());
		}
	};

	/**
	 * Initializes me without a working set. I figure it out, later. This is only appropriate usage for
	 * context-menu contribution, where the workbench selection is obvious.
	 */
	public AbstractWorkingSetConfigsContribution() {
		super();
	}

	/**
	 * Initializes me with my working set.
	 * 
	 * @param workingSet
	 *            my working set
	 */
	AbstractWorkingSetConfigsContribution(IWorkingSetProxy workingSet) {
		super();

		this.workingSet = workingSet;
	}

	@Override
	protected IContributionItem[] getContributionItems() {
		if (getWorkingSet() == null) {
			return new IContributionItem[0];
		}

		// sort the configurations by name
		List<IWorkingSetConfiguration> configs = new java.util.ArrayList<IWorkingSetConfiguration>(
				getWorkingSet().getConfigurations());
		Collections.sort(configs, configOrdering);

		IContributionItem[] result = new IContributionItem[configs.size()];
		int i = 0;
		for (IWorkingSetConfiguration next : configs) {
			result[i] = createContribution(next, i);
			i++;
		}

		return result;
	}

	/**
	 * Creates a contribution item for a specific configuration of my working set.
	 * 
	 * @param config
	 *            a configuration of my working set
	 * @param index
	 *            the index of the contribution in the composite
	 *            
	 * @return the contribution
	 */
	protected abstract IContributionItem createContribution(IWorkingSetConfiguration config, int index);

	/**
	 * Obtains my working set. It may be lazily determined from the current workbench selection.
	 * 
	 * @return my working set
	 */
	protected IWorkingSetProxy getWorkingSet() {
		if (workingSet == null) {
			ISelection sel = CUIPlugin.getActivePage().getSelection();
			if (sel instanceof IStructuredSelection) {
				IStructuredSelection ssel = (IStructuredSelection) sel;

				if (!ssel.isEmpty()) {
					Object first = ssel.getFirstElement();
					if (first instanceof IWorkingSet) {
						workingSet = WorkingSetConfigurationManager.getDefault().getWorkingSet(
								((IWorkingSet) first).getName());
					}
				}
			}
		}

		return workingSet;
	}
}
