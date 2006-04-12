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
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.model.IHost;
import org.eclipse.rse.model.ISystemRegistry;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.SystemResources;
import org.eclipse.rse.ui.SystemWidgetHelpers;
import org.eclipse.rse.ui.view.SystemTestFilterStringAPIProviderImpl;
import org.eclipse.rse.ui.view.SystemViewForm;
import org.eclipse.rse.ui.widgets.SystemHostCombo;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
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
public class SystemTestFilterStringDialog
       extends SystemPromptDialog
       implements  ISelectionChangedListener, SelectionListener
{
	protected ISubSystem subsystem = null;
	protected ISystemRegistry sr = null;
	protected String subsystemFactoryId = null;
	protected String filterString = null;
    protected SystemTestFilterStringAPIProviderImpl inputProvider = null;
	// GUI widgets
    protected Label prompt, promptValue;
	protected SystemViewForm tree;
	protected SystemHostCombo connectionCombo;

	/**
	 * Constructor
	 * @param shell The shell to hang the dialog off of
	 * @param subsystem The contextual subsystem that owns this filter string
	 * @param filterString The filter string that is to be tested.
	 */
	public SystemTestFilterStringDialog(Shell shell, ISubSystem subsystem, String filterString)
	{
		this(shell, SystemResources.RESID_TESTFILTERSTRING_TITLE, subsystem, filterString);			
	}
	/**
	 * Constructor when unique title desired
	 * @param shell The shell to hang the dialog off of
	 * @param title The title to give the dialog
	 * @param subsystem The contextual subsystem that owns this filter string
	 * @param filterString The filter string that is to be tested.
	 */
	public SystemTestFilterStringDialog(Shell shell, String title, ISubSystem subsystem, String filterString)
	{
		super(shell, title);	
        setCancelButtonLabel(SystemResources.BUTTON_CLOSE);
        setShowOkButton(false);		
		setBlockOnOpen(true); // always modal	
		this.subsystem = subsystem;
		this.filterString = filterString;
		this.subsystemFactoryId = subsystem.getSubSystemConfiguration().getId();
		sr = RSEUIPlugin.getTheSystemRegistry();
		setNeedsProgressMonitor(true);
		//pack();
	}	

    // ------------------
    // PUBLIC METHODS...
    // ------------------
    // ------------------
    // PRIVATE METHODS...
    // ------------------
	/**
     * Private method.
	 * @see SystemPromptDialog#getInitialFocusControl()
	 */
	protected Control getInitialFocusControl()
	{
		//return tree.getTreeControl();
		return connectionCombo.getCombo();
	}

	/**
     * Private method.
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

        // filter string prompt
       // Composite promptComposite = composite_prompts;
        Composite promptComposite = connectionCombo;
        prompt = SystemWidgetHelpers.createLabel(promptComposite, SystemResources.RESID_TESTFILTERSTRING_PROMPT_LABEL, SystemResources.RESID_TESTFILTERSTRING_PROMPT_TOOLTIP);
        promptValue = SystemWidgetHelpers.createLabel(promptComposite, SystemResources.RESID_TESTFILTERSTRING_PROMPT_LABEL, SystemResources.RESID_TESTFILTERSTRING_PROMPT_TOOLTIP);

        promptValue.setToolTipText(filterString); // Since the dialog is not resizable, this is the way to show the whole string

        // Make sure the label width is not longer than the window width
        // Otherwise the combo box dropdown arrow above it will be pushed beyond the window and invisible
        //promptValue.setText(filterString);

        String label = filterString;

        if ( label.length() > 30)
           label = label.substring(0,30) + " ...";   // Use ... to show that not entire string is displayed
        promptValue.setText( label);

        //Point point = promptValue.computeSize(SWT.DEFAULT, SWT.DEFAULT);
        //GridData data = new GridData();	
	    //data.widthHint = point.x < 230 ? point.x : 230;
	    GridData data = new GridData();	
	    data.widthHint =  200;
	    promptValue.setLayoutData(data);	

		// TREE
		inputProvider = new SystemTestFilterStringAPIProviderImpl(subsystem, filterString);		  		
		tree = new SystemViewForm(getShell(), composite_prompts, SWT.NULL, inputProvider, false, getMessageLine(), gridColumns, 1);

	    // add selection listeners
		//tree.addSelectionChangedListener(this);				
        connectionCombo.addSelectionListener(this);

		return composite_prompts;
	}

	/**
	 * Override of parent. Must pass selected object onto the form for initializing fields.
	 * Called by SystemDialogAction's default run() method after dialog instantiated.
	 */
	public void setInputObject(Object inputObject)
	{
		super.setInputObject(inputObject);
	}

    /**
     * When re-using this dialog between runs, call this to reset its contents.
     * Assumption: original input subsystem factory Id doesn't change between runs
     */
    public void reset(ISubSystem subsystem, String filterString)
    {
		this.subsystem = subsystem;
		this.filterString = filterString;
		//this.subsystemFactoryId = subsystem.getParentSubSystemFactory().getId();
    	inputProvider.setSubSystem(subsystem);
    	inputProvider.setFilterString(filterString);
    	tree.reset(inputProvider);    		
    }

    /**
     * ISelectionChangedListener interface method
     */
    public void selectionChanged(SelectionChangedEvent event)
    {
    }	
    public void widgetDefaultSelected(SelectionEvent event)
    {
    }
    public void widgetSelected(SelectionEvent event)
    {
    	Object src = event.getSource();
    	//if (src == connectionCombo.getCombo())
    	{
    		//System.out.println("connection changed");
    		IHost newConnection = connectionCombo.getHost();
    		ISubSystem[] newSubSystems = sr.getSubSystems(subsystemFactoryId, newConnection);
    		ISubSystem newSubSystem = null;
    		if ((newSubSystems != null) && (newSubSystems.length>0))
    		{
    		  newSubSystem = newSubSystems[0];
		      subsystemFactoryId = subsystem.getSubSystemConfiguration().getId();
    		}
    		inputProvider.setSubSystem(newSubSystem);
    		tree.reset(inputProvider);    		
    	}
    }

}