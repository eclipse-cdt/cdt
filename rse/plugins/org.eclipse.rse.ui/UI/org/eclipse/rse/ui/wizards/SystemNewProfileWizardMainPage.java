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
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.ui.ISystemMessages;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.SystemResources;
import org.eclipse.rse.ui.SystemWidgetHelpers;
import org.eclipse.rse.ui.messages.ISystemMessageLine;
import org.eclipse.rse.ui.validators.ISystemValidator;
import org.eclipse.rse.ui.validators.ValidatorProfileName;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;


/**
 * Default main page of the "New Profile" wizard.
 * This page asks for the following information:
 * <ul>
 *   <li>Profile name
 * </ul> 
 */

public class SystemNewProfileWizardMainPage 
	   extends AbstractSystemWizardPage
	   implements  ISystemMessages, 
	              ISystemMessageLine
{  
	
	private String profileName;
	private Text   textName;	
	private Button makeActiveCB;
	private boolean makeActive;
	private SystemMessage errorMessage;
	protected ISystemValidator nameValidator;
	private static final String HELPID_PREFIX = RSEUIPlugin.HELPPREFIX + "wnpr";
		  
	/**
	 * Constructor.
	 */
	public SystemNewProfileWizardMainPage(Wizard wizard)
	{
		super(wizard, "NewProfile", 
		      SystemResources.RESID_NEWPROFILE_PAGE1_TITLE, 
		      SystemResources.RESID_NEWPROFILE_PAGE1_DESCRIPTION);
		nameValidator = new ValidatorProfileName(RSEUIPlugin.getTheSystemRegistry().getAllSystemProfileNamesVector());
		setHelp(HELPID_PREFIX+"0000");	
	}

	/**
	 * CreateContents is the one method that must be overridden from the parent class.
	 * In this method, we populate an SWT container with widgets and return the container
	 *  to the caller (JFace). This is used as the contents of this page.
	 */
	public Control createContents(Composite parent)
	{
		// Inner composite
		int nbrColumns = 2;
		Composite composite_prompts = SystemWidgetHelpers.createComposite(parent, nbrColumns);	

		// Name
		textName = SystemWidgetHelpers.createLabeledTextField(
			composite_prompts, null, SystemResources.RESID_NEWPROFILE_NAME_LABEL, SystemResources.RESID_NEWPROFILE_NAME_TOOLTIP);
        textName.setTextLimit(ValidatorProfileName.MAX_PROFILENAME_LENGTH); // defect 41816			
		//SystemWidgetHelpers.setHelp(textName, HELPID_PREFIX+"0001", HELPID_PREFIX+"0000");
		SystemWidgetHelpers.setHelp(textName, HELPID_PREFIX+"0001");
		
		// Make active
		makeActiveCB = SystemWidgetHelpers.createCheckBox(
			composite_prompts, nbrColumns, null, SystemResources.RESID_NEWPROFILE_MAKEACTIVE_LABEL, SystemResources.RESID_NEWPROFILE_MAKEACTIVE_TOOLTIP);
	    makeActiveCB.setSelection(true);
		//SystemWidgetHelpers.setHelp(makeActiveCB, HELPID_PREFIX+"0002", HELPID_PREFIX+"0000");
		SystemWidgetHelpers.setHelp(makeActiveCB, HELPID_PREFIX+"0002");
		
		// Verbage
		addGrowableFillerLine(composite_prompts, nbrColumns);
		addSeparatorLine(composite_prompts, nbrColumns);
		SystemWidgetHelpers.createVerbage(composite_prompts, SystemResources.RESID_NEWPROFILE_VERBAGE, nbrColumns, false, 200);

		textName.addModifyListener(
			new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					validateNameInput();
				}
			}
		);			
		
		// SET CONTEXT HELP IDS...
		//SystemWidgetHelpers.setHelp(textName, HELPID_PREFIX + "0001", HELPID_PREFIX + "0000");
		//SystemWidgetHelpers.setHelp(makeActiveCB, HELPID_PREFIX + "0002", HELPID_PREFIX + "0000");
		
		return composite_prompts;		
	}
	/**
	 * Return the Control to be given initial focus.
	 * Override from parent. Return control to be given initial focus.
	 */
	protected Control getInitialFocusControl()
	{
        return textName;
	}
	
  	/**
	 * This hook method is called whenever the text changes in the input field.
	 * The default implementation delegates the request to an <code>ISystemValidator</code> object.
	 * If the <code>ISystemValidator</code> reports an error the error message is displayed
	 * in the Dialog's message line.	
	 */	
	protected SystemMessage validateNameInput() 
	{			
	    errorMessage= nameValidator.validate(textName.getText());
	    if (errorMessage != null)
		  setErrorMessage(errorMessage);		
		else
		  clearErrorMessage();
		setPageComplete(errorMessage==null);
		return errorMessage;		
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
		boolean ok = (validateNameInput()==null);
		if (ok)
		{
		  profileName = textName.getText().trim();
		  makeActive = makeActiveCB.getSelection();
		}
		return ok;
	}
    
	// --------------------------------- //
	// METHODS FOR EXTRACTING USER DATA ... 
	// --------------------------------- //
	/**
	 * Return user-entered profile name.
	 * Call this after finish ends successfully.
	 */
	public String getProfileName()
	{
		return profileName;
	}    
	/**
	 * Return user-entered decision to make the new profile active.
	 * Call this after finish ends successfully.
	 */
	public boolean getMakeActive()
	{
		return makeActive;
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
		return (textName.getText().trim().length()>0);
	}
}