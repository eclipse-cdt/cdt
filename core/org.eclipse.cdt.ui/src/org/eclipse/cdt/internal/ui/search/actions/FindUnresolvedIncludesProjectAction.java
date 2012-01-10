/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.internal.ui.search.actions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchSite;

import org.eclipse.cdt.core.model.ICProject;

import org.eclipse.cdt.internal.ui.actions.SelectionConverter;
import org.eclipse.cdt.internal.ui.search.CSearchMessages;
import org.eclipse.cdt.internal.ui.search.CSearchUnresolvedIncludesQuery;
import org.eclipse.cdt.internal.ui.util.StatusLineHandler;

/**
 * Searches projects for unresolved includes.
 * Could be extended to work on resource selections.
 */
public class FindUnresolvedIncludesProjectAction implements IObjectActionDelegate {
	private ISelection fSelection;
	private IWorkbenchSite fSite;

	public FindUnresolvedIncludesProjectAction() {
	}

	@Override
	public void run(IAction action) {
		List<ICProject> projects= new ArrayList<ICProject>();
		IStructuredSelection cElements= SelectionConverter.convertSelectionToCElements(fSelection);
		for (Iterator<?> i = cElements.iterator(); i.hasNext();) {
			Object elem = i.next();
			if (elem instanceof ICProject) {
				projects.add((ICProject) elem);
			}
		}
		
	 	if (projects.isEmpty()) {
			StatusLineHandler.showStatusLineMessage(fSite, CSearchMessages.CSearchOperation_operationUnavailable_message);
	 		return;
	 	}

	 	ISearchQuery searchJob= new CSearchUnresolvedIncludesQuery(projects.toArray(new ICProject[projects.size()]));

		StatusLineHandler.clearStatusLine(fSite);
		NewSearchUI.activateSearchResultView();
		NewSearchUI.runQueryInBackground(searchJob);
	}

	@Override
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		fSite= targetPart.getSite();
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		fSelection= selection;
	}
}
