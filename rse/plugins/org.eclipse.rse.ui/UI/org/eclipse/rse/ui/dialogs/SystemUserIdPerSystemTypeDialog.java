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

package org.eclipse.rse.ui.dialogs;
import org.eclipse.rse.core.SystemPlugin;
import org.eclipse.rse.core.SystemPreferencesManager;
import org.eclipse.rse.core.SystemType;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.ui.ISystemIconConstants;
import org.eclipse.rse.ui.ISystemMessages;
import org.eclipse.rse.ui.SystemResources;
import org.eclipse.rse.ui.SystemWidgetHelpers;
import org.eclipse.rse.ui.messages.ISystemMessageLine;
import org.eclipse.rse.ui.validators.ISystemValidator;
import org.eclipse.rse.ui.validators.ValidatorUserId;
import org.eclipse.rse.ui.view.ISystemPropertyConstants;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * Dialog for renaming a system profile.
 */
public class SystemUserIdPerSystemTypeDialog extends SystemPromptDialog 
                                implements ISystemMessages, ISystemPropertyConstants,
                                           ISystemIconConstants
{
    private Text userId;
    private Label systemTypePromptLabel, systemTypeLabel;
    private String userIdString, inputUserId;
    private SystemMessage errorMessage;
    private ISystemValidator userIdValidator;
    private boolean initialized = false;
    private SystemType systemType;
	
	/**
	 * Constructor
	 */
	public SystemUserIdPerSystemTypeDialog(Shell shell, SystemType systemType) 
	{
		super(shell, SystemResources.RESID_USERID_PER_SYSTEMTYPE_TITLE);				
		this.systemType = systemType;
		if (systemType != null)
		{		
		   setInputObject(systemType);
		}
        userIdValidator = new ValidatorUserId(false); // false => allow empty? No.		   
		//pack();
		setHelp(SystemPlugin.HELPPREFIX + "ddid0000");
	}
	/**
	 * Create message line. Intercept so we can set msg line of form.
	 */
	protected ISystemMessageLine createMessageLine(Composite c)
	{
		ISystemMessageLine msgLine = super.createMessageLine(c);
		//form.setMessageLine(msgLine);
		return fMessageLine;
	}


	/**
	 * @see SystemPromptDialog#getInitialFocusControl()
	 */
	protected Control getInitialFocusControl() 
	{
		return userId;
	}
	
	/**
	 * Set the UserId validator. 
	 * By default, we use ValidatorUserId
	 */
	public void setUserIdValidator(ISystemValidator uiv)
	{
		userIdValidator = uiv;
	}

	/**
	 * @see SystemPromptDialog#createInner(Composite)
	 */
	protected Control createInner(Composite parent) 
	{
		// Inner composite
		Composite composite_prompts = SystemWidgetHelpers.createComposite(
			parent, 2);	

        // SYSTEM TYPE		
		systemTypePromptLabel = SystemWidgetHelpers.createLabel(composite_prompts,SystemResources.RESID_USERID_PER_SYSTEMTYPE_SYSTEMTYPE_LABEL);
		//systemTypePromptLabel.setToolTipText(SystemPlugin.getString(RESID_USERID_PER_SYSTEMTYPE_SYSTEMTYPE_ROOT+"tooltip"));
		
		systemTypeLabel = SystemWidgetHelpers.createLabel(composite_prompts,"");
		systemTypeLabel.setToolTipText(SystemResources.RESID_USERID_PER_SYSTEMTYPE_TOOLTIP);
		systemTypeLabel.setText(systemType.getName());


        // ENTRY FIELD		
		userId = SystemWidgetHelpers.createLabeledTextField(composite_prompts,null,
		                                                    SystemResources.RESID_USERID_PER_SYSTEMTYPE_LABEL, 
		                                                    SystemResources.RESID_USERID_PER_SYSTEMTYPE_TOOLTIP);
	    initialize();
			
		// add keystroke listeners...
		userId.addModifyListener(
			new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					validateUserIdInput();
				}
			}
		);			   
			
		return composite_prompts;
	}
	

	/**
	 * Override of parent. Must pass selected object onto the form for initializing fields.
	 * Called by SystemDialogAction's default run() method after dialog instantiated.
	 * INPUT OBJECT MUST BE OF TYPE SYSTEMTYPE.
	 */
	public void setInputObject(Object inputObject)
	{
		//System.out.println("INSIDE SETINPUTOBJECT: " + inputObject + ", "+inputObject.getClass().getName());
		super.setInputObject(inputObject);
		if (inputObject instanceof SystemType)
		{
		  SystemType type = (SystemType)inputObject;
		  inputUserId = SystemPreferencesManager.getPreferencesManager().getDefaultUserId(type.getName());
		}
		initialize();
	}
	
	/**
	 * Initialize input fields from input
	 */	
	protected void initialize()
	{
		if (!initialized && (userId!=null) && (inputUserId!=null))
		{
		  initialized = true;
		  userId.setText(inputUserId);
		  userId.selectAll();
		}
		setPageComplete(false); // well, should empty be valid! 
	}
	/**
	 * Called when user presses OK button. 
	 * Return true to close dialog.
	 * Return false to not close dialog.
	 */
	protected boolean processOK() 
	{
		userIdString = userId.getText().trim();		
		boolean closeDialog = verify();
		if (closeDialog)
		{
			setOutputObject(userIdString);
		}
		return closeDialog;
	}	
	/**
	 * Verifies all input.
	 * @return true if there are no errors in the user input
	 */
	public boolean verify() 
	{
		//clearErrorMessage();				 
		SystemMessage errMsg = validateUserIdInput();
		if (errMsg != null)
		{
		  userId.setFocus();
		}
		return (errMsg == null);
	}
	
  	/**
	 * Validate the userId as the user types it.
	 * @see #setUserIdValidator(ISystemValidator)
	 */
	protected SystemMessage validateUserIdInput() 
	{			
	    errorMessage = userIdValidator.validate(userId.getText().trim());
	    if (errorMessage != null)
		  setErrorMessage(errorMessage);
		else
		  clearErrorMessage();
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
		{
		  String theNewUserId = userId.getText().trim();
		  pageComplete = (theNewUserId.length() > 0);
		  //pageComplete = (theNewUserId.length() > 0) && !(theNewUserId.equalsIgnoreCase(inputUserId));
		  //pageComplete = true; // should empty be valid?
		}
		return pageComplete;
	}
	
	/**
	 * Inform caller of page-complete status of this form
	 */
	public void setPageComplete()
	{
		setPageComplete(isPageComplete());
	}

	/**
	 * Returns the user-entered new user Id
	 */
	public String getUserId()
	{
		return userIdString;
	}    	
	
}