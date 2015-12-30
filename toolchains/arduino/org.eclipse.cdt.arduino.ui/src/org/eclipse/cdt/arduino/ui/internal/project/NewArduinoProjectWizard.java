/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.arduino.ui.internal.project;

import org.eclipse.cdt.arduino.core.internal.ArduinoProjectGenerator;
import org.eclipse.cdt.arduino.ui.internal.Activator;
import org.eclipse.cdt.arduino.ui.internal.Messages;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.wizards.newresource.BasicNewProjectResourceWizard;

public class NewArduinoProjectWizard extends BasicNewProjectResourceWizard {

	@Override
	public void addPages() {
		super.addPages();
	}

	@Override
	public boolean performFinish() {
		if (!super.performFinish())
			return false;

		new Job(Messages.NewArduinoProjectWizard_0) {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					final ArduinoProjectGenerator generator = new ArduinoProjectGenerator(getNewProject());
					generator.generate(monitor);
					getWorkbench().getDisplay().asyncExec(new Runnable() {
						@Override
						public void run() {
							try {
								IWorkbenchPage activePage = getWorkbench().getActiveWorkbenchWindow().getActivePage();
								IDE.openEditor(activePage, generator.getSourceFile());
							} catch (PartInitException e) {
								Activator.getDefault().getLog().log(e.getStatus());
							}
						}
					});
					return Status.OK_STATUS;
				} catch (CoreException e) {
					return e.getStatus();
				}
			}
		}.schedule();

		return true;
	}

}
