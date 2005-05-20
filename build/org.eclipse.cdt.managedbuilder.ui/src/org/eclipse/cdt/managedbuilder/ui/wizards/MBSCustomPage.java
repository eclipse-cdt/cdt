/*******************************************************************************
 * Copyright (c) 2005 Texas Instruments Incorporated and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Texas Instruments - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.managedbuilder.ui.wizards;

import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardPage;

/**
 * This abstract class provides a convenient, partial implementation of the IWizardPage interface.
 * This class consults with the MBSCustomPageManager to determine its actions.
 
 * If an ISV’s custom pages do not subclass MBSCustomPage then their page implementation must be
 * carefully coded to function properly while still respecting the rules laid out by the page manager.
 */
public abstract class MBSCustomPage implements IWizardPage
{

	protected String pageID = null;
	protected IWizard wizard = null;
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.IWizardPage#isCustomPageComplete()
	 */
	public boolean canFlipToNextPage() {
		return (getNextPage() != null && isCustomPageComplete());
		
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.IWizardPage#getWizard()
	 */
	public IWizard getWizard()
	{
		return wizard;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.IWizardPage#setPreviousPage
	 */
	public void setPreviousPage(IWizardPage page)
	{
		// do nothing, we use the page manager

	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.IWizardPage#setWizard()
	 */
	public void setWizard(IWizard newWizard)
	{
		wizard = newWizard;

	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.IWizardPage#getNextPage()
	 */
	public IWizardPage getNextPage()
	{
		// consult with the page manager to determine which pages are to be displayed
		return MBSCustomPageManager.getNextPage(pageID);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.IWizardPage#getPreviousPage()
	 */
	public IWizardPage getPreviousPage()
	{
		// consult with the page manager to determine which pages are to be displayed
		return MBSCustomPageManager.getPreviousPage(pageID);
	}

	
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.IWizardPage#isPageComplete()
	 */
	public boolean isPageComplete()
	{
		/* Since the wizard must have all the pages added to it regardless of whether they are visible
		 * or not, this method consults the page manager to see if the page is visible.  If it is not,
		 * then the page is always considered complete.  If the page is visible then the child class is
		 * consulted to see if it determines the page to be complete based on its own criteria.  
		*/
		
		// if we are not visible then return true so that the wizard can enable the Finish button
		if (!MBSCustomPageManager.isPageVisible(pageID))
		{
			return true;
		}

		return isCustomPageComplete();
	}

	/**
	 * @return The unique ID by which this page is referred.
	 */
	public String getPageID()
	{
		return pageID;
	}

	/**
	 * @return true if the page is complete, false otherwise.  This method is called to determine
	 * the status of the wizard's Finish button.
	 * 
	 * @see org.eclipse.jface.wizard.IWizardPage#isPageComplete()
	 */
	protected abstract boolean isCustomPageComplete();
}
