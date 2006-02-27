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
import org.eclipse.debug.core.sourcelookup.ISourceContainer;
import org.eclipse.debug.core.sourcelookup.ISourceContainerType;
import org.eclipse.debug.core.sourcelookup.ISourceLookupDirector;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.sourcelookup.ISourceContainerBrowser;
import org.eclipse.jface.viewers.IStructuredSelection;

/**
 * Action used to edit source containers on a source lookup path
 */
public class EditContainerAction extends SourceContainerAction {
	
	private ISourceLookupDirector fDirector;
	private ISourceContainer[] fContainers;
	private ISourceContainerBrowser fBrowser;
	
	public EditContainerAction() {
		super(SourceLookupUIMessages.getString( "EditContainerAction.0" )); //$NON-NLS-1$
	}
	
	/**
	 * Prompts for a project to add.
	 * 
	 * @see org.eclipse.jface.action.IAction#run()
	 */	
	public void run() {
		ISourceContainer[] replacements = fBrowser.editSourceContainers(getShell(), fDirector, fContainers);
		int j = 0;
		ISourceContainer[] existing = getViewer().getEntries();
		for (int i = 0; i < existing.length && j < replacements.length; i++) {
			ISourceContainer toBeReplaced = fContainers[j];
			ISourceContainer container = existing[i];
			if (container.equals(toBeReplaced)) {
				existing[i] = replacements[j];
				j++;
			}
		}
		getViewer().setEntries(existing);
	}
	
	public void setSourceLookupDirector(ISourceLookupDirector director) {
		fDirector = director;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.actions.BaseSelectionListenerAction#updateSelection(org.eclipse.jface.viewers.IStructuredSelection)
	 */
	protected boolean updateSelection(IStructuredSelection selection) {
		if(selection == null || selection.isEmpty()) {
			return false;
		}
		if (getViewer().getTree().getSelection()[0].getParentItem()==null) {
			// can only edit top level items of same type
			fContainers = new ISourceContainer[selection.size()];
			Iterator iterator = selection.iterator();
			ISourceContainer container = (ISourceContainer) iterator.next();
			ISourceContainerType type = container.getType();
			fContainers[0] = container;
			int i = 1;
			while (iterator.hasNext()) {
				container = (ISourceContainer) iterator.next();
				fContainers[i] = container;
				i++;
				if (!container.getType().equals(type)) {
					return false;
				}
			}
			// all the same type, see if editing is supported
			fBrowser = DebugUITools.getSourceContainerBrowser(type.getId());
			if (fBrowser != null) {
				return fBrowser.canEditSourceContainers(fDirector, fContainers);
			}
		}
		return false;
	}
}
