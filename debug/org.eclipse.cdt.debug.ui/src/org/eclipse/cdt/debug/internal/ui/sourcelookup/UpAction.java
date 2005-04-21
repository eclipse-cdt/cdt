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
 
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.viewers.IStructuredSelection;

/**
 * The action used to move source containers up in the list 
 */
public class UpAction extends SourceContainerAction {
	
	public UpAction() {
		super(SourceLookupUIMessages.getString( "UpAction.0" )); //$NON-NLS-1$
	}
	/**
	 * Moves all selected entries up one position (if possible).
	 * 
	 * @see IAction#run()
	 */
	public void run() {
		List targets = getOrderedSelection();
		if (targets.isEmpty()) {
			return;
		}
		int top = 0;
		int index = 0;
		List list = getEntriesAsList();
		Iterator entries = targets.iterator();
		while (entries.hasNext()) {
			Object target = entries.next();
			index = list.indexOf(target);
			if (index > top) {
				top = index - 1;
				Object temp = list.get(top);
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
	protected boolean updateSelection(IStructuredSelection selection) {
		//check that something is selected, it's not first in the list, and it is a root tree node.
		return !selection.isEmpty() && !isIndexSelected(selection, 0) && getViewer().getTree().getSelection()[0].getParentItem()==null;
	}
	
}
