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

import org.eclipse.cdt.make.internal.ui.MakeUIPlugin;
import org.eclipse.cdt.make.ui.views.MakeTarget;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.viewers.IStructuredSelection;

public abstract class MakeBuildAction extends AbstractMakeBuilderAction {
	protected final String makeActionID = "org.eclipse.cdt.make.ui.makeBuildAction."; //$NON-NLS-1$
	/**
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
		if (fSelection instanceof IStructuredSelection
			&& ((IStructuredSelection) fSelection).getFirstElement() instanceof IProject) {
			IProject project = (IProject) ((IStructuredSelection) fSelection).getFirstElement();
			MakeTarget target = null;
			String id = action.getId();
			if ( id.startsWith(makeActionID) ) {
				String targets = id.substring(makeActionID.length());
				if ( targets.length() > 0) {
					
				}
			};
			if ( target != null ) {
				ProgressMonitorDialog pd = new ProgressMonitorDialog(MakeUIPlugin.getActiveWorkbenchShell());
				MakeBuild.run(true, pd, new MakeTarget[] {target});
			} else {
				MakeUIPlugin.errorDialog(getShell(), "Make Build Contribution Error", "build target not defined", (IStatus)null);
			}
 		}
	}
}
