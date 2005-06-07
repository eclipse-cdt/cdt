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

	/**
	 *  Unique string ID for this page.  Used by Managed Build's custom page manager to refer to this page.
	 */
	public static final String PAGE_ID = "org.eclipse.cdt.ui.wizard.basicPage"; //$NON-NLS-1$
	
	/* TODO: Implement proper data publishing from this wizard page.
	 * 
	 * The following items would in theory be used to publish the project name and location with
	 * the managed build system's custom wizard page manager.  However, this would create a dependency
	 * on MBS by the core, which is not very attractive.  It seems like it might be worthwhile in the future
	 * to move the data publishing capabilities of the page manager out into another, more generic class
	 * in the core.
	 * 
	 * For now, interested parties can obtain the IWizard page of this page from the page manager, cast it
	 * to a NewCProjectWizardPage, and obtain the data via its public methods.  Messy, but it avoids
	 * the unwanted dependency. 
	 * 
	 * 
	public static final String PROJECT_NAME = "projectName"; //$NON-NLS-1$
	public static final String PROJECT_LOCATION = "projectLocation"; //$NON-NLS-1$
	*/
	
	
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
