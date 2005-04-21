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
 * The action used to remove source containers in the source location dialog/tab.
 * 
 */
public class RemoveAction extends SourceContainerAction {
	public RemoveAction() {
		super(SourceLookupUIMessages.getString( "RemoveAction.0" )); //$NON-NLS-1$
	}
	/**
	 * Removes all selected entries.
	 * 
	 * @see IAction#run()
	 */
	public void run() {
		List targets = getOrderedSelection();
		List list = getEntriesAsList();
		list.removeAll(targets);
		setEntries(list);
	}
	
	/**
	 * @see SelectionListenerAction#updateSelection(IStructuredSelection)
	 */
	protected boolean updateSelection(IStructuredSelection selection) {
		//check that something is selected and it is a root tree node.
		return !selection.isEmpty() && getViewer().getTree().getSelection()[0].getParentItem()==null;
	}
	
}
