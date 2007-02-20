/********************************************************************************
 * Copyright (c) 2002, 2007 IBM Corporation and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * Uwe Stieber (Wind River) - Reworked new connection wizard extension point.
 ********************************************************************************/

package org.eclipse.rse.ui.wizards.newconnection;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.rse.core.IRSESystemType;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.ui.ISystemConnectionFormCaller;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.SystemConnectionForm;
import org.eclipse.rse.ui.validators.ISystemValidator;
import org.eclipse.rse.ui.wizards.AbstractSystemWizardPage;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;



/**
 * Default main page of the "New Connection" wizard.
 * This page asks for the primary information, including:
 * <ul>
 *   <li>Connection Name
 *   <li>Hostname/IP-address
 *   <li>UserId
 *   <li>Description
 * </ul> 
 */

public class RSEDefaultNewConnectionWizardMainPage extends AbstractSystemWizardPage implements ISystemConnectionFormCaller {
		
	private String[] restrictSystemTypesTo;
	private SystemConnectionForm form;
	private String parentHelpId;

	/**
	 * Constructor. Use this when you want to supply your own title and
	 *              description strings.
	 */
	public RSEDefaultNewConnectionWizardMainPage(IWizard wizard, String title, String description) {
		super(wizard, "NewConnection", title, description); //$NON-NLS-1$
		
		parentHelpId = RSEUIPlugin.HELPPREFIX + "wncc0000"; //$NON-NLS-1$
		setHelp(parentHelpId);
	}

	/**
	 * Call this to restrict the system type that the user is allowed to choose
	 * 
	 * @param systemType The system type to restrict the page to. Must be not <code>null</code>.
	 */
	public void restrictSystemType(String systemType) {
		assert systemType != null;
		restrictSystemTypesTo = new String[] { systemType };
		getForm().restrictSystemTypes(restrictSystemTypesTo);
	}

	/**
	 * Overrride this if you want to supply your own form. This may be called
	 *  multiple times so please only instantatiate if the form instance variable
	 *  is null, and then return the form instance variable.
	 * @see org.eclipse.rse.ui.SystemConnectionForm
	 */
	public SystemConnectionForm getForm() {
		if (form == null) form = new SystemConnectionForm(this, this);
		return form;
	}

	/**
	 * Call this to specify a validator for the connection name. It will be called per keystroke.
	 */
	public void setConnectionNameValidators(ISystemValidator[] v) {
		getForm().setConnectionNameValidators(v);
	}

	/**
	 * Call this to specify a validator for the hostname. It will be called per keystroke.
	 */
	public void setHostNameValidator(ISystemValidator v) {
		getForm().setHostNameValidator(v);
	}

	/**
	 * Call this to specify a validator for the userId. It will be called per keystroke.
	 */
	public void setUserIdValidator(ISystemValidator v) {
		getForm().setUserIdValidator(v);
	}

	/**
	 * This method allows setting of the initial user Id. Sometimes subsystems
	 *  like to have their own default userId preference page option. If so, query
	 *  it and set it here by calling this.
	 */
	public void setUserId(String userId) {
		getForm().setUserId(userId);
	}

	/**
	 * Set the profile names to show in the combo
	 */
	public void setProfileNames(String[] names) {
		getForm().setProfileNames(names);
	}

	/**
	 * Set the profile name to preselect
	 */
	public void setProfileNamePreSelection(String name) {
		getForm().setProfileNamePreSelection(name);
	}

	/**
	 * Set the currently selected connection so as to better initialize input fields
	 */
	public void setCurrentlySelectedConnection(IHost connection) {
		getForm().setCurrentlySelectedConnection(connection);
	}

	/**
	 * Preset the connection name
	 */
	public void setConnectionName(String name) {
		getForm().setConnectionName(name);
	}

	/**
	 * Preset the host name
	 */
	public void setHostName(String name) {
		getForm().setHostName(name);
	}

	/**
	 * CreateContents is the one method that must be overridden from the parent class.
	 * In this method, we populate an SWT container with widgets and return the container
	 *  to the caller (JFace). This is used as the contents of this page.
	 */
	public Control createContents(Composite parent) {
		return getForm().createContents(parent, SystemConnectionForm.CREATE_MODE, parentHelpId);
	}

