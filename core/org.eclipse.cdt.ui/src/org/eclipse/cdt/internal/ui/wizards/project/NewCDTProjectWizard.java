package org.eclipse.cdt.internal.ui.wizards.project;

import org.eclipse.tools.templates.ui.NewWizard;

public class NewCDTProjectWizard extends NewWizard {

	private static final String cdtTag = "org.eclipse.cdt.ui.cdtTag"; //$NON-NLS-1$

	public NewCDTProjectWizard() {
		super(cdtTag);
		setTemplateSelectionPageTitle("Templates for New C/C++ Project");
	}

}
