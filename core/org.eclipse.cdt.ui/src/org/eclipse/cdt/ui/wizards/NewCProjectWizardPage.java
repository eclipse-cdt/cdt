package org.eclipse.cdt.ui.wizards;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.IStatus;
import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;

/**
 * Standard main page for a wizard that is creates a project resource.
 * <p>
 * This page may be used by clients as-is; it may be also be subclassed to suit.
 * </p>
 * <p>
 * Example useage:
 * <pre>
 * mainPage = new CProjectWizardPage("basicCProjectPage");
 * mainPage.setTitle("Project");
 * mainPage.setDescription("Create a new project resource.");
 * </pre>
 * </p>
 */
public class NewCProjectWizardPage extends WizardNewProjectCreationPage {

	public NewCProjectWizardPage(String pageName) {
		super(pageName);
	}

	/**
	 * Returns whether this page's controls currently all contain valid 
	 * values.
	 *
	 * @return <code>true</code> if all controls are valid, and
	 *   <code>false</code> if at least one is invalid
	 */
	protected boolean validatePage() {
		if (super.validatePage() == true) {

			// Give a chance to the wizard to do its own validation
			IStatus validName = ((NewCProjectWizard) getWizard()).isValidName(getProjectName());
			if (!validName.isOK()) {
				setErrorMessage(validName.getMessage());
				return false;
			}

			// Give a chance to the wizard to do its own validation
			IStatus validLocation = ((NewCProjectWizard) getWizard()).isValidLocation(getLocationPath().toOSString());
			if (!validLocation.isOK()) {
				setErrorMessage(validLocation.getMessage());
				return false;
			}
			return true;
		}
		return false;
	}
}
