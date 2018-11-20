/*******************************************************************************
 * Copyright (c) 2003, 2012 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.sourcelookup;

import java.util.List;

import org.eclipse.debug.core.sourcelookup.ISourceContainer;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.actions.SelectionListenerAction;

/**
 * The action for sorting the order of source containers in the dialog.
 */
public class DownAction extends SourceContainerAction {

	public DownAction() {
		super(SourceLookupUIMessages.DownAction_0);
	}

	/**
	 * @see IAction#run()
	 */
	@Override
	public void run() {
		List<ISourceContainer> targets = getOrderedSelection();
		if (targets.isEmpty()) {
			return;
		}
		List<ISourceContainer> list = getEntriesAsList();
		int bottom = list.size() - 1;
		int index = 0;
		for (int i = targets.size() - 1; i >= 0; i--) {
			ISourceContainer target = targets.get(i);
			index = list.indexOf(target);
			if (index < bottom) {
				bottom = index + 1;
				ISourceContainer temp = list.get(bottom);
				list.set(bottom, target);
				list.set(index, temp);
			}
			bottom = index;
		}
		setEntries(list);
	}

	/**
	 * @see SelectionListenerAction#updateSelection(IStructuredSelection)
	 */
	@Override
	protected boolean updateSelection(IStructuredSelection selection) {
		return !selection.isEmpty() && !isIndexSelected(selection, getEntriesAsList().size() - 1)
				&& getViewer().getTree().getSelection()[0].getParentItem() == null;
	}
}
