/*******************************************************************************
 * Copyright (c) 2006, 2008 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.includebrowser;

import org.eclipse.cdt.internal.ui.viewsupport.WorkingSetFilterUI;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.ui.IWorkingSet;

public class IBWorkingSetFilter extends ViewerFilter {

	private WorkingSetFilterUI fWorkingSetFilter;

	public IBWorkingSetFilter(WorkingSetFilterUI wsFilter) {
		fWorkingSetFilter = wsFilter;
	}

	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		if (parentElement instanceof IBNode && element instanceof IBNode) {
			IBNode node = (IBNode) element;
			if (!fWorkingSetFilter.isPartOfWorkingSet(node.getRepresentedTranslationUnit())) {
				return false;
			}
		}
		return true;
	}

	public WorkingSetFilterUI getUI() {
		return fWorkingSetFilter;
	}

	public String getLabel() {
		IWorkingSet ws = fWorkingSetFilter.getWorkingSet();
		if (ws != null) {
			return ws.getLabel();
		}
		return IBMessages.IBViewPart_workspaceScope;
	}
}
