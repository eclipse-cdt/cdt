/**********************************************************************
 * Copyright (c) 2004 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.ui.actions;

import org.eclipse.cdt.internal.ui.ICHelpContextIds;
import org.eclipse.cdt.internal.ui.refactoring.RefactoringMessages;
import org.eclipse.cdt.internal.ui.refactoring.RenameRefactoringAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.help.WorkbenchHelp;


/**
 * Renames a C element or workbench resource.
 * <p>
 * Action is applicable to selections containing elements of type
 * <code>ICElement</code>.
 * 
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 * 
 * @since 2.0
 */
public class RenameAction extends SelectionDispatchAction {

	private RenameRefactoringAction fRenameCElement;
	/**
	 * Creates a new <code>RenameAction</code>. The action requires
	 * that the selection provided by the site's selection provider is of type <code>
	 * org.eclipse.jface.viewers.IStructuredSelection</code>.
	 * 
	 * @param site the site providing context information for this action
	 */
	public RenameAction(IWorkbenchSite site) {
		super(site);
		setText(RefactoringMessages.getString("RenameAction.text")); //$NON-NLS-1$
		fRenameCElement= new RenameRefactoringAction(site);
		fRenameCElement.setText(getText());
		WorkbenchHelp.setHelp(this, ICHelpContextIds.RENAME_ACTION);
	}
	/*
	 * @see ISelectionChangedListener#selectionChanged(SelectionChangedEvent)
	 */
	public void selectionChanged(SelectionChangedEvent event) {
		fRenameCElement.selectionChanged(event);
		setEnabled(computeEnabledState());		
	}

	/*
	 * @see SelectionDispatchAction#update(ISelection)
	 */
	public void update(ISelection selection) {
		fRenameCElement.update(selection);
		
		setEnabled(computeEnabledState());		
	}
	
	private boolean computeEnabledState(){
		return fRenameCElement.isEnabled();
	}
	
	public void run(IStructuredSelection selection) {
		 if (fRenameCElement.isEnabled())
			fRenameCElement.run(selection);
	}
}
