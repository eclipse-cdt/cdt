/********************************************************************************
 * Copyright (c) 2002, 2006 IBM Corporation. All rights reserved.
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
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.rse.core.subsystems.ISubSystemConfiguration;


/**
 * Interface that all subsystem factory supplied pages contributed to the New Connection wizard 
 *  must implement.
 * @see org.eclipse.rse.ui.wizards.AbstractSystemNewConnectionWizardPage
 * @see org.eclipse.rse.core.subsystems.ISubSystemConfiguration#getNewConnectionWizardPages(IWizard)
 */
public interface ISystemNewConnectionWizardPage extends IWizardPage
{

	/**
	 * This is called when the users presses Finish. All that should be done here is validation
	 *  of the input, returning true if all is ok and the finish can proceed.
	 */
	public boolean performFinish();

	/**
	 * This is called frequently by the framework to decide whether to enable the Finish and 
	 *   Next buttons. 
	 * <p>
	 * Return true if the page is complete and has no errors
	 */
	public boolean isPageComplete();
	
	/**
	 * Return the subsystem factory that supplied this page
	 */
	public ISubSystemConfiguration getSubSystemConfiguration();	
}