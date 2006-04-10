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
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.ui.ISystemMessages;
import org.eclipse.rse.ui.SystemResources;
import org.eclipse.rse.ui.SystemWidgetHelpers;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;


/**
 * Dialog for prompting the user to add / change an environment variable.
 */
public class EnvironmentVariablesPromptDialog extends SystemPromptDialog implements ModifyListener {


	private Text nameTextField, valueTextField;
	private String name, value, systemType, invalidNameChars;
	private boolean change;		// Is this dialog for add or change
	private String[] existingNames;	
	
	/**
	 * Constructor for EnvironmentVariablesPromptDialog.
	 * @param shell
	 * @param title
	 */
	public EnvironmentVariablesPromptDialog(Shell shell, String title, String systemType, String invalidNameChars, String[] existingNames, boolean change) {
		super(shell, title);
		this.change = change;
		this.systemType = systemType;
		this.invalidNameChars = invalidNameChars;
		this.existingNames = existingNames;
	}

	/**
	 * Constructor for EnvironmentVariablesPromptDialog.
	 * @param shell
	 * @param title
	 * @param inputObject
	 */
	public EnvironmentVariablesPromptDialog(Shell shell, String title, Object inputObject, String invalidNameChars, String[] existingNames, boolean change) {
		super(shell, title, inputObject);
		this.change = change;
		this.invalidNameChars = invalidNameChars;		
		this.existingNames = existingNames;
	}

	/**
	 * @see org.eclipse.rse.ui.dialogs.SystemPromptDialog#createInner(Composite)
	 */
	protected Control createInner(Composite parent) {
		
		Composite page = SystemWidgetHelpers.createComposite(parent, 2);
		
		// Prompt for name 
		SystemWidgetHelpers.createLabel(page, SystemResources.RESID_SUBSYSTEM_ENVVAR_NAME_LABEL);
		nameTextField = SystemWidgetHelpers.createTextField(page, null);
		nameTextField.setToolTipText(SystemResources.RESID_SUBSYSTEM_ENVVAR_NAME_TOOLTIP);
		if (name != null && !name.trim().equals(""))
		{
			nameTextField.setText(name);
			setInitialOKButtonEnabledState(true);
		}
		else
		{
			setInitialOKButtonEnabledState(false);
		}
		nameTextField.addModifyListener(this);
		
		// Prompt for value
		SystemWidgetHelpers.createLabel(page, SystemResources.RESID_SUBSYSTEM_ENVVAR_VALUE_LABEL);
		valueTextField = SystemWidgetHelpers.createTextField(page, null);		
		valueTextField.setToolTipText(SystemResources.RESID_SUBSYSTEM_ENVVAR_VALUE_TOOLTIP);
		if (value != null)
		{
			valueTextField.setText(value);
		}
		
		if (!change)		
			SystemWidgetHelpers.setCompositeHelp(parent, SystemPlugin.HELPPREFIX + "envv0001");
		else
			SystemWidgetHelpers.setCompositeHelp(parent, SystemPlugin.HELPPREFIX + "envv0002");
		
		
		// Set name and value limits for known system types
		if (systemType != null)
		{
			if (systemType.equals("iSeries"))
			{
				nameTextField.setTextLimit(128);
				valueTextField.setTextLimit(1024);
			}
			else if (systemType.equals("Windows"))
			{
				nameTextField.setTextLimit(300);
				valueTextField.setTextLimit(1024);
			}
			else if (systemType.equals("Local"))
			{
		        if (System.getProperty("os.name").toLowerCase().indexOf("win") != -1)
		        {
					nameTextField.setTextLimit(300);
					valueTextField.setTextLimit(1024);
		        }
			}			
		}
		
		return parent;
	}

	/**
	 * @see org.eclipse.rse.ui.dialogs.SystemPromptDialog#getInitialFocusControl()
	 */
	protected Control getInitialFocusControl() {
		return nameTextField;
	}

	/**
	 * Get the environment varaible name entered in the dialog.
	 */	
	public String getName() 
	{
		return name;
	}

	/**
	 * Get the environment varaible value entered in the dialog.
	 */	
	public String getValue()
	{
		return value;
	}
	
	/**
	 * Preset the name for the environment variable
	 */
	public void setName(String name)
	{
		this.name = name;
	}
	
	/**
	 * Preset the value for the environment variable
	 */
	public void setValue(String value)
	{
		this.value = value;
	}
	
	/**
	 * @see org.eclipse.rse.ui.dialogs.SystemPromptDialog#processOK()
	 */
	protected boolean processOK() {
		if (nameTextField.getText() != null && !nameTextField.getText().trim().equals("")) 
		{
			String nameStr;
			if (invalidNameChars != null && invalidNameChars.indexOf(' ') != -1)
			{
				nameStr = nameTextField.getText().trim();
			}
			else
			{
				nameStr = nameTextField.getText();
			}
			
			// dy:  Change to use a String of invalid charactes supplied by the subsystem
			//if (nameStr.indexOf('=') > 0 || nameStr.indexOf(' ') > 0)
			if (invalidNameChars != null)
			{
				for (int i = 0; i < invalidNameChars.length(); i++)
				{
					if (nameStr.indexOf(invalidNameChars.charAt(i)) != -1)
					{
						setErrorMessage(SystemPlugin.getPluginMessage(ISystemMessages.MSG_COMM_ENVVAR_INVALIDCHAR));
						nameTextField.setFocus();
						return false;
					}
				}
			}

			if (existingNames != null)
			{
				// Check if this one already exists
				for (int i = 0; i < existingNames.length; i++)
				{
					if (nameStr.equals(existingNames[i]))
					{
						if (!change || !nameStr.equals(name))
						{
							SystemMessage msg = SystemPlugin.getPluginMessage(ISystemMessages.MSG_COMM_ENVVAR_DUPLICATE);
							msg.makeSubstitution(nameStr);
							setErrorMessage(msg);
							nameTextField.setFocus();
							return false;
						}
					}
				}
			}

			name = nameStr;
			value = valueTextField.getText();
			return true;
		} 
		else 
		{
			setErrorMessage(SystemPlugin.getPluginMessage(ISystemMessages.MSG_COMM_ENVVAR_NONAME));
			nameTextField.setFocus();
			return false;
		}	
	}

	/**
	 * @see org.eclipse.swt.events.ModifyListener#modifyText(ModifyEvent)
	 */
	public void modifyText(ModifyEvent e) {
		if (nameTextField.getText().trim().equals(""))
		{
			enableOkButton(false);
		}
		else
		{
			enableOkButton(true);		
		}			

	}

}