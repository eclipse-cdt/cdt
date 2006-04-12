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

import org.eclipse.rse.ui.ISystemIconConstants;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.SystemResources;

/**
 * Much of the new filter wizard is configurable, especially with respect to translated strings.
 * While there exists setters and overridable methods for most of it, sometimes that gets overwhelming.
 * This class is designed to capture all the configurable attributes that are not likely to change
 * from usage to usage of the wizard (eg, not context sensitive) such that for convenience you can 
 * subclass it in a class and instantiate a singleton instance of that class to re-use in your
 * wizard.
 * <p>
 * Your best option is to subclass this and override just those things you want to change.
 */
public class SystemNewFilterWizardConfigurator
	implements ISystemNewFilterWizardConfigurator, ISystemIconConstants
{
	// cached attrs
	private String pageTitle;
	private String page1Description, page2Help, page3Tip1, page3Tip2;
	
	/**
	 * Constructor for SystemNewFilterWizardConfigurator.
	 */
	protected SystemNewFilterWizardConfigurator() 
	{
		this(SystemResources.RESID_NEWFILTER_PAGE_TITLE);
	}
	/**
	 * Constructor for SystemNewFilterWizardConfigurator when you want to change the page title
	 */
	protected SystemNewFilterWizardConfigurator(String pageTitle) 
	{
		super();
		this.pageTitle = pageTitle;
		this.page1Description = SystemResources.RESID_NEWFILTER_PAGE1_DESCRIPTION;
		this.page3Tip1 = SystemResources.RESID_NEWFILTER_PAGE3_STRINGS_VERBAGE;
		this.page3Tip2 = SystemResources.RESID_NEWFILTER_PAGE3_POOLS_VERBAGE;
		this.page2Help = RSEUIPlugin.HELPPREFIX + "nfp20000";
	}
	
	/**
	 * Return the default page title to use for each page, unless overridden individually
	 */
	public String getPageTitle()
	{
		return pageTitle;
	}

	/**
	 * Return the page title for page 1 (which prompts for the filter string)
	 */
	public String getPage1Title() 
	{
		return pageTitle;
	}

	/**
	 * Return the description for page 1 (which prompts for the filter string)
	 */
	public String getPage1Description() 
	{
		return page1Description;
	}
	/*page1 help of a wizard comes from the setDialogHelp of the wizard... so this is meaningless
	 * Return the help ID for page 1
	 *
	public String getPage1HelpID()
	{
		return RSEUIPlugin.HELPPREFIX + "nfp10000";
	}*/

	/**
	 * Return the page title for page 2 (which prompts for the name and filter pool)
	 */
	public String getPage2Title() 
	{
		return pageTitle;
	}
	/**
	 * Return the description for page 2 (which prompts for the name and filter pool)
	 */
	public String getPage2Description() 
	{
		return SystemResources.RESID_NEWFILTER_PAGE2_DESCRIPTION;
	}
	/**
	 * Return the help ID for page 2
	 */
	public String getPage2HelpID()
	{
		return page2Help;
	}
	/**
	 * Return the verbage for the name prompt on page 2
	 */
	public String getPage2NameVerbage()
	{
		return SystemResources.RESID_NEWFILTER_PAGE2_NAME_VERBAGE;
	}
	/**
	 * Return the verbage for the name prompt on page 2
	 */
	public String getPage2PoolVerbage()
	{
		return SystemResources.RESID_NEWFILTER_PAGE2_POOL_VERBAGE;
	}
	/**
	 * Return the verbage tooltip for the name prompt on page 2
	 */
	public String getPage2PoolVerbageTip()
	{
		return SystemResources.RESID_NEWFILTER_PAGE2_POOL_VERBAGE_TIP;
	}
	
	public String getPage2NamePromptLabel()
	{
		return SystemResources.RESID_NEWFILTER_PAGE2_NAME_LABEL;
	}
	
	public String getPage2NamePromptTooltip()
	{
		return SystemResources.RESID_NEWFILTER_PAGE2_NAME_TOOLTIP;
	}

	public String getPage2PoolPromptLabel()
	{
		return SystemResources.RESID_NEWFILTER_PAGE2_POOL_LABEL;
	}
	
	public String getPage2PoolPromptTooltip()
	{
		return SystemResources.RESID_NEWFILTER_PAGE2_POOL_TOOLTIP;
	}

	/**
	 * Get the "Unique to this connection" checkbox label 
	 */
	public String getPage2UniqueToConnectionLabel()
	{
		return SystemResources.RESID_NEWFILTER_PAGE2_UNIQUE_LABEL;
	}
	/**
	 * Set the "Unique to this connection" checkbox tooltip
	 */
	public String getPage2UniqueToConnectionToolTip()
	{
		return SystemResources.RESID_NEWFILTER_PAGE2_UNIQUE_TOOLTIP;
	}
		
	/**
	 * Return the page title for page 3 (which shows 2 tips)
	 */
	public String getPage3Title() 
	{
		return pageTitle;
	}
	/**
	 * Return the description for page 3 (which shows 2 tips)
	 */
	public String getPage3Description() 
	{
		return SystemResources.RESID_NEWFILTER_PAGE3_DESCRIPTION;
	}
	/**
	 * Return the description for page 3 (which shows 2 tips)
	 */
	public String getPage3Tip1() 
	{
		return page3Tip1;
	}

	/**
	 * Return the second tip on page 3
	 */
	public String getPage3Tip2() 
	{
		return page3Tip2;
	}


	
	// -------
	// SETTERS
	// -------
	/**
	 * Set the default page title. Sometimes this is all you want to change and don't want to subclass.
	 */
	public void setPageTitle(String pageTitle)
	{
		this.pageTitle = pageTitle;
	}
	/**
	 * Set the description for page 1
	 */
	public void setPage1Description(String description)
	{
		this.page1Description = description;
	}
	/**
	 * Set the help ID for page 2
	 */
	public void setPage2HelpID(String helpId)
	{
		this.page2Help = helpId;
	}
	/**
	 * Set the first tip to show for page 3
	 */
	public void setPage3Tip1(String tip)
	{
		this.page3Tip1 = tip;
	}
	/**
	 * Set the second tip to show for page 3
	 */
	public void setPage3Tip2(String tip)
	{
		this.page3Tip2 = tip;
	}

}