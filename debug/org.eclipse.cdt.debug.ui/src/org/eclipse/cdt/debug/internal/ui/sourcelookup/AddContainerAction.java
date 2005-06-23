/*******************************************************************************
 * Copyright (c) 2004, 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.sourcelookup; 
 
import org.eclipse.debug.core.sourcelookup.ISourceLookupDirector;
import org.eclipse.jface.viewers.IStructuredSelection;

/**
 * The action to add a new source container.
 * Used by the CommonSourceNotFoundEditor, the launch configuration source tab,
 * and the EditSourceLookupPathDialog.
 */
public class AddContainerAction extends SourceContainerAction {
	
	private ISourceLookupDirector fDirector;
	
	public AddContainerAction() {
		super(SourceLookupUIMessages.getString( "AddContainerAction.0" )); //$NON-NLS-1$
	}
	
	/**
	 * Prompts for a project to add.
	 * 
	 * @see org.eclipse.jface.action.IAction#run()
	 */	
	public void run() {
		AddSourceContainerDialog dialog = new AddSourceContainerDialog(getShell(), getViewer(), fDirector);
		dialog.open();			
	}
	
	public void setSourceLookupDirector(ISourceLookupDirector director) {
		fDirector = director;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.actions.BaseSelectionListenerAction#updateSelection(org.eclipse.jface.viewers.IStructuredSelection)
	 */
	protected boolean updateSelection(IStructuredSelection selection) {
		if(selection == null || selection.isEmpty()) {
			return true;
		} 
		return getViewer().getTree().getSelection()[0].getParentItem()==null;
	}
}
