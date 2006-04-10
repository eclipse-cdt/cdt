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

package org.eclipse.rse.ui;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.rse.core.SystemPlugin;
import org.eclipse.rse.model.ISystemProfile;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.ui.dialogs.SystemPromptDialog;
import org.eclipse.rse.ui.messages.ISystemMessageLine;
import org.eclipse.rse.ui.validators.ISystemValidator;
import org.eclipse.rse.ui.validators.ValidatorProfileName;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;


/**
 * A reusable form for prompting for profile information,
 *  in new or update mode.
 * <p>
 * May be used to populate a dialog or a wizard page.
 */

public class SystemProfileForm 
	   implements Listener,  ISystemMessages
{

	// GUI widgets
	protected Label profileLabel;
	protected Control verbage;	
	//protected Combo profileCombo;
	protected Text  profileName;
	protected ISystemMessageLine msgLine;
	// validators
	protected ISystemValidator nameValidator;	
	protected Object caller;
	protected boolean callerInstanceOfWizardPage, callerInstanceOfSystemPromptDialog;
	
	// max lengths
	protected static final int profileNameLength = ValidatorProfileName.MAX_PROFILENAME_LENGTH;
	// state
	protected ISystemProfile profile;
	private boolean showVerbage = true;
	private SystemMessage errorMessage = null;
    
	/**
	 * Constructor.
	 * @param msgLine A GUI widget capable of writing error messages to.
	 * @param caller. The wizardpage or dialog hosting this form.
	 * @param profile. The existing profile being updated, or null for New action.
	 * @param showVerbage. Specify true to show first-time-user verbage.
	 */
	public SystemProfileForm(ISystemMessageLine msgLine, Object caller, ISystemProfile profile, boolean showVerbage)
	{
		this.msgLine = msgLine;
		this.caller = caller;
		this.profile = profile;
		this.showVerbage = showVerbage;
		callerInstanceOfWizardPage = (caller instanceof WizardPage);
		callerInstanceOfSystemPromptDialog = (caller instanceof SystemPromptDialog);		
		nameValidator = SystemPlugin.getTheSystemRegistry().getSystemProfileManager().getProfileNameValidator(profile);
	}
	
	/**
	 * Often the message line is null at the time of instantiation, so we have to call this after
	 *  it is created.
	 */
	public void setMessageLine(ISystemMessageLine msgLine)
	{
		this.msgLine = msgLine;
	}

	/**
	 * Call this to specify a validator for the profile name. It will be called per keystroke.
	 * If not specified, a default is used.
	 */
	public void setNameValidators(ISystemValidator v)
	{
		nameValidator = v;
	}
	/**
	 * Call to initialize the profile name in create mode. Must be called after createContents
	 */
	public void setProfileName(String name)
	{
		if ((name != null) && (profileName != null))
		  profileName.setText(name);
	}
             
	/**
	 * CreateContents is the one method that must be overridden from the parent class.
	 * In this method, we populate an SWT container with widgets and return the container
	 *  to the caller (JFace). This is used as the contents of this page.
	 * @param parent The parent composite
	 */
	public Control createContents(Composite parent)
	{
		// Inner composite
		int nbrColumns = 2;
		Composite composite_prompts = SystemWidgetHelpers.createComposite(parent, nbrColumns);

		// VERBAGE LABEL
		if (showVerbage)
		{
		  verbage = SystemWidgetHelpers.createVerbage(
			 composite_prompts, SystemResources.RESID_PROFILE_PROFILENAME_VERBAGE, nbrColumns, false, 200);
	      SystemWidgetHelpers.createLabel(composite_prompts, "", nbrColumns); // dummy line for spacing
		}
   
        // NAME PROMPT
		String temp = SystemWidgetHelpers.appendColon(SystemResources.RESID_PROFILE_PROFILENAME_LABEL);
		profileLabel = SystemWidgetHelpers.createLabel(composite_prompts, temp);
		profileName  = SystemWidgetHelpers.createTextField(
			composite_prompts,this,
			SystemResources.RESID_PROFILE_PROFILENAME_TIP);			
	    profileName.setTextLimit(profileNameLength);
	    
	    if (profile != null)
	      profileName.setText(profile.getName());
	            
		profileName.setFocus();
		  		  
		
		// add keystroke listeners...
		profileName.addModifyListener(
			new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					validateNameInput();
				}
			}
		);
		return composite_prompts;
	}
	
	/**
	 * Return control to recieve initial focus
	 */
	public Control getInitialFocusControl()
	{
		return profileName;
	}
	
	/**
	 * Default implementation to satisfy Listener interface. Does nothing.
	 */
	public void handleEvent(Event evt) {}
	
	/**
	 * Verifies all input.
	 * @return true if there are no errors in the user input
	 */
	public boolean verify() 
	{
		SystemMessage errMsg = null;
		Control controlInError = null;
		if (msgLine != null)
		  msgLine.clearErrorMessage();
		errMsg = validateNameInput();
		if (errMsg != null)
		  controlInError = profileName;
		else
		{
		}		
		if (errMsg != null)
		  {
		  	controlInError.setFocus();
		  	showErrorMessage(errMsg);
		  }
		return (errMsg == null);
	}
    
	// --------------------------------- //
	// METHODS FOR EXTRACTING USER DATA ... 
	// --------------------------------- //
	/**
	 * Return user-entered profile Name.
	 * Call this after finish ends successfully.
	 */
	public String getProfileName()
	{
		return profileName.getText().trim();
	}    
    /**
     * Display error message or clear error message
     */
	private void showErrorMessage(SystemMessage msg)
	{
		if (msgLine != null)
		  if (msg != null)
		    msgLine.setErrorMessage(msg);
		  else
		    msgLine.clearErrorMessage();
		else
		  System.out.println("MSGLINE NULL. TRYING TO WRITE MSG " + msg);
	}
    
	// ---------------------------------------------
	// METHODS FOR VERIFYING INPUT PER KEYSTROKE ...
	// ---------------------------------------------
  	/**
	 * This hook method is called whenever the text changes in the input field.
	 * The default implementation delegates the request to an <code>ISystemValidator</code> object.
	 * If the <code>ISystemValidator</code> reports an error the error message is displayed
	 * in the Dialog's message line.
	 * @see #setNameValidators(ISystemValidator)
	 */
	protected SystemMessage validateNameInput() 
	{			
	    errorMessage= null;
	    errorMessage= nameValidator.validate(profileName.getText().trim());
		showErrorMessage(errorMessage);
		setPageComplete();		
		return errorMessage;		
	}
	
	/**
	 * This method can be called by the dialog or wizard page host, to decide whether to enable
	 * or disable the next, final or ok buttons. It returns true if the minimal information is
	 * available and is correct.
	 */
	public boolean isPageComplete()
	{
		boolean pageComplete = false;
		if (errorMessage == null)
		  pageComplete = (getProfileName().length() > 0);
		return pageComplete;
	}
	
	/**
	 * Inform caller of page-complete status of this form
	 */
	public void setPageComplete()
	{
		boolean complete = isPageComplete();
		if (callerInstanceOfWizardPage)
		{			
		  ((WizardPage)caller).setPageComplete(complete);
		}
		else if (callerInstanceOfSystemPromptDialog)
		{
		  ((SystemPromptDialog)caller).setPageComplete(complete);
		}		
	}	
}