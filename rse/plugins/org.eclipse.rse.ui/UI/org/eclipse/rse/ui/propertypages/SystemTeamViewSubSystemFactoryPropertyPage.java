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
import org.eclipse.rse.core.subsystems.ISubSystemConfiguration;
import org.eclipse.rse.core.subsystems.ISubSystemConfigurationProxy;
import org.eclipse.rse.ui.ISystemMessages;
import org.eclipse.rse.ui.SystemPropertyResources;
import org.eclipse.rse.ui.SystemResources;
import org.eclipse.rse.ui.SystemWidgetHelpers;
import org.eclipse.rse.ui.view.SystemViewResources;
import org.eclipse.rse.ui.view.team.SystemTeamViewSubSystemFactoryNode;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;


/**
 * The property page for subsystem factory nodes in the Team view.
 * This is an output-only page.
 */
public class SystemTeamViewSubSystemFactoryPropertyPage extends SystemBasePropertyPage
       implements  ISystemMessages
{
	
	protected Label labelType, labelName, labelId, labelVendor, labelTypes;
	protected String errorMessage;
    protected boolean initDone = false;
    	
	/**
	 * Constructor for SystemFilterPropertyPage
	 */
	public SystemTeamViewSubSystemFactoryPropertyPage()
	{
		super();
	}
	/**
	 * Create the page's GUI contents.
	 * @see org.eclipse.jface.preference.PreferencePage#createContents(Composite)
	 */
	protected Control createContentArea(Composite parent)
	{
		// Inner composite
		int nbrColumns = 2;
		Composite composite_prompts = SystemWidgetHelpers.createComposite(parent, nbrColumns);	

		// Type prompt
		labelType = createLabeledLabel(composite_prompts, SystemPropertyResources.RESID_PROPERTY_TYPE_LABEL, SystemPropertyResources.RESID_PROPERTY_TYPE_TOOLTIP);
		labelType.setText(SystemViewResources.RESID_PROPERTY_TEAM_SSFACTORY_TYPE_VALUE);

		// Name prompt
		labelName = createLabeledLabel(composite_prompts, SystemPropertyResources.RESID_PROPERTY_NAME_LABEL, SystemPropertyResources.RESID_PROPERTY_NAME_TOOLTIP);

		// Id prompt
		labelId = createLabeledLabel(composite_prompts, SystemResources.RESID_PP_SUBSYSFACTORY_ID_LABEL, SystemResources.RESID_PP_SUBSYSFACTORY_ID_TOOLTIP);

		// Vendor prompt
		labelVendor = createLabeledLabel(composite_prompts, SystemResources.RESID_PP_SUBSYSFACTORY_VENDOR_LABEL, SystemResources.RESID_PP_SUBSYSFACTORY_VENDOR_TOOLTIP);

		// System Types prompt
		labelTypes = createLabeledLabel(composite_prompts, SystemResources.RESID_PP_SUBSYSFACTORY_TYPES_LABEL, SystemResources.RESID_PP_SUBSYSFACTORY_TYPES_TOOLTIP);
		
		// description
		addFillerLine(composite_prompts, nbrColumns);
		addSeparatorLine(composite_prompts, nbrColumns);
		SystemWidgetHelpers.createVerbiage(composite_prompts, SystemResources.RESID_PP_SUBSYSFACTORY_VERBAGE, nbrColumns, false, 200);

	    if (!initDone)	
	      doInitializeFields();		  
		
		return composite_prompts;
	}
	
	/**
	 * From parent: do full page validation
	 */
	protected boolean verifyPageContents()
	{
		return true;
	}
	
	/**
	 * Get the input team view subsystem factory node
	 */
	protected ISubSystemConfiguration getSubSystemFactory()
	{
		Object element = getElement();
		SystemTeamViewSubSystemFactoryNode ssfNode = (SystemTeamViewSubSystemFactoryNode)element;
		return ssfNode.getSubSystemFactory();
	}

	/**
	 * Initialize values of input fields based on input
	 */
	protected void doInitializeFields()
	{
		initDone = true;
		ISubSystemConfiguration ssf = getSubSystemFactory();
		ISubSystemConfigurationProxy proxy = ssf.getSubSystemFactoryProxy();
		// populate GUI...
		labelName.setText(ssf.getName());
		labelId.setText(proxy.getId());
		labelVendor.setText(proxy.getVendor());
		String systypes = "";
		String[] types = ssf.getSystemTypes();
		if (ssf.getSubSystemFactoryProxy().supportsAllSystemTypes())
		{
			systypes = SystemResources.TERM_ALL;
		}
		else
		{
			for (int idx=0; idx<types.length; idx++)
			{
				if (idx==0)
					systypes += types[idx];
				else
					systypes += ", " + types[idx];
			}
		}
		labelTypes.setText(systypes);		
	}
	
	/**
	 * Called by parent when user presses OK
	 */
	public boolean performOk()
	{
		boolean ok = true;
		return ok;
	}

}