/*******************************************************************************
 * Copyright (c) 2000, 2010 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.wizards;


import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;


/**
 * @deprecated as of CDT 4.0. Being kept here for API compatibility only.
 */
@Deprecated
public abstract class NewCCProjectWizard extends NewCProjectWizard {

	public NewCCProjectWizard() {
		super();
		setDialogSettings(CUIPlugin.getDefault().getDialogSettings());
	}

	public NewCCProjectWizard(String title, String description) {
		super(title, description);
		setDialogSettings(CUIPlugin.getDefault().getDialogSettings());
	}


	@Override
	protected void doRun(IProgressMonitor monitor) throws CoreException {
		super.doRun(monitor);
		// Add C++ Nature to the newly created project.
        if (newProject != null){
            CCorePlugin.getDefault().convertProjectFromCtoCC(newProject, monitor);
        }
	}
}
