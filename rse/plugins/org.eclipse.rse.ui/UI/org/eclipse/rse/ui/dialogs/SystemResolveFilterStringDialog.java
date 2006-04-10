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
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.ui.SystemResources;
import org.eclipse.rse.ui.SystemWidgetHelpers;
import org.eclipse.rse.ui.view.SystemResolveFilterStringAPIProviderImpl;
import org.eclipse.rse.ui.view.SystemViewForm;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;



/**
 * Dialog for testing a filter string. Typically called from a create/update filter string dialog.
 * <p>
 * Caller must supply the subsystem which owns this existing or potential filter string.
 * <p>
 * This dialog contains a dropdown for selecting connections to use in the test. Only connections which
 * contain subsystems with the same parent factory as the given subsystem factory are shown.
 *
 */
public class SystemResolveFilterStringDialog extends SystemTestFilterStringDialog
{

	/**
	 * Constructor
	 * @param shell The shell to hang the dialog off of
	 * @param subsystem The contextual subsystem that owns this filter string
	 * @param filterString The filter string that is to be tested.
	 */
	public SystemResolveFilterStringDialog(Shell shell, ISubSystem subsystem, String filterString)
	{
	  super(shell, subsystem, filterString);	
      setShowOkButton(true);	  		
	}
	
	/**
	 * Constructor when unique title desired
	 * @param shell The shell to hang the dialog off of
	 * @param title The title to give the dialog
	 * @param subsystem The contextual subsystem that owns this filter string
	 * @param filterString The filter string that is to be tested.
	 */
	public SystemResolveFilterStringDialog(Shell shell, String title, ISubSystem subsystem, String filterString)
	{
		super(shell, title, subsystem, filterString);			
        setShowOkButton(true);
   	}	


	/**
	 * @see SystemPromptDialog#createInner(Composite)
	 */
	protected Control createInner(Composite parent)
	{
		// Inner composite
		int gridColumns = 2;
		Composite composite_prompts = SystemWidgetHelpers.createComposite(parent, gridColumns);	

        // connection selection combo
        connectionCombo = SystemWidgetHelpers.createConnectionCombo(composite_prompts, null, null, subsystem.getSubSystemConfiguration(),
                                                                    null, null, subsystem.getHost(), gridColumns, false);

        // Composite promptComposite = composite_prompts;
        Composite promptComposite = connectionCombo;
        prompt = SystemWidgetHelpers.createLabel(promptComposite, SystemResources.RESID_TESTFILTERSTRING_PROMPT_LABEL, SystemResources.RESID_TESTFILTERSTRING_PROMPT_TOOLTIP);
        promptValue = SystemWidgetHelpers.createLabel(promptComposite, SystemResources.RESID_TESTFILTERSTRING_PROMPT_LABEL, SystemResources.RESID_TESTFILTERSTRING_PROMPT_TOOLTIP);

        promptValue.setToolTipText(filterString); // Since the dialog is not resizable, this is the way to show the whole string

        String label = filterString;

        if ( label.length() > 30)
           label = label.substring(0,30) + " ...";   // Use ... to show that not entire string is displayed
        promptValue.setText(label);

	    GridData data = new GridData();	
	    data.widthHint =  200;
	    promptValue.setLayoutData(data);	

		// Tree viewer
		inputProvider = new SystemResolveFilterStringAPIProviderImpl(subsystem, filterString);		  		
		tree = new SystemViewForm(getShell(), composite_prompts, SWT.NULL, inputProvider, true, getMessageLine(), gridColumns, 1);

	    // add selection listeners
		//tree.addSelectionChangedListener(this);				
        connectionCombo.addSelectionListener(this);

		return composite_prompts;
	} // end createInner()


} // end class SystemResolveFilterStringDialog