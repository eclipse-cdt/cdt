package org.eclipse.cdt.make.ui.wizards;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.make.internal.ui.MakeUIPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;

/**
 */
public class NewMakeCCProjectWizard extends NewMakeProjectWizard {

	private static final String WZ_TITLE = "MakeCCWizard.title"; //$NON-NLS-1$
	private static final String WZ_DESC = "MakeCCWizard.description"; //$NON-NLS-1$

	private static final String WZ_SETTINGS_TITLE = "MakeCCWizard.title"; //$NON-NLS-1$
	private static final String WZ_SETTINGS_DESC = "MakeCCWizard.description"; //$NON-NLS-1$

	public NewMakeCCProjectWizard() {
		super(MakeUIPlugin.getResourceString(WZ_TITLE), MakeUIPlugin.getResourceString(WZ_DESC));
	}

	public void addPages() {
		super.addPages();
		addPage(
			fOptionPage =
				new MakeProjectWizardOptionPage(
					MakeUIPlugin.getResourceString(WZ_SETTINGS_TITLE),
					MakeUIPlugin.getResourceString(WZ_SETTINGS_DESC)));
	}

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
