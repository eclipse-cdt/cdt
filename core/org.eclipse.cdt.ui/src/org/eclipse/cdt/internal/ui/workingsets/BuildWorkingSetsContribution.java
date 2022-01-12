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

import org.eclipse.jface.action.IContributionItem;

/**
 * A dynamic contribution of sub-menus to build working sets, with further sub-menus showing the
 * configurations to choose from.
 *
 * @author Christian W. Damus (cdamus)
 *
 * @since 6.0
 */
public class BuildWorkingSetsContribution extends AbstractWorkingSetsContribution {

	/**
	 * Initializes me.
	 */
	public BuildWorkingSetsContribution() {
		super();
	}

	@Override
	protected IContributionItem createContribution(IWorkingSetProxy workingSet) {
		return new BuildWorkingSetConfigsContribution(workingSet);
	}
}
