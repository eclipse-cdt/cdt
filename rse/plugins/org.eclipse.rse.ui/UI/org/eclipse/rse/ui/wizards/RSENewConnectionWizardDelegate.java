/********************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation. All rights reserved.
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
 * {Name} (company) - description of contribution.
 ********************************************************************************/

package org.eclipse.rse.ui.wizards;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.rse.core.IRSESystemType;

/**
 * 
 */
public abstract class RSENewConnectionWizardDelegate implements IRSENewConnectionWizardDelegate {
	
	private RSENewConnectionWizard wizard;
	private IRSESystemType systemType;
	private boolean isInitialized;

	/**
	 * @see org.eclipse.rse.ui.wizards.IRSENewConnectionWizardDelegate#init(org.eclipse.rse.ui.wizards.RSENewConnectionWizard, org.eclipse.rse.core.IRSESystemType)
	 */
	public void init(RSENewConnectionWizard wizard, IRSESystemType systemType) {
		setWizard(wizard);
		setSystemType(systemType);
		setInitialized(true);
	}
	
	protected void setWizard(RSENewConnectionWizard wizard) {
		this.wizard = wizard;
	}
	
	protected void setSystemType(IRSESystemType systemType) {
		this.systemType = systemType;
	}
	
	protected void setInitialized(boolean isInitialized) {
		this.isInitialized = true;
	}

	/**
	 * @see org.eclipse.rse.ui.wizards.IRSENewConnectionWizardDelegate#getWizard()
	 */
	public RSENewConnectionWizard getWizard() {
		return wizard;
	}
	
	/**
	 * @see org.eclipse.rse.ui.wizards.IRSENewConnectionWizardDelegate#getSystemType()
	 */
	public IRSESystemType getSystemType() {
		return systemType;
	}

	/**
	 * @see org.eclipse.rse.ui.wizards.IRSENewConnectionWizardDelegate#isInitialized()
	 */
	public boolean isInitialized() {
		return isInitialized;
	}

	/**
	 * @see org.eclipse.rse.ui.wizards.IRSENewConnectionWizardDelegate#addPages()
	 */
	public void addPages() {
	}

	/**
	 * @see org.eclipse.rse.ui.wizards.IRSENewConnectionWizardDelegate#canFinish()
	 */
	public boolean canFinish() {
		return false;
	}

	/**
	 * @see org.eclipse.rse.ui.wizards.IRSENewConnectionWizardDelegate#getNextPage(org.eclipse.jface.wizard.IWizardPage)
	 */
	public IWizardPage getNextPage(IWizardPage page) {
		return null;
	}

	/**
	 * @see org.eclipse.rse.ui.wizards.IRSENewConnectionWizardDelegate#getPreviousPage(org.eclipse.jface.wizard.IWizardPage)
	 */
	public IWizardPage getPreviousPage(IWizardPage page) {
		return wizard.getStartingPage();
	}

	/**
	 * @see org.eclipse.rse.ui.wizards.IRSENewConnectionWizardDelegate#systemTypeChanged(org.eclipse.rse.core.IRSESystemType)
	 */
	public void systemTypeChanged(IRSESystemType systemType) {
		
	}
}