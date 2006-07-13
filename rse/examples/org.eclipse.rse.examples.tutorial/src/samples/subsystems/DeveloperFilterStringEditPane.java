/********************************************************************************
 * Copyright (c) 2006 IBM Corporation. All rights reserved.
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
 * Martin Oberhuber (Wind River) - Adapted original tutorial code to Open RSE.
 ********************************************************************************/

package samples.subsystems;

import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.ui.SystemWidgetHelpers;
import org.eclipse.rse.ui.filters.SystemFilterStringEditPane;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import samples.RSESamplesPlugin;

/**
 * Our specialized filter string edit pane for developer filters.
 */
public class DeveloperFilterStringEditPane extends SystemFilterStringEditPane {

	// gui widgets
	private Text textTeam, textDevr;

	/**
	 * Constructor for DeveloperFilterStringEditPane.
	 * @param shell - parent window
	 */
	public DeveloperFilterStringEditPane(Shell shell)
	{
		super(shell);
	}

	/**
	 * Override of parent method.
	 * This is where we populate the client area.
	 * @param parent - the composite that will be the parent of the returned client area composite
	 * @return Control - a client-area composite populated with widgets.
	 * 
	 * @see org.eclipse.rse.ui.SystemWidgetHelpers
	 */
	public Control createContents(Composite parent) 
	{		
		// Inner composite
		int nbrColumns = 1;
		Composite composite_prompts = SystemWidgetHelpers.createComposite(parent, nbrColumns);	
		((GridLayout)composite_prompts.getLayout()).marginWidth = 0;
		
		// CREATE TEAM-PARENT PROMPT
		textTeam = SystemWidgetHelpers.createLabeledTextField(
			composite_prompts,
			null,
			RSESamplesPlugin.getResourceString("filter.devr.teamprompt.label"), //$NON-NLS-1$
			RSESamplesPlugin.getResourceString("filter.devr.teamprompt.tooltip") //$NON-NLS-1$
		); 

		// CREATE DEVELOPER PROMPT
		textDevr = SystemWidgetHelpers.createLabeledTextField(
			composite_prompts,
			null,
			RSESamplesPlugin.getResourceString("filter.devr.devrprompt.label"), //$NON-NLS-1$
			RSESamplesPlugin.getResourceString("filter.devr.devrprompt.tooltip") //$NON-NLS-1$
		); 
		
		resetFields();
		doInitializeFields();
		  		  
		// add keystroke listeners...
		textTeam.addModifyListener(
			new ModifyListener() 
			{
				public void modifyText(ModifyEvent e) 
				{
					validateStringInput();
				}
			}
		);		
		textDevr.addModifyListener(
			new ModifyListener() 
			{
				public void modifyText(ModifyEvent e) 
				{
					validateStringInput();
				}
			}
		);		
		return composite_prompts;
	}
	/**
	 * Override of parent method.
	 * Return the control to recieve initial focus. 
	 */
	public Control getInitialFocusControl()
	{
		return textTeam;
	}	

	/**
	 * Override of parent method.
	 * Initialize the input fields based on the inputFilterString, and perhaps refProvider.
	 * This can be called before createContents, so test for null widgets first!
	 * Prior to this being called, resetFields is called to set the initial default state prior to input
	 */		
	protected void doInitializeFields()
	{
		if (textTeam == null)
		  return; // do nothing
		if (inputFilterString != null)
		{
			int idx = inputFilterString.indexOf('/');
			if (idx < 0)
		      textTeam.setText(inputFilterString);
		    else
		    {
		    	textTeam.setText(inputFilterString.substring(0,idx));
		    	textDevr.setText(inputFilterString.substring(idx+1));
		    }
		}
	}	
	/**
	 * Override of parent method.
	 * This is called in the change filter dialog when the user selects "new", or selects another string.
	 */
	protected void resetFields()
	{
	    textTeam.setText(""); //$NON-NLS-1$		
	    textDevr.setText("*"); //$NON-NLS-1$
	}
	/**
	 * Override of parent method.
	 * Called by parent to decide if information is complete enough to enable finish. 
	 */
	protected boolean areFieldsComplete()
	{
		if ((textTeam == null) || (textDevr == null))
		  return false;
		else
		  return (textTeam.getText().trim().length()>0) && (textDevr.getText().trim().length()>0);
	}
	
	/**
	 * Override of parent method.
	 * Get the filter string in its current form. 
	 * Functional opposite of doInitializeFields, which tears apart the input string in update mode,
	 *  to populate the GUIs. This method creates the filter string from the information in the GUI.
	 */
	public String getFilterString()
	{
		if ((textTeam == null) || (textDevr == null))
		  return inputFilterString; // return what we were given.
		else
		{
			String teamName = textTeam.getText().trim();
			String devrName = textDevr.getText().trim();
			return teamName + "/" + devrName; //$NON-NLS-1$
		}
	}	

	/**
	 * Override of parent method.
	 * Does complete verification of input fields. If this 
	 * method returns null, there are no errors and the dialog or wizard can close.
	 *
	 * @return error message if there is one, else null if ok
	 */
	public SystemMessage verify() 
	{
		errorMessage = null;
		Control controlInError = null;
		
		/*
		errorMessage = validateTeamInput(); // todo: implement if we want to syntax check input
		if (errorMessage != null)
		  controlInError = textTeam;
		else
		{
		   errorMessage = validateDevrInput(); // todo: implement to syntax check input
		   if (errorMessage != null)
		     controlInError = textDevr;
		}
		*/
		
		if (errorMessage != null)
		{
			if (controlInError != null)
		      controlInError.setFocus();
		}
		return errorMessage;		
	}	

}
