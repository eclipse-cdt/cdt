/*
 * Created on 25-Jul-2003
 *
 * Copyright (c) 2002,2003 QNX Software Systems Ltd.
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.make.ui.actions;

import org.eclipse.cdt.core.model.ICContainer;
import org.eclipse.cdt.make.ui.dialogs.MakeTargetDialog;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.actions.ActionDelegate;

public class CreateTargetAction extends ActionDelegate implements IObjectActionDelegate, IWorkbenchWindowActionDelegate {

	IWorkbenchPart fPart;
	IContainer fContainer;
	
	public void run(IAction action) {
		if ( fContainer != null ) {
			MakeTargetDialog dialog;
			try {
				dialog = new MakeTargetDialog(fPart.getSite().getShell(), fContainer);
				dialog.open();
			} catch (CoreException e) {
			}
		}
	}

	public void setActivePart(IAction action, IWorkbenchPart targetPart) {		
		fPart = targetPart;
	}

	public void init(IWorkbenchWindow window) {
	}

	public void selectionChanged(IAction action, ISelection selection) {
		if ( selection instanceof IStructuredSelection ) {
			IStructuredSelection sel = (IStructuredSelection)selection;
			if ( sel.getFirstElement() instanceof ICContainer ) {
				fContainer = (IContainer) ((ICContainer)sel.getFirstElement()).getUnderlyingResource();
			} else if (sel.getFirstElement() instanceof IContainer ) {
				fContainer = (IContainer)sel.getFirstElement();
			} else {
				fContainer = null;
			}
		}
	}
}
