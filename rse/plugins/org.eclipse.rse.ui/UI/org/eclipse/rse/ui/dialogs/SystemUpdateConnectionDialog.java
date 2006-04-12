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
import org.eclipse.rse.model.IHost;
import org.eclipse.rse.model.ISystemRegistry;
import org.eclipse.rse.ui.ISystemConnectionFormCaller;
import org.eclipse.rse.ui.SystemConnectionForm;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.SystemResources;
import org.eclipse.rse.ui.messages.ISystemMessageLine;
import org.eclipse.rse.ui.validators.ISystemValidator;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;



/**
 * Dialog for updating a connection.
 * THIS DIALOG AND ITS ACTION ARE NO LONGER USED. THEY ARE REPLACED WITH A PROPERTIES DIALOG.
 */
public class SystemUpdateConnectionDialog extends SystemPromptDialog implements ISystemConnectionFormCaller
{
    protected SystemConnectionForm form;
    protected String parentHelpId;
    
	/**
	 * Constructor for SystemUpdateConnectionDialog
	 */
	public SystemUpdateConnectionDialog(Shell shell) 
	{
		super(shell, SystemResources.RESID_CHGCONN_TITLE);				
        parentHelpId = RSEUIPlugin.HELPPREFIX + "dcon0000";
		getForm();		
		//pack();
	}

    /**
     * Overrride this if you want to supply your own form. This may be called
     *  multiple times so please only instantatiate if the form instance variable
     *  is null, and then return the form instance variable.
     * @see org.eclipse.rse.ui.SystemConnectionForm
     */
    public SystemConnectionForm getForm()
    {
		//System.out.println("INSIDE GETFORM");    	
    	if (form == null)
    	{
    	  form = new SystemConnectionForm(getMessageLine(),this);
    	}
    	return form;
    }

	/**
	 * Create message line. Intercept so we can set msg line of form.
	 */
	protected ISystemMessageLine createMessageLine(Composite c)
	{
		ISystemMessageLine msgLine = super.createMessageLine(c);
		form.setMessageLine(msgLine);
		return fMessageLine;
	}


	/**
	 * @see SystemPromptDialog#getInitialFocusControl()
	 */
	protected Control getInitialFocusControl() 
	{
		Control control = form.getInitialFocusControl();
		return control;
	}

	/**
	 * @see SystemPromptDialog#createInner(Composite)
	 */
	protected Control createInner(Composite parent) 
	{
		Control c = form.createContents(parent, SystemConnectionForm.UPDATE_MODE, parentHelpId);
		return c;
	}

	/**
	 * Override of parent. Must pass selected object onto the form for initializing fields.
	 * Called by SystemDialogAction's default run() method after dialog instantiated.
	 */
	public void setInputObject(Object inputObject)
	{
		super.setInputObject(inputObject);
		form.initializeInputFields((IHost)inputObject);
		
    	IHost conn = (IHost)inputObject;
    	ISystemValidator connectionNameValidators[] = new ISystemValidator[1];
    	connectionNameValidators[0] = SystemConnectionForm.getConnectionNameValidator(conn);    	
	    form.setConnectionNameValidators(connectionNameValidators);
	}
	
	/**
	 * Called when user presses OK button. 
	 * Return true to close dialog.
	 * Return false to not close dialog.
	 */
	protected boolean processOK() 
	{
		boolean closeDialog = form.verify(true);
		if (closeDialog)
		{
		  IHost conn = (IHost)getInputObject();
		  ISystemRegistry sr = RSEUIPlugin.getDefault().getSystemRegistry();
		  sr.updateHost( getShell(),conn,conn.getSystemType(),form.getConnectionName(),
		                       form.getHostName(), form.getConnectionDescription(),
		                       form.getDefaultUserId(), form.getUserIdLocation() );
		}
		return closeDialog;
	}	

    // ----------------------------------------
    // CALLBACKS FROM SYSTEM CONNECTION FORM...
    // ----------------------------------------
    /**
     * Event: the user has selected a system type.
     */
    public void systemTypeSelected(String systemType, boolean duringInitialization)
    {
    }

}