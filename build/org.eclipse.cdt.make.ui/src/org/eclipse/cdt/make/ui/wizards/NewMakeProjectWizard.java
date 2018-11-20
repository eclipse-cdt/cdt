/*******************************************************************************
 * Copyright (c) 2000, 2010 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.make.ui.wizards;

import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.cdt.make.core.MakeProjectNature;
import org.eclipse.cdt.make.core.scannerconfig.ScannerConfigNature;
import org.eclipse.cdt.make.internal.ui.MakeUIPlugin;
import org.eclipse.cdt.make.ui.IMakeHelpContextIds;
import org.eclipse.cdt.ui.wizards.NewCProjectWizard;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.widgets.Composite;

/**
 * This abstract wizard was used for 3.X style projects. It is left here for compatibility
 * reasons only. The wizards are superseded by MBS C++ Project Wizards.
 *
 * @deprecated as of CDT 4.0.
 *
 * @noextend This class is not intended to be subclassed by clients.
 */
@Deprecated
public abstract class NewMakeProjectWizard extends NewCProjectWizard {

	protected MakeProjectWizardOptionPage fOptionPage;

	public NewMakeProjectWizard(String title, String desc) {
		super(title, desc);
	}

	@Override
	protected void doRunPrologue(IProgressMonitor monitor) {
	}

	@Override
	protected void doRunEpilogue(IProgressMonitor monitor) {
	}

	@Override
	protected void doRun(IProgressMonitor monitor) throws CoreException {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		monitor.beginTask(MakeUIPlugin.getResourceString("MakeCWizard.task_name"), 10); //$NON-NLS-1$

		// super.doRun() just creates the project and does not assign a builder to it.
		super.doRun(new SubProgressMonitor(monitor, 5));

		MakeProjectNature.addNature(getProjectHandle(), new SubProgressMonitor(monitor, 1));
		ScannerConfigNature.addScannerConfigNature(getProjectHandle());

		// Modify the project based on what the user has selected
		if (newProject != null) {
			fOptionPage.performApply(new SubProgressMonitor(monitor, 4));
			monitor.done();
		}
	}

	@Override
	public String getProjectID() {
		return MakeCorePlugin.MAKE_PROJECT_ID;
	}

	@Override
	public void createPageControls(Composite pageContainer) {
		super.createPageControls(pageContainer);

		IWizardPage[] pages = getPages();

		if (pages != null && pages.length == 2) {
			MakeUIPlugin.getDefault().getWorkbench().getHelpSystem().setHelp(pages[0].getControl(),
					IMakeHelpContextIds.MAKE_PROJ_WIZ_NAME_PAGE);

			MakeProjectWizardOptionPage optionPage = (MakeProjectWizardOptionPage) pages[1];
			optionPage.setupHelpContextIds();
		}
	}
}
