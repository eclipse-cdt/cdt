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

package org.eclipse.rse.ui.filters.dialogs;
import org.eclipse.rse.ui.dialogs.SystemWizardDialog;
import org.eclipse.rse.ui.filters.SystemFilterDialogInterface;
import org.eclipse.rse.ui.filters.SystemFilterDialogOutputs;
import org.eclipse.rse.ui.filters.actions.SystemFilterAbstractFilterAction;
import org.eclipse.swt.widgets.Shell;



/**
 * Extends WizardDialog to support ability to pass data in from the
 * common wizard action class, and get data out.
 * This is deferred to the actual wizard, which in turn defers to the wizard's first page.
 */
public class SystemFilterWizardDialog 
       extends SystemWizardDialog 
       implements SystemFilterDialogInterface
{
	
    // all ctors are from parent...
	/**
	 * Constructor
	 */
	public SystemFilterWizardDialog(Shell shell, ISystemFilterWizard wizard)
	{
		super(shell, wizard);
	}
	/**
	 * Constructor two. Use when you have an input object at instantiation time.
	 */
	public SystemFilterWizardDialog(Shell shell, ISystemFilterWizard wizard, Object inputObject)
	{
		super(shell,wizard,inputObject);
	}
  
    /**
     * Return wrapped filter wizard
     */
    public ISystemFilterWizard getFilterWizard()
    {
    	return (ISystemFilterWizard)getWizard();
    }
      
    /**
     * Return an object containing user-specified information pertinent to filter actions
     */
    public SystemFilterDialogOutputs getFilterDialogOutputs()
    {
    	return getFilterWizard().getFilterDialogOutputs();
    }
	
	/**
	 * Allow base action to pass instance of itself for callback to get info
	 */
    public void setFilterDialogActionCaller(SystemFilterAbstractFilterAction caller)
    {
    	getFilterWizard().setFilterDialogActionCaller(caller);
    }
}