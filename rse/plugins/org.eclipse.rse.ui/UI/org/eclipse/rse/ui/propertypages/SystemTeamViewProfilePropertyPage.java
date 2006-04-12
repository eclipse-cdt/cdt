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
import org.eclipse.rse.model.ISystemProfile;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.SystemPropertyResources;
import org.eclipse.rse.ui.SystemWidgetHelpers;
import org.eclipse.rse.ui.view.SystemViewResources;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;


/**
 * The property page for profile nodes in the Team view.
 * This is an output-only page.
 */
public class SystemTeamViewProfilePropertyPage extends SystemBasePropertyPage
{
	
	protected Label labelType, labelName, labelStatus;
	protected String errorMessage;
    protected boolean initDone = false;
    	
	/**
	 * Constructor for SystemFilterPropertyPage
	 */
	public SystemTeamViewProfilePropertyPage()
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
		Composite composite_prompts = SystemWidgetHelpers.createComposite(parent, 2);	

		// Type prompt
		labelType = createLabeledLabel(composite_prompts, SystemPropertyResources.RESID_PROPERTY_TYPE_LABEL, SystemPropertyResources.RESID_PROPERTY_TYPE_TOOLTIP);
		labelType.setText(SystemViewResources.RESID_PROPERTY_PROFILE_TYPE_VALUE);

		// Name prompt
		labelName = createLabeledLabel(composite_prompts, SystemPropertyResources.RESID_PROPERTY_NAME_LABEL, SystemPropertyResources.RESID_PROPERTY_NAME_TOOLTIP);

		// Status prompt
		labelStatus = createLabeledLabel(composite_prompts, SystemViewResources.RESID_PROPERTY_PROFILESTATUS_LABEL, SystemViewResources.RESID_PROPERTY_PROFILESTATUS_TOOLTIP);


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
	 * Get the input team view category node
	 */
	protected ISystemProfile getProfile()
	{
		Object element = getElement();
		return ((ISystemProfile)element);
	}

	/**
	 * Initialize values of input fields based on input
	 */
	protected void doInitializeFields()
	{
		initDone = true;
		ISystemProfile profile = getProfile();
		// populate GUI...
		labelName.setText(profile.getName());
		boolean active = RSEUIPlugin.getTheSystemRegistry().getSystemProfileManager().isSystemProfileActive(profile.getName());
		if (active)
			labelStatus.setText(SystemViewResources.RESID_PROPERTY_PROFILESTATUS_ACTIVE_LABEL);
		else
			labelStatus.setText(SystemViewResources.RESID_PROPERTY_PROFILESTATUS_NOTACTIVE_LABEL);		  
		
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