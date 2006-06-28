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

package org.eclipse.rse.files.ui.dialogs;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.files.ui.widgets.SystemRemoteFolderCombo;
import org.eclipse.rse.filters.ISystemFilter;
import org.eclipse.rse.model.IHost;
import org.eclipse.rse.subsystems.files.core.SystemFileResources;
import org.eclipse.rse.ui.SystemWidgetHelpers;
import org.eclipse.rse.ui.dialogs.SystemPromptDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

/**
 * Dialog for prompting the user for his home folder. This is called once when
 *  the user first expands his default "/home/username" filter string.
 * <p>NOT USED YET</p>
 */
public class SystemPromptForHomeFolderDialog 
       extends SystemPromptDialog 
       implements SelectionListener
{  
	private IHost connection;
	private ISubSystem subsystem;
    private SystemRemoteFolderCombo folderCombo = null;	

	/**
	 * Constructor 
	 */
	public SystemPromptForHomeFolderDialog(Shell shell, ISystemFilter filter)
	{
		super(shell, SystemFileResources.RESID_HOMEPROMPT_TITLE);				
		setBlockOnOpen(true); // always modal	
		subsystem = (ISubSystem)filter.getProvider();
		connection = subsystem.getHost();
		//pack();
	}	

	/**
     * Return initial control to be given focus
	 */
	protected Control getInitialFocusControl() 
	{
		return folderCombo.getFolderCombo();
	}

	/**
     * Create and populate dialog area
	 * @see SystemPromptDialog#createInner(Composite)
	 */
	protected Control createInner(Composite parent) 
	{
		// Inner composite
		int gridColumns = 1;
		Composite composite_prompts = SystemWidgetHelpers.createComposite(parent, gridColumns);	
		
		// verbiage
		boolean wantBorder = false;
		int span = 1;
		int widthHint = 200;
		SystemWidgetHelpers.createVerbiage(composite_prompts, SystemFileResources.RESID_HOMEPROMPT_TITLE, span, wantBorder, widthHint);

        // connection\folder prompt
        boolean readOnly = false;
        folderCombo = new SystemRemoteFolderCombo(composite_prompts, SWT.BORDER, null, readOnly);        
        folderCombo.setSystemConnection(connection);
        folderCombo.setText("/home/"+connection.getDefaultUserId());

        // listen for selections
        //folderCombo.addSelectionListener(this);
        
		return composite_prompts;
	}

    /**
     * From SelectionListener interface
     */
    public void widgetDefaultSelected(SelectionEvent event)
    {
    }

    /**
     * From SelectionListener interface.
     * Called when user selects new item in dropdown
     */
    public void widgetSelected(SelectionEvent event)
    {
    	Object src = event.getSource();
    	Combo comboWidget = folderCombo.getCombo();
    	if (src == comboWidget)
    	{ 
    	    //clearErrorMessage();
    	    //clearMessage();
    		//String selectedFolder = folderCombo.getText();   		
    	}
    }

	
	/**
	 * Called when user presses OK button. 
	 * Return true to close dialog.
	 * Return false to not close dialog.
	 */
	protected boolean processOK() 
	{
		boolean ok = true;
		return ok;
	}	


}