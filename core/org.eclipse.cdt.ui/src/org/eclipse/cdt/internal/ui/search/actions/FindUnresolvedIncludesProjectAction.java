/*******************************************************************************
 * Copyright (c) 2008, 2014 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.search.actions;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.internal.ui.actions.AbstractUpdateIndexAction;
import org.eclipse.cdt.internal.ui.search.CSearchMessages;
import org.eclipse.cdt.internal.ui.search.CSearchUnresolvedIncludesQuery;
import org.eclipse.cdt.internal.ui.util.StatusLineHandler;
import org.eclipse.jface.action.IAction;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchSite;

/**
 * Searches projects for unresolved includes.
 * Could be extended to work on resource selections.
 */
public class FindUnresolvedIncludesProjectAction extends AbstractUpdateIndexAction {
	private IWorkbenchSite fSite;

	public FindUnresolvedIncludesProjectAction() {
	}

	@Override
	protected void doRun(ICElement[] elements) {
		if (elements.length == 0) {
			StatusLineHandler.showStatusLineMessage(fSite,
					CSearchMessages.CSearchOperation_operationUnavailable_message);
			return;
		}

		ISearchQuery searchJob = new CSearchUnresolvedIncludesQuery(elements);

		StatusLineHandler.clearStatusLine(fSite);
		NewSearchUI.activateSearchResultView();
		NewSearchUI.runQueryInBackground(searchJob);
	}

	@Override
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		fSite = targetPart.getSite();
	}

	@Override
	protected int getUpdateOptions() {
		return 0;
	}
}
