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

package org.eclipse.rse.ui.propertypages;
import java.util.ResourceBundle;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.rse.core.SystemPlugin;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.core.subsystems.ISubSystemConfiguration;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.ui.ISystemMessages;
import org.eclipse.rse.ui.SystemResources;
import org.eclipse.rse.ui.SystemWidgetHelpers;
import org.eclipse.rse.ui.dialogs.SystemPromptDialog;
import org.eclipse.rse.ui.messages.ISystemMessageLine;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.PropertyPage;

/**
 * The form for the property page for core subsystem properties.
 */
public abstract class AbstractSystemSubSystemPropertyPageCoreForm 
       implements ISystemMessages, ISystemSubSystemPropertyPageCoreForm
{
	
	protected Label labelTypePrompt, labelVendorPrompt, labelNamePrompt, labelConnectionPrompt, labelProfilePrompt;

	protected Label labelType, labelVendor, labelName, labelConnection, labelProfile;

	protected SystemMessage errorMessage;
    protected ResourceBundle rb;	
    protected boolean initDone = false;
 	protected String xlatedNotApplicable = null;
	// Inputs from caller
	protected ISystemMessageLine msgLine;
	protected Object inputElement;
    protected Shell  shell;
    protected Object caller;
	protected boolean callerInstanceOfWizardPage, callerInstanceOfSystemPromptDialog, callerInstanceOfPropertyPage;
		
	/**
	 * Constructor 
	 */
	public AbstractSystemSubSystemPropertyPageCoreForm(ISystemMessageLine msgLine, Object caller)
	{
		super();
		this.msgLine = msgLine;
		this.caller = caller;
		callerInstanceOfWizardPage         = (caller instanceof WizardPage);
		callerInstanceOfSystemPromptDialog = (caller instanceof SystemPromptDialog);		
		callerInstanceOfPropertyPage       = (caller instanceof PropertyPage);
		SystemPlugin sp = SystemPlugin.getDefault();
	}
	/**
	 * Get the input element
	 */
	private Object getElement()
	{
		return inputElement;
	}
	/**
	 * Get the shell
	 */
	protected Shell getShell()
	{
		return shell;
	}
	

	
	/**
	 * Create the GUI contents.
	 */
	public Control createContents(Composite parent, Object inputElement, Shell shell)
	{
		this.shell = shell;
		this.inputElement = inputElement;
		String labelText = null;
		// Inner composite
		Composite composite_prompts = SystemWidgetHelpers.createComposite(parent, 2);	

		// Type display
		labelText = SystemWidgetHelpers.appendColon(SystemResources.RESID_SUBSYSTEM_TYPE_LABEL);
		labelTypePrompt = SystemWidgetHelpers.createLabel(composite_prompts, labelText);
		labelType = SystemWidgetHelpers.createLabel(composite_prompts, SystemResources.RESID_SUBSYSTEM_TYPE_VALUE);

		// Vendor display
		labelText = SystemWidgetHelpers.appendColon(SystemResources.RESID_SUBSYSTEM_VENDOR_LABEL);
		labelVendorPrompt = SystemWidgetHelpers.createLabel(composite_prompts, labelText);
		labelVendor = SystemWidgetHelpers.createLabel(composite_prompts, " ");

		// Name display
		labelText = SystemWidgetHelpers.appendColon(SystemResources.RESID_SUBSYSTEM_NAME_LABEL);
		labelNamePrompt = SystemWidgetHelpers.createLabel(composite_prompts, labelText);
		labelName = SystemWidgetHelpers.createLabel(composite_prompts, " ");

		// Connection display
		labelText = SystemWidgetHelpers.appendColon(SystemResources.RESID_SUBSYSTEM_CONNECTION_LABEL);
		labelConnectionPrompt = SystemWidgetHelpers.createLabel(composite_prompts, labelText);
		labelConnection = SystemWidgetHelpers.createLabel(composite_prompts, " ");

		// Profile display
		labelText = SystemWidgetHelpers.appendColon(SystemResources.RESID_SUBSYSTEM_PROFILE_LABEL);
		labelProfilePrompt = SystemWidgetHelpers.createLabel(composite_prompts, labelText);
		labelProfile = SystemWidgetHelpers.createLabel(composite_prompts, " ");

		createInner(composite_prompts, inputElement, shell);
	
		return composite_prompts;
	}

	/**
	 * Return control to recieve initial focus
	 */
	public Control getInitialFocusControl()
	{
		  return null;
	}
	/**
	 * Get the input subsystem object
	 */
	protected ISubSystem getSubSystem()
	{
		Object element = getElement();
		if (element instanceof ISubSystem)
		  return (ISubSystem)element;
		else  
		  return null;
	}


	/**
	 * Initialize values of input fields based on input
	 */
	protected void doInitializeFields()
	{
		initDone = true;
	    ISubSystem ss = getSubSystem();
	    ISubSystemConfiguration ssFactory = ss.getSubSystemConfiguration();

	    //getPortValidator();
	    // vendor    
	    labelVendor.setText(ssFactory.getVendor());	    
	    // name    
	    labelName.setText(ss.getName());
	    // connection
	    labelConnection.setText(ss.getHostAliasName());
	    // profile
	    labelProfile.setText(ss.getSystemProfileName());
	 
	    doInitializeInnerFields();
	}
	





	/**
	 * This method can be called by the dialog or wizard page host, to decide whether to enable
	 * or disable the next, final or ok buttons. It returns true if the minimal information is
	 * available and is correct.
	 */
	public boolean isPageComplete()
	{
		boolean pageComplete = false;
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
		else if (callerInstanceOfPropertyPage)
		{
		  ((PropertyPage)caller).setValid(complete);
		}		
	}

 

    
 

	/*
	 * Create the inner portion of the contents.  These include any additional fields for the subsystem
	 */
	protected abstract Control createInner(Composite parent, Object inputElement, Shell shell);
	
	/*
	 * Initialize the inner portion of the contents.  These include any additional fields for the subsystem
	 */
	protected abstract void doInitializeInnerFields();

}