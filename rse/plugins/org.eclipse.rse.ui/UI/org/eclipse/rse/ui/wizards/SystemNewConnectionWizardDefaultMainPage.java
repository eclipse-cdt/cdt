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
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.rse.model.IHost;
import org.eclipse.rse.ui.ISystemConnectionFormCaller;
import org.eclipse.rse.ui.ISystemMessages;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.SystemConnectionForm;
import org.eclipse.rse.ui.messages.ISystemMessageLine;
import org.eclipse.rse.ui.validators.ISystemValidator;
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

public class SystemNewConnectionWizardDefaultMainPage 
	   //extends WizardPage
	   extends AbstractSystemWizardPage
	   implements  ISystemMessages, ISystemNewConnectionWizardMainPage,
	              ISystemMessageLine, ISystemConnectionFormCaller
{
    protected String[] restrictSystemTypesTo;	
	protected SystemConnectionForm form;
	protected String parentHelpId;
	        
	/**
	 * Constructor. Use this when you want to supply your own title and
	 *              description strings.
	 */
	public SystemNewConnectionWizardDefaultMainPage(Wizard wizard,
													String title,
													String description)
	{
		super(wizard, "NewConnection", title, description);
        parentHelpId = RSEUIPlugin.HELPPREFIX + "wncc0000";
	    setHelp(parentHelpId);
		form = getForm();
	}

    /**
     * Call this to restrict the system type that the user is allowed to choose
     */
    public void restrictSystemType(String systemType)
    {
    	restrictSystemTypesTo = new String[1];
    	restrictSystemTypesTo[0] = systemType;
    	form.restrictSystemTypes(restrictSystemTypesTo);
    }	
    /**
     * Call this to restrict the system types that the user is allowed to choose
     */
    public void restrictSystemTypes(String[] systemTypes)
    {
    	restrictSystemTypesTo = systemTypes;
    	form.restrictSystemTypes(restrictSystemTypesTo);
    }

    /**
     * Return the main wizard typed 
     */
    private SystemNewConnectionWizard getOurWizard()
    {
    	IWizard wizard = getWizard();
    	if (wizard instanceof SystemNewConnectionWizard)
    	  return (SystemNewConnectionWizard)wizard;
    	else
    	  return null;
    }


    /**
     * Overrride this if you want to supply your own form. This may be called
     *  multiple times so please only instantatiate if the form instance variable
     *  is null, and then return the form instance variable.
     * @see org.eclipse.rse.ui.SystemConnectionForm
     */
    protected SystemConnectionForm getForm()
    {
    	if (form == null)
    	  form = new SystemConnectionForm(this,this);
    	return form;
    }
	/**
	 * Call this to specify a validator for the connection name. It will be called per keystroke.
	 */
	public void setConnectionNameValidators(ISystemValidator[] v)
	{
		form.setConnectionNameValidators(v);
	}
	/**
	 * Call this to specify a validator for the hostname. It will be called per keystroke.
	 */
	public void setHostNameValidator(ISystemValidator v)
	{
		form.setHostNameValidator(v);
	}	
	/**
	 * Call this to specify a validator for the userId. It will be called per keystroke.
	 */
	public void setUserIdValidator(ISystemValidator v)
	{
		form.setUserIdValidator(v);
	}
  
    /**
     * This method allows setting of the initial user Id. Sometimes subsystems
     *  like to have their own default userId preference page option. If so, query
     *  it and set it here by calling this.
     */
    public void setUserId(String userId)
    {
    	form.setUserId(userId);
    }	
      
    /**
     * Set the profile names to show in the combo
     */
    public void setProfileNames(String[] names)
    {
    	form.setProfileNames(names);
    }
    /**
     * Set the profile name to preselect
     */
    public void setProfileNamePreSelection(String name)
    {
    	form.setProfileNamePreSelection(name);
    }

    /**
     * Set the currently selected connection so as to better initialize input fields
     */
    public void setCurrentlySelectedConnection(IHost connection)
    {
    	form.setCurrentlySelectedConnection(connection);
    }

	/**
	 * Preset the connection name
	 */
	public void setConnectionName(String name)
	{
		form.setConnectionName(name);
	}
	/**
	 * Preset the host name
	 */
	public void setHostName(String name)
	{
		form.setHostName(name);
	}

     
	/**
	 * CreateContents is the one method that must be overridden from the parent class.
	 * In this method, we populate an SWT container with widgets and return the container
	 *  to the caller (JFace). This is used as the contents of this page.
	 */
	public Control createContents(Composite parent)
	{
		return form.createContents(parent, SystemConnectionForm.CREATE_MODE, parentHelpId);
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
		return form.verify(true);
	}
    
	// --------------------------------- //
	// METHODS FOR EXTRACTING USER DATA ... 
	// --------------------------------- //
	/**
	 * Return user-entered System Type.
	 * Call this after finish ends successfully.
	 */
	public String getSystemType()
	{
		return form.getSystemType();
	}    
	/**
	 * Return user-entered Connection Name.
	 * Call this after finish ends successfully.
	 */
	public String getConnectionName()
	{
		return form.getConnectionName();
	}    
	/**
	 * Return user-entered Host Name.
	 * Call this after finish ends successfully.
	 */
	public String getHostName()
	{
		return form.getHostName();
	}
	/**
	 * Return user-entered Default User Id.
	 * Call this after finish ends successfully.
	 */	    
	public String getDefaultUserId()
	{
		return form.getDefaultUserId();
	}
	/**
	 * Return location where default user id is to be set.
	 * @see org.eclipse.rse.core.ISystemUserIdConstants
	 */
	public int getDefaultUserIdLocation()
	{
		return form.getUserIdLocation();
	}
	/**
	 * Return user-entered Description.
	 * Call this after finish ends successfully.
	 */	    
	public String getConnectionDescription()
	{
		return form.getConnectionDescription();
	}    
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
		//System.out.println("Inside isPageComplete. " + form.isPageComplete());
		if (form!=null)
		  return form.isPageComplete() && form.isConnectionUnique();
		else
		  return false;
	}
	
	/**
	 * Intercept of WizardPage so we know when Next is pressed
	 */
    public IWizardPage getNextPage() 
    {
	    //if (wizard == null)
		  //return null;
	    //return wizard.getNextPage(this);	

    	SystemNewConnectionWizard newConnWizard = getOurWizard();
    	if (newConnWizard != null)
    	{
    	  return newConnWizard.getFirstAdditionalPage();
    	}
        else
	      return super.getNextPage();
    }
    /**
     * Intercept of WizardPge so we know when the wizard framework is deciding whether
     *   to enable next or not.
     */
    public boolean canFlipToNextPage() 
    {
	    //return isPageComplete() && getNextPage() != null;

    	SystemNewConnectionWizard newConnWizard = getOurWizard();
    	if (newConnWizard != null)
    	{
    	  return (isPageComplete() && newConnWizard.hasAdditionalPages() && form.isConnectionUnique());
    	}
        else
	      return super.canFlipToNextPage();
    }

    // ----------------------------------------
    // CALLBACKS FROM SYSTEM CONNECTION FORM...
    // ----------------------------------------
    /**
     * Event: the user has selected a system type.
     */
    public void systemTypeSelected(String systemType, boolean duringInitialization)
    {
    	SystemNewConnectionWizard newConnWizard = getOurWizard();
    	if (newConnWizard != null)
    	{
    		newConnWizard.systemTypeSelected(systemType, duringInitialization);
    	}
    }

}