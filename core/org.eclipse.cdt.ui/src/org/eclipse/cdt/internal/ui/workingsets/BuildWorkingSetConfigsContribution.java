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

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.osgi.util.NLS;

/**
 * A dynamic contribution of items to build configurations of a working set.
 * 
 * @author Christian W. Damus (cdamus)
 * 
 * @since 6.0
 */
public class BuildWorkingSetConfigsContribution extends AbstractWorkingSetConfigsContribution {

	/**
	 * Initializes me without a working set. I figure it out, later. This is only appropriate usage for
	 * context-menu contribution, where the workbench selection is obvious.
	 */
	public BuildWorkingSetConfigsContribution() {
		super();
	}

	/**
	 * Initializes me with my working set.
	 * 
	 * @param workingSet
	 *            my working set
	 */
	BuildWorkingSetConfigsContribution(IWorkingSetProxy workingSet) {
		super(workingSet);
	}

	@Override
	protected IContributionItem createContribution(IWorkingSetConfiguration config, int index) {
		return new ActionContributionItem(new BuildConfigAction(config, index + 1));
	}

	//
	// Nested classes
	//

	private static class BuildConfigAction extends Action {
		private IWorkingSetConfiguration workingSetConfig;

		BuildConfigAction(IWorkingSetConfiguration workingSetConfig, int ordinal) {
			super(NLS.bind(WorkingSetMessages.WorkingSetMenus_enumPattern, ordinal, workingSetConfig
					.getName()));

			this.workingSetConfig = workingSetConfig;
		}

		@Override
		public void run() {
			new BuildJob(workingSetConfig).schedule();
		}
	}
}
