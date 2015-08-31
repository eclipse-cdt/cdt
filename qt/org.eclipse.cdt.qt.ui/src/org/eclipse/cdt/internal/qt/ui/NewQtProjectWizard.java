/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.internal.qt.ui;

import org.eclipse.cdt.internal.qt.core.project.QtProjectGenerator;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.wizards.newresource.BasicNewProjectResourceWizard;

public class NewQtProjectWizard extends BasicNewProjectResourceWizard {

	@Override
	public boolean performFinish() {
		if (!super.performFinish()) {
			return false;
		}

		new Job("Creating Qt Project") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					QtProjectGenerator generator = new QtProjectGenerator(getNewProject());
					generator.generate(monitor);
				} catch (CoreException e) {
					return e.getStatus();
				}
				return Status.OK_STATUS;
			}
		}.schedule();

		return true;
	}

}
