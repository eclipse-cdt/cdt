/*******************************************************************************
 * Copyright (c) 2003, 2005 IBM Corporation and others.
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

import org.eclipse.jface.viewers.IStructuredSelection;

/**
 * The action for sorting the order of source containers in the dialog.
 * 
 */
public class DownAction	extends SourceContainerAction {
	
	public DownAction() {
		super(SourceLookupUIMessages.getString( "DownAction.0" )); //$NON-NLS-1$
	}
	/**
	 * @see IAction#run()
	 */
	public void run() {
		List targets = getOrderedSelection();
		if (targets.isEmpty()) {
			return;
		}
		List list = getEntriesAsList();
		int bottom = list.size() - 1;
		int index = 0;
		for (int i = targets.size() - 1; i >= 0; i--) {
			Object target = targets.get(i);
			index = list.indexOf(target);
			if (index < bottom) {
				bottom = index + 1;
				Object temp = list.get(bottom);
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
	protected boolean updateSelection(IStructuredSelection selection) {
		return !selection.isEmpty() && !isIndexSelected(selection, getEntriesAsList().size() - 1) && getViewer().getTree().getSelection()[0].getParentItem()==null;	
	}
	
}
