/*******************************************************************************
 * Copyright (c) 2003, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.sourcelookup; 
 
import java.util.List;

import org.eclipse.debug.core.sourcelookup.ISourceContainer;
import org.eclipse.jface.viewers.IStructuredSelection;

/**
 * The action used to move source containers up in the list 
 */
public class UpAction extends SourceContainerAction {
	
	public UpAction() {
		super(SourceLookupUIMessages.UpAction_0);
	}

	/**
	 * Moves all selected entries up one position (if possible).
	 * 
	 * @see IAction#run()
	 */
	@Override
	public void run() {
		List<ISourceContainer> targets = getOrderedSelection();
		if (targets.isEmpty()) {
			return;
		}
		int top = 0;
		int index = 0;
		List<ISourceContainer> list = getEntriesAsList();
		for (ISourceContainer target : targets) {
			index = list.indexOf(target);
			if (index > top) {
				top = index - 1;
				ISourceContainer temp = list.get(top);
				list.set(top, target);
				list.set(index, temp);
			}
			top = index;
		} 
		setEntries(list);
	}
	
	/**
	 * @see SelectionListenerAction#updateSelection(IStructuredSelection)
	 */
	@Override
	protected boolean updateSelection(IStructuredSelection selection) {
		//check that something is selected, it's not first in the list, and it is a root tree node.
		return !selection.isEmpty() && !isIndexSelected(selection, 0) && getViewer().getTree().getSelection()[0].getParentItem()==null;
	}
}
