/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.make.ui.actions;

import org.eclipse.cdt.make.ui.views.MakeTarget;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.actions.ActionDelegate;

public class MakeAction extends ActionDelegate implements IObjectActionDelegate {
	ISelection fSelection;
	IWorkbenchPart part;
	
	/**
	 * @see IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)
	 */
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		part = targetPart;
	}

	/**
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
		if (fSelection instanceof IStructuredSelection
			&& ((IStructuredSelection) fSelection).getFirstElement() instanceof IProject) {
			IProject project = (IProject) ((IStructuredSelection) fSelection).getFirstElement();
			MakeBuildAction build = null;
			if (action.getId().equals("org.eclipse.cdt.make.ui.makeAction.all")) {
				build = new MakeBuildAction(new org.eclipse.cdt.make.ui.views.MakeTarget[] { new MakeTarget(project, "all")}, part.getSite().getShell(), "all");
			} else if (action.getId().equals("org.eclipse.cdt.make.ui.makeAction.clean")) {
				build = new MakeBuildAction(new MakeTarget[] { new MakeTarget(project, "all")}, part.getSite().getShell(), "all");
			} else if (action.getId().equals("org.eclipse.cdt.make.ui.makeAction.rebuild")) {
				build = new MakeBuildAction(new MakeTarget[] { new MakeTarget(project, "all")}, part.getSite().getShell(), "all");
			}
			if ( build != null ) {
				build.run();
			}
 		}
	}
	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		fSelection = selection;
	}

}
