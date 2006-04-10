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
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.rse.core.SystemPlugin;
import org.eclipse.rse.model.ISystemProfile;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.ui.ISystemMessages;
import org.eclipse.rse.ui.SystemResources;
import org.eclipse.rse.ui.SystemWidgetHelpers;
import org.eclipse.rse.ui.messages.ISystemMessageLine;
import org.eclipse.rse.ui.validators.ISystemValidator;
import org.eclipse.rse.ui.validators.ValidatorProfileName;
import org.eclipse.rse.ui.view.ISystemPropertyConstants;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * Dialog for copying a system profile.
 */
public class SystemCopyProfileDialog extends SystemPromptDialog 
                                implements ISystemMessages, ISystemPropertyConstants
{
    private Text newName;
    private Button makeActiveCB;
    private String newNameString, inputName;
    private boolean makeActive = false;
    private SystemMessage errorMessage;    
    private ISystemValidator nameValidator;
    private boolean initialized = false;
    private ISystemProfile profile;

	/**
	 * Constructor when profile not already known
	 * @param shell The parent window hosting this dialog
	 */
	public SystemCopyProfileDialog(Shell shell) 
	{
		this(shell, null);
	}
	/**
	 * Constructor when profile known
	 * @param shell The parent window hosting this dialog
	 * @param profile The profile to be copied
	 */
	public SystemCopyProfileDialog(Shell shell, ISystemProfile profile) 
	{
		super(shell, SystemResources.RESID_COPY_PROFILE_TITLE);				
		this.profile = profile;
		if (profile != null)
		{		
		   setInputObject(profile);
		}
		nameValidator = SystemPlugin.getTheSystemRegistry().getSystemProfileManager().getProfileNameValidator((String)null);		
		//pack();
		setHelp(SystemPlugin.HELPPREFIX+"drnp0000");
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
	 * Return widget to set focus to initially
	 */
	protected Control getInitialFocusControl() 
	{
		return newName;
	}
	
	/**
	 * Set the name validator
	 */
	public void setNameValidator(ISystemValidator nv)
	{
		nameValidator = nv;
	}

	/**
	 * Create widgets, and populate given composite with them
	 */
	protected Control createInner(Composite parent) 
	{
		// Inner composite
		int nbrColumns = 2;
		Composite composite_prompts = SystemWidgetHelpers.createComposite(parent, nbrColumns);	

        // ENTRY FIELD		
		newName = SystemWidgetHelpers.createLabeledTextField(composite_prompts,null,
				SystemResources.RESID_COPY_PROFILE_PROMPT_LABEL, SystemResources.RESID_COPY_PROFILE_PROMPT_TOOLTIP);
        newName.setTextLimit(ValidatorProfileName.MAX_PROFILENAME_LENGTH); // defect 41816
		// Make active
		makeActiveCB = SystemWidgetHelpers.createCheckBox(
			composite_prompts, nbrColumns, null, SystemResources.RESID_NEWPROFILE_MAKEACTIVE_LABEL, SystemResources.RESID_NEWPROFILE_MAKEACTIVE_TOOLTIP);

		// SET HELP CONTEXT IDS...                                                    
		//SystemWidgetHelpers.setHelp(newName, SystemPlugin.HELPPREFIX+"drnp0002", SystemPlugin.HELPPREFIX+"drnp0000");
		SystemWidgetHelpers.setHelp(newName, SystemPlugin.HELPPREFIX+"drnp0002");
		//SystemWidgetHelpers.setHelp(makeActiveCB, SystemPlugin.HELPPREFIX+"drnp0003", SystemPlugin.HELPPREFIX+"drnp0000");
		SystemWidgetHelpers.setHelp(makeActiveCB, SystemPlugin.HELPPREFIX+"drnp0003");
		                  
	    initialize();
			
		// add keystroke listeners...
		newName.addModifyListener(
			new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					validateNameInput();
				}
			}
		);			   
			
		return composite_prompts;
	}
	

	/**
	 * Override of parent. Must pass selected object onto the form for initializing fields.
	 * Called by SystemDialogAction's default run() method after dialog instantiated.
	 */
	public void setInputObject(Object inputObject)
	{
		//System.out.println("INSIDE SETINPUTOBJECT: " + inputObject + ", "+inputObject.getClass().getName());
		super.setInputObject(inputObject);
		if (inputObject instanceof SystemSimpleContentElement)
		{
		  SystemSimpleContentElement element = (SystemSimpleContentElement)inputObject;
		  inputName = element.getName();
		}
		else if (inputObject instanceof ISelection)
		{
		   SystemSimpleContentElement element = (SystemSimpleContentElement)(((IStructuredSelection)inputObject).getFirstElement());
		   inputName = element.getName();
		}
		else if (inputObject instanceof ISystemProfile)
		  inputName = profile.getName();
		initialize();
	}
	
	/**
	 * Initialize input fields from input
	 */	
	protected void initialize()
	{
		if (!initialized && (newName!=null) && (inputName!=null))
		{
		  initialized = true;
		  newName.setText(inputName);
		  newName.selectAll();
		  if (makeActiveCB != null)
		    makeActiveCB.setSelection(true);
		  setPageComplete(false);
		}
	}
	/**
	 * Called when user presses OK button. 
	 * Return true to close dialog.
	 * Return false to not close dialog.
	 */
	protected boolean processOK() 
	{
		newNameString = newName.getText().trim();		
		boolean closeDialog = verify();
		if (closeDialog)
		{
			if (makeActiveCB != null)
			  makeActive = makeActiveCB.getSelection();
			setOutputObject(newNameString);
		}
		return closeDialog;
	}	
	/**
	 * Verifies all input.
	 * @return true if there are no errors in the user input
	 */
	public boolean verify() 
	{
		clearErrorMessage();				
		errorMessage = validateNameInput();
		if (errorMessage != null)
		  newName.setFocus();
		return (errorMessage == null);
	}
	
  	/**
	 * This hook method is called whenever the text changes in the input field.
	 * The default implementation delegates the request to an <code>ISystemValidator</code> object.
	 * If the <code>ISystemValidator</code> reports an error the error message is displayed
	 * in the Dialog's message line.
	 * @see #setNameValidator(ISystemValidator)
	 */
	protected SystemMessage validateNameInput() 
	{			
	    errorMessage = nameValidator.validate(newName.getText());
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
		  String theNewName = newName.getText().trim();
		  pageComplete = (theNewName.length() > 0) && !(theNewName.equalsIgnoreCase(inputName));
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
	 * Returns the user-entered new name
	 */
	public String getNewName()
	{
		return newNameString;
	}    	
	/**
	 * Returns the make-active checkbox state
	 */
	public boolean getMakeActive()
	{
		return makeActive;
	}    	

	
	/**
	 * Returns the user-entered new name as an array for convenience to ISystemRenameTarget hosts.
	 */
	public String[] getNewNameArray()
	{
		String[] newNames = new String[1];
		newNames[0] = newNameString;
		return newNames;
	}    		
	
}