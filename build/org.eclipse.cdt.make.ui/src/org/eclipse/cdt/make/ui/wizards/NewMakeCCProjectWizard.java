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

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.make.internal.ui.MakeUIPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;

/**
 * This wizard was used for 3.X style projects. It is left here for compatibility
 * reasons only. The wizard is superseded by MBS C++ Project Wizard,
 * class {@link org.eclipse.cdt.ui.wizards.CCProjectWizard}.
 *
 * @deprecated as of CDT 4.0.
 *
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
@Deprecated
public class NewMakeCCProjectWizard extends NewMakeProjectWizard {

	private static final String WZ_TITLE = "MakeCCWizard.title"; //$NON-NLS-1$
	private static final String WZ_DESC = "MakeCCWizard.description"; //$NON-NLS-1$

	private static final String WZ_SETTINGS_TITLE = "MakeCCWizard.title"; //$NON-NLS-1$
	private static final String WZ_SETTINGS_DESC = "MakeCCWizard.description"; //$NON-NLS-1$

	public NewMakeCCProjectWizard() {
		super(MakeUIPlugin.getResourceString(WZ_TITLE), MakeUIPlugin.getResourceString(WZ_DESC));
	}

	@Override
	public void addPages() {
		super.addPages();
		addPage(fOptionPage = new MakeProjectWizardOptionPage(MakeUIPlugin.getResourceString(WZ_SETTINGS_TITLE),
				MakeUIPlugin.getResourceString(WZ_SETTINGS_DESC)));
	}

	@Override
	protected void doRun(IProgressMonitor monitor) throws CoreException {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		monitor.beginTask(MakeUIPlugin.getResourceString("MakeCCWizard.task_name"), 10); //$NON-NLS-1$
		super.doRun(new SubProgressMonitor(monitor, 9));
		// Add C++ Nature.
		if (newProject != null) {
			// Add C++ Nature to the newly created project.
			CCorePlugin.getDefault().convertProjectFromCtoCC(newProject, new SubProgressMonitor(monitor, 1));
		}
		monitor.done();
	}
}
