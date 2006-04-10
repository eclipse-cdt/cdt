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
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.rse.core.SystemPlugin;
import org.eclipse.rse.ui.ISystemMessages;
import org.eclipse.rse.ui.SystemProfileForm;
import org.eclipse.rse.ui.SystemResources;
import org.eclipse.rse.ui.messages.ISystemMessageLine;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;


/**
 * First page of the New Connection wizard when creating 
 * the very first connection.
 * <p>
 * This page asks for a unique personal name for the private profile.
 */

public class SystemNewConnectionWizardRenameProfilePage 
	   extends AbstractSystemWizardPage
	   implements  ISystemMessages, 
	              ISystemMessageLine
{
	
	protected SystemProfileForm form;
    
	/**
	 * Constructor.
	 */
	public SystemNewConnectionWizardRenameProfilePage(Wizard wizard)
	{
		super(wizard, "RenamePrivateProfile", 
	          SystemResources.RESID_RENAMEDEFAULTPROFILE_PAGE1_TITLE,
	          SystemResources.RESID_RENAMEDEFAULTPROFILE_PAGE1_DESCRIPTION);
		form = getForm();
		setHelp(SystemPlugin.HELPPREFIX + "wncp0000");
	}

    /**
     * Return our hosting wizard
     */	
	protected SystemNewConnectionWizard getOurWizard()
	{
		return (SystemNewConnectionWizard)getWizard();
	}

    /**
     * Overrride this if you want to supply your own form. This may be called
     *  multiple times so please only instantatiate if the form instance variable
     *  is null, and then return the form instance variable.
     * @see org.eclipse.rse.ui.SystemProfileForm
     */
    protected SystemProfileForm getForm()
    {
    	if (form == null)
    	  form = new SystemProfileForm(this,this,null, true);
    	                               //SystemStartHere.getSystemProfileManager().getDefaultPrivateSystemProfile());
    	return form;
    }
	/**
	 * CreateContents is the one method that must be overridden from the parent class.
	 * In this method, we populate an SWT container with widgets and return the container
	 *  to the caller (JFace). This is used as the contents of this page.
	 */
	public Control createContents(Composite parent)
	{
		Control c = form.createContents(parent);
		form.getInitialFocusControl().setFocus();
		
  		String initProfileName = SystemPlugin.getLocalMachineName();
  		int dotIndex = initProfileName.indexOf('.');
  		
  		if (dotIndex != -1) {
  			initProfileName = initProfileName.substring(0, dotIndex);
  		}
  		
		form.setProfileName(initProfileName);
		
		return c;
	}	
	/**
	 * Return the Control to be given initial focus.
	 * Override from parent. Return control to be given initial focus.
	 */
	protected Control getInitialFocusControl()
	{
        return form.getInitialFocusControl();
	}
	
	/**
	 * Completes processing of the wizard. If this 
	 * method returns true, the wizard will close; 
	 * otherwise, it will stay active.
	 * This method is an override from the parent Wizard class. 
	 *
	 * @return whether the wizard finished successfully
	 */
	public boolean performFinish() 
	{
		return form.verify();
	}
    
	// --------------------------------- //
	// METHODS FOR EXTRACTING USER DATA ... 
	// --------------------------------- //
	/**
	 * Return name of profile to contain new connection.
	 * Call this after finish ends successfully.
	 */	    
	public String getProfileName()
	{
		return form.getProfileName();
	}    	
	
	// ISystemMessageLine methods
//	public void clearMessage()
//	{
//		setMessage(null);
//	}
	//public void clearErrorMessage()
	//{
		//setErrorMessage(null);		
	//}
	
	public Object getLayoutData()
	{
		return null;
	}
	
	public void setLayoutData(Object gridData)
	{
	}   
	
	/**
	 * Return true if the page is complete, so to enable Finish.
	 * Called by wizard framework.
	 */
	public boolean isPageComplete()
	{
        boolean ok = false;
		if (form!=null)
		{
		  ok = form.isPageComplete();
		  if (ok 
		      && isCurrentPage()) // defect 41831
		    getOurWizard().setNewPrivateProfileName(form.getProfileName());
		}
	    return ok;
	}
}