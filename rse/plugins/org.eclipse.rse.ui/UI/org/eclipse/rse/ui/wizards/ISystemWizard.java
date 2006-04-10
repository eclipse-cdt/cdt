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
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.rse.ui.dialogs.ISystemPromptDialog;
import org.eclipse.rse.ui.dialogs.SystemWizardDialog;
import org.eclipse.ui.INewWizard;


/**
 * Suggested interface for wizards launchable via remote system explorer.
 */
public interface ISystemWizard extends INewWizard, ISystemPromptDialog
{
	/**
	 * Called when wizard to be used for update vs create.
	 * This is the input object to be updated. Automatically sets input mode to update.
	 */
	//public void setUpdateInput(Object input);
	/**
	 * Retrieve update mode
	 */
	//public boolean getUpdateMode();
	/**
	 * Retrieve input object used in update mode.
	 */
	//public Object getUpdateInput();
	/**
	 * Set current selection of viewer, at time wizard launched
	 */
    //public void setSelection(IStructuredSelection selection);
	/**
	 * Get current selection of viewer, at time wizard launched, as set
	 *  by setSelection(IStructuredSelection selection)
	 */
    //public IStructuredSelection getSelection();     
    
    public void setMinimumPageSize(int width, int height);
    public int getMinimumPageWidth();
    public int getMinimumPageHeight();

    /**
     * Set the help context Id (infoPop) for this wizard. This must be fully qualified by
     *  plugin ID.
     * <p>
     * Same as {@link org.eclipse.rse.ui.actions.SystemBaseAction #setHelp(String)}
     * @see org.eclipse.rse.ui.actions.SystemBaseAction #getHelpContextId()
     */
    public void setHelp(String id);
    /**
     * Return the help Id as set in setHelp(String)
     */
    public String getHelpContextId();

	/**
	 * Set the Viewer that called this wizard. It is good practice for actions to call this
	 *  so wizard can directly access the originating viewer if needed.
	 */
	public void setViewer(Viewer v);
	/**
	 * Get the Viewer that called this wizard. This will be null unless set by the action that started this wizard.
	 */
	public Viewer getViewer();

	/**
	 * Set the wizard page title. Using this makes it possible to avoid subclassing.
	 * The page title goes below the wizard title, and can be unique per page. However,
	 * typically the wizard page title is the same for all pages... eg "Filter".
	 * <p>
	 * This is not used by default, but can be queried via getPageTitle() when constructing
	 *  pages.
	 */
	public void setWizardPageTitle(String pageTitle);
	/**
	 * Return the page title as set via setWizardPageTitle
	 */
	public String getWizardPageTitle();
	/**
	 * Called from SystemWizardDialog when it is used as the hosting dialog
	 */
	public void setSystemWizardDialog(SystemWizardDialog dlg);	
	/**
	 * Return the result of setSystemWizardDialog
	 */
	public SystemWizardDialog getSystemWizardDialog();
    /**
     * Exposes this nice new 2.0 capability to the public. 
     * Only does anything if being hosted by SystemWizardDialog.
     */
    public void updateSize();
}