	/**
	 * Return the Control to be given initial focus.
	 * Override from parent. Return control to be given initial focus.
	 */
	protected Control getInitialFocusControl() {
		return getForm().getInitialFocusControl();
	}

	/**
	 * Completes processing of the wizard. If this 
	 * method returns true, the wizard will close; 
	 * otherwise, it will stay active.
	 * This method is an override from the parent Wizard class. 
	 *
	 * @return whether the wizard finished successfully
	 */
	public boolean performFinish() {
		return getForm().verify(true);
	}

	// --------------------------------- //
	// METHODS FOR EXTRACTING USER DATA ... 
	// --------------------------------- //

	/**
	 * Return user-entered Connection Name.
	 * Call this after finish ends successfully.
	 */
	public String getConnectionName() {
		return getForm().getConnectionName();
	}

	/**
	 * Return user-entered Host Name.
	 * Call this after finish ends successfully.
	 */
	public String getHostName() {
		return getForm().getHostName();
	}

	/**
	 * Return user-entered Default User Id.
	 * Call this after finish ends successfully.
	 */
	public String getDefaultUserId() {
		return getForm().getDefaultUserId();
	}

	/**
	 * Return location where default user id is to be set.
	 * @see org.eclipse.rse.core.IRSEUserIdConstants
	 */
	public int getDefaultUserIdLocation() {
		return getForm().getUserIdLocation();
	}

	/**
	 * Return user-entered Description.
	 * Call this after finish ends successfully.
	 */
	public String getConnectionDescription() {
		return getForm().getConnectionDescription();
	}

	/**
	 * Return name of profile to contain new connection.
	 * Call this after finish ends successfully.
	 */
	public String getProfileName() {
		return getForm().getProfileName();
	}


	/**
	 * Return true if the page is complete, so to enable Finish.
	 * Called by wizard framework.
	 */
	public boolean isPageComplete() {
		//System.out.println("Inside isPageComplete. " + form.isPageComplete());
		if (getForm() != null)
			return getForm().isPageComplete() && getForm().isConnectionUnique();
		else
			return false;
	}

	/**
	 * Intercept of WizardPage so we know when Next is pressed
	 */
	public IWizardPage getNextPage() {
		//if (wizard == null)
		//return null;
		//return wizard.getNextPage(this);

		// verify contents of page before going to main page
		// this is done because the main page may have input that is not valid, but can
		// only be verified when next is pressed since it requires a long running operation
		boolean verify = getForm().verify(true);

		if (!verify) {
			return null;
		}

		RSEDefaultNewConnectionWizard newConnWizard = getWizard() instanceof RSEDefaultNewConnectionWizard ? (RSEDefaultNewConnectionWizard)getWizard() : null;
		if (newConnWizard != null) {
			return (IWizardPage)newConnWizard.getFirstAdditionalPage();
		} else
			return super.getNextPage();
	}

	/**
	 * Intercept of WizardPge so we know when the wizard framework is deciding whether
	 *   to enable next or not.
	 */
	public boolean canFlipToNextPage() {
		//return isPageComplete() && getNextPage() != null;

		RSEDefaultNewConnectionWizard newConnWizard = getWizard() instanceof RSEDefaultNewConnectionWizard ? (RSEDefaultNewConnectionWizard)getWizard() : null;
		if (newConnWizard != null) {
			return (isPageComplete() && newConnWizard.hasAdditionalPages() && getForm().isConnectionUnique());
		} else
			return super.canFlipToNextPage();
	}

	// ----------------------------------------
	// CALLBACKS FROM SYSTEM CONNECTION FORM...
	// ----------------------------------------
	/**
	 * Event: the user has selected a system type.
	 */
	public void systemTypeSelected(String systemType, boolean duringInitialization) {
		RSEDefaultNewConnectionWizard newConnWizard = getWizard() instanceof RSEDefaultNewConnectionWizard ? (RSEDefaultNewConnectionWizard)getWizard() : null;
		if (newConnWizard != null) {
			newConnWizard.systemTypeSelected(systemType, duringInitialization);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.ui.wizards.ISystemNewConnectionWizardMainPage#getSystemType()
	 */
	public IRSESystemType getSystemType() {
		if (getWizard() instanceof RSEDefaultNewConnectionWizard) {
			RSEDefaultNewConnectionWizard wizard = (RSEDefaultNewConnectionWizard)getWizard();
			return wizard.getSystemType();
		}
			
		return null;
	}

}