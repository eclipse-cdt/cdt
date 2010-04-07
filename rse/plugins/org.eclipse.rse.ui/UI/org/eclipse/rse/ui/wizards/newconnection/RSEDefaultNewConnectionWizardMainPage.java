/********************************************************************************
 * Copyright (c) 2002, 2010 IBM Corporation and others. All rights reserved.
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
 * Martin Oberhuber (Wind River) - [184095] Replace systemTypeName by IRSESystemType
 * Uwe Stieber (Wind River) - Fix stack overflow in canFlipToNextPage() and getNextPage()
 ********************************************************************************/

package org.eclipse.rse.ui.wizards.newconnection;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.rse.core.IRSESystemType;
import org.eclipse.rse.ui.ISystemConnectionFormCaller;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.SystemConnectionForm;
import org.eclipse.rse.ui.wizards.RSEDialogPageMessageLine;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;



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

public class RSEDefaultNewConnectionWizardMainPage extends WizardPage implements ISystemConnectionFormCaller {
	private final String parentHelpId = RSEUIPlugin.HELPPREFIX + "wncc0000"; //$NON-NLS-1$;

	private SystemConnectionForm form;
	private final RSEDialogPageMessageLine messageLine;
	
	// Remember in getNextPage() if we called form.verify(...) already once.
	// As the form is coming back to this page to invoke setPageComplete(boolean),
	// form.verify(...) triggers an update of the wizard buttons, which in turn invoke
	// canFlipToNextPage(...) which does call getNextPage(...). If the page is not used
	// with a RSEDefaultNewConnectionWizard, this will end up in a StackOverflowError.
	private boolean formVerificationGateKeeper = false;
	
	/**
	 * Constructor. Use this when you want to supply your own title and
	 *              description strings.
	 */
	public RSEDefaultNewConnectionWizardMainPage(IWizard wizard, String title, String description) {
		super(RSEDefaultNewConnectionWizardMainPage.class.getName());
		
		if (wizard != null) setWizard(wizard);
		if (title != null) setTitle(title);
		if (description != null) setDescription(description);
		
		messageLine = new RSEDialogPageMessageLine(this);
	}
	
	/**
	 * Set the system type the page is working with.
	 * 
	 * @param systemType The system type.
	 */
	public void setSystemType(IRSESystemType systemType) {
		if (systemType != null) {
			// The page _always_ restrict the system connection form
			// to only one system type.
			getSystemConnectionForm().restrictSystemType(systemType);
		}
	}
	
	/**
	 * Returns the associated system connection form instance. Override to
	 * return custom system connection forms. As the system connection form
	 * is accessed directly to set and query the managed data of this form,
	 * this method must return always the same instance once the instance has
	 * been created for each subsequent call, until the page is disposed!
	 * 
	 * @see org.eclipse.rse.ui.SystemConnectionForm
	 * @return The associated system connection form. Must be never <code>null</code>.
	 */
	public SystemConnectionForm getSystemConnectionForm() {
		if (form == null) {
			form = new SystemConnectionForm(messageLine, this);
			form.setConnectionNameValidators(SystemConnectionForm.getConnectionNameValidators());
		}
		return form;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.DialogPage#setVisible(boolean)
	 */
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if (visible && getSystemConnectionForm() != null && getSystemConnectionForm().getInitialFocusControl() != null) {
			getSystemConnectionForm().getInitialFocusControl().setFocus();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.ui.wizards.AbstractSystemWizardPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		getSystemConnectionForm().createContents(composite, SystemConnectionForm.CREATE_MODE, parentHelpId);
		
		setControl(composite);
		
		PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(), parentHelpId);
	}
	
	/**
	 * Return true if the page is complete, so to enable Finish.
	 * Called by wizard framework.
	 */
	public boolean isPageComplete() {
		//System.out.println("Inside isPageComplete. " + form.isPageComplete());
		if (getSystemConnectionForm() != null)
			return getSystemConnectionForm().isPageComplete() && getSystemConnectionForm().isConnectionUnique();
		
		return false;
	}

	/**
	 * Intercept of WizardPage so we know when Next is pressed
	 */
	public IWizardPage getNextPage() {
		// verify contents of page before going to main page
		// this is done because the main page may have input that is not valid, but can
		// only be verified when next is pressed since it requires a long running operation
		if (!formVerificationGateKeeper) {
			formVerificationGateKeeper = true;
			if (!getSystemConnectionForm().verify(true)) return null;
			formVerificationGateKeeper = false;
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
			return (isPageComplete() && newConnWizard.hasAdditionalPages());
		}
		
		return super.canFlipToNextPage();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.ui.ISystemConnectionFormCaller#systemTypeSelected(java.lang.String, boolean)
	 */
	public void systemTypeSelected(IRSESystemType systemType, boolean duringInitialization) {
		// Not applicable: The Page is driving the system connection form and not the way around!!!
	}

}