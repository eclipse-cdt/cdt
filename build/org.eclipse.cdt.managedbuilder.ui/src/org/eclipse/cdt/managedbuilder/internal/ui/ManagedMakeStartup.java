/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.managedbuilder.internal.ui;

import org.eclipse.cdt.managedbuilder.ui.actions.UpdateManagedProjectAction;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IStartup;

/**
 *
 */
public class ManagedMakeStartup implements IStartup {
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IStartup#earlyStartup()
	 */
	public void earlyStartup() {
		// Get any 1.2 projects from the workspace
		final IProject[] projects = UpdateManagedProjectAction.getVersion12Projects();
		if (projects.length > 0) {
			Display.getDefault().asyncExec(new Runnable() {
				// Start the process that will update the 1.2 projects
				public void run() {
					Shell shell = ManagedBuilderUIPlugin.getDefault().getShell();
					for (int index = projects.length - 1; index >= 0; --index) {
						IProject project = projects[index];
						boolean shouldUpdate = MessageDialog.openQuestion(shell,
								ManagedBuilderUIMessages.getResourceString("ManagedBuilderStartup.update.12x.title"), //$NON-NLS-1$
								ManagedBuilderUIMessages.getFormattedString("ManagedBuilderStartup.update.12x.message", new String[]{project.getName()})); //$NON-NLS-1$
						// Go for it
						if (shouldUpdate) {
							ProgressMonitorDialog pd = new ProgressMonitorDialog(shell);
							UpdateManagedProjectAction.run(false, pd, project);
						}
					}

				}
			});
		}
	}
}
