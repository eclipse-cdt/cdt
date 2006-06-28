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
import org.eclipse.rse.ui.ISystemMessages;
import org.eclipse.rse.ui.SystemWidgetHelpers;
import org.eclipse.rse.ui.wizards.AbstractSystemWizardPage;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;



/**
 * Third page of the New Filter wizard that simply shows information
 */
public class SystemNewFilterWizardInfoPage 
	   extends AbstractSystemWizardPage
	   implements ISystemMessages
{
	private ISystemNewFilterWizardConfigurator configurator;
			    
	/**
	 * Constructor.
	 */
	public SystemNewFilterWizardInfoPage(SystemNewFilterWizard wizard, boolean filterPoolsShowing, ISystemNewFilterWizardConfigurator data)
	{
		super(wizard, "NewFilterPage3", data.getPage3Title(), data.getPage3Description());
	    this.configurator = data;
		//setHelp(data.getPage3HelpID());
	}
	// ---------------------------------
	// LIFECYCLE METHODS...
	// ---------------------------------

	// ---------------------------------
	// LIFECYCLE METHODS...
	// ---------------------------------

	/**
	 * Populate the dialog area with our widgets. Return the composite they are in.
	 */
	public Control createContents(Composite parent)
	{
		int nbrColumns = 1;
		Composite composite_prompts = SystemWidgetHelpers.createComposite(parent, nbrColumns);
		
		if (configurator.getPage3Tip1() != null)
		{
		  SystemWidgetHelpers.createVerbiage(composite_prompts, configurator.getPage3Tip1(), nbrColumns, false, 200);		
          addSeparatorLine(composite_prompts, nbrColumns);
          addFillerLine(composite_prompts, nbrColumns);
		}
        
        if (((SystemNewFilterWizard)getWizard()).isFromRSE())
        {
        	if (configurator.getPage3Tip2() != null)
		      SystemWidgetHelpers.createVerbiage(composite_prompts, configurator.getPage3Tip2(), nbrColumns, false, 200);		
        }
		        
		return composite_prompts;
	}	

	/**
	 * Return the Control to be given initial focus.
	 * Override from parent. Return control to be given initial focus.
	 */
	protected Control getInitialFocusControl()
	{
        return null;
	}
	
	/**
	 * Completes processing of the wizard. If this 
	 * method returns true, the wizard will close; 
	 * otherwise, it will stay active.
	 * This method is an override from the parent Wizard class. 
	 *
	 * @return true
	 */
	public boolean performFinish() 
	{
	    return true;
	}

	/**
	 * Return true if the page is complete, so to enable Finish.
	 * Called by wizard framework.
	 * @return true
	 */
	public boolean isPageComplete()
	{
		return true;
	}
	
	/**
	 * Inform wizard of page-complete status of this page
	 */
	public void setPageComplete()
	{
		setPageComplete(isPageComplete());
	}	
		
}