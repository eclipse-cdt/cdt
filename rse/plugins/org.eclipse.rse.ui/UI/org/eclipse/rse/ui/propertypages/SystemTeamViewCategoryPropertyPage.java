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
import org.eclipse.rse.ui.SystemPropertyResources;
import org.eclipse.rse.ui.SystemWidgetHelpers;
import org.eclipse.rse.ui.view.SystemViewResources;
import org.eclipse.rse.ui.view.team.SystemTeamViewCategoryNode;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;


/**
 * The property page for category nodes in the Team view.
 * This is an output-only page.
 */
public class SystemTeamViewCategoryPropertyPage extends SystemBasePropertyPage
{
	
	protected Label labelType, labelName, labelDescription;
	protected String errorMessage;
    protected boolean initDone = false;
    	
	/**
	 * Constructor for SystemFilterPropertyPage
	 */
	public SystemTeamViewCategoryPropertyPage()
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
		String typeLabel = SystemPropertyResources.RESID_PROPERTY_TYPE_LABEL;
		String typeTooltip = SystemPropertyResources.RESID_PROPERTY_TYPE_TOOLTIP;
		
		labelType = createLabeledLabel(composite_prompts, typeLabel, typeTooltip);
		labelType.setText(SystemViewResources.RESID_PROPERTY_TEAM_CATEGORY_TYPE_VALUE);

		// Name prompt
		String nameLabel = SystemPropertyResources.RESID_PROPERTY_NAME_LABEL;
		String nameTooltip = SystemPropertyResources.RESID_PROPERTY_NAME_TOOLTIP;
		
		labelName = createLabeledLabel(composite_prompts, nameLabel, nameTooltip);

		// Description prompt
		addFillerLine(composite_prompts, nbrColumns);
		addSeparatorLine(composite_prompts, nbrColumns);
		//key = ISystemConstants.RESID_PROPERTY_DESCRIPTION_ROOT;
		//Label l = SystemWidgetHelpers.createLabel(composite_prompts, rb, key, nbrColumns, false);
		//l.setText(l.getText() + ":");
		labelDescription = (Label)SystemWidgetHelpers.createVerbiage(composite_prompts, "", nbrColumns, false, 200);

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
	protected SystemTeamViewCategoryNode getCategoryNode()
	{
		Object element = getElement();
		return ((SystemTeamViewCategoryNode)element);
	}

	/**
	 * Initialize values of input fields based on input
	 */
	protected void doInitializeFields()
	{
		initDone = true;
		SystemTeamViewCategoryNode node = getCategoryNode();
		// populate GUI...
		labelName.setText(node.getLabel());
		labelDescription.setText(node.getDescription());
	}
	
